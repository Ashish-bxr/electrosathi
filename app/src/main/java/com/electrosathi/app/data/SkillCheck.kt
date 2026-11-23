package com.electrosathi.app.data

/**
 * Skill Check (Batch 10) — the 12-question first-run diagnostic.
 *
 * The questions are the drafted bank
 * (references/skill-check-question-bank-2026-11-11.md), used verbatim:
 * question text, options, correct answers, explanations and gap tags.
 * Every question maps to a topic that already exists in Repository with
 * real lesson content, so a wrong answer names a topic the student can
 * open immediately. Like the topics and flashcards, the bank is built
 * into the app in v1 — the Content Lead can refine the wording later,
 * and a future batch can move it behind the Admin Panel.
 *
 * Subject ids and topic names match Repository exactly — do not rename
 * one side without the other, the pathway is built from these strings.
 */
data class SkillQuestion(
    val subjectId: String,
    val subjectName: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

object SkillCheck {

    /** The three quizzed subjects, in quiz order: id to display name. */
    val SUBJECTS: List<Pair<String, String>> = listOf(
        "analog-circuits" to "Analog Circuits",
        "digital-electronics" to "Digital Electronics",
        "signals-and-systems" to "Signals & Systems"
    )

    val QUESTIONS: List<SkillQuestion> = listOf(
        // ---------------- Analog Circuits ----------------
        SkillQuestion(
            subjectId = "analog-circuits",
            subjectName = "Analog Circuits",
            topic = "Semiconductor Diodes & Rectifiers",
            question = "A full-wave bridge rectifier is usually preferred over a half-wave rectifier because its ripple factor is…",
            options = listOf(
                "1.21 — higher, so the output is smoother",
                "0.48 — lower, so the output is smoother",
                "0.48 — lower, so the output is rougher",
                "The same, 1.21, in both circuits"
            ),
            correctIndex = 1,
            explanation = "Half-wave uses only one half-cycle (ripple factor 1.21); full-wave uses both (0.48). Lower ripple = smoother DC."
        ),
        SkillQuestion(
            subjectId = "analog-circuits",
            subjectName = "Analog Circuits",
            topic = "BJT Biasing & the Q-Point",
            question = "Why is the voltage-divider (self-bias) circuit the most stable way to bias a BJT?",
            options = listOf(
                "It uses the fewest resistors",
                "It holds the base voltage nearly fixed, so β variations barely move the Q-point",
                "It removes the emitter resistor entirely",
                "It always puts the Q-point at saturation"
            ),
            correctIndex = 1,
            explanation = "The divider current is designed ~10× the base current, and the emitter resistor fights thermal runaway through negative feedback."
        ),
        SkillQuestion(
            subjectId = "analog-circuits",
            subjectName = "Analog Circuits",
            topic = "Op-Amp Fundamentals",
            question = "An ideal op-amp is used with negative feedback. Which statement is true?",
            options = listOf(
                "A large current flows into both input terminals",
                "The two inputs sit at very different voltages",
                "No current flows into either input, and feedback holds both inputs at the same voltage",
                "The output is always at a supply rail"
            ),
            correctIndex = 2,
            explanation = "The two \"golden rules\" — no input current, and the virtual short between the inputs."
        ),
        SkillQuestion(
            subjectId = "analog-circuits",
            subjectName = "Analog Circuits",
            topic = "MOSFET Basics & Biasing",
            question = "An n-channel enhancement MOSFET is in saturation. What happens if you raise VGS (still above the threshold voltage Vth)?",
            options = listOf(
                "Nothing — the drain current is fixed by Vth alone",
                "The MOSFET turns off",
                "The drain current rises, roughly as ID = k(VGS − Vth)²",
                "The gate starts drawing a large current"
            ),
            correctIndex = 2,
            explanation = "The MOSFET conducts only when VGS exceeds Vth, and in saturation the current follows the square law; the insulated gate still draws almost no current."
        ),
        // ---------------- Digital Electronics ----------------
        SkillQuestion(
            subjectId = "digital-electronics",
            subjectName = "Digital Electronics",
            topic = "Number Systems & Codes",
            question = "How many bits does one hexadecimal digit represent?",
            options = listOf("2", "3", "4", "8"),
            correctIndex = 2,
            explanation = "Hex is base 16 = 2⁴, so each hex digit is exactly four bits; convert by grouping bits in fours."
        ),
        SkillQuestion(
            subjectId = "digital-electronics",
            subjectName = "Digital Electronics",
            topic = "Karnaugh Maps & Combinational Design",
            question = "When circling groups of 1s on a Karnaugh map, which group sizes are allowed?",
            options = listOf(
                "Any size — 3, 5, 6, 7 all work",
                "Only powers of two: 1, 2, 4, 8…",
                "Only groups of exactly 4",
                "Only single cells, never groups"
            ),
            correctIndex = 1,
            explanation = "The map is laid out in Gray-code order so adjacent cells differ in one variable; a power-of-two group deletes the variables that change inside it, leaving a minimal sum-of-products expression."
        ),
        SkillQuestion(
            subjectId = "digital-electronics",
            subjectName = "Digital Electronics",
            topic = "Flip-Flops & Latches",
            question = "What is the race-around condition, and how is it fixed?",
            options = listOf(
                "A counter that counts too fast; fixed by adding more flip-flops",
                "In a level-clocked JK flip-flop with J = K = 1, the output toggles repeatedly while the clock stays high; edge triggering or a master–slave JK fixes it",
                "A setup-time violation; fixed by slowing the data, not the clock",
                "Two flip-flops capturing different values; fixed by using latches instead"
            ),
            correctIndex = 1,
            explanation = "The toggle can repeat uncontrollably during one long clock pulse, so the final state is unpredictable."
        ),
        SkillQuestion(
            subjectId = "digital-electronics",
            subjectName = "Digital Electronics",
            topic = "ADC & DAC",
            question = "A signal's highest frequency is 5 kHz. What is the minimum sampling rate that avoids aliasing?",
            options = listOf(
                "5 kHz",
                "10 kHz",
                "2.5 kHz",
                "Any rate — aliasing can always be undone later in software"
            ),
            correctIndex = 1,
            explanation = "Nyquist — sample at least twice the highest frequency (fs ≥ 2·fmax). Once aliasing happens, no processing can undo it."
        ),
        // ---------------- Signals & Systems ----------------
        SkillQuestion(
            subjectId = "signals-and-systems",
            subjectName = "Signals & Systems",
            topic = "Signals & Their Classification",
            question = "A periodic signal (like a sine wave that goes on forever) is which kind of signal?",
            options = listOf(
                "An energy signal — finite total energy, zero average power",
                "A power signal — finite average power, infinite total energy",
                "Neither energy nor power",
                "An aperiodic signal"
            ),
            correctIndex = 1,
            explanation = "A single pulse is an energy signal; any periodic signal has finite average power but infinite energy over all time."
        ),
        SkillQuestion(
            subjectId = "signals-and-systems",
            subjectName = "Signals & Systems",
            topic = "LTI Systems & Convolution",
            question = "An LTI system is causal when…",
            options = listOf(
                "Its impulse response is absolutely integrable",
                "Its impulse response h(t) = 0 for t < 0 — it cannot respond before the input arrives",
                "Its step response is zero",
                "Its output is always larger than its input"
            ),
            correctIndex = 1,
            explanation = "Causality is about time order; absolute integrability (∫|h(t)| dt < ∞) is the separate BIBO-stability test."
        ),
        SkillQuestion(
            subjectId = "signals-and-systems",
            subjectName = "Signals & Systems",
            topic = "Fourier Series",
            question = "When a square wave is rebuilt from its Fourier series term by term, what happens at each sharp jump?",
            options = listOf(
                "The overshoot shrinks to zero as you add terms",
                "A stubborn ~9% overshoot stays at each jump, however many terms you add",
                "The series stops converging entirely",
                "Only the even harmonics cause it, so dropping them removes it"
            ),
            correctIndex = 1,
            explanation = "The Gibbs phenomenon — the overshoot narrows as terms are added but never shrinks in height."
        ),
        SkillQuestion(
            subjectId = "signals-and-systems",
            subjectName = "Signals & Systems",
            topic = "Sampling & Aliasing",
            question = "Why do real systems put a low-pass (anti-alias) filter before the sampler?",
            options = listOf(
                "To amplify the signal before sampling",
                "To remove frequencies above half the sampling rate, so their spectral copies cannot fold back into the baseband",
                "To convert the signal from discrete-time to continuous-time",
                "To make the sampling rate lower than Nyquist"
            ),
            correctIndex = 1,
            explanation = "Sampling copies the spectrum to every multiple of fs; if copies overlap, high frequencies masquerade as lower ones (aliasing), and it cannot be undone afterwards."
        )
    )

    fun subjectName(subjectId: String): String =
        SUBJECTS.firstOrNull { it.first == subjectId }?.second ?: ""

    /** Which subject a topic belongs to (from the bank's gap tags). */
    fun subjectIdForTopic(topic: String): String? =
        QUESTIONS.firstOrNull { it.topic == topic }?.subjectId

    fun subjectNameForTopic(topic: String): String? =
        QUESTIONS.firstOrNull { it.topic == topic }?.subjectName

    /** Score out of 4 for one subject, from the picked option indexes. */
    fun scoreFor(picks: List<Int>, subjectId: String): Int =
        QUESTIONS.indices.count { i ->
            QUESTIONS[i].subjectId == subjectId &&
                picks.getOrNull(i) == QUESTIONS[i].correctIndex
        }

    /** Topics of the wrongly answered questions, in quiz order. */
    fun weakTopicsFor(picks: List<Int>): List<String> =
        QUESTIONS.indices
            .filter { i -> picks.getOrNull(i) != QUESTIONS[i].correctIndex }
            .map { QUESTIONS[it].topic }

    /** Per-subject status from its score out of 4. */
    fun statusFor(score: Int): String = when {
        score <= 1 -> "Needs work"
        score == 2 -> "Getting there"
        else -> "Strong"
    }
}
