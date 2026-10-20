package com.pathwayfit.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.IconButton
import com.pathwayfit.app.ui.efe.JuaGuideScreen
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import com.pathwayfit.app.ui.shared.AiSetupDialog
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.ui.platform.LocalContext
import com.pathwayfit.app.ui.theme.careerfitPalette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pathwayfit.app.AppContainer
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.ui.cbe.CbeAnalysisScreen
import com.pathwayfit.app.ui.cbe.CbeAssessmentScreen
import com.pathwayfit.app.ui.cbe.CbeExplorerScreen
import com.pathwayfit.app.ui.cbe.CbeHomeScreen
import com.pathwayfit.app.ui.cbe.CbeJourneyScreen
import com.pathwayfit.app.ui.cbe.CbeNextYearsScreen
import com.pathwayfit.app.ui.cbe.CbePathwayMapScreen
import com.pathwayfit.app.ui.cbe.CbeProfileScreen
import com.pathwayfit.app.ui.cbe.CbeRecommendationDetailScreen
import com.pathwayfit.app.ui.cbe.CbeResultsScreen
import com.pathwayfit.app.ui.cbe.CbeWhatIfScreen
import com.pathwayfit.app.ui.components.PremiumComingSoonDialog
import com.pathwayfit.app.ui.efe.CareerExplorerScreen
import com.pathwayfit.app.ui.efe.CourseComparisonScreen
import com.pathwayfit.app.ui.efe.CareerfitCourseScreen
import com.pathwayfit.app.ui.efe.CourseExplorerScreen
import com.pathwayfit.app.ui.efe.CareerfitRecommendationsScreen
import com.pathwayfit.app.ui.efe.GeminiAnalysisScreen
import com.pathwayfit.app.ui.efe.EfeAssessmentScreen
import com.pathwayfit.app.ui.efe.EfeCareerMapScreen
import com.pathwayfit.app.ui.efe.CareerfitHomeScreen
import com.pathwayfit.app.ui.efe.AssessmentIntroScreen
import com.pathwayfit.app.ui.efe.EfeJourneyScreen
import com.pathwayfit.app.ui.efe.EfeNextYearsScreen
import com.pathwayfit.app.ui.efe.EfeProfileScreen
import com.pathwayfit.app.ui.efe.KcseResultsScreen
import com.pathwayfit.app.ui.efe.UniversityExplorerScreen
import com.pathwayfit.app.ui.efe.UniversityShortlistScreen
import com.pathwayfit.app.ui.onboarding.ChooseSystemScreen
import com.pathwayfit.app.ui.onboarding.LoginScreen
import com.pathwayfit.app.ui.onboarding.RegisterScreen
import com.pathwayfit.app.ui.onboarding.SplashScreen
import com.pathwayfit.app.ui.onboarding.WelcomeScreen
import com.pathwayfit.app.ui.shared.DeveloperSettingsScreen
import com.pathwayfit.app.ui.shared.HelpScreen
import com.pathwayfit.app.ui.shared.LocalPremiumUi
import com.pathwayfit.app.ui.shared.PremiumPreviewScreen
import com.pathwayfit.app.ui.shared.PremiumScreen
import com.pathwayfit.app.ui.shared.PremiumUi
import com.pathwayfit.app.ui.shared.SettingsScreen
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PathwayFitTheme
import com.pathwayfit.app.ui.viewmodel.AppViewModelFactory
import com.pathwayfit.app.ui.viewmodel.CbeViewModel
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel
import com.pathwayfit.app.ui.viewmodel.PremiumViewModel
import com.pathwayfit.app.ui.viewmodel.SessionState
import com.pathwayfit.app.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

object Routes {
    const val SPLASH = "splash"
    const val ROUTER = "router?auth={auth}"
    fun router(afterAuth: Boolean = false) = "router?auth=$afterAuth"
    const val WELCOME = "welcome"
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val CHOOSE = "choose_system"

    // ---- CBE graph (never contains 8-4-4 destinations)
    const val CBE_GRAPH = "cbe"
    const val CBE_HOME = "cbe/home"
    const val CBE_PROFILE = "cbe/profile?setup={setup}"
    fun cbeProfile(setup: Boolean = false) = "cbe/profile?setup=$setup"
    const val CBE_ASSESSMENT = "cbe/assessment"
    const val CBE_ANALYSIS = "cbe/analysis"
    const val CBE_RESULTS = "cbe/results"
    const val CBE_DETAIL = "cbe/detail/{id}"
    fun cbeDetail(id: String) = "cbe/detail/$id"
    const val CBE_EXPLORE = "cbe/explore"
    const val CBE_JOURNEY = "cbe/journey"
    const val CBE_ME = "cbe/me"
    const val CBE_MAP = "cbe/premium/map"
    const val CBE_WHAT_IF = "cbe/premium/what_if"
    const val CBE_NEXT = "cbe/premium/next3"

    // ---- 8-4-4 graph (never contains CBE destinations)
    const val EFE_GRAPH = "efe"
    const val EFE_HOME = "efe/home"
    const val EFE_PROFILE = "efe/profile?setup={setup}"
    fun efeProfile(setup: Boolean = false) = "efe/profile?setup=$setup"
    const val EFE_KCSE = "efe/kcse?edit={edit}"
    fun efeKcse(edit: Boolean = false) = "efe/kcse?edit=$edit"
    const val EFE_ASSESSMENT = "efe/assessment"
    const val EFE_QUESTIONS = "efe/assessment/questions"
    const val EFE_ANALYSIS = "efe/analysis"
    const val EFE_COURSES = "efe/courses"
    const val EFE_COURSE = "efe/course/{id}"
    fun efeCourse(id: String) = "efe/course/$id"
    const val EFE_COURSE_EXPLORER = "efe/course_explorer"
    const val EFE_UNIVERSITIES = "efe/universities"
    const val EFE_CAREERS = "efe/careers"
    const val EFE_JOURNEY = "efe/journey"
    const val EFE_ME = "efe/me"
    const val EFE_COMPARE = "efe/premium/compare"
    const val EFE_MAP = "efe/premium/map"
    const val EFE_SHORTLIST = "efe/premium/shortlist"
    const val EFE_NEXT = "efe/premium/next3"

    // ---- shared (content is role-aware)
    const val PREMIUM = "premium"
    const val PREMIUM_PREVIEW = "premium_preview"
    const val DEV = "developer"
    const val HELP = "help"
    const val GUIDE = "juaguide"

    fun homeOf(system: EducationSystem) = if (system == EducationSystem.CBE) CBE_GRAPH else EFE_GRAPH
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val CbeTabs = listOf(
    Tab(Routes.CBE_HOME, "Home", Icons.Outlined.Home),
    Tab(Routes.CBE_ASSESSMENT, "Assessment", Icons.AutoMirrored.Outlined.Assignment),
    Tab(Routes.CBE_RESULTS, "Results", Icons.Outlined.BarChart),
    Tab(Routes.CBE_ME, "Profile", Icons.Outlined.Person),
)

private val EfeTabs = listOf(
    Tab(Routes.EFE_HOME, "Home", Icons.Outlined.Home),
    Tab(Routes.EFE_ASSESSMENT, "Assessment", Icons.AutoMirrored.Outlined.Assignment),
    Tab(Routes.EFE_ME, "Profile", Icons.Outlined.Person),
    Tab(Routes.GUIDE, "JuaGuide", Icons.Outlined.ChatBubbleOutline),
)

@Composable
fun PathwayFitRoot(container: AppContainer) {
    val factory = remember { AppViewModelFactory(container) }
    val session: SessionViewModel = viewModel(factory = factory)
    val cbe: CbeViewModel = viewModel(factory = factory)
    val efe: EightFourFourViewModel = viewModel(factory = factory)
    val premiumVm: PremiumViewModel = viewModel(factory = factory)

    val state by session.state.collectAsState()
    val access by premiumVm.access.collectAsState()
    val notifyMe by premiumVm.notifyMe.collectAsState()
    val system = (state as? SessionState.Active)?.system

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var comingSoon by remember { mutableStateOf(false) }
    var aiSetup by remember { mutableStateOf(false) }

    val premiumUi = PremiumUi(
        access = access,
        requestUnlock = { feature -> if (!access.has(feature)) comingSoon = true },
        openPremium = { nav.navigate(Routes.PREMIUM) },
        openPreview = { nav.navigate(Routes.PREMIUM_PREVIEW) },
    )

    val themeContext = LocalContext.current
    val themePreferences = remember { themeContext.getSharedPreferences("appearance", android.content.Context.MODE_PRIVATE) }
    var darkTheme by remember { mutableStateOf(themePreferences.getBoolean("dark_mode", false)) }
    PathwayFitTheme(system = system, darkTheme = darkTheme) {
        CompositionLocalProvider(LocalPremiumUi provides premiumUi) {
            val tabs = when (system) {
                EducationSystem.CBE -> CbeTabs
                EducationSystem.EIGHT_FOUR_FOUR -> EfeTabs
                null -> emptyList()
            }
            val showBar = tabs.any { it.route == route } || route == Routes.EFE_ANALYSIS || route == Routes.EFE_COURSES || route == Routes.EFE_COURSE
            Scaffold(
                bottomBar = { if (showBar) RoleBottomBar(tabs, if (route == Routes.EFE_ANALYSIS || route == Routes.EFE_COURSES || route == Routes.EFE_COURSE) Routes.EFE_ASSESSMENT else route) { r ->
                    if (r != route) {
                        when (r) {
                            Routes.CBE_ASSESSMENT -> {
                                cbe.startAssessment(resume = false)
                                nav.navigate(r) { popUpTo(Routes.CBE_HOME); launchSingleTop = true }
                            }
                            Routes.EFE_ASSESSMENT -> {
                                nav.navigate(r) { popUpTo(Routes.EFE_HOME); launchSingleTop = true }
                            }
                            else -> nav.navigateTab(r, tabs.first().route)
                        }
                    }
                } },
                containerColor = MaterialTheme.colorScheme.background,
            ) { padding ->
                Box(Modifier.fillMaxSize().imePadding()
                    .padding(bottom = padding.calculateBottomPadding())
                    .consumeWindowInsets(PaddingValues(bottom = padding.calculateBottomPadding()))) {
                    NavHost(
                        navController = nav,
                        startDestination = Routes.SPLASH,
                        modifier = Modifier.padding(top = if (route == Routes.SPLASH || route == Routes.WELCOME) 0.dp else 42.dp),
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        onboardingGraph(nav, session)
                        cbeGraph(nav, session, cbe, premiumVm)
                        eightFourFourGraph(nav, session, efe, premiumVm)
                        sharedGraph(nav, session, cbe, efe, premiumVm)
                    }
                    if (route != Routes.SPLASH) IconButton(
                        onClick = {
                            darkTheme = !darkTheme
                            themePreferences.edit().putBoolean("dark_mode", darkTheme).apply()
                        },
                        modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = if (route == Routes.WELCOME) 34.dp else 0.dp, end = if (route == Routes.WELCOME) 22.dp else 8.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)),
                    ) {
                        Icon(if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (darkTheme) "Switch to light mode" else "Switch to dark mode",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (comingSoon) {
                PremiumComingSoonDialog(notifyMe = notifyMe, onNotify = { premiumVm.setNotifyMe(true) }, onDismiss = { comingSoon = false })
            }
        }
    }
}

@Composable
private fun RoleBottomBar(tabs: List<Tab>, current: String?, onSelect: (String) -> Unit) {
    if (tabs == EfeTabs) {
        Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
            HorizontalDivider(color = Color(0xFFE0E9F8))
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                tabs.forEach { tab ->
                    val selected = current == tab.route
                    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(Modifier.weight(1f).clickable { onSelect(tab.route) }.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        val icon = when (tab.route) {
                            Routes.EFE_HOME -> Icons.Filled.Home
                            Routes.EFE_ME -> Icons.Outlined.PersonOutline
                            else -> tab.icon
                        }
                        Icon(icon, tab.label, tint = tint, modifier = Modifier.size(25.dp))
                        Text(tab.label, color = tint, fontSize = 12.sp)
                        Spacer(Modifier.height(5.dp))
                        if (selected) Box(Modifier.width(48.dp).height(3.dp).background(tint, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
        return
    }
    val accent = LocalRoleAccent.current
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { t ->
            NavigationBarItem(
                selected = current == t.route,
                onClick = { onSelect(t.route) },
                icon = { Icon(t.icon, contentDescription = t.label) },
                label = { Text(t.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = accent.main, selectedTextColor = accent.main, indicatorColor = MaterialTheme.colorScheme.surface),
            )
        }
    }
}

private fun NavHostController.navigateTab(route: String, home: String) = navigate(route) {
    popUpTo(home) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun NavHostController.resetTo(route: String) = navigate(route) {
    popUpTo(0) { inclusive = true }
    launchSingleTop = true
}

/**
 * Role guard. Every role-specific destination is wrapped in this; if the signed-in
 * learner belongs to the other education system they are sent to their own home,
 * so nobody can accidentally enter the wrong workflow.
 */
@Composable
private fun RoleGuard(required: EducationSystem, session: SessionViewModel, nav: NavHostController, content: @Composable () -> Unit) {
    val state by session.state.collectAsState()
    when (val s = state) {
        is SessionState.Active -> if (s.system == required) content() else LaunchedEffect(s.system) { nav.resetTo(Routes.homeOf(s.system)) }
        SessionState.SignedOut -> LaunchedEffect(Unit) { nav.resetTo(Routes.WELCOME) }
        else -> Unit // loading / mid-switch: render nothing for a frame
    }
}

// ============================================================== ONBOARDING

private fun NavGraphBuilder.onboardingGraph(nav: NavHostController, session: SessionViewModel) {
    composable(Routes.SPLASH) { SplashScreen(onFinished = { nav.resetTo(Routes.router()) }) }

    composable(Routes.ROUTER, arguments = listOf(navArgument("auth") { type = NavType.BoolType; defaultValue = false })) { entry ->
        val afterAuth = entry.arguments?.getBoolean("auth") ?: false
        val state by session.state.collectAsState()
        LaunchedEffect(state) {
            when (val s = state) {
                SessionState.Loading -> Unit
                SessionState.SignedOut -> if (!afterAuth) nav.resetTo(Routes.WELCOME) else { delay(1500); if (session.state.value == SessionState.SignedOut) nav.resetTo(Routes.WELCOME) }
                is SessionState.NeedsSystem -> nav.resetTo(Routes.CHOOSE)
                is SessionState.Active -> nav.resetTo(if (afterAuth || s.system == EducationSystem.CBE) Routes.CHOOSE else Routes.homeOf(s.system))
            }
        }
        Box(Modifier.fillMaxSize())
    }

    composable(Routes.WELCOME) { WelcomeScreen(onGetStarted = { nav.navigate(Routes.REGISTER) }, onLogin = { nav.navigate(Routes.LOGIN) }) }

    composable(Routes.REGISTER) {
        RegisterScreen(
            onBack = { nav.popBackStack() },
            onLogin = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) } },
            onRegister = { n, e, p, done -> session.register(n, e, p) { err -> done(err); if (err == null) nav.resetTo(Routes.router(true)) } },
        )
    }
    composable(Routes.LOGIN) {
        LoginScreen(
            onBack = { nav.popBackStack() },
            onRegister = { nav.navigate(Routes.REGISTER) { popUpTo(Routes.WELCOME) } },
            onLogin = { e, p, done -> session.login(e, p) { err -> done(err); if (err == null) nav.resetTo(Routes.router(true)) } },
        )
    }
    composable(Routes.CHOOSE) {
        val user by session.user.collectAsState()
        ChooseSystemScreen(name = user?.firstName ?: "") { system ->
            session.chooseSystem(system) {
                nav.resetTo(Routes.EFE_GRAPH)
            }
        }
    }
}

// ============================================================== CBE

private fun NavGraphBuilder.cbeGraph(nav: NavHostController, session: SessionViewModel, vm: CbeViewModel, premium: PremiumViewModel) {
    val sys = EducationSystem.CBE
    navigation(startDestination = Routes.CBE_HOME, route = Routes.CBE_GRAPH) {
        val startAssessment = { vm.startAssessment(resume = false); nav.navigate(Routes.CBE_ASSESSMENT) }

        composable(Routes.CBE_HOME) {
            RoleGuard(sys, session, nav) {
                CbeHomeScreen(
                    vm,
                    onStartAssessment = startAssessment,
                    onExplore = { nav.navigateTab(Routes.CBE_EXPLORE, Routes.CBE_HOME) },
                    onRecommendations = { nav.navigateTab(Routes.CBE_RESULTS, Routes.CBE_HOME) },
                    onJourney = { nav.navigateTab(Routes.CBE_JOURNEY, Routes.CBE_HOME) },
                    onProfile = { nav.navigate(Routes.cbeProfile()) },
                    onSettings = { nav.navigateTab(Routes.CBE_ME, Routes.CBE_HOME) },
                )
            }
        }
        composable(Routes.CBE_PROFILE, arguments = listOf(navArgument("setup") { type = NavType.BoolType; defaultValue = false })) { e ->
            val setup = e.arguments?.getBoolean("setup") ?: false
            RoleGuard(sys, session, nav) {
                CbeProfileScreen(vm, setup, onBack = if (setup) null else ({ nav.popBackStack(); Unit }), onDone = { nav.popBackStack() })
            }
        }
        composable(Routes.CBE_ASSESSMENT) {
            RoleGuard(sys, session, nav) {
                CbeAssessmentScreen(vm, onClose = { nav.popBackStack() }, onFinished = {
                    nav.navigate(Routes.CBE_ANALYSIS) { popUpTo(Routes.CBE_HOME) }
                })
            }
        }
        composable(Routes.CBE_ANALYSIS) {
            RoleGuard(sys, session, nav) {
                CbeAnalysisScreen(vm,
                    onSeeResults = { nav.navigate(Routes.CBE_RESULTS) { popUpTo(Routes.CBE_HOME) } },
                    onPremiumPreview = { nav.navigate(Routes.PREMIUM_PREVIEW) })
            }
        }
        composable(Routes.CBE_RESULTS) {
            RoleGuard(sys, session, nav) {
                CbeResultsScreen(vm, onBack = null, onOpen = { nav.navigate(Routes.cbeDetail(it)) }, onStart = startAssessment, onExplore = { nav.navigate(Routes.CBE_EXPLORE) })
            }
        }
        composable(Routes.CBE_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            RoleGuard(sys, session, nav) {
                CbeRecommendationDetailScreen(
                    vm, e.arguments?.getString("id") ?: "",
                    onBack = { nav.popBackStack() },
                    onPathwayMap = { nav.navigate(Routes.CBE_MAP) },
                    onWhatIf = { nav.navigate(Routes.CBE_WHAT_IF) },
                    onNextYears = { nav.navigate(Routes.CBE_NEXT) },
                )
            }
        }
        composable(Routes.CBE_EXPLORE) { RoleGuard(sys, session, nav) { CbeExplorerScreen(vm, onBack = { nav.popBackStack() }) } }
        composable(Routes.CBE_JOURNEY) {
            RoleGuard(sys, session, nav) {
                CbeJourneyScreen(vm, onBack = { nav.popBackStack() }, onStart = startAssessment, onProfile = { nav.navigate(Routes.cbeProfile()) }, onPathwayMap = { nav.navigate(Routes.CBE_MAP) })
            }
        }
        composable(Routes.CBE_ME) {
            RoleGuard(sys, session, nav) {
                val profile by vm.profile.collectAsState()
                val saved by vm.saved.collectAsState()
                SettingsHost(nav, session, premium, profile.completion(), saved,
                    onEditProfile = { nav.navigate(Routes.cbeProfile()) },
                    onOpenSaved = { nav.navigate(Routes.cbeDetail(it.itemId)) })
            }
        }
        composable(Routes.CBE_MAP) { RoleGuard(sys, session, nav) { CbePathwayMapScreen(vm) { nav.popBackStack() } } }
        composable(Routes.CBE_WHAT_IF) { RoleGuard(sys, session, nav) { CbeWhatIfScreen(vm) { nav.popBackStack() } } }
        composable(Routes.CBE_NEXT) { RoleGuard(sys, session, nav) { CbeNextYearsScreen(vm) { nav.popBackStack() } } }
    }
}

// ============================================================== 8-4-4

private fun NavGraphBuilder.eightFourFourGraph(nav: NavHostController, session: SessionViewModel, vm: EightFourFourViewModel, premium: PremiumViewModel) {
    val sys = EducationSystem.EIGHT_FOUR_FOUR
    navigation(startDestination = Routes.EFE_HOME, route = Routes.EFE_GRAPH) {
        val startAssessment = { nav.navigate(Routes.EFE_ASSESSMENT) }
        // "Find My Courses": grades first if none are entered, then the assessment.
        val findCourses = {
            if (vm.profile.value.subjectGrades.isEmpty() && vm.profile.value.meanGrade == null) nav.navigate(Routes.efeKcse(true))
            else startAssessment()
        }

        composable(Routes.EFE_HOME) {
            RoleGuard(sys, session, nav) {
                val user by vm.user.collectAsState()
                val profile by vm.profile.collectAsState()
                CareerfitHomeScreen(
                    name = user?.firstName ?: "",
                    profileComplete = profile.completion() >= 1f,
                    onProfile = { nav.navigate(Routes.efeProfile()) },
                    onAssessment = startAssessment,
                )
            }
        }
        composable(Routes.EFE_PROFILE, arguments = listOf(navArgument("setup") { type = NavType.BoolType; defaultValue = false })) { e ->
            val setup = e.arguments?.getBoolean("setup") ?: false
            RoleGuard(sys, session, nav) {
                EfeProfileScreen(
                    vm, setup,
                    onBack = if (setup) null else ({ nav.popBackStack(); Unit }),
                    onEditGrades = { nav.navigate(Routes.efeKcse(true)) { if (setup) popUpTo(Routes.EFE_HOME) } },
                    onDone = { nav.popBackStack() },
                )
            }
        }
        composable(Routes.EFE_KCSE, arguments = listOf(navArgument("edit") { type = NavType.BoolType; defaultValue = false })) { e ->
            RoleGuard(sys, session, nav) {
                val completed by vm.completed.collectAsState()
                KcseResultsScreen(
                    vm, startEditing = e.arguments?.getBoolean("edit") ?: false,
                    onBack = { nav.popBackStack() },
                    onUpdateRecommendations = {
                        if (completed) nav.navigate(Routes.EFE_COURSES) { popUpTo(Routes.EFE_HOME) } else startAssessment()
                    },
                )
            }
        }
        composable(Routes.EFE_ASSESSMENT) {
            RoleGuard(sys, session, nav) {
                AssessmentIntroScreen(onBack = { nav.popBackStack() }, onStart = {
                    vm.startAssessment(resume = false)
                    nav.navigate(Routes.EFE_QUESTIONS)
                })
            }
        }
        composable(Routes.EFE_QUESTIONS) {
            RoleGuard(sys, session, nav) {
                EfeAssessmentScreen(vm, onClose = { nav.popBackStack() }, onFinished = { nav.navigate(Routes.EFE_ANALYSIS) { popUpTo(Routes.EFE_HOME) } })
            }
        }
        composable(Routes.EFE_ANALYSIS) {
            RoleGuard(sys, session, nav) {
                GeminiAnalysisScreen(vm, onReady = { nav.navigate(Routes.EFE_COURSES) { popUpTo(Routes.EFE_HOME) } })
            }
        }
        composable(Routes.EFE_COURSES) {
            RoleGuard(sys, session, nav) {
                CareerfitRecommendationsScreen(vm, onOpen = { nav.navigate(Routes.efeCourse(it)) }, onStart = startAssessment)
            }
        }
        composable(Routes.EFE_COURSE, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            RoleGuard(sys, session, nav) {
                CareerfitCourseScreen(vm, e.arguments?.getString("id") ?: "", onBack = { nav.popBackStack() })
            }
        }
        composable(Routes.EFE_COURSE_EXPLORER) {
            RoleGuard(sys, session, nav) { CourseExplorerScreen(vm, onBack = { nav.popBackStack() }, onOpen = { nav.navigate(Routes.efeCourse(it)) }) }
        }
        composable(Routes.EFE_UNIVERSITIES) {
            RoleGuard(sys, session, nav) {
                UniversityExplorerScreen(vm, onBack = { nav.popBackStack() }, onOpenCourse = { nav.navigate(Routes.efeCourse(it)) }, onShortlist = { nav.navigate(Routes.EFE_SHORTLIST) })
            }
        }
        composable(Routes.EFE_CAREERS) {
            RoleGuard(sys, session, nav) { CareerExplorerScreen(vm, onBack = { nav.popBackStack() }, onOpenCourse = { nav.navigate(Routes.efeCourse(it)) }) }
        }
        composable(Routes.EFE_JOURNEY) {
            RoleGuard(sys, session, nav) {
                EfeJourneyScreen(
                    vm, onBack = { nav.popBackStack() },
                    onKcse = { nav.navigate(Routes.efeKcse()) },
                    onProfile = { nav.navigate(Routes.efeProfile()) },
                    onAssessment = startAssessment,
                    onCourses = { nav.navigateTab(Routes.EFE_COURSES, Routes.EFE_HOME) },
                    onCareerMap = { nav.navigate(Routes.EFE_MAP) },
                )
            }
        }
        composable(Routes.EFE_ME) {
            RoleGuard(sys, session, nav) {
                val profile by vm.profile.collectAsState()
                val saved by vm.saved.collectAsState()
                SettingsHost(nav, session, premium, profile.completion(), saved,
                    onEditProfile = { nav.navigate(Routes.efeProfile()) },
                    onOpenSaved = { nav.navigate(Routes.efeCourse(it.itemId)) })
            }
        }
        composable(Routes.EFE_COMPARE) { RoleGuard(sys, session, nav) { CourseComparisonScreen(vm) { nav.popBackStack() } } }
        composable(Routes.EFE_MAP) { RoleGuard(sys, session, nav) { EfeCareerMapScreen(vm) { nav.popBackStack() } } }
        composable(Routes.EFE_SHORTLIST) {
            RoleGuard(sys, session, nav) { UniversityShortlistScreen(vm, onBack = { nav.popBackStack() }, onOpenCourse = { nav.navigate(Routes.efeCourse(it)) }) }
        }
        composable(Routes.EFE_NEXT) { RoleGuard(sys, session, nav) { EfeNextYearsScreen(vm) { nav.popBackStack() } } }
    }
}

// ============================================================== SHARED

@Composable
private fun SettingsHost(
    nav: NavHostController,
    session: SessionViewModel,
    premium: PremiumViewModel,
    completion: Float,
    saved: List<com.pathwayfit.app.core.model.SavedRecommendation>,
    onEditProfile: () -> Unit,
    onOpenSaved: (com.pathwayfit.app.core.model.SavedRecommendation) -> Unit,
) {
    val user by session.user.collectAsState()
    val history by session.history.collectAsState()
    val reminders by session.reminders.collectAsState()
    SettingsScreen(
        user = user, profileCompletion = completion, history = history, saved = saved, reminders = reminders,
        devToolsVisible = premium.devToolsVisible, onBack = null,
        onEditProfile = onEditProfile, onOpenSaved = onOpenSaved,
        onReminders = { session.setReminders(it) },
        onPremium = { nav.navigate(Routes.PREMIUM) },
        onHelp = { nav.navigate(Routes.HELP) },
        onDeveloper = { nav.navigate(Routes.DEV) },
        onUpdateName = { session.updateName(it) },
        onSwitchSystem = { session.switchSystem { nav.resetTo(Routes.CHOOSE) } },
        onLogout = { session.logout { nav.resetTo(Routes.WELCOME) } },
    )
}

private fun NavGraphBuilder.sharedGraph(nav: NavHostController, session: SessionViewModel, cbe: CbeViewModel, efe: EightFourFourViewModel, premium: PremiumViewModel) {
    composable(Routes.GUIDE) { RoleGuard(EducationSystem.EIGHT_FOUR_FOUR, session, nav) { JuaGuideScreen(efe, onUpgrade = { nav.navigate(Routes.PREMIUM) }) } }
    composable(Routes.PREMIUM_PREVIEW) {
        val state by session.state.collectAsState()
        val system = (state as? SessionState.Active)?.system
        val cbeTop by cbe.results.collectAsState()
        val efeTop by efe.results.collectAsState()
        val (title, pct) = when (system) {
            EducationSystem.CBE -> cbeTop?.top?.let { it.pathway.name to it.percent } ?: (null to null)
            EducationSystem.EIGHT_FOUR_FOUR -> efeTop?.top?.let { it.course.name to it.percent } ?: (null to null)
            null -> null to null
        }
        PremiumPreviewScreen(system, title, pct, onBack = { nav.popBackStack() }, onSeePremium = { nav.navigate(Routes.PREMIUM) })
    }
    composable(Routes.PREMIUM) {
        val state by session.state.collectAsState()
        val notify by premium.notifyMe.collectAsState()
        PremiumScreen((state as? SessionState.Active)?.system, notify, onNotify = { premium.setNotifyMe(true) }, onBack = { nav.popBackStack() })
    }
    composable(Routes.DEV) {
        val on by premium.developerPreview.collectAsState()
        if (premium.devToolsVisible) DeveloperSettingsScreen(on, onTogglePreview = { premium.setDeveloperPreview(it) }, onBack = { nav.popBackStack() })
        else LaunchedEffect(Unit) { nav.popBackStack() }
    }
    composable(Routes.HELP) {
        val state by session.state.collectAsState()
        HelpScreen((state as? SessionState.Active)?.system) { nav.popBackStack() }
    }
}
