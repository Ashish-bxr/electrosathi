package com.electrosathi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.electrosathi.app.R
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.theme.BrandCyan

private data class OnboardStep(
    val title: String,
    val options: List<String>,
    val selected: String?,
    val onSelect: (String) -> Unit
)

/**
 * First-run onboarding (Batch 4 visual rewrite; same contract as before —
 * MainActivity shows this until onDone fires):
 *  - Pages 1–2: illustrated welcome pages from the reference design.
 *  - Page 3: the name question (Batch 6) — "What should we call you?"
 *  - Pages 4–6: the original personalisation questions (who / goal /
 *    level), restyled. Skip is available on every page.
 *  - Answers are saved to the local profile store on exit (Batch 6) and
 *    synced to the user's Firestore profile at sign-in.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var persona by remember { mutableStateOf<String?>(null) }
    var goal by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }

    // Every exit (Skip or "Start learning") stores the answers first.
    val finish: () -> Unit = {
        UserProfile.saveOnboardingAnswers(
            context,
            name = name.trim(),
            who = persona ?: "",
            goal = goal ?: "",
            level = level ?: ""
        )
        onDone()
    }

    val questions = listOf(
        OnboardStep(
            "Who are you?",
            listOf("School student", "Undergrad", "Diploma", "Hobbyist", "Professional"),
            persona
        ) { persona = it },
        OnboardStep(
            "What's your goal?",
            listOf("Crack exams", "Get a job", "Build projects", "Learn for fun"),
            goal
        ) { goal = it },
        OnboardStep(
            "Your current level?",
            listOf("Beginner", "Intermediate", "Advanced"),
            level
        ) { level = it }
    )
    val totalPages = 2 + 1 + questions.size
    val isNamePage = page == 2
    val question = if (page >= 3) questions[page - 3] else null

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0E1C), Color(0xFF0D1B3E)))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = finish) {
                    Text("Skip", color = Color(0xFF9AA3B8))
                }
            }

            when {
                page == 0 -> OnboardVisual(
                    image = R.drawable.onboard_learn,
                    title = "Learn electronics step by step",
                    subtitle = "Explore easy-to-understand interactive modules " +
                        "and practical lessons."
                )
                page == 1 -> OnboardVisual(
                    image = R.drawable.onboard_career,
                    title = "Exams, projects and internships — one app",
                    subtitle = "Everything you need to go from a curious learner " +
                        "to a skilled professional."
                )
                else -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Make ElectroSathi yours",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Four quick questions so the app fits you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(28.dp))
                    if (isNamePage) {
                        Text(
                            "What should we call you?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else if (question != null) {
                        Text(
                            question.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            question.options.forEach { o ->
                                FilterChip(
                                    selected = question.selected == o,
                                    onClick = { question.onSelect(o) },
                                    label = { Text(o) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Page dots — the current page stretches into a cyan pill.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { i ->
                    Box(
                        Modifier.height(8.dp)
                            .width(if (i == page) 22.dp else 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (i == page) BrandCyan
                                else Color.White.copy(alpha = 0.25f)
                            )
                    )
                    if (i < totalPages - 1) Spacer(Modifier.width(6.dp))
                }
            }
            Spacer(Modifier.height(20.dp))

            if (page < 2) {
                Button(
                    onClick = { page++ },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Next", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { page-- },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(50)
                    ) { Text("Back") }
                    Button(
                        onClick = { if (page < totalPages - 1) page++ else finish() },
                        enabled = if (isNamePage) name.isNotBlank()
                            else question?.selected != null,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(if (page < totalPages - 1) "Next" else "Start learning")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardVisual(image: Int, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(280.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.dp,
                    BrandCyan.copy(alpha = 0.35f),
                    RoundedCornerShape(28.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(28.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
