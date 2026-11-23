package com.electrosathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.Exam
import com.electrosathi.app.data.Repository
import com.electrosathi.app.data.SkillAttempts
import com.electrosathi.app.data.SkillCheck
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.components.AccentProjects
import com.electrosathi.app.ui.components.AccentStudy
import com.electrosathi.app.ui.components.Eyebrow
import com.electrosathi.app.ui.theme.BrandCyan
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ---------------------------------------------------------------------------
// My Pathway (Batch 10) — the personal route built from the Skill Check.
//
// The focus order is honest and simple: topics answered wrongly come
// first (weak spots), then every topic of a subject that scored
// "Getting there". Tapping a row opens that subject's contents page.
//
// The Study Plan card counts those focus topics at 2 per week and
// measures the finish date against a real exam date from Repository —
// the GATE entry when the user's onboarding goal mentions GATE, else
// the nearest exam whose stored dates parse to a future day. If the
// plan would finish after the exam, the card says so and suggests
// 3 topics a week instead of hiding it.
// ---------------------------------------------------------------------------

private data class FocusEntry(
    val topic: String,
    val subjectId: String,
    val subjectName: String,
    val needsWork: Boolean
)

@Composable
fun PathwayScreen(
    onTakeSkillCheck: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onSeeProgress: () -> Unit = {}
) {
    val context = LocalContext.current
    val skill = remember { UserProfile.skillResults(context) }

    if (!skill.done) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AccentStudy.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Quiz,
                    contentDescription = null,
                    tint = AccentStudy,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Your pathway starts with the Skill Check",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Answer 12 quick questions and ElectroSathi will order your topics — weakest first — and build a study plan around your exam date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onTakeSkillCheck,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Take the Skill Check")
            }
        }
        return
    }

    val weakEntries = skill.weakTopics.mapNotNull { topic ->
        SkillCheck.subjectIdForTopic(topic)?.let { subjectId ->
            FocusEntry(
                topic = topic,
                subjectId = subjectId,
                subjectName = Repository.subject(subjectId)?.name
                    ?: SkillCheck.subjectName(subjectId),
                needsWork = true
            )
        }
    }
    val weakSet = skill.weakTopics.toSet()
    val shakyEntries = SkillCheck.SUBJECTS
        .filter { (subjectId, _) ->
            SkillCheck.statusFor(skill.scoreFor(subjectId)) == "Getting there"
        }
        .flatMap { (subjectId, subjectName) ->
            Repository.subjectTopics(subjectId)
                .filter { it.title !in weakSet }
                .map { FocusEntry(it.title, subjectId, subjectName, needsWork = false) }
        }
    val focus = weakEntries + shakyEntries

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ScreenHeader(
                "My Pathway",
                "Built from your Skill Check — weakest topics first"
            )
        }
        item { Eyebrow("Your focus order") }
        if (focus.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text(
                        "Nothing to fix from your Skill Check — no weak or shaky topics found. Retake it from Study anytime your level changes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            itemsIndexed(focus) { index, entry ->
                FocusRow(index, entry) { onSubjectClick(entry.subjectId) }
            }
        }
        item { Eyebrow("Study plan") }
        item {
            StudyPlanCard(
                focusCount = focus.size,
                goal = UserProfile.goal(context)
            )
        }
        item { Eyebrow("Your improvement") }
        item { ImprovementCard(onSeeProgress = onSeeProgress) }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun FocusRow(index: Int, entry: FocusEntry, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(30.dp)
                    .clip(CircleShape)
                    .background(AccentStudy.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentStudy
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.topic,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    entry.subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            SkillStatusChip(if (entry.needsWork) "Needs work" else "Getting there")
        }
    }
}

// ---------------------------------------------------------------------------
// Study plan math. Dates come only from Repository's stored exam dates
// text (parsed defensively, first full date wins — the same approach as
// the Exam Center); the only fallback is GATE 2027's first exam day,
// 6 Feb 2027, which is also the first date in the GATE entry's text.
// ---------------------------------------------------------------------------
private val PlanDateRegex = Regex("(\\d{1,2})\\s+([A-Za-z]{3,})\\s+(\\d{4})")
private val PlanDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

private fun parsePlanDate(text: String): LocalDate? {
    val m = PlanDateRegex.find(text) ?: return null
    val month = when (m.groupValues[2].take(3).lowercase()) {
        "jan" -> 1
        "feb" -> 2
        "mar" -> 3
        "apr" -> 4
        "may" -> 5
        "jun" -> 6
        "jul" -> 7
        "aug" -> 8
        "sep" -> 9
        "oct" -> 10
        "nov" -> 11
        "dec" -> 12
        else -> return null
    }
    return runCatching {
        LocalDate.of(m.groupValues[3].toInt(), month, m.groupValues[1].toInt())
    }.getOrNull()
}

private fun targetExam(goal: String): Pair<Exam, LocalDate>? {
    val today = LocalDate.now()
    val exams = Repository.exams()
    val gate = exams.firstOrNull {
        it.id == "gate" || it.name.contains("GATE", ignoreCase = true)
    }
    val gateDate = LocalDate.of(2027, 2, 6)
    if (goal.contains("GATE", ignoreCase = true) && gate != null) {
        return gate to (parsePlanDate(gate.dates) ?: gateDate)
    }
    val dated = exams.mapNotNull { e ->
        parsePlanDate(e.dates)?.let { d -> e to d }
    }.filter { !it.second.isBefore(today) }
    dated.minByOrNull { it.second }?.let { return it }
    return gate?.let { it to (parsePlanDate(it.dates) ?: gateDate) }
}

@Composable
private fun StudyPlanCard(focusCount: Int, goal: String) {
    val today = LocalDate.now()
    val target = remember(goal) { targetExam(goal) }
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Assignment,
                    contentDescription = null,
                    tint = BrandCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Your study plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            if (focusCount == 0) {
                Text(
                    "No focus topics to schedule right now — your Skill Check found nothing weak or shaky.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val weeks = (focusCount + 1) / 2 // 2 topics a week, rounded up
                val finish = today.plusWeeks(weeks.toLong())
                Text(
                    "$focusCount focus topics · about $weeks " +
                        (if (weeks == 1) "week" else "weeks") +
                        " at 2 topics a week",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Starting today, you finish around ${finish.format(PlanDateFormat)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (target != null) {
                    val (exam, examDate) = target
                    val daysAway = ChronoUnit.DAYS.between(today, examDate)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${exam.name} is on ${examDate.format(PlanDateFormat)}" +
                            if (daysAway >= 0) " — $daysAway days away." else ".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (finish.isAfter(examDate)) {
                        val weeks3 = (focusCount + 2) / 3 // 3 topics a week
                        val finish3 = today.plusWeeks(weeks3.toLong())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tight — that finish lands after ${exam.name}. " +
                                "At 3 topics a week (about $weeks3 " +
                                (if (weeks3 == 1) "week" else "weeks") +
                                ") you'd finish around ${finish3.format(PlanDateFormat)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentProjects
                        )
                    }
                }
            }
        }
    }
}

/**
 * Batch 16 — the improvement summary on My Pathway. Reads the SQLite
 * attempt history (SkillAttempts): with two or more attempts it
 * compares first vs latest per subject; with fewer it says honestly
 * what happens next. The full report lives on My Progress.
 */
@Composable
private fun ImprovementCard(onSeeProgress: () -> Unit) {
    val context = LocalContext.current
    val attempts = remember { SkillAttempts.all(context) }
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            when {
                attempts.isEmpty() -> Text(
                    "Improvement tracking starts with your next Skill Check — " +
                        "from now on every attempt is saved on this phone, and this " +
                        "card will compare your first attempt with your latest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                attempts.size == 1 -> {
                    val a = attempts.first()
                    val date = Instant.ofEpochMilli(a.takenAt)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                        .format(
                            DateTimeFormatter.ofPattern(
                                "d MMM yyyy", Locale.ENGLISH
                            )
                        )
                    Text(
                        "First attempt saved — $date · Total ${a.total}/12 " +
                            "(Analog ${a.analog}/4 · Digital ${a.digital}/4 · " +
                            "Signals ${a.signals}/4). Retake the Skill Check after " +
                            "studying and this card shows your improvement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    val first = attempts.first()
                    val latest = attempts.last()
                    SkillCheck.SUBJECTS.forEach { (subjectId, subjectName) ->
                        val f = first.scoreFor(subjectId)
                        val l = latest.scoreFor(subjectId)
                        val delta = l - f
                        Text(
                            "$subjectName: $f → $l / 4" +
                                when {
                                    delta > 0 -> "  (+$delta ▲)"
                                    delta < 0 -> "  ($delta ▼)"
                                    else -> "  (same)"
                                },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Total: ${first.total} → ${latest.total} / 12 across " +
                            "${attempts.size} attempts",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSeeProgress,
                shape = RoundedCornerShape(50)
            ) { Text("See full progress") }
        }
    }
}
