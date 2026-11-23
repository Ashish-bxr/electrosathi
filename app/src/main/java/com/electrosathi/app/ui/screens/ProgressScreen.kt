package com.electrosathi.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.Lang
import com.electrosathi.app.data.SkillAttempt
import com.electrosathi.app.data.SkillAttempts
import com.electrosathi.app.data.SkillCheck
import com.electrosathi.app.ui.components.Eyebrow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * My Progress — Batch 16: the real-time improvement report the
 * mentoring session asked for.
 *
 * Every Skill Check attempt is stored on the phone (SQLite —
 * [SkillAttempts]); this screen compares the FIRST attempt with the
 * LATEST, subject by subject, and lists the full history. It reads
 * the store fresh every time it opens, so finishing a retake and
 * coming straight here shows the new numbers immediately — that is
 * the "real time": computed on-device, the moment it happens.
 */
@Composable
fun ProgressScreen(onRetake: () -> Unit) {
    val context = LocalContext.current
    val attempts = remember { SkillAttempts.all(context) }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ScreenHeader(
                Lang.tr("My Progress"),
                "Your Skill Check history — saved on this phone, updated the moment you finish a retake"
            )
        }
        if (attempts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "No Skill Check attempts yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Take the Skill Check and your first result lands here. " +
                                "Every retake is saved too, so you can watch each subject improve.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRetake) { Text("Take the Skill Check") }
                    }
                }
            }
        } else {
            val first = attempts.first()
            val latest = attempts.last()
            item { Eyebrow("Improvement — first vs latest") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SkillCheck.SUBJECTS.forEach { (subjectId, subjectName) ->
                            SubjectProgressRow(
                                subjectName = subjectName,
                                firstScore = first.scoreFor(subjectId),
                                latestScore = latest.scoreFor(subjectId),
                                singleAttempt = attempts.size == 1
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        val delta = latest.total - first.total
                        Text(
                            when {
                                attempts.size == 1 ->
                                    "Total: ${latest.total} / 12 — retake the Skill Check to start tracking improvement."
                                delta > 0 ->
                                    "Total: ${first.total} → ${latest.total} / 12 — up by $delta. Keep going."
                                delta < 0 ->
                                    "Total: ${first.total} → ${latest.total} / 12 — down by ${-delta}. Your pathway already re-orders around the new weak topics."
                                else ->
                                    "Total: ${latest.total} / 12 — same total, but check the subjects: strength may have moved."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            item { Eyebrow("Attempt history") }
            items(attempts.reversed()) { attempt ->
                AttemptRow(attempt)
            }
            item {
                Button(
                    onClick = onRetake,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(50)
                ) { Text("Retake the Skill Check") }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SubjectProgressRow(
    subjectName: String,
    firstScore: Int,
    latestScore: Int,
    singleAttempt: Boolean
) {
    Column {
        Row {
            Text(
                subjectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (singleAttempt) "$latestScore / 4"
                else "$firstScore → $latestScore / 4",
                style = MaterialTheme.typography.bodyLarge
            )
            if (!singleAttempt) {
                Spacer(Modifier.width(8.dp))
                val delta = latestScore - firstScore
                Text(
                    when {
                        delta > 0 -> "+$delta ▲"
                        delta < 0 -> "$delta ▼"
                        else -> "same"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        delta > 0 -> MaterialTheme.colorScheme.primary
                        delta < 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = latestScore / 4f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AttemptRow(attempt: SkillAttempt) {
    val date = Instant.ofEpochMilli(attempt.takenAt)
        .atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                date,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Analog ${attempt.analog}/4 · Digital ${attempt.digital}/4 · " +
                    "Signals ${attempt.signals}/4 · Total ${attempt.total}/12",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (attempt.weakTopics.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Focus: ${attempt.weakTopics.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
