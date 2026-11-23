package com.electrosathi.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.Lang
import com.electrosathi.app.data.SkillCheck
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.components.AccentExams
import com.electrosathi.app.ui.components.AccentLibrary
import com.electrosathi.app.ui.components.AccentProjects
import com.electrosathi.app.ui.components.Eyebrow

/**
 * My Profile — Batch 17. Until this batch the top-bar profile icon was
 * a dead placeholder and the app had no profile screen at all; the
 * drawer was the closest thing, and it shows sections, not the
 * student. This screen is the student's own page: who they are (the
 * onboarding answers), their account on this phone, their Skill Check
 * standing, and the two learning routes (Pathway / Progress).
 *
 * Everything shown is read from the on-device stores — the profile
 * prefs and the local account — so it works fully offline, like the
 * rest of the app since Batch 14.
 */
@Composable
fun ProfileScreen(
    guestMode: Boolean,
    onSeePathway: () -> Unit,
    onSeeProgress: () -> Unit,
    onTakeSkillCheck: () -> Unit,
    onOpenLanguage: () -> Unit,
    onSignIn: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val email = remember { UserProfile.sessionEmail(context) ?: "" }
    val isAdmin = remember { UserProfile.currentAccount(context)?.isAdmin == true }
    val name = UserProfile.cachedName(context).ifBlank {
        if (email.isNotBlank()) email.substringBefore("@") else "Learner"
    }
    val who = UserProfile.who(context)
    val goal = UserProfile.goal(context)
    val level = UserProfile.level(context)
    val results = remember { UserProfile.skillResults(context) }
    val whenText = remember { UserProfile.skillCheckWhenText(context) }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ScreenHeader(
                Lang.tr("My Profile"),
                "Your account and learning — stored on this phone only"
            )
        }

        // ---- Identity card ----
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(76.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.trim().firstOrNull()?.uppercase() ?: "L",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (guestMode) "Guest — browsing without an account"
                        else email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!guestMode && isAdmin) {
                        Spacer(Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Admin of this phone") }
                        )
                    }
                }
            }
        }

        // ---- About ----
        item { Eyebrow("About") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ProfileRow(
                        Icons.Filled.AccountCircle, AccentLibrary,
                        "Who I am", who.ifBlank { "Not set yet" }
                    )
                    Spacer(Modifier.height(12.dp))
                    ProfileRow(
                        Icons.Filled.Flag, AccentProjects,
                        "My goal", goal.ifBlank { "Not set yet" }
                    )
                    Spacer(Modifier.height(12.dp))
                    ProfileRow(
                        Icons.Filled.School, AccentExams,
                        "My level", level.ifBlank { "Not set yet" }
                    )
                }
            }
        }

        // ---- My learning ----
        item { Eyebrow("My learning") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (results.done) {
                        Text(
                            "Skill Check — latest scores" +
                                if (whenText.isNotBlank()) " · $whenText" else "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        SkillCheck.SUBJECTS.forEach { (subjectId, subjectName) ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    subjectName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${results.scoreFor(subjectId)}/4",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        if (results.weakTopics.isNotEmpty()) {
                            Text(
                                "Focus next: " + results.weakTopics.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    } else {
                        Text(
                            "You haven't taken the Skill Check yet — it finds " +
                                "your gaps and shapes your personal pathway.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            onClick = if (results.done) onSeePathway else onTakeSkillCheck,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (results.done) "My Pathway" else "Take Skill Check")
                        }
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = onSeeProgress,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
                        ) { Text("My Progress") }
                    }
                }
            }
        }

        // ---- App ----
        item { Eyebrow("App") }
        item {
            Card(
                onClick = onOpenLanguage,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (Lang.isHindi) "हिन्दी" else "English",
                            style = MaterialTheme.typography.bodyMedium,
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

        // ---- Account action ----
        item {
            if (guestMode) {
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(50)
                ) { Text("Sign in / Create account") }
            } else {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log out")
                }
            }
        }
        item {
            Text(
                "ElectroSathi keeps your account, chats and progress on this " +
                    "phone. Nothing about you is uploaded anywhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

/** One About row: tinted icon chip + label over value. */
@Composable
private fun ProfileRow(icon: ImageVector, tint: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
