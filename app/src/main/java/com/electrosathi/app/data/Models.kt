package com.electrosathi.app.data

/**
 * Data models for the app.
 *
 * Phase 2 note: these exact classes become your Firestore documents.
 * The screens never change — only the Repository's data source does.
 */

data class Exam(
    val id: String,
    val name: String,
    val category: String,
    val overview: String,
    val eligibility: String,
    val pattern: String,
    val syllabus: String,
    val dates: String,
    val papers: String,
    val cutoff: String
)

data class Project(
    val id: String,
    val title: String,
    val level: String,   // Basic | Intermediate | Advanced
    val domain: String,
    val time: String,
    val cost: String,
    val overview: String,
    val components: List<String>,
    val steps: List<String>,
    val learns: String
)

data class Internship(
    val id: String,
    val title: String,
    val org: String,
    val type: String,       // Government | Private
    val paid: String,       // Paid | Unpaid
    val location: String,
    val eligibility: String,
    val deadline: String,
    val description: String,
    // Batch 7: official application/programme page. Blank = no link —
    // the Apply button then falls back to the detail page instead of
    // opening a browser, and the detail screen hides its Apply button.
    val link: String = ""
)

data class Hackathon(
    val id: String,
    val name: String,
    val org: String,
    val deadline: String,
    val mode: String,       // Online | Offline | Hybrid
    val location: String,
    val prize: String,
    val description: String,
    val link: String
)

/** A book on the E-Library shelf. Cover art is drawn in code from the
 *  book's position in the list — unless [cover] names a bundled cover
 *  image (Batch 7: "book_analog"), which is then used as the cover's
 *  background with the title/author still drawn over it as text. */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val format: String,     // PDF | Read
    val rating: String,     // display string, e.g. "4.8"
    val cover: String = ""  // bundled cover-art key; blank = code-drawn cover
)

/** A subject card on the Study Hub. Its thumbnail and accent colour are
 *  picked in the UI from the subject's position in the list. */
data class StudySubject(
    val id: String,
    val name: String,
    val progressPercent: Int,   // 0–100, the big % on the card
    val topicsDone: Int,
    val topicsTotal: Int,
    val nextTopic: String
)

/** One topic inside a subject's contents page (Batch 7): a title plus a
 *  short, self-contained lesson note shown when the topic is expanded. */
data class SubjectTopic(
    val title: String,
    val note: String
)

/** One flashcard (Batch 7): front = question/term, back = the answer. */
data class Flashcard(
    val front: String,
    val back: String
)
