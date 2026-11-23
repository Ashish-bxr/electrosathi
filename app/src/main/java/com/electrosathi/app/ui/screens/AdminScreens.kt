package com.electrosathi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.electrosathi.app.data.Repository
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.components.AccentCard
import com.electrosathi.app.ui.components.AccentCareer
import com.electrosathi.app.ui.components.AccentExams
import com.electrosathi.app.ui.components.AccentHack
import com.electrosathi.app.ui.components.AccentLibrary
import com.electrosathi.app.ui.components.AccentProjects
import com.electrosathi.app.ui.components.AccentStudy

// ---------------------------------------------------------------------------
// ADMIN LOGIN
// ---------------------------------------------------------------------------
@Composable
fun AdminLoginScreen(onLoggedIn: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Admin Panel", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Log in with your admin account to publish content.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                busy = true
                error = null
                Repository.adminLogin(context, email, password) { ok, msg ->
                    if (!ok) {
                        busy = false
                        error = msg
                    } else {
                        // Batch 14: signing in is not enough — the account
                        // must also carry the admin flag in the phone's
                        // local account store (the first account created
                        // on a phone is its admin / device owner).
                        UserProfile.isAdmin(context) { admin ->
                            busy = false
                            if (admin) {
                                onLoggedIn()
                            } else {
                                Repository.adminLogout(context)
                                UserProfile.clearAdminCache(context)
                                error = "This account is not an admin. On this phone, the first account created becomes the admin — sign in with that account, or create the first account from the app's sign-in screen."
                            }
                        }
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (busy) "Logging in…" else "Log in")
        }
        Text(
            "Fully offline — everything you publish is stored on this phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// ADMIN HOME — what can I manage?
// ---------------------------------------------------------------------------
@Composable
fun AdminHomeScreen(onManage: (String) -> Unit, onLogout: () -> Unit) {
    // Batch 14: defence in depth — even if navigation lands here directly,
    // the manage UI renders only after the local admin-flag check passes.
    val context = LocalContext.current
    var verifiedAdmin by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        UserProfile.isAdmin(context) { ok ->
            if (ok) verifiedAdmin = true else onLogout()
        }
    }
    if (!verifiedAdmin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Checking admin access…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Admin Panel", "Everything your team publishes lives here") }
        item {
            AccentCard(
                title = "Internships",
                subtitle = "${Repository.internships().size} live",
                accent = AccentCareer, icon = Icons.Filled.Work,
                badge = "Manage", onClick = { onManage("internships") }
            )
        }
        item {
            AccentCard(
                title = "Exams",
                subtitle = "${Repository.exams().size} live",
                accent = AccentExams, icon = Icons.Filled.Assignment,
                badge = "Manage", onClick = { onManage("exams") }
            )
        }
        item {
            AccentCard(
                title = "Projects",
                subtitle = "${Repository.projects().size} live",
                accent = AccentProjects, icon = Icons.Filled.Build,
                badge = "Manage", onClick = { onManage("projects") }
            )
        }
        item {
            AccentCard(
                title = "Hackathons",
                subtitle = "${Repository.hackathons().size} live",
                accent = AccentHack, icon = Icons.Filled.EmojiEvents,
                badge = "Manage", onClick = { onManage("hackathons") }
            )
        }
        item {
            AccentCard(
                title = "Books",
                subtitle = "${Repository.books().size} live",
                accent = AccentLibrary, icon = Icons.Filled.MenuBook,
                badge = "Manage", onClick = { onManage("books") }
            )
        }
        item {
            AccentCard(
                title = "Study Subjects",
                subtitle = "${Repository.subjects().size} live",
                accent = AccentStudy, icon = Icons.Filled.School,
                badge = "Manage", onClick = { onManage("subjects") }
            )
        }
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Log out") }
        }
    }
}

// ---------------------------------------------------------------------------
// MANAGE LIST — view + delete, per content type.
// ---------------------------------------------------------------------------
@Composable
fun ManageListScreen(type: String, onAdd: () -> Unit) {
    var tick by remember { mutableStateOf(0) }
    val title = type.replaceFirstChar { it.uppercase() }
    val rows: List<Pair<String, String>> = remember(tick) {
        when (type) {
            "internships" -> Repository.internships().map { it.id to "${it.title} — ${it.org}" }
            "exams" -> Repository.exams().map { it.id to it.name }
            "hackathons" -> Repository.hackathons().map { it.id to "${it.name} — ${it.deadline}" }
            "books" -> Repository.books().map { it.id to "${it.title} — ${it.author}" }
            "subjects" -> Repository.subjects().map { it.id to "${it.name} — ${it.progressPercent}%" }
            else -> Repository.projects().map { it.id to it.title }
        }
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Manage $title", "Trash icon deletes instantly") }
        items(rows) { (id, label) ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        label, Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = {
                        Repository.deleteDoc(type, id)
                        tick++
                    }) {
                        Icon(
                            Icons.Filled.Delete, contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Add new") }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared form pieces.
// ---------------------------------------------------------------------------
@Composable
fun FormField(label: String, value: String, multi: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multi, minLines = if (multi) 3 else 1
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChipPicker(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                FilterChip(
                    selected = selected == o,
                    onClick = { onSelect(o) },
                    label = { Text(o) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Router: picks the right form for the content type.
// ---------------------------------------------------------------------------
@Composable
fun AdminFormRouter(type: String, onSaved: () -> Unit) {
    when (type) {
        "exams" -> ExamFormScreen(onSaved)
        "projects" -> ProjectFormScreen(onSaved)
        "hackathons" -> HackathonFormScreen(onSaved)
        "books" -> BookFormScreen(onSaved)
        "subjects" -> StudySubjectFormScreen(onSaved)
        else -> InternshipFormScreen(onSaved)
    }
}

// ---------------------------------------------------------------------------
// INTERNSHIP FORM — the full 10-field publishing form.
// ---------------------------------------------------------------------------
@Composable
fun InternshipFormScreen(onSaved: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Government") }
    var paid by remember { mutableStateOf("Paid") }
    var location by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Internship", "Publishes to the Opportunities tab instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Title *", title) { title = it }
                FormField("Organization *", org) { org = it }
                ChipPicker("Type", listOf("Government", "Private"), type) { type = it }
                ChipPicker("Stipend", listOf("Paid", "Unpaid"), paid) { paid = it }
                FormField("Location / Remote", location) { location = it }
                FormField("Eligibility", eligibility) { eligibility = it }
                FormField("Apply-by date", deadline) { deadline = it }
                FormField("Description", description, multi = true) { description = it }
                FormField("Application link (official page URL)", link) { link = it }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = {
                        if (title.isBlank() || org.isBlank()) {
                            error = "Title and organization are required."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "internships",
                            mapOf(
                                "title" to title, "org" to org, "type" to type,
                                "paid" to paid, "location" to location,
                                "eligibility" to eligibility, "deadline" to deadline,
                                "description" to description, "link" to link
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish internship")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// EXAM FORM (essentials) and PROJECT FORM (essentials).
// ---------------------------------------------------------------------------
@Composable
fun ExamFormScreen(onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Higher Studies") }
    var overview by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Exam", "Publishes to the Exams tab instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Exam name *", name) { name = it }
                ChipPicker(
                    "Category",
                    listOf("Higher Studies", "Government Jobs", "PSUs via GATE", "Defence", "Management"),
                    category
                ) { category = it }
                FormField("Overview", overview, multi = true) { overview = it }
                FormField("Eligibility", eligibility, multi = true) { eligibility = it }
                FormField("Important dates", dates) { dates = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Exam name is required."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "exams",
                            mapOf(
                                "name" to name, "category" to category,
                                "overview" to overview, "eligibility" to eligibility,
                                "pattern" to "", "syllabus" to "",
                                "dates" to dates, "papers" to "", "cutoff" to ""
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish exam")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProjectFormScreen(onSaved: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Basic") }
    var domain by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Project", "Publishes to the Projects Hub instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Title *", title) { title = it }
                ChipPicker("Level", listOf("Basic", "Intermediate", "Advanced"), level) { level = it }
                FormField("Domain", domain) { domain = it }
                FormField("Time needed", time) { time = it }
                FormField("Cost", cost) { cost = it }
                FormField("Overview", overview, multi = true) { overview = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            error = "Title is required."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "projects",
                            mapOf(
                                "title" to title, "level" to level, "domain" to domain,
                                "time" to time, "cost" to cost, "overview" to overview,
                                "components" to emptyList<String>(),
                                "steps" to emptyList<String>(), "learns" to ""
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish project")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HACKATHON FORM (Batch 5 — ported from the bulk-import patch).
// ---------------------------------------------------------------------------
@Composable
fun HackathonFormScreen(onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Online") }
    var location by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Hackathon", "Publishes to Live Hackathons instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Hackathon name *", name) { name = it }
                FormField("Organizer", org) { org = it }
                FormField("Registration deadline", deadline) { deadline = it }
                ChipPicker("Mode", listOf("Online", "Offline", "Hybrid"), mode) { mode = it }
                FormField("Location", location) { location = it }
                FormField("Prizes", prize) { prize = it }
                FormField("Description", description, multi = true) { description = it }
                FormField("Registration link", link) { link = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Hackathon name is required."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "hackathons",
                            mapOf(
                                "name" to name, "org" to org, "deadline" to deadline,
                                "mode" to mode, "location" to location, "prize" to prize,
                                "description" to description, "link" to link
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish hackathon")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BOOK FORM (Batch 5) — the fields the E-Library covers display.
// ---------------------------------------------------------------------------
@Composable
fun BookFormScreen(onSaved: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("PDF") }
    var rating by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Book", "Publishes to the E-Library instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Book title *", title) { title = it }
                FormField("Author(s)", author) { author = it }
                ChipPicker("Format", listOf("PDF", "Read"), format) { format = it }
                FormField("Rating (e.g. 4.8)", rating) { rating = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            error = "Book title is required."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "books",
                            mapOf(
                                "title" to title, "author" to author,
                                "format" to format, "rating" to rating
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish book")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STUDY SUBJECT FORM (Batch 5) — progress numbers + next-up topic.
// ---------------------------------------------------------------------------
@Composable
fun StudySubjectFormScreen(onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf("") }
    var topicsDone by remember { mutableStateOf("") }
    var topicsTotal by remember { mutableStateOf("") }
    var nextTopic by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("New Study Subject", "Publishes to the Study Hub instantly") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Subject name *", name) { name = it }
                FormField("Progress % (0–100)", progress) { progress = it }
                FormField("Topics completed", topicsDone) { topicsDone = it }
                FormField("Topics total", topicsTotal) { topicsTotal = it }
                FormField("Next-up topic", nextTopic) { nextTopic = it }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Subject name is required."
                            return@Button
                        }
                        val pct = progress.toIntOrNull()
                        val done = topicsDone.toIntOrNull()
                        val total = topicsTotal.toIntOrNull()
                        if (pct == null || done == null || total == null) {
                            error = "Progress, topics completed and topics total must be numbers."
                            return@Button
                        }
                        saving = true
                        Repository.addDoc(
                            "subjects",
                            mapOf(
                                "name" to name, "progressPercent" to pct,
                                "topicsDone" to done, "topicsTotal" to total,
                                "nextTopic" to nextTopic
                            )
                        ) { ok ->
                            saving = false
                            if (ok) onSaved()
                            else error = "Couldn't save on this phone — please try again."
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Publishing…" else "Publish subject")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
