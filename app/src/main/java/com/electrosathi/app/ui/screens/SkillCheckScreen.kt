package com.electrosathi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.SkillCheck
import com.electrosathi.app.data.SkillQuestion
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.components.AccentExams
import com.electrosathi.app.ui.components.AccentProjects
import com.electrosathi.app.ui.components.AccentStudy
import com.electrosathi.app.ui.theme.BrandCyan

// ---------------------------------------------------------------------------
// Skill Check (Batch 10) — intro → 12 questions → results.
//
// Shown two ways, with the same composable:
// - as a start-gate stage (MainActivity): first run, right after sign-in,
//   before Home. "Skip for now" leaves without nagging on later launches.
// - as the "skillcheck" route: retakes from the Study hub. Finishing a
//   retake overwrites the stored results — that is the adaptation loop.
//
// Results are saved the moment the last question is answered, through
// UserProfile (prefs + merged into users/{uid}); the results phase below
// only reads the picks back and scores them.
// ---------------------------------------------------------------------------

private enum class SkillPhase { Intro, Quiz, Results }

/** Rose used for wrong answers — the Exam Center badge red. */
private val SkillBad = Color(0xFFE11D48)

/** Small coloured pill for a subject status: Needs work / Getting there / Strong. */
@Composable
fun SkillStatusChip(status: String) {
    val color = when (status) {
        "Strong" -> AccentExams
        "Getting there" -> AccentProjects
        else -> SkillBad
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f)
    ) {
        Text(
            status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun SkillCheckScreen(
    onExit: () -> Unit,
    onGoHome: () -> Unit,
    onSeePathway: () -> Unit
) {
    val context = LocalContext.current
    val questions = SkillCheck.QUESTIONS
    var phase by remember { mutableStateOf(SkillPhase.Intro) }
    var index by remember { mutableStateOf(0) }
    var currentPick by remember { mutableStateOf<Int?>(null) }
    val picks = remember { mutableListOf<Int>() }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        when (phase) {
            SkillPhase.Intro -> {
                SkillIntro(
                    onStart = { phase = SkillPhase.Quiz },
                    onSkip = {
                        UserProfile.markSkillCheckSkipped(context)
                        onExit()
                    }
                )
            }
            SkillPhase.Quiz -> {
                SkillQuizQuestion(
                    question = questions[index],
                    index = index,
                    total = questions.size,
                    currentPick = currentPick,
                    onPick = { currentPick = it },
                    onNext = {
                        picks.add(currentPick ?: -1)
                        if (index == questions.lastIndex) {
                            UserProfile.saveSkillResults(
                                context = context,
                                analog = SkillCheck.scoreFor(picks, "analog-circuits"),
                                digital = SkillCheck.scoreFor(picks, "digital-electronics"),
                                signals = SkillCheck.scoreFor(picks, "signals-and-systems"),
                                weakTopics = SkillCheck.weakTopicsFor(picks)
                            )
                            phase = SkillPhase.Results
                        } else {
                            index++
                            currentPick = null
                        }
                    }
                )
            }
            SkillPhase.Results -> {
                SkillResults(
                    picks = picks,
                    onSeePathway = onSeePathway,
                    onGoHome = onGoHome
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SkillIntro(onStart: () -> Unit, onSkip: () -> Unit) {
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
        "Skill Check",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "12 quick questions — 4 each from Analog Circuits, Digital Electronics and Signals & Systems. About 3 minutes, and there's no timer.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Your answers show us exactly which topics to put first in your pathway, so ElectroSathi shapes itself around you. Nothing here is graded — just answer honestly.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    ) {
        Text("Start the Skill Check")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(
        onClick = onSkip,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SkillQuizQuestion(
    question: SkillQuestion,
    index: Int,
    total: Int,
    currentPick: Int?,
    onPick: (Int) -> Unit,
    onNext: () -> Unit
) {
    val answered = currentPick != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(50),
            color = AccentStudy.copy(alpha = 0.16f)
        ) {
            Text(
                question.subjectName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AccentStudy,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Question ${index + 1} of $total",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(10.dp))
    LinearProgressIndicator(
        progress = { (index + 1) / total.toFloat() },
        modifier = Modifier.fillMaxWidth(),
        color = BrandCyan
    )
    Spacer(Modifier.height(18.dp))
    Text(
        question.question,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.options.forEachIndexed { i, option ->
            SkillOptionRow(
                letter = "ABCD"[i].toString(),
                text = option,
                state = when {
                    !answered -> OptionState.Idle
                    i == question.correctIndex -> OptionState.Correct
                    i == currentPick -> OptionState.WrongPick
                    else -> OptionState.Idle
                },
                onClick = { if (!answered) onPick(i) }
            )
        }
    }
    if (answered) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    "WHY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    question.explanation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = onNext,
        enabled = answered,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    ) {
        Text(if (index == total - 1) "See my results" else "Next")
    }
}

private enum class OptionState { Idle, Correct, WrongPick }

@Composable
private fun SkillOptionRow(
    letter: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit
) {
    val borderColor = when (state) {
        OptionState.Correct -> AccentExams
        OptionState.WrongPick -> SkillBad
        OptionState.Idle -> MaterialTheme.colorScheme.outlineVariant
    }
    val background = when (state) {
        OptionState.Correct -> AccentExams.copy(alpha = 0.14f)
        OptionState.WrongPick -> SkillBad.copy(alpha = 0.14f)
        OptionState.Idle -> MaterialTheme.colorScheme.surface
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                letter,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        when (state) {
            OptionState.Correct -> Icon(
                Icons.Filled.Check,
                contentDescription = "Correct",
                tint = AccentExams
            )
            OptionState.WrongPick -> Icon(
                Icons.Filled.Close,
                contentDescription = "Not correct",
                tint = SkillBad
            )
            OptionState.Idle -> {}
        }
    }
}

@Composable
private fun SkillResults(
    picks: List<Int>,
    onSeePathway: () -> Unit,
    onGoHome: () -> Unit
) {
    Text(
        "Your results",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Here's where you stand in each subject — and the topics your pathway will put first.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))
    SkillCheck.SUBJECTS.forEach { (subjectId, subjectName) ->
        val score = SkillCheck.scoreFor(picks, subjectId)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$score/4",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(10.dp))
                SkillStatusChip(SkillCheck.statusFor(score))
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(
        "FOCUS FIRST",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(8.dp))
    val weak = SkillCheck.weakTopicsFor(picks)
    if (weak.isEmpty()) {
        Text(
            "No weak topics — you answered every question correctly. Your pathway will start from your goal instead.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        weak.forEach { topic ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(8.dp)
                        .clip(CircleShape)
                        .background(SkillBad)
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
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onSeePathway,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    ) {
        Text("See my pathway")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(
        onClick = onGoHome,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Go to Home", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
