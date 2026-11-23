package com.electrosathi.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrosathi.app.R
import com.electrosathi.app.data.Exam
import com.electrosathi.app.data.Hackathon
import com.electrosathi.app.data.Internship
import com.electrosathi.app.data.Lang
import com.electrosathi.app.data.Project
import com.electrosathi.app.data.Repository
import com.electrosathi.app.data.SkillCheck
import com.electrosathi.app.data.StudySubject
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.components.AccentCard
import com.electrosathi.app.ui.components.AccentCareer
import com.electrosathi.app.ui.components.AccentExams
import com.electrosathi.app.ui.components.AccentHack
import com.electrosathi.app.ui.components.AccentLibrary
import com.electrosathi.app.ui.components.AccentProjects
import com.electrosathi.app.ui.components.AccentSkill
import com.electrosathi.app.ui.components.AccentStudy
import com.electrosathi.app.ui.components.AccentVolt
import com.electrosathi.app.ui.components.CountdownCard
import com.electrosathi.app.ui.components.DeadlineTile
import com.electrosathi.app.ui.components.Eyebrow
import com.electrosathi.app.ui.components.GradientTile
import com.electrosathi.app.ui.components.PillSearchBar
import com.electrosathi.app.ui.components.ProgressRing
import com.electrosathi.app.ui.components.PromoBanner
import com.electrosathi.app.ui.theme.BrandCyan
import com.electrosathi.app.ui.theme.BrandGradient
import com.electrosathi.app.ui.theme.BrandPurple
import com.electrosathi.app.ui.theme.CountdownGradientCool
import com.electrosathi.app.ui.theme.CountdownGradientWarm
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ---------------------------------------------------------------------------
// Shared bits.
// ---------------------------------------------------------------------------

/** Opens a web link in the device browser (Batch 7 — internship Apply).
 *  Any failure (no browser, bad URL) is swallowed silently: opening a
 *  link must never crash the app. */
fun openInBrowser(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
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
// STUDY — "My Study Hub" (Batch 4 Part 2, from Ashish's reference board):
// search pill and active-subject progress cards with generated thumbnails,
// then the original pathway / plan / sharpen-up cards below, unchanged.
// Batch 5: the subject cards are Repository content now (Admin Panel →
// Study Subjects); thumbnails and accent colours stay in the UI, keyed
// by the subject's position in the list.
// ---------------------------------------------------------------------------
private val SubjectAccents = listOf(AccentStudy, BrandPurple, AccentCareer)

private fun subjectImage(index: Int): Int = when (index) {
    0 -> R.drawable.subj_analog
    1 -> R.drawable.subj_digital
    else -> R.drawable.subj_signals
}

@Composable
private fun StudySubjectCard(s: StudySubject, index: Int, onClick: () -> Unit) {
    val accent = SubjectAccents[index % SubjectAccents.size]
    val progress = s.progressPercent.coerceIn(0, 100) / 100f
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(14.dp)) {
            Image(
                painter = painterResource(subjectImage(index)),
                contentDescription = null,
                modifier = Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        s.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${s.progressPercent}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.fillMaxWidth().height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier.fillMaxWidth(progress).height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(listOf(accent, BrandCyan))
                            )
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        "Next up: ${s.nextTopic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        "${s.topicsDone}/${s.topicsTotal} Topics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "View contents →",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandCyan
                )
            }
        }
    }
}

@Composable
fun StudyScreen(
    onSubjectClick: (String) -> Unit,
    onFlashcards: () -> Unit,
    onSkillCheck: () -> Unit
) {
    val context = LocalContext.current
    val subjects = remember { Repository.subjects() }
    // Batch 10: the Skill Check card reports the real stored state —
    // not taken / skipped / last result + date — and opens the quiz (or
    // a retake, which overwrites the old results: the adaptation loop).
    val skill = remember { UserProfile.skillResults(context) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("My Study Hub", "Your personalised learning path") }
        item { PillSearchBar(hint = "Search subjects, topics...", onClick = { }) }
        item { Eyebrow("Active subjects") }
        itemsIndexed(subjects) { index, s ->
            StudySubjectCard(s, index) { onSubjectClick(s.id) }
        }
        item { Eyebrow("Continue learning") }
        item {
            AccentCard(
                title = "My Pathway — Embedded Systems",
                subtitle = "4 of 12 milestones complete",
                accent = AccentStudy, icon = Icons.Filled.Timeline,
                badge = "34%"
            )
        }
        item {
            AccentCard(
                title = "This week's plan",
                subtitle = "Mon: ADC interfacing • Wed: Timers quiz • Sat: Mini project",
                accent = AccentStudy, icon = Icons.Filled.School
            )
        }
        item { Eyebrow("Sharpen up") }
        item {
            AccentCard(
                title = "Skill Check",
                subtitle = when {
                    skill.done && skill.weakTopics.isEmpty() ->
                        "Last taken ${UserProfile.skillCheckWhenText(context)} — no weak topics found"
                    skill.done ->
                        "Last taken ${UserProfile.skillCheckWhenText(context)} — ${skill.weakTopics.size} focus topics"
                    skill.skipped ->
                        "Skipped for now — 12 questions, ~3 min when you're ready"
                    else ->
                        "Not taken yet — 12 questions, ~3 min to map your gaps"
                },
                accent = AccentStudy, icon = Icons.Filled.Quiz,
                badge = if (skill.done) "Retake" else null,
                onClick = onSkillCheck
            )
        }
        item {
            AccentCard(
                title = "Micro Lab — 8051/8085",
                subtitle = "Assembler + step simulator",
                accent = AccentStudy, icon = Icons.Filled.Science,
                badge = "Soon"
            )
        }
        item {
            AccentCard(
                title = "Flashcards",
                subtitle = "Analog Circuits deck — 12 real cards, tap to flip",
                accent = AccentStudy, icon = Icons.Filled.Psychology,
                onClick = onFlashcards
            )
        }
    }
}

// ---------------------------------------------------------------------------
// EXAMS — "Exam Center" (Batch 4 Part 2): search pill, filter chips and
// exam cards with a bright badge on the right, cycling red/orange/yellow/
// purple like the reference board.
// BADGE RULE (same as Home — do not break): the badge counts down ONLY
// when the exam's dates text holds a full future date ("19 Oct 2027");
// otherwise it shows the dates text itself. Never an invented day count.
// ---------------------------------------------------------------------------
private val ExamBadgeColors = listOf(
    Color(0xFFE11D48), // red
    Color(0xFFF97316), // orange
    Color(0xFFEAB308), // yellow
    Color(0xFFA855F7)  // purple
)

private fun examDaysLeft(exam: Exam): Long? {
    val d = parseExamDate(exam.dates) ?: return null
    val today = LocalDate.now()
    return if (d.isAfter(today)) ChronoUnit.DAYS.between(today, d) else null
}

/** Short badge text from the dates the app already stores. */
private fun examBadgeText(exam: Exam): String =
    exam.dates.split(".").first().split("—").first().trim()

/** Sub-line under the exam name, from its previous-papers field. */
private fun examSubline(exam: Exam): String {
    val papers = exam.papers.substringBefore("(").split(".").first().trim()
    return papers.ifBlank { exam.category }
}

@Composable
private fun ExamBadgeCard(exam: Exam, badgeColor: Color, onClick: () -> Unit) {
    val days = remember(exam.dates) { examDaysLeft(exam) }
    val onBadge =
        if (badgeColor == ExamBadgeColors[2]) Color(0xFF1C1917) else Color.White
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    exam.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    examSubline(exam),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    exam.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            // Batch 5 fix: the badge is wide enough (112.dp) and the text
            // small enough that long words like "Notification" wrap at
            // word boundaries — they never split mid-word.
            Box(
                modifier = Modifier.width(112.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (days != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            days.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = onBadge
                        )
                        Text(
                            "Days Left",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = onBadge
                        )
                    }
                } else {
                    Text(
                        examBadgeText(exam),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = onBadge,
                        maxLines = 4,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(onExamClick: (String) -> Unit) {
    val exams = remember { Repository.exams() }
    val filters = listOf("All", "Upcoming", "Mock Tests")
    var selected by remember { mutableStateOf(filters[0]) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Exam Center", "Tap any exam for the full breakdown") }
        item { PillSearchBar(hint = "Search exams...", onClick = { }) }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { f ->
                    FilterChip(
                        selected = selected == f,
                        onClick = { selected = f },
                        label = { Text(f) }
                    )
                }
            }
        }
        itemsIndexed(exams) { index, exam ->
            ExamBadgeCard(
                exam = exam,
                badgeColor = ExamBadgeColors[index % ExamBadgeColors.size],
                onClick = { onExamClick(exam.id) }
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

// ---------------------------------------------------------------------------
// PROJECTS — "DIY Project Arena" (Batch 4 Part 2): Explore / My Builds
// tabs, the original level filter, and photo cards for the first builds.
// Photo rule: the first three projects of the full list get a generated
// photo header; every later project keeps a gradient + icon header.
// ---------------------------------------------------------------------------
private fun projectPhoto(index: Int): Int? = when (index) {
    0 -> R.drawable.proj_arduino
    1 -> R.drawable.proj_iot
    2 -> R.drawable.proj_robot
    else -> null
}

@Composable
private fun ProjectPhotoCard(p: Project, photo: Int?, onClick: () -> Unit) {
    var liked by remember { mutableStateOf(false) }
    val tagColor = if (p.level == "Basic") Color(0xFF16A34A) else Color(0xFF9333EA)
    val difficulty = when (p.level) {
        "Basic" -> "Easy"
        "Advanced" -> "Hard"
        else -> "Medium"
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(168.dp)) {
                if (photo != null) {
                    Image(
                        painter = painterResource(photo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                listOf(
                                    AccentProjects.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = AccentProjects,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = tagColor,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        p.level,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        p.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { liked = !liked },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (liked) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save project",
                            tint = if (liked) AccentVolt
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Difficulty: $difficulty • ${p.time} • ${p.cost}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            if (p.level == "Advanced") "Take Challenge"
                            else "Start Project",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(onProjectClick: (String) -> Unit) {
    val levels = listOf("Basic", "Intermediate", "Advanced")
    var selected by remember { mutableStateOf(levels[0]) }
    var tab by remember { mutableStateOf(0) }
    val all = remember { Repository.projects() }
    val projects = remember(selected) { all.filter { it.level == selected } }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("DIY Project Arena", "Tap a project for the full build guide") }
        item {
            TabRow(selectedTabIndex = tab, modifier = Modifier.padding(horizontal = 16.dp)) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Explore") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Builds") })
            }
            Spacer(Modifier.height(8.dp))
        }
        if (tab == 1) {
            item {
                AccentCard(
                    title = "My Builds",
                    subtitle = "Your portfolio — coming soon",
                    accent = AccentProjects, icon = Icons.Filled.Star,
                    badge = "Soon"
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    levels.forEach { l ->
                        FilterChip(
                            selected = selected == l,
                            onClick = { selected = l },
                            label = { Text(l) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(projects) { p ->
                ProjectPhotoCard(
                    p = p,
                    photo = projectPhoto(all.indexOf(p)),
                    onClick = { onProjectClick(p.id) }
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

// ---------------------------------------------------------------------------
// Shared internship card (Batch 4 Part 2) — gradient initial chip, title,
// org, a meta line from the stored fields and a pill Apply button. Used by
// Career (Opportunities). No stipend is shown: the data model stores
// Paid/Unpaid, not an amount, so none is invented here.
// Batch 7: tapping the card still opens the detail page, but the Apply
// pill now opens the internship's official link in the browser when one
// is stored; with no link it falls back to the detail page.
// ---------------------------------------------------------------------------
@Composable
private fun InternshipCard(i: Internship, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (i.org.firstOrNull()?.uppercase() ?: "?"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    i.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    i.org,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${i.location} • ${i.paid} • by ${i.deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (i.link.isNotBlank()) openInBrowser(context, i.link) else onClick()
                },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "Apply Now",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OPPORTUNITIES — teal identity, deadline tile on top.
// ---------------------------------------------------------------------------
@Composable
fun OpportunitiesScreen(onInternshipClick: (String) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val internships = remember { Repository.internships() }
    val sectors = listOf(
        "Semiconductors", "Consumer Electronics", "Telecom", "Defence",
        "Power & EV", "IoT", "Industrial Automation", "Medical Electronics", "Space"
    )
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Opportunities", "Internships, jobs & the sectors behind them") }
        item {
            // Next upcoming deadline, PrepFusion-countdown style.
            val next = internships.firstOrNull()
            if (next != null) {
                DeadlineTile(
                    label = "${next.title} • ${next.org}",
                    date = next.deadline
                )
            }
        }
        item {
            TabRow(selectedTabIndex = tab, modifier = Modifier.padding(horizontal = 16.dp)) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("India") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Abroad") })
            }
            Spacer(Modifier.height(8.dp))
        }
        item { Eyebrow("Open internships") }
        items(internships) { i ->
            InternshipCard(i) { onInternshipClick(i.id) }
        }
        item { Eyebrow("Browse sectors") }
        items(sectors) { s ->
            AccentCard(
                title = s,
                subtitle = if (tab == 0) "Govt + private orgs in India" else "Global orgs hiring",
                accent = AccentCareer, icon = Icons.Filled.Business
            )
        }
    }
}

// ---------------------------------------------------------------------------
// DIGITAL LIBRARY — "E-Library" (Batch 4 Part 2): search pill, Books /
// Notes tabs, and a 2-column grid of covers drawn in code (gradient +
// title + author, so the text is always accurate). The Notes tab keeps
// the original subject list view, so nothing from the old Library is
// lost. Books and notes only — internships live in the Career section.
// ---------------------------------------------------------------------------
private val BookCoverGradients = listOf(
    listOf(Color(0xFF1D4ED8), Color(0xFF0EA5E9)),
    listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
    listOf(Color(0xFF0F766E), Color(0xFF22D3EE)),
    listOf(Color(0xFFB45309), Color(0xFFF59E0B)),
    listOf(Color(0xFF1E3A8A), Color(0xFF6366F1)),
    listOf(Color(0xFF9D174D), Color(0xFFF97316))
)

/** Bundled cover-art keys → drawables (Batches 7–8). Blank/unknown keys keep
 *  the code-drawn gradient cover. */
private fun bookCoverRes(cover: String): Int? = when (cover) {
    "book_analog" -> R.drawable.book_analog
    "book_digital" -> R.drawable.book_digital
    "book_signals" -> R.drawable.book_signals
    else -> null
}

@Composable
private fun BookCover(
    title: String,
    author: String,
    rating: String,
    format: String,
    index: Int,
    coverRes: Int? = null,
    modifier: Modifier = Modifier
) {
    val colors = BookCoverGradients[index % BookCoverGradients.size]
    Column(modifier) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(0.74f)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(colors))
        ) {
            // Batch 7: a book with bundled cover art uses the image as
            // the cover background (with a scrim so the overlaid text
            // stays readable); title/author remain Compose text on top.
            if (coverRes != null) {
                Image(
                    painter = painterResource(coverRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.34f))
                )
            }
            Box(Modifier.fillMaxSize().padding(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (format == "PDF") Color(0xFFDC2626) else Color(0xFF111827),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        format,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Column(Modifier.align(Alignment.BottomStart)) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 3
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        author,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 2
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = AccentSkill,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                rating,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Downloaded",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LibraryScreen() {
    // Batch 5: the shelf is Repository content now (Admin Panel → Books).
    // Cover gradients are still drawn in code from the list position.
    val books = remember { Repository.books() }
    var tab by remember { mutableStateOf(0) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("E-Library", "Every ECE book that matters") }
        item { PillSearchBar(hint = "Search books, notes...", onClick = { }) }
        item {
            TabRow(selectedTabIndex = tab, modifier = Modifier.padding(horizontal = 16.dp)) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Books") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Notes") })
            }
            Spacer(Modifier.height(8.dp))
        }
        if (tab == 0) {
            items(books.chunked(2)) { rowBooks ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowBooks.forEach { b ->
                        val idx = books.indexOf(b)
                        BookCover(
                            title = b.title,
                            author = b.author,
                            rating = b.rating,
                            format = b.format,
                            index = idx,
                            coverRes = bookCoverRes(b.cover),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowBooks.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            item { Eyebrow("By subject") }
            items(books) { b ->
                AccentCard(
                    title = b.title, subtitle = b.author,
                    accent = AccentLibrary, icon = Icons.Filled.MenuBook
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

// ---------------------------------------------------------------------------
// LIVE HACKATHONS (drawer) — orange identity.
// Batch 5: driven by Repository, like every other content section —
// the team publishes these from the Admin Panel (Admin → Hackathons),
// and tapping a card opens the full hackathon detail page.
// ---------------------------------------------------------------------------
@Composable
private fun HackathonCard(h: Hackathon, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AccentHack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    h.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    h.org,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Register by ${h.deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentHack,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HackathonsScreen(onHackathonClick: (String) -> Unit) {
    val hackathons = remember { Repository.hackathons() }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Live Hackathons", "Compete, build, get noticed") }
        item { Eyebrow("Register now") }
        if (hackathons.isEmpty()) {
            item {
                AccentCard(
                    title = "No live hackathons right now",
                    subtitle = "New ones land here the moment the team publishes them",
                    accent = AccentHack, icon = Icons.Filled.EmojiEvents
                )
            }
        } else {
            items(hackathons) { h ->
                HackathonCard(h) { onHackathonClick(h.id) }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

// ---------------------------------------------------------------------------
// SKILL SHOW (drawer) — yellow identity.
// ---------------------------------------------------------------------------
@Composable
fun SkillShowScreen() {
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Skill Show", "Show what you can build") }
        item { Eyebrow("Trending builds") }
        item {
            AccentCard(
                title = "My line follower in 60 seconds",
                subtitle = "@arjun_ece • Badge: Robotics",
                accent = AccentSkill, icon = Icons.Filled.Star,
                badge = "128 ♥"
            )
        }
        item {
            AccentCard(
                title = "Home automation under ₹500",
                subtitle = "@priya_builds • Badge: IoT",
                accent = AccentSkill, icon = Icons.Filled.Star,
                badge = "96 ♥"
            )
        }
        item {
            AccentCard(
                title = "Monthly theme: best sensor project",
                subtitle = "Post your build before 31 Oct",
                accent = AccentSkill, icon = Icons.Filled.FilterList
            )
        }
    }
}

// ---------------------------------------------------------------------------
// HOME — Batch 4: rebuilt to Ashish's reference design. Brand header,
// search pill, gradient GATE banner, six shortcut tiles, a Continue
// Learning ring card and two exam-countdown cards. Every tile and button
// navigates to a real destination via the callbacks from MainActivity.
// ---------------------------------------------------------------------------
@Composable
fun HomeScreen(
    userName: String,
    onOpenDrawer: () -> Unit,
    onStudy: () -> Unit,
    onExams: () -> Unit,
    onProjects: () -> Unit,
    onCareer: () -> Unit,
    onLibrary: () -> Unit,
    onHackathons: () -> Unit,
    onPathway: () -> Unit,
    onSubjectClick: (String) -> Unit,
    profileTick: Int = 0
) {
    // Personalised greeting (Batch 6): the signed-in user's first name.
    // Guests — and any blank name — are greeted as "Learner".
    val firstName = userName.trim().substringBefore(' ').ifBlank { "Learner" }
    val countdowns = remember { examCountdowns() }
    // Batch 10: the Skill Check results behind the "Your focus" card.
    // profileTick changes when the app-start profile refresh lands, so
    // results saved on another device appear without a restart.
    val context = LocalContext.current
    val skill = remember(profileTick) { UserProfile.skillResults(context) }
    LazyColumn(Modifier.fillMaxSize()) {
        // ---- Brand row + greeting ----
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FlashOn,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) { append("Electro") }
                                withStyle(
                                    SpanStyle(
                                        color = BrandCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) { append("Sathi") }
                            },
                            fontSize = 24.sp
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Namaste, $firstName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Avatar: initial in a gradient ring (no photo feature yet).
                // The app bar steps aside on Home, so tapping the avatar
                // opens the side drawer instead.
                Box(
                    modifier = Modifier.size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, BrandGradient, CircleShape)
                        .clickable(onClick = onOpenDrawer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            }
        }

        // ---- Search pill (opens Study for now; search backend is later) ----
        item {
            PillSearchBar(
                hint = "Search topics, exams, projects...",
                onClick = onStudy
            )
        }

        // ---- Your focus (Batch 10) — only once a Skill Check exists.
        // New users meet the quiz at the start gate instead, so Home
        // stays clean for them; skipped users see no card either. ----
        if (skill.done) {
            item { HomeSectionHeader(Lang.tr("Your Focus")) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (skill.weakTopics.isEmpty()) {
                            Text(
                                "No weak topics from your Skill Check — all three subjects are looking strong.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "From your Skill Check — weakest first:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            skill.weakTopics.take(3).forEach { topic ->
                                val subjectId = SkillCheck.subjectIdForTopic(topic)
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = subjectId != null) {
                                            subjectId?.let(onSubjectClick)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Quiz,
                                        contentDescription = null,
                                        tint = BrandCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            topic,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            SkillCheck.subjectNameForTopic(topic) ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        TextButton(onClick = onPathway) {
                            Text("My Pathway →", color = BrandCyan)
                        }
                    }
                }
            }
        }

        // ---- GATE promo banner ----
        item {
            PromoBanner(
                title = "GATE 2027 — Exam Days Announced",
                subtitle = "Dates, pattern, syllabus and previous papers — all in one place.",
                buttonText = "View Exam",
                onButtonClick = onExams
            )
        }

        // ---- Shortcut tiles ----
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientTile(
                        "Study", Icons.Filled.School, AccentStudy, onStudy,
                        Modifier.weight(1f)
                    )
                    GradientTile(
                        "Exams", Icons.Filled.Assignment, AccentExams, onExams,
                        Modifier.weight(1f)
                    )
                    GradientTile(
                        "Projects", Icons.Filled.Build, AccentProjects, onProjects,
                        Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientTile(
                        "Career", Icons.Filled.Work, AccentCareer, onCareer,
                        Modifier.weight(1f)
                    )
                    GradientTile(
                        "Library", Icons.Filled.MenuBook, AccentLibrary, onLibrary,
                        Modifier.weight(1f)
                    )
                    GradientTile(
                        "Hackathons", Icons.Filled.EmojiEvents, AccentHack, onHackathons,
                        Modifier.weight(1f)
                    )
                }
            }
        }

        // ---- Continue learning ----
        item { HomeSectionHeader("Continue Learning", "See All", onStudy) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Digital Electronics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Karnaugh Maps (K-Maps)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Button(
                                onClick = onStudy,
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp, vertical = 8.dp
                                )
                            ) {
                                Text("Resume", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    ProgressRing(progress = 0.68f)
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(R.drawable.thumb_chip),
                        contentDescription = null,
                        modifier = Modifier.size(76.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // ---- Exam countdown ----
        item { HomeSectionHeader("Exam Countdown") }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                countdowns.forEachIndexed { index, c ->
                    CountdownCard(
                        name = c.exam.name,
                        days = c.days,
                        detail = c.detail,
                        brush = if (index == 0) CountdownGradientWarm
                        else CountdownGradientCool,
                        onClick = onExams,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item { Spacer(Modifier.height(28.dp)) }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = BrandCyan)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Exam-countdown data for Home. Uses only what Repository already stores:
// when an exam's dates text holds a full future date (e.g. "19 Oct 2027"),
// the card counts down to it; otherwise the card shows the dates text
// itself. No exam dates are invented here.
// ---------------------------------------------------------------------------
private data class ExamCountdown(val exam: Exam, val days: Long?, val detail: String)

private fun examCountdowns(): List<ExamCountdown> {
    val today = LocalDate.now()
    val exams = Repository.exams()
    val dated = exams.mapNotNull { e ->
        val d = parseExamDate(e.dates)
        if (d != null && !d.isBefore(today)) {
            ExamCountdown(e, ChronoUnit.DAYS.between(today, d), "")
        } else {
            null
        }
    }.sortedBy { it.days }
    if (dated.size >= 2) return dated.take(2)
    val used = dated.map { it.exam.id }.toSet()
    val filler = exams.filter { it.id !in used }.map { e ->
        ExamCountdown(e, null, e.dates.split(".").first().trim())
    }
    return (dated + filler).take(2)
}

private val ExamDateRegex = Regex("(\\d{1,2})\\s+([A-Za-z]{3,})\\s+(\\d{4})")

private fun parseExamDate(text: String): LocalDate? {
    val m = ExamDateRegex.find(text) ?: return null
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
