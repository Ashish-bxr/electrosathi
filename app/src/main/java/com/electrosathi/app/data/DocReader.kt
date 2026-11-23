package com.electrosathi.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

/** What kind of file the student shared with Volt. */
enum class DocKind { PHOTO, PDF }

/** Outcome of reading a shared file's text. No fake text, ever. */
sealed class DocReadResult {
    /** Text was read. [text] is normalised and already capped. */
    data class Ready(val kind: DocKind, val text: String) : DocReadResult()

    /** The file opened fine but held no readable text. */
    object Empty : DocReadResult()

    /** Something went wrong; [message] is short and honest. */
    data class Error(val message: String) : DocReadResult()
}

/**
 * DocReader — Batch 13. Reads the TEXT out of a photo or PDF the
 * student shares with Volt, fully on-device.
 *
 * Volt's brain (Llama 3.2 1B) is text-only — it cannot see pictures.
 * So this reader turns the file into plain text and only that text is
 * ever sent to Volt as context:
 * - Photo: decoded (downscaled, EXIF-rotated upright), then OCR'd
 *   with the bundled ML Kit text recogniser (model inside the APK,
 *   works offline, no download).
 * - PDF: rendered with Android's built-in PdfRenderer — the first
 *   [MAX_PDF_PAGES] pages only — and each page OCR'd the same way.
 *
 * The result is whitespace-normalised and hard-capped at
 * [MAX_TEXT_CHARS] characters so a document question always fits
 * Volt's reading budget.
 */
object DocReader {

    private const val TAG = "DocReader"

    // A photo's largest side after downscaling — plenty for OCR of a
    // textbook page, small enough to stay fast and light on memory.
    private const val MAX_IMAGE_SIDE = 2000

    // Only the first pages of a PDF are read (stated honestly in-app).
    private const val MAX_PDF_PAGES = 3

    // PDF pages render at ~150 dpi (PdfRenderer points are 1/72 inch).
    private const val PDF_RENDER_DPI = 150f

    // Safety cap for a rendered page bitmap's largest side.
    private const val MAX_RENDER_SIDE = 2600

    // Hard cap on extracted text sent to Volt.
    private const val MAX_TEXT_CHARS = 1500

    /**
     * Reads [uri] (from the system document picker) and returns its
     * text. Never throws — failures come back as [DocReadResult.Error].
     */
    suspend fun readText(context: Context, uri: Uri): DocReadResult {
        val appCtx = context.applicationContext
        val kind = try {
            resolveKind(appCtx, uri)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open shared file", e)
            return DocReadResult.Error("Couldn't open that file")
        }
        // One recogniser per read; closed in every path below.
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val raw = when (kind) {
                DocKind.PHOTO -> {
                    val bitmap = decodePhoto(appCtx, uri)
                        ?: return DocReadResult.Error("Couldn't open that photo")
                    try {
                        recognize(recognizer, bitmap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Photo OCR failed", e)
                        return DocReadResult.Error("Couldn't read text from that photo")
                    } finally {
                        bitmap.recycle()
                    }
                }

                DocKind.PDF -> try {
                    readPdf(appCtx, uri, recognizer)
                } catch (e: Exception) {
                    Log.e(TAG, "PDF read failed", e)
                    return DocReadResult.Error(
                        "Couldn't read that PDF (damaged or password-protected " +
                            "files can't be read)"
                    )
                }
            }
            val cleaned = cleanAndCap(raw)
            return if (cleaned.isBlank()) {
                DocReadResult.Empty
            } else {
                DocReadResult.Ready(kind, cleaned)
            }
        } finally {
            try {
                recognizer.close()
            } catch (e: Exception) {
                // Closing is best-effort; never mask the real result.
            }
        }
    }

    // ---- Kind detection ---------------------------------------------

    private fun resolveKind(context: Context, uri: Uri): DocKind {
        val mime = context.contentResolver.getType(uri)
        return when {
            mime != null && mime.startsWith("image/") -> DocKind.PHOTO
            mime == "application/pdf" -> DocKind.PDF
            // Some providers report nothing useful — sniff the header.
            else -> if (startsWithPdfMagic(context, uri)) DocKind.PDF else DocKind.PHOTO
        }
    }

    private fun startsWithPdfMagic(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val head = ByteArray(5)
                val read = input.read(head)
                read == 5 && String(head, Charsets.US_ASCII) == "%PDF-"
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ---- Photo --------------------------------------------------------

    /**
     * Decodes the photo at a size OCR can use: sample-down so the
     * largest side is near [MAX_IMAGE_SIDE], then a precise scale if
     * still over, then rotated upright from its EXIF orientation (a
     * photographed page is usually stored sideways — OCR on a rotated
     * page reads nothing).
     */
    private fun decodePhoto(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        val largest = max(bounds.outWidth, bounds.outHeight)
        while (largest / (sample * 2) >= MAX_IMAGE_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val bigSide = max(bitmap.width, bitmap.height)
        if (bigSide > MAX_IMAGE_SIDE) {
            val factor = MAX_IMAGE_SIDE.toFloat() / bigSide
            val scaled = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * factor).roundToInt().coerceAtLeast(1),
                (bitmap.height * factor).roundToInt().coerceAtLeast(1),
                true
            )
            if (scaled !== bitmap) {
                bitmap.recycle()
                bitmap = scaled
            }
        }

        val rotation = readExifRotation(context, uri)
        if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val upright = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            if (upright !== bitmap) {
                bitmap.recycle()
                bitmap = upright
            }
        }
        return bitmap
    }

    private fun readExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                when (
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0 // A photo we can't orient is still worth one OCR attempt.
        }
    }

    // ---- PDF ----------------------------------------------------------

    private suspend fun readPdf(
        context: Context,
        uri: Uri,
        recognizer: TextRecognizer
    ): String {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalStateException("No file descriptor for PDF")
        try {
            val renderer = PdfRenderer(descriptor)
            try {
                val pages = minOf(MAX_PDF_PAGES, renderer.pageCount)
                val texts = mutableListOf<String>()
                for (index in 0 until pages) {
                    val page = renderer.openPage(index)
                    val bitmap = try {
                        renderPage(page)
                    } finally {
                        page.close()
                    }
                    try {
                        val pageText = recognize(recognizer, bitmap)
                        if (pageText.isNotBlank()) texts.add(pageText)
                    } finally {
                        bitmap.recycle()
                    }
                }
                return texts.joinToString("\n\n")
            } finally {
                renderer.close()
            }
        } finally {
            descriptor.close()
        }
    }

    private fun renderPage(page: PdfRenderer.Page): Bitmap {
        val scale = PDF_RENDER_DPI / 72f
        var width = (page.width * scale).roundToInt().coerceAtLeast(1)
        var height = (page.height * scale).roundToInt().coerceAtLeast(1)
        val longest = max(width, height)
        if (longest > MAX_RENDER_SIDE) {
            val factor = MAX_RENDER_SIDE.toFloat() / longest
            width = (width * factor).roundToInt().coerceAtLeast(1)
            height = (height * factor).roundToInt().coerceAtLeast(1)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Pages render over transparency — give them the white paper
        // background print expects, or OCR sees black-on-black.
        Canvas(bitmap).drawColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    // ---- OCR ----------------------------------------------------------

    /**
     * Bridges ML Kit's Task API into a coroutine. kotlinx-coroutines-
     * play-services is not in this project, so no `.await()` — the
     * listeners resume the continuation directly.
     */
    private suspend fun recognize(recognizer: TextRecognizer, bitmap: Bitmap): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { visionText ->
                    if (cont.isActive) cont.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }

    // ---- Text cleanup ---------------------------------------------------

    private fun cleanAndCap(raw: String): String {
        var text = raw
            .replace(Regex("[\\t ]+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
        if (text.length > MAX_TEXT_CHARS) {
            var cut = text.substring(0, MAX_TEXT_CHARS)
            // Cut cleanly: back up to the last word break when one is
            // reasonably close to the cap.
            val lastBreak = cut.lastIndexOfAny(charArrayOf(' ', '\n'))
            if (lastBreak > MAX_TEXT_CHARS - 300) cut = cut.substring(0, lastBreak)
            text = cut.trim()
        }
        return text
    }
}
