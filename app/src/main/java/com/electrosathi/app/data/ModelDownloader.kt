package com.electrosathi.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Where the one-time Volt model download stands. Shown on the Volt screen. */
sealed class ModelDownloadState {
    object Idle : ModelDownloadState()
    data class Downloading(
        val fileLabel: String,
        val fileDone: Long,
        val fileTotal: Long,
        val overallDone: Long,
        val overallTotal: Long,
        val percent: Int
    ) : ModelDownloadState()

    object Verifying : ModelDownloadState()
    object Done : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
    object Cancelled : ModelDownloadState()
}

/**
 * ModelDownloader — Batch 11. Fetches Volt's brain from inside the app,
 * once, so a fresh phone no longer needs the adb cable push.
 *
 * Sources (Hugging Face, public): the same SpinQuant build proven in
 * Step 0 / Batch 9. Files land in EXACTLY the folder [VoltEngine]
 * reads — getExternalFilesDir("volt") — under the names it expects.
 *
 * How it behaves:
 * - Tokenizer first (2 MB, quick), then the model (1.14 GB).
 * - Everything is written to "<name>.part" files and supports resume:
 *   an existing .part is continued with an HTTP Range request; if the
 *   server answers 200 instead of 206 the file restarts cleanly.
 * - A file only counts as downloaded when its byte size matches the
 *   known-good size EXACTLY. Only when both files verify are the .part
 *   files renamed into place, so VoltEngine never sees a half model.
 * - Cancel keeps the .part files, so Resume picks up where it stopped.
 *
 * Plain HttpURLConnection + coroutines — no new dependencies.
 */
object ModelDownloader {

    private const val TAG = "ModelDownloader"

    private const val MODEL_URL =
        "https://huggingface.co/executorch-community/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8-ET/resolve/main/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8.pte"
    private const val TOKENIZER_URL =
        "https://huggingface.co/executorch-community/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8-ET/resolve/main/tokenizer.model"

    // Same names VoltEngine looks for — do not rename.
    private const val MODEL_FILE = "Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8.pte"
    private const val TOKENIZER_FILE = "tokenizer.bin"

    // Known-good byte sizes of the proven Step 0 downloads.
    private const val MODEL_SIZE = 1135951488L
    private const val TOKENIZER_SIZE = 2183982L
    private const val TOTAL_SIZE = MODEL_SIZE + TOKENIZER_SIZE

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    @Volatile
    private var cancelRequested = false

    @Volatile
    private var currentConnection: HttpURLConnection? = null

    val state = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)

    // Same folder VoltEngine reads from — keep in sync with VoltEngine.modelDir.
    private fun modelDir(context: Context): File =
        context.getExternalFilesDir("volt") ?: File(context.filesDir, "volt")

    /** True when part of the model is already on this phone as .part files. */
    fun hasPartialDownload(context: Context): Boolean = partialBytes(context) > 0L

    /** Bytes already saved in .part files — drives the Resume label. */
    fun partialBytes(context: Context): Long {
        val dir = modelDir(context.applicationContext)
        var sum = 0L
        val tok = File(dir, TOKENIZER_FILE + ".part")
        val mod = File(dir, MODEL_FILE + ".part")
        if (tok.exists()) sum += tok.length()
        if (mod.exists()) sum += mod.length()
        return sum
    }

    /**
     * True when the active network looks like Wi-Fi / unmetered. If the
     * check itself fails for any reason, returns true — the mobile-data
     * guard is a courtesy prompt, never a hard block.
     */
    fun isOnUnmeteredNetwork(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return true
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            Log.w(TAG, "Metered check failed: ${e.message}")
            true
        }
    }

    /** Start (or resume) the download. Safe to call again after error/cancel. */
    fun start(context: Context) {
        if (job?.isActive == true) return
        val appCtx = context.applicationContext
        cancelRequested = false
        job = scope.launch { runDownload(appCtx) }
    }

    /** Stop promptly; .part files stay on disk so Resume can continue. */
    fun cancel() {
        cancelRequested = true
        try {
            currentConnection?.disconnect()
        } catch (e: Exception) {
            // Best effort — the read loop also checks the flag itself.
        }
    }

    private fun runDownload(context: Context) {
        val dir = modelDir(context)
        if (!dir.exists()) dir.mkdirs()
        val tokPart = File(dir, TOKENIZER_FILE + ".part")
        val modelPart = File(dir, MODEL_FILE + ".part")
        try {
            // 1) Tokenizer first — small, so the first success comes fast.
            if (!downloadFile(TOKENIZER_URL, tokPart, TOKENIZER_SIZE, "dictionary file", 0L)) return
            state.value = ModelDownloadState.Verifying
            if (tokPart.length() != TOKENIZER_SIZE) {
                tokPart.delete()
                state.value = ModelDownloadState.Error(
                    "The small dictionary file didn't arrive correctly (wrong size), " +
                        "so it was removed. Tap Try again."
                )
                return
            }

            // 2) The model itself.
            if (!downloadFile(MODEL_URL, modelPart, MODEL_SIZE, "brain file", TOKENIZER_SIZE)) return
            state.value = ModelDownloadState.Verifying
            if (modelPart.length() != MODEL_SIZE) {
                modelPart.delete()
                state.value = ModelDownloadState.Error(
                    "The brain file didn't arrive completely (wrong size), so it was " +
                        "removed. The dictionary file is safe — tap Resume to fetch " +
                        "the brain file again."
                )
                return
            }

            // 3) Both verified — only now move them into place.
            val tokFinal = File(dir, TOKENIZER_FILE)
            val modelFinal = File(dir, MODEL_FILE)
            if (tokFinal.exists()) tokFinal.delete()
            if (modelFinal.exists()) modelFinal.delete()
            val okTok = tokPart.renameTo(tokFinal)
            val okModel = modelPart.renameTo(modelFinal)
            if (!okTok || !okModel) {
                state.value = ModelDownloadState.Error(
                    "Download finished but the files couldn't be moved into place. " +
                        "Please try again."
                )
                return
            }
            Log.i(TAG, "Volt model download complete and verified")
            state.value = ModelDownloadState.Done
        } catch (e: IOException) {
            if (cancelRequested) {
                state.value = ModelDownloadState.Cancelled
                return
            }
            Log.e(TAG, "Download failed", e)
            state.value = ModelDownloadState.Error(
                "Connection lost — your progress is saved, tap Resume"
            )
        } catch (e: Exception) {
            if (cancelRequested) {
                state.value = ModelDownloadState.Cancelled
                return
            }
            Log.e(TAG, "Download failed", e)
            state.value = ModelDownloadState.Error(
                "Download stopped (${e.message ?: "unknown error"}) — " +
                    "your progress is saved, tap Resume"
            )
        }
    }

    /**
     * Downloads [url] into [partFile], resuming from existing bytes when a
     * .part file is already there. Returns true when the file is fully
     * downloaded (length checked by the caller), false when cancelled.
     * Throws IOException on network/server failures.
     */
    private fun downloadFile(
        url: String,
        partFile: File,
        expectedSize: Long,
        fileLabel: String,
        bytesBefore: Long
    ): Boolean {
        if (partFile.exists() && partFile.length() == expectedSize) {
            emitProgress(fileLabel, expectedSize, expectedSize, bytesBefore)
            return true
        }
        if (partFile.exists() && partFile.length() > expectedSize) {
            // Longer than the real file can only be wrong — start it over.
            partFile.delete()
        }
        var attempt = 0
        while (attempt < 2) {
            attempt++
            val existing = if (partFile.exists()) partFile.length() else 0L
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
                }
                currentConnection = conn
                val code = conn.responseCode
                if (code == 416 && existing > 0) {
                    // Server can't resume from here — the .part can't be
                    // trusted; delete it and retry once from zero.
                    partFile.delete()
                    continue
                }
                if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_PARTIAL) {
                    throw IOException("Server replied $code")
                }
                // 206 = our Range was honoured, append. 200 after a Range
                // request = server ignored it, restart this file cleanly.
                val append = code == HttpURLConnection.HTTP_PARTIAL && existing > 0
                var done = if (append) existing else 0L
                conn.inputStream.use { input ->
                    FileOutputStream(partFile, append).use { out ->
                        val buffer = ByteArray(256 * 1024)
                        emitProgress(fileLabel, done, expectedSize, bytesBefore)
                        while (true) {
                            if (cancelRequested) {
                                out.flush()
                                state.value = ModelDownloadState.Cancelled
                                return false
                            }
                            val read = input.read(buffer)
                            if (read == -1) break
                            out.write(buffer, 0, read)
                            done += read
                            emitProgress(fileLabel, done, expectedSize, bytesBefore)
                        }
                        out.flush()
                    }
                }
                return true
            } finally {
                if (currentConnection === conn) currentConnection = null
                conn?.disconnect()
            }
        }
        throw IOException("Server kept rejecting the resume point")
    }

    private fun emitProgress(fileLabel: String, fileDone: Long, fileTotal: Long, bytesBefore: Long) {
        val overallDone = bytesBefore + fileDone
        val percent = (overallDone * 100 / TOTAL_SIZE).toInt().coerceIn(0, 100)
        state.value = ModelDownloadState.Downloading(
            fileLabel = fileLabel,
            fileDone = fileDone,
            fileTotal = fileTotal,
            overallDone = overallDone,
            overallTotal = TOTAL_SIZE,
            percent = percent
        )
    }
}
