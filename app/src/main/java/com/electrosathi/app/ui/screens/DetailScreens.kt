package com.electrosathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.Repository
import com.electrosathi.app.ui.components.DeadlineTile

// ---------------------------------------------------------------------------
// Shared detail-page building blocks.
// ---------------------------------------------------------------------------
@Composable
fun DetailBlock(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BulletList(items: List<String>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items.forEach {
            Text(
                "•  $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun NumberedList(items: List<String>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items.forEachIndexed { i, s ->
            Text(
                "${i + 1}.  $s",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipRow(items: List<String>) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { AssistChip(onClick = { }, label = { Text(it) }) }
    }
}

@Composable
fun DetailHeader(title: String, subtitle: String) {
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// EXAM DETAIL
// ---------------------------------------------------------------------------
@Composable
fun ExamDetailScreen(examId: String) {
    val exam = Repository.exam(examId) ?: return
    LazyColumn(Modifier.fillMaxSize()) {
        item { DetailHeader(exam.name, exam.category) }
        item { ChipRow(listOf(exam.category)) }
        item { DetailBlock("Overview", exam.overview) }
        item { DetailBlock("Eligibility", exam.eligibility) }
        item { DetailBlock("Exam Pattern", exam.pattern) }
        item { DetailBlock("Syllabus", exam.syllabus) }
        item { DetailBlock("Important Dates", exam.dates) }
        item { DetailBlock("Previous Papers", exam.papers) }
        item { DetailBlock("Cutoff Trend", exam.cutoff) }
        item {
            Button(
                onClick = { /* TODO Phase 3: generate a prep plan in Study */ },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Make my prep plan")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PROJECT DETAIL
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(projectId: String) {
    val p = Repository.project(projectId) ?: return
    LazyColumn(Modifier.fillMaxSize()) {
        item { DetailHeader(p.title, p.domain) }
        item { ChipRow(listOf(p.level, p.time, p.cost)) }
        item { DetailBlock("Overview", p.overview) }
        item { DetailBlock("Components", p.components.joinToString("\n") { "•  $it" }) }
        item {
            DetailBlock(
                "Build Steps",
                p.steps.mapIndexed { i, s -> "${i + 1}.  $s" }.joinToString("\n\n")
            )
        }
        item { DetailBlock("What You'll Learn", p.learns) }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* TODO: ask Volt about this project */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ask Volt about this build")
                }
                OutlinedButton(
                    onClick = { /* TODO: save to My Builds */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as built")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// INTERNSHIP DETAIL — countdown-style deadline tile on top.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InternshipDetailScreen(internshipId: String) {
    val i = Repository.internship(internshipId) ?: return
    LazyColumn(Modifier.fillMaxSize()) {
        item { DetailHeader(i.title, i.org) }
        item { ChipRow(listOf(i.type, i.paid, i.location)) }
        item {
            DeadlineTile(
                label = "Applications close — don't miss it",
                date = i.deadline
            )
        }
        item { DetailBlock("About", i.description) }
        item { DetailBlock("Eligibility", i.eligibility) }
        if (i.link.isNotBlank()) {
            item { DetailBlock("Official page", i.link) }
        }
        item {
            val context = LocalContext.current
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Batch 7: Apply opens the official page in the browser.
                // No stored link → no Apply button at all (never a dead
                // or invented one).
                if (i.link.isNotBlank()) {
                    Button(
                        onClick = { openInBrowser(context, i.link) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply now")
                    }
                }
                OutlinedButton(
                    onClick = { /* TODO: save to application tracker */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Track this application")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HACKATHON DETAIL — deadline tile + register button (Batch 5).
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HackathonDetailScreen(hackathonId: String) {
    val h = Repository.hackathon(hackathonId) ?: return
    LazyColumn(Modifier.fillMaxSize()) {
        item { DetailHeader(h.name, h.org) }
        item { ChipRow(listOf(h.mode, h.location)) }
        item {
            DeadlineTile(
                label = "Registration closes — don't miss it",
                date = h.deadline
            )
        }
        item { DetailBlock("About", h.description) }
        item { DetailBlock("Prizes", h.prize) }
        if (h.link.isNotBlank()) {
            item { DetailBlock("Registration link", h.link) }
        }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* TODO: open h.link in browser */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register now")
                }
                OutlinedButton(
                    onClick = { /* TODO: team finder */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Find a team")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SUBJECT DETAIL (Batch 7) — a Study Hub subject's real contents page.
// Subjects with built-in contents (today: Analog Circuits) show their
// topic list with expandable lesson notes; subjects without contents
// show an honest "being prepared" state — never placeholder topics.
// ---------------------------------------------------------------------------
@Composable
fun SubjectDetailScreen(subjectId: String, onFlashcards: (String) -> Unit) {
    val subject = Repository.subject(subjectId) ?: return
    val topics = remember(subjectId) { Repository.subjectTopics(subjectId) }
    val deckSize = remember(subjectId) { Repository.flashcards(subjectId).size }
    var expanded by remember(subjectId) { mutableStateOf(-1) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { DetailHeader(subject.name, "Study subject") }
        item {
            DetailBlock(
                "Your progress",
                "${subject.progressPercent}% complete • " +
                    "${subject.topicsDone} of ${subject.topicsTotal} topics done • " +
                    "Next up: ${subject.nextTopic}"
            )
        }
        if (deckSize > 0) {
            item {
                Button(
                    onClick = { onFlashcards(subjectId) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Practice Flashcards ($deckSize cards)")
                }
            }
        }
        if (topics.isEmpty()) {
            item {
                DetailBlock(
                    "Contents",
                    "Contents for this subject are being prepared by the team — " +
                        "real topics and notes land here soon. Nothing placeholder " +
                        "is shown in the meantime."
                )
            }
        } else {
            item { DetailBlock("Contents", "Tap a topic to read its lesson note.") }
            topics.forEachIndexed { index, topic ->
                item {
                    Card(
                        onClick = { expanded = if (expanded == index) -1 else index },
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${index + 1}.  ${topic.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (expanded == index) Icons.Filled.KeyboardArrowUp
                                    else Icons.Filled.KeyboardArrowDown,
                                    contentDescription =
                                        if (expanded == index) "Collapse topic" else "Expand topic"
                                )
                            }
                            if (expanded == index) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    topic.note,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------------------------
// FLASHCARDS (Batch 7) — tap the card to flip between question and
// answer; Previous/Next move through the deck with a running count.
// ---------------------------------------------------------------------------
@Composable
fun FlashcardsScreen(subjectId: String) {
    val subject = Repository.subject(subjectId)
    val deck = remember(subjectId) { Repository.flashcards(subjectId) }
    var index by remember(subjectId) { mutableStateOf(0) }
    var flipped by remember(subjectId) { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            DetailHeader(
                "Flashcards",
                subject?.let { "${it.name} deck" } ?: "Flashcards"
            )
        }
        if (deck.isEmpty()) {
            item {
                DetailBlock(
                    "No deck yet",
                    "Flashcards for this subject are being prepared — check back soon."
                )
            }
        } else {
            val card = deck[index]
            item {
                Card(
                    onClick = { flipped = !flipped },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(280.dp)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (flipped) "ANSWER" else "QUESTION",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (flipped) card.back else card.front,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Tap the card to flip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    LinearProgressIndicator(
                        progress = { (index + 1).toFloat() / deck.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Card ${index + 1} of ${deck.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (index > 0) {
                                    index--
                                    flipped = false
                                }
                            },
                            enabled = index > 0,
                            modifier = Modifier.weight(1f)
                        ) { Text("Previous") }
                        Button(
                            onClick = {
                                if (index < deck.size - 1) {
                                    index++
                                    flipped = false
                                }
                            },
                            enabled = index < deck.size - 1,
                            modifier = Modifier.weight(1f)
                        ) { Text("Next") }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
