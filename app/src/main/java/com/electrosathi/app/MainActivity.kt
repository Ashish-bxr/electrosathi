package com.electrosathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.electrosathi.app.data.Lang
import com.electrosathi.app.data.Repository
import com.electrosathi.app.data.UserProfile
import com.electrosathi.app.ui.screens.AdminFormRouter
import com.electrosathi.app.ui.screens.AdminHomeScreen
import com.electrosathi.app.ui.screens.AdminLoginScreen
import com.electrosathi.app.ui.screens.AuthScreen
import com.electrosathi.app.ui.screens.ExamDetailScreen
import com.electrosathi.app.ui.screens.ExamsScreen
import com.electrosathi.app.ui.screens.FlashcardsScreen
import com.electrosathi.app.ui.screens.HackathonDetailScreen
import com.electrosathi.app.ui.screens.HackathonsScreen
import com.electrosathi.app.ui.screens.HomeScreen
import com.electrosathi.app.ui.screens.InternshipDetailScreen
import com.electrosathi.app.ui.screens.LibraryScreen
import com.electrosathi.app.ui.screens.ManageListScreen
import com.electrosathi.app.ui.screens.OnboardingScreen
import com.electrosathi.app.ui.screens.OpportunitiesScreen
import com.electrosathi.app.ui.screens.PathwayScreen
import com.electrosathi.app.ui.screens.ProfileScreen
import com.electrosathi.app.ui.screens.ProgressScreen
import com.electrosathi.app.ui.screens.ProjectDetailScreen
import com.electrosathi.app.ui.screens.ProjectsScreen
import com.electrosathi.app.ui.screens.SkillCheckScreen
import com.electrosathi.app.ui.screens.SkillShowScreen
import com.electrosathi.app.ui.screens.SplashScreen
import com.electrosathi.app.ui.screens.StudyScreen
import com.electrosathi.app.ui.screens.SubjectDetailScreen
import com.electrosathi.app.ui.screens.VoltScreen
import com.electrosathi.app.ui.theme.BrandCyan
import com.electrosathi.app.ui.theme.BrandGradient
import com.electrosathi.app.ui.theme.ElectroSathiTheme
import com.electrosathi.app.ui.theme.MutedText
import com.electrosathi.app.ui.theme.NavBarNavy
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Destinations.
// ---------------------------------------------------------------------------
sealed class Dest(
    val route: String,
    val title: String,
    val navLabel: String,
    val icon: ImageVector
) {
    data object Home : Dest("home", "Home", "Home", Icons.Filled.Home)
    data object Study : Dest("study", "Study", "Study", Icons.Filled.School)
    data object Exams : Dest("exams", "Exams", "Exams", Icons.Filled.Assignment)
    data object Projects : Dest("projects", "Projects Hub", "Projects", Icons.Filled.Build)
    data object Opportunities : Dest("opportunities", "Opportunities", "Career", Icons.Filled.Work)
    data object Library : Dest("library", "Digital Library", "Library", Icons.Filled.MenuBook)
    data object Volt : Dest("volt", "Volt", "Volt", Icons.Filled.SmartToy)
    data object Hackathons : Dest("hackathons", "Live Hackathons", "Hackathons", Icons.Filled.EmojiEvents)
    data object SkillShow : Dest("skillshow", "Skill Show", "Skill Show", Icons.Filled.Star)
    data object Admin : Dest("adminLogin", "Admin Panel", "Admin", Icons.Filled.AdminPanelSettings)
}

// Bottom bar follows the Batch 4 reference: Home / Study / Exams / Career /
// Library. Projects stays a registered destination — it moved to Home's
// big Projects tile (and keeps its detail routes), it is not deleted.
private val bottomDests = listOf(
    Dest.Home, Dest.Study, Dest.Exams, Dest.Opportunities, Dest.Library
)
private val drawerDests = listOf(Dest.Hackathons, Dest.SkillShow, Dest.Admin)

private const val EXAM_DETAIL = "examDetail"
private const val PROJECT_DETAIL = "projectDetail"
private const val INTERNSHIP_DETAIL = "internshipDetail"
private const val HACKATHON_DETAIL = "hackathonDetail"
private const val SUBJECT_DETAIL = "subjectDetail"
private const val FLASHCARDS = "flashcards"
private const val SKILL_CHECK = "skillcheck"
private const val PATHWAY = "pathway"
private const val PROGRESS = "progress"
private const val PROFILE = "profile"

private fun isDetailRoute(pattern: String?): Boolean =
    pattern != null && (
        pattern.startsWith(EXAM_DETAIL) ||
            pattern.startsWith(PROJECT_DETAIL) ||
            pattern.startsWith(INTERNSHIP_DETAIL) ||
            pattern.startsWith(HACKATHON_DETAIL) ||
            pattern.startsWith(SUBJECT_DETAIL) ||
            pattern.startsWith(FLASHCARDS)
        )

private fun isFullScreen(pattern: String?): Boolean =
    isDetailRoute(pattern) || pattern?.startsWith("admin") == true ||
        pattern == SKILL_CHECK || pattern == PATHWAY

private fun titleFor(pattern: String?): String = when (pattern) {
    Dest.Home.route -> "ElectroSathi"
    Dest.Study.route -> Dest.Study.title
    Dest.Exams.route -> Dest.Exams.title
    Dest.Projects.route -> Dest.Projects.title
    Dest.Opportunities.route -> Dest.Opportunities.title
    Dest.Library.route -> Dest.Library.title
    Dest.Volt.route -> Dest.Volt.title
    Dest.Hackathons.route -> Dest.Hackathons.title
    Dest.SkillShow.route -> Dest.SkillShow.title
    else -> "ElectroSathi"
}

// ---------------------------------------------------------------------------
// Start flow (Batch 6): Splash → session check → app. Signed-in users and
// guests skip straight to the app; everyone else goes Onboarding → Auth.
// Batch 10: after onboarding, a user who has neither taken nor skipped
// the Skill Check meets it as a gate stage before the app — the quiz
// that finds their skill gaps and shapes their pathway.
// ---------------------------------------------------------------------------
private enum class StartStage { Onboarding, Auth, SkillCheck, App }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Batch 16: load the saved language choice before the UI.
        Lang.init(this)
        setContent {
            ElectroSathiTheme {
                val context = LocalContext.current
                var splashDone by rememberSaveable { mutableStateOf(false) }
                var stage by remember {
                    mutableStateOf(
                        when {
                            (UserProfile.isSignedIn(context) || UserProfile.isGuest(context)) &&
                                UserProfile.needsSkillCheck(context) ->
                                StartStage.SkillCheck
                            UserProfile.isSignedIn(context) || UserProfile.isGuest(context) ->
                                StartStage.App
                            UserProfile.onboardingDone(context) -> StartStage.Auth
                            else -> StartStage.Onboarding
                        }
                    )
                }
                var displayName by remember {
                    mutableStateOf(UserProfile.cachedName(context))
                }
                var guestMode by remember {
                    mutableStateOf(
                        UserProfile.isGuest(context) && !UserProfile.isSignedIn(context)
                    )
                }
                // Batch 10: a route the Skill Check gate asks to land on
                // once the scaffold exists ("See my pathway"), and a tick
                // that tells Home to re-read the refreshed profile.
                var pendingRoute by remember { mutableStateOf<String?>(null) }
                var profileTick by remember { mutableStateOf(0) }

                // Signed in? Re-resolve the greeting name locally (the
                // profile lives on this phone — Batch 14). Silent and
                // instant; the cached name is used as-is.
                LaunchedEffect(stage) {
                    if (stage == StartStage.App && UserProfile.isSignedIn(context)) {
                        UserProfile.refreshCachedProfile(context) { fresh ->
                            if (fresh.isNotBlank()) displayName = fresh
                            // Batch 10: the refresh also cached any Skill
                            // Check results — nudge Home to re-read them.
                            profileTick++
                        }
                    }
                }

                when {
                    !splashDone -> SplashScreen(onFinished = { splashDone = true })
                    stage == StartStage.Onboarding ->
                        OnboardingScreen(onDone = { stage = StartStage.Auth })
                    stage == StartStage.Auth -> AuthScreen(
                        onSignedIn = { name ->
                            displayName = name
                            guestMode = false
                            stage = if (UserProfile.needsSkillCheck(context)) {
                                StartStage.SkillCheck
                            } else {
                                StartStage.App
                            }
                        },
                        onGuest = {
                            guestMode = true
                            stage = if (UserProfile.needsSkillCheck(context)) {
                                StartStage.SkillCheck
                            } else {
                                StartStage.App
                            }
                        }
                    )
                    stage == StartStage.SkillCheck -> SkillCheckScreen(
                        onExit = { stage = StartStage.App },
                        onGoHome = { stage = StartStage.App },
                        onSeePathway = {
                            pendingRoute = PATHWAY
                            stage = StartStage.App
                        }
                    )
                    else -> MainScaffold(
                        userName = displayName,
                        guestMode = guestMode,
                        initialRoute = pendingRoute,
                        profileTick = profileTick,
                        onLogout = {
                            UserProfile.signOut(context)
                            displayName = ""
                            guestMode = false
                            pendingRoute = null
                            stage = StartStage.Onboarding
                        },
                        onSignIn = { stage = StartStage.Auth }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    userName: String,
    guestMode: Boolean,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
    initialRoute: String? = null,
    profileTick: Int = 0
) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val entry by nav.currentBackStackEntryAsState()
    val pattern = entry?.destination?.route
    val fullScreen = isFullScreen(pattern)
    // Batch 16: language picker dialog (opened from the drawer).
    var showLangDialog by remember { mutableStateOf(false) }

    // Load locally published content from the on-device store on
    // every app start (Batch 14 — fully offline, instant).
    LaunchedEffect(Unit) {
        Repository.sync(context)
        // Batch 10: the Skill Check gate can ask to land on a route
        // (My Pathway) once the scaffold exists.
        if (initialRoute != null) nav.navigate(initialRoute)
    }

    val title = when {
        pattern == null -> "ElectroSathi"
        pattern.startsWith(EXAM_DETAIL) ->
            Repository.exam(entry?.arguments?.getString("examId") ?: "")?.name ?: "Exam"
        pattern.startsWith(PROJECT_DETAIL) ->
            Repository.project(entry?.arguments?.getString("projectId") ?: "")?.title ?: "Project"
        pattern.startsWith(INTERNSHIP_DETAIL) ->
            Repository.internship(entry?.arguments?.getString("internshipId") ?: "")?.title ?: "Internship"
        pattern.startsWith(HACKATHON_DETAIL) ->
            Repository.hackathon(entry?.arguments?.getString("hackathonId") ?: "")?.name ?: "Hackathon"
        pattern.startsWith(SUBJECT_DETAIL) ->
            Repository.subject(entry?.arguments?.getString("subjectId") ?: "")?.name ?: "Subject"
        pattern.startsWith(FLASHCARDS) -> "Flashcards"
        pattern == SKILL_CHECK -> "Skill Check"
        pattern == PATHWAY -> Lang.tr("My Pathway")
        pattern == PROGRESS -> Lang.tr("My Progress")
        pattern == PROFILE -> Lang.tr("My Profile")
        pattern == "adminLogin" -> "Admin Login"
        pattern == "admin" -> "Admin Panel"
        pattern.startsWith("adminList") -> "Manage " +
            (entry?.arguments?.getString("type") ?: "").replaceFirstChar { it.uppercase() }
        pattern.startsWith("adminForm") -> "Add new"
        else -> titleFor(pattern)
    }

    // Batch 16: the language picker. Choosing takes effect instantly
    // (Lang.code is Compose state) and is remembered on the phone.
    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Language / भाषा") },
            text = { Text("Choose the app language. Volt also answers in the chosen language.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Lang.set(context, "hi")
                        showLangDialog = false
                    }
                ) { Text("हिन्दी") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Lang.set(context, "en")
                        showLangDialog = false
                    }
                ) { Text("English") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "ElectroSathi",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                // Batch 17: profile header — the student's name and
                // email at the top of the drawer, like every standard
                // app. Tapping it opens the My Profile screen (the
                // top-bar profile icon works too, now).
                val headerName = userName.ifBlank {
                    UserProfile.cachedName(context).ifBlank { "Learner" }
                }
                val headerEmail = remember { UserProfile.sessionEmail(context) ?: "" }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            nav.navigate(PROFILE) { launchSingleTop = true }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            headerName.trim().firstOrNull()?.uppercase() ?: "L",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            headerName,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            if (guestMode) "Guest" else headerEmail,
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                HorizontalDivider()
                drawerDests.forEach { d ->
                    NavigationDrawerItem(
                        label = { Text(Lang.tr(d.title)) },
                        icon = { Icon(d.icon, contentDescription = null) },
                        selected = pattern == d.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate(d.route) { launchSingleTop = true }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                // Batch 16: My Progress (the improvement report) and the
                // language switch (English / हिन्दी) live in the drawer.
                NavigationDrawerItem(
                    label = { Text(Lang.tr("My Progress")) },
                    icon = { Icon(Icons.Filled.TrendingUp, contentDescription = null) },
                    selected = pattern == PROGRESS,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(PROGRESS) { launchSingleTop = true }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(Lang.tr("Language")) },
                    icon = { Icon(Icons.Filled.Language, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLangDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider()
                // Account (Batch 6): signed-in users get Log out; guests
                // get Sign in, which routes back to the Auth screen.
                if (guestMode) {
                    NavigationDrawerItem(
                        label = { Text("Sign in") },
                        icon = { Icon(Icons.Filled.Login, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSignIn()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Log out") },
                        icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Home carries its own brand header (Batch 4 reference), so
                // the app bar steps aside there. The drawer stays reachable
                // by swiping from the left edge or tapping Home's avatar,
                // and every other tab keeps this bar — nothing is lost.
                if (pattern != Dest.Home.route) {
                    CenterAlignedTopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (fullScreen) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                            }
                        }
                    },
                    actions = {
                        // Batch 17: this icon was a dead placeholder
                        // (empty onClick) until now — it opens My Profile.
                        IconButton(onClick = {
                            nav.navigate(PROFILE) { launchSingleTop = true }
                        }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                        }
                    }
                    )
                }
            },
            floatingActionButton = {
                if (pattern != Dest.Volt.route) {
                    VoltPill(
                        onClick = { nav.navigate(Dest.Volt.route) { launchSingleTop = true } }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                if (!fullScreen) {
                    NavigationBar(containerColor = NavBarNavy) {
                        bottomDests.forEach { d ->
                            NavigationBarItem(
                                selected = pattern == d.route,
                                onClick = { nav.navigate(d.route) { launchSingleTop = true } },
                                icon = { Icon(d.icon, contentDescription = d.navLabel) },
                                label = { Text(Lang.tr(d.navLabel)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = BrandCyan,
                                    selectedTextColor = BrandCyan,
                                    indicatorColor = Color(0xFF1E3A8A),
                                    unselectedIconColor = MutedText,
                                    unselectedTextColor = MutedText
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Dest.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Dest.Home.route) {
                    HomeScreen(
                        userName = userName,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onStudy = {
                            nav.navigate(Dest.Study.route) { launchSingleTop = true }
                        },
                        onExams = {
                            nav.navigate(Dest.Exams.route) { launchSingleTop = true }
                        },
                        onProjects = {
                            nav.navigate(Dest.Projects.route) { launchSingleTop = true }
                        },
                        onCareer = {
                            nav.navigate(Dest.Opportunities.route) { launchSingleTop = true }
                        },
                        onLibrary = {
                            nav.navigate(Dest.Library.route) { launchSingleTop = true }
                        },
                        onHackathons = {
                            nav.navigate(Dest.Hackathons.route) { launchSingleTop = true }
                        },
                        onPathway = {
                            nav.navigate(PATHWAY) { launchSingleTop = true }
                        },
                        onSubjectClick = { id -> nav.navigate("$SUBJECT_DETAIL/$id") },
                        profileTick = profileTick
                    )
                }
                composable(Dest.Study.route) {
                    StudyScreen(
                        onSubjectClick = { id -> nav.navigate("$SUBJECT_DETAIL/$id") },
                        onFlashcards = { nav.navigate("$FLASHCARDS/analog-circuits") },
                        onSkillCheck = {
                            nav.navigate(SKILL_CHECK) { launchSingleTop = true }
                        }
                    )
                }
                composable(Dest.Exams.route) {
                    ExamsScreen(onExamClick = { id -> nav.navigate("$EXAM_DETAIL/$id") })
                }
                composable(Dest.Projects.route) {
                    ProjectsScreen(onProjectClick = { id -> nav.navigate("$PROJECT_DETAIL/$id") })
                }
                composable(Dest.Opportunities.route) {
                    OpportunitiesScreen(onInternshipClick = { id -> nav.navigate("$INTERNSHIP_DETAIL/$id") })
                }
                composable(Dest.Library.route) {
                    LibraryScreen()
                }
                composable(Dest.Volt.route) { VoltScreen() }
                composable(Dest.Hackathons.route) {
                    HackathonsScreen(onHackathonClick = { id -> nav.navigate("$HACKATHON_DETAIL/$id") })
                }
                composable(Dest.SkillShow.route) { SkillShowScreen() }

                // ---- Skill Check + Pathway (Batch 10) ----
                composable(SKILL_CHECK) {
                    SkillCheckScreen(
                        onExit = { nav.popBackStack() },
                        onGoHome = {
                            nav.popBackStack(Dest.Home.route, inclusive = false)
                        },
                        onSeePathway = {
                            nav.navigate(PATHWAY) {
                                popUpTo(SKILL_CHECK) { inclusive = true }
                            }
                        }
                    )
                }
                composable(PATHWAY) {
                    PathwayScreen(
                        onTakeSkillCheck = {
                            nav.navigate(SKILL_CHECK) { launchSingleTop = true }
                        },
                        onSubjectClick = { id -> nav.navigate("$SUBJECT_DETAIL/$id") },
                        onSeeProgress = {
                            nav.navigate(PROGRESS) { launchSingleTop = true }
                        }
                    )
                }
                // ---- My Progress (Batch 16) ----
                composable(PROGRESS) {
                    ProgressScreen(
                        onRetake = {
                            nav.navigate(SKILL_CHECK) { launchSingleTop = true }
                        }
                    )
                }
                // ---- My Profile (Batch 17) ----
                composable(PROFILE) {
                    ProfileScreen(
                        guestMode = guestMode,
                        onSeePathway = {
                            nav.navigate(PATHWAY) { launchSingleTop = true }
                        },
                        onSeeProgress = {
                            nav.navigate(PROGRESS) { launchSingleTop = true }
                        },
                        onTakeSkillCheck = {
                            nav.navigate(SKILL_CHECK) { launchSingleTop = true }
                        },
                        onOpenLanguage = { showLangDialog = true },
                        onSignIn = onSignIn,
                        onLogout = onLogout
                    )
                }

                composable(
                    route = "$EXAM_DETAIL/{examId}",
                    arguments = listOf(navArgument("examId") { type = NavType.StringType })
                ) { backStackEntry ->
                    ExamDetailScreen(
                        examId = backStackEntry.arguments?.getString("examId") ?: ""
                    )
                }
                composable(
                    route = "$PROJECT_DETAIL/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    ProjectDetailScreen(
                        projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    )
                }
                composable(
                    route = "$INTERNSHIP_DETAIL/{internshipId}",
                    arguments = listOf(navArgument("internshipId") { type = NavType.StringType })
                ) { backStackEntry ->
                    InternshipDetailScreen(
                        internshipId = backStackEntry.arguments?.getString("internshipId") ?: ""
                    )
                }
                composable(
                    route = "$HACKATHON_DETAIL/{hackathonId}",
                    arguments = listOf(navArgument("hackathonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    HackathonDetailScreen(
                        hackathonId = backStackEntry.arguments?.getString("hackathonId") ?: ""
                    )
                }
                composable(
                    route = "$SUBJECT_DETAIL/{subjectId}",
                    arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    SubjectDetailScreen(
                        subjectId = backStackEntry.arguments?.getString("subjectId") ?: "",
                        onFlashcards = { id -> nav.navigate("$FLASHCARDS/$id") }
                    )
                }
                composable(
                    route = "$FLASHCARDS/{subjectId}",
                    arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    FlashcardsScreen(
                        subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                    )
                }

                // ---- Admin ----
                composable("adminLogin") {
                    AdminLoginScreen(onLoggedIn = {
                        nav.navigate("admin") {
                            popUpTo("adminLogin") { inclusive = true }
                        }
                    })
                }
                composable("admin") {
                    AdminHomeScreen(
                        onManage = { type -> nav.navigate("adminList/$type") },
                        onLogout = {
                            Repository.adminLogout(context)
                            nav.popBackStack()
                        }
                    )
                }
                composable(
                    route = "adminList/{type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType })
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type") ?: "internships"
                    ManageListScreen(
                        type = type,
                        onAdd = { nav.navigate("adminForm/$type") }
                    )
                }
                composable(
                    route = "adminForm/{type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType })
                ) { backStackEntry ->
                    AdminFormRouter(
                        type = backStackEntry.arguments?.getString("type") ?: "internships",
                        onSaved = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}


// ---------------------------------------------------------------------------
// The glowing "Volt AI" pill (Batch 4) — replaces the round FAB from the
// reference design: dark pill, cyan outline + glow, bolt icon and label.
// Opens the Volt route exactly like the FAB did.
// Batch 5: glassy — the fill is semi-transparent deep navy, so cards
// scrolling underneath stay faintly visible instead of being hidden.
// (The bolt + label stay fully opaque; only the fill is translucent.)
// ---------------------------------------------------------------------------
@Composable
private fun VoltPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color(0xFF0B2233).copy(alpha = 0.82f),
        border = BorderStroke(1.5.dp, BrandCyan),
        modifier = Modifier.shadow(
            elevation = 18.dp,
            shape = RoundedCornerShape(50),
            ambientColor = BrandCyan,
            spotColor = BrandCyan
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.FlashOn,
                contentDescription = "Ask Volt",
                tint = BrandCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Volt AI", color = BrandCyan, fontWeight = FontWeight.Bold)
        }
    }
}
