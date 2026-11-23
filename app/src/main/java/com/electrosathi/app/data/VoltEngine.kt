package com.electrosathi.app.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import org.pytorch.executorch.ExecutorchRuntimeException
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule
import java.io.File
import java.util.concurrent.Executors

/** Volt's on-device state, shown honestly on the Volt screen. */
enum class VoltState { NO_MODEL, LOADING, READY, GENERATING, ERROR }

/**
 * VoltEngine — Batch 9. Wraps Meta's ExecuTorch LlmModule running
 * Llama 3.2 1B Instruct (SpinQuant INT4) fully on the phone.
 *
 * The API usage (constructor, load, generate, LlmCallback, prompt
 * format, stats formula) is lifted from Meta's LlamaDemo, which was
 * run unmodified on the team's phone in Step 0 (12 Nov 2026) and
 * answered correctly. Runtime pinned to executorch-android 1.1.0 —
 * the version that demo ships and the version proven on-device.
 *
 * The model files live OUTSIDE the APK in the app's own folder
 * (getExternalFilesDir("volt")): the .pte is ~1.14 GB, far too big
 * to bundle. For the Phase 1 build they are pushed with adb; the
 * production design is a one-time in-app Wi-Fi download (PRD R-06).
 *
 * v1 is single-turn by design: the context is reset before every
 * question, so each answer is independent and predictable.
 */
object VoltEngine : LlmCallback {

    private const val TAG = "VoltEngine"
    private const val MODEL_FILE = "Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8.pte"
    private const val TOKENIZER_FILE = "tokenizer.bin"

    // Demo's ModelUtils: LLAMA_3 on the XNNPACK backend = TEXT_MODEL.
    private const val CATEGORY_TEXT = 1

    // Demo's TEXT_MODEL_SEQ_LEN was 768 (prompt + answer budget per
    // question). Batch 12 trims it to 512: long answers end sooner,
    // which also shortens the worst-case wait when an answer runs away.
    private const val SEQ_LEN = 512

    // Temperature 0.6 — Batch 15, on the mentoring session's advice
    // (Step 0 and Batches 9–13 ran at 0.0: fully deterministic, but
    // stiff). 0.6 keeps answers factual while letting Volt phrase
    // them more naturally and vary its examples.
    private const val TEMPERATURE = 0.6f

    private const val SYSTEM_PROMPT =
        "You are Volt, an electronics tutor for Indian engineering students. " +
            "Explain simply, in a few sentences, with a simple example when helpful. " +
            "If you are not sure, say so. " +
            "If the student asks for a quiz or practice questions, ask one question " +
            "at a time and wait for their answer before asking the next."

    private val executor = Executors.newSingleThreadExecutor()
    private var module: LlmModule? = null

    val state = MutableStateFlow(VoltState.NO_MODEL)
    val answer = MutableStateFlow("")
    val tokensPerSecond = MutableStateFlow(0f)
    val errorMessage = MutableStateFlow("")

    // Batch 15: total inference time of the last answer, in seconds —
    // the mentoring session asked for it in the report, so the engine
    // surfaces it and the Volt screen shows it under every answer.
    val totalInferenceSeconds = MutableStateFlow(0f)

    // Batch 12: true when the user cut the current/last answer short
    // with the Stop button. Lets the screen caption it honestly, and
    // lets ask() treat a stop-triggered generation exception as a
    // normal ending instead of an error.
    val answerStopped = MutableStateFlow(false)

    private fun modelDir(context: Context): File =
        context.getExternalFilesDir("volt") ?: File(context.filesDir, "volt")

    fun modelAvailable(context: Context): Boolean {
        val dir = modelDir(context)
        return File(dir, MODEL_FILE).exists() && File(dir, TOKENIZER_FILE).exists()
    }

    /** Call when the Volt screen opens. Loads the model once, off the UI thread. */
    fun prepare(context: Context) {
        val appCtx = context.applicationContext
        if (!modelAvailable(appCtx)) {
            state.value = VoltState.NO_MODEL
            return
        }
        if (module != null) {
            if (state.value != VoltState.GENERATING) state.value = VoltState.READY
            return
        }
        if (state.value == VoltState.LOADING) return
        state.value = VoltState.LOADING
        executor.execute {
            try {
                val dir = modelDir(appCtx)
                val m = LlmModule(
                    CATEGORY_TEXT,
                    File(dir, MODEL_FILE).absolutePath,
                    File(dir, TOKENIZER_FILE).absolutePath,
                    TEMPERATURE
                )
                m.load()
                module = m
                state.value = VoltState.READY
                Log.i(TAG, "Volt model loaded")
            } catch (e: ExecutorchRuntimeException) {
                Log.e(TAG, "Volt load failed", e)
                errorMessage.value = e.message ?: "model failed to load"
                state.value = VoltState.ERROR
            } catch (e: Exception) {
                Log.e(TAG, "Volt load failed", e)
                errorMessage.value = e.message ?: "model failed to load"
                state.value = VoltState.ERROR
            }
        }
    }

    /**
     * Ask one question. The answer streams into [answer]; stats land in
     * [tokensPerSecond]. [seqLen] is the prompt + answer budget for this
     * question — Batch 13 passes a bigger one (1024) when the question
     * carries text read from a shared file; normal chat keeps 512.
     *
     * Batch 17: [previousQuestion] + [previousAnswer] carry the last
     * exchange, woven into the prompt as a real first turn so a
     * follow-up ("how many pins?") keeps its topic. Callers pass them
     * only for plain chat — quiz and file questions stay single-turn.
     */
    fun ask(
        question: String,
        seqLen: Int = SEQ_LEN,
        previousQuestion: String? = null,
        previousAnswer: String? = null
    ) {
        val m = module ?: return
        if (state.value == VoltState.GENERATING || state.value == VoltState.LOADING) return
        answer.value = ""
        tokensPerSecond.value = 0f
        totalInferenceSeconds.value = 0f
        answerStopped.value = false
        state.value = VoltState.GENERATING
        val prompt = buildPrompt(question, previousQuestion, previousAnswer)
        executor.execute {
            try {
                m.resetContext()
                // generate() blocks until done; onResult streams meanwhile (demo pattern).
                m.generate(prompt, seqLen, this, false)
                if (state.value == VoltState.GENERATING) state.value = VoltState.READY
            } catch (e: ExecutorchRuntimeException) {
                if (answerStopped.value) {
                    // Stop can make generate() throw on its way out —
                    // a stopped answer is a normal ending, not an error.
                    if (state.value == VoltState.GENERATING) state.value = VoltState.READY
                } else {
                    Log.e(TAG, "Volt generate failed", e)
                    errorMessage.value = e.message ?: "generation failed"
                    state.value = VoltState.ERROR
                }
            } catch (e: Exception) {
                if (answerStopped.value) {
                    if (state.value == VoltState.GENERATING) state.value = VoltState.READY
                } else {
                    Log.e(TAG, "Volt generate failed", e)
                    errorMessage.value = e.message ?: "generation failed"
                    state.value = VoltState.ERROR
                }
            }
        }
    }

    /**
     * Batch 12: the Stop button. Flags the stop first (so the generate
     * thread's exit path reads it), then halts the module. Whatever
     * answer text already streamed stays on screen as a partial answer.
     */
    fun stop() {
        answerStopped.value = true
        module?.stop()
    }

    // ---- LlmCallback (called from the native generation thread) ----

    override fun onResult(result: String) {
        when (result) {
            // End of the answer: stop and never show the token itself.
            "<|eot_id|>" -> {
                module?.stop()
                return
            }
            // Formatting tokens from multi-turn templates must never reach the bubble.
            "<|start_header_id|>", "<|end_header_id|>" -> return
        }
        answer.value += result
    }

    override fun onStats(stats: String) {
        try {
            val json = JSONObject(stats)
            val tokens = json.getInt("generated_tokens")
            val inferenceEnd = json.getInt("inference_end_ms")
            val promptEvalEnd = json.getInt("prompt_eval_end_ms")
            if (tokens > 0 && inferenceEnd > promptEvalEnd) {
                tokensPerSecond.value =
                    tokens.toFloat() / (inferenceEnd - promptEvalEnd) * 1000f
            }
            // inference_end_ms is the engine's own total for the whole
            // answer (prompt + generation), measured from the start.
            if (inferenceEnd > 0) {
                totalInferenceSeconds.value = inferenceEnd / 1000f
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse stats: ${e.message}")
        }
    }

    // Llama 3 Instruct template, copied from the demo's PromptFormat.
    // Batch 17: with a previous exchange, it becomes a real first turn
    // (user → assistant) ahead of the new question — that is what gives
    // Volt follow-up memory. The previous answer is trimmed so the
    // pair stays inside the token budget.
    private fun buildPrompt(
        question: String,
        previousQuestion: String? = null,
        previousAnswer: String? = null
    ): String {
        val sb = StringBuilder()
        sb.append("<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n")
        sb.append(SYSTEM_PROMPT).append("<|eot_id|>")
        if (!previousQuestion.isNullOrBlank() && !previousAnswer.isNullOrBlank()) {
            sb.append("<|start_header_id|>user<|end_header_id|>\n")
            sb.append(previousQuestion).append("<|eot_id|>")
            sb.append("<|start_header_id|>assistant<|end_header_id|>\n")
            sb.append(previousAnswer.take(700)).append("<|eot_id|>")
        }
        sb.append("<|start_header_id|>user<|end_header_id|>\n")
        sb.append(question).append("<|eot_id|>")
        sb.append("<|start_header_id|>assistant<|end_header_id|>")
        return sb.toString()
    }
}
