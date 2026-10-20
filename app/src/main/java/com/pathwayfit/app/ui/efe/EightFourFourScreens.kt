package com.pathwayfit.app.ui.efe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.ai.AiExplanation
import com.pathwayfit.app.core.model.InstitutionType
import com.pathwayfit.app.core.model.JourneyStatus
import com.pathwayfit.app.core.model.KcseGrade
import com.pathwayfit.app.core.model.KcseSubjectGrade
import com.pathwayfit.app.core.model.UniversityProfile
import com.pathwayfit.app.engine.CourseMatch
import com.pathwayfit.app.engine.EightFourFourRecommendationEngine
import com.pathwayfit.app.premium.PremiumFeature
import com.pathwayfit.app.premium.PremiumInsightBuilder
import com.pathwayfit.app.ui.cbe.AnalysingView
import com.pathwayfit.app.ui.cbe.CheckRow
import com.pathwayfit.app.ui.cbe.PremiumGuard
import com.pathwayfit.app.ui.cbe.PremiumToolRow
import com.pathwayfit.app.ui.cbe.StrengthChips
import com.pathwayfit.app.ui.components.ChainFlow
import com.pathwayfit.app.ui.components.ChainNode
import com.pathwayfit.app.ui.components.CheckLine
import com.pathwayfit.app.ui.components.ChipSelector
import com.pathwayfit.app.ui.components.DisclaimerCard
import com.pathwayfit.app.ui.components.EmptyState
import com.pathwayfit.app.ui.components.FactorBar
import com.pathwayfit.app.ui.components.HeroCard
import com.pathwayfit.app.ui.components.LockedPreview
import com.pathwayfit.app.ui.components.MatchPill
import com.pathwayfit.app.ui.components.BrandHeader
import com.pathwayfit.app.ui.components.ProfileSavedDialog
import com.pathwayfit.app.ui.components.ProfileReminder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Surface
import com.pathwayfit.app.ui.components.ListRowCard
import com.pathwayfit.app.ui.components.PfCard
import com.pathwayfit.app.ui.components.PfScreen
import com.pathwayfit.app.ui.components.PremiumBadge
import com.pathwayfit.app.ui.components.PrimaryButton
import com.pathwayfit.app.ui.components.ScoreRing
import com.pathwayfit.app.ui.components.SecondaryButton
import com.pathwayfit.app.ui.components.SectionTitle
import com.pathwayfit.app.ui.components.TileCard
import com.pathwayfit.app.ui.components.VerifyNotice
import com.pathwayfit.app.ui.shared.AssessmentFlow
import com.pathwayfit.app.ui.shared.CareerMapView
import com.pathwayfit.app.ui.shared.ComparisonTable
import com.pathwayfit.app.ui.shared.CountyDialog
import com.pathwayfit.app.ui.shared.CountyPickerField
import com.pathwayfit.app.ui.shared.LocalPremiumUi
import com.pathwayfit.app.ui.shared.YearPlanView
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PfColors
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

const val EFE_DISCLAIMER = "The JuaFit Match Score is guidance based on your grades and answers. It is not an official KUCCPS score and does not guarantee admission or placement."

// ================================================================== HOME

@Composable
fun EfeHomeScreen(
    vm: EightFourFourViewModel,
    onFindCourses: () -> Unit,
    onKcse: () -> Unit,
    onCourseExplorer: () -> Unit,
    onUniversities: () -> Unit,
    onCareers: () -> Unit,
    onJourney: () -> Unit,
    onRecommendations: () -> Unit,
    onOpenCourse: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val user by vm.user.collectAsState()
    val profile by vm.profile.collectAsState()
    val results by vm.results.collectAsState()
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BrandHeader(onSettings) }
        item {
            Text("Hi, ${user?.firstName?.ifBlank { "learner" } ?: "learner"}", style = MaterialTheme.typography.headlineMedium)
            Text("Your university and career journey", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { ProfileReminder(profile.completion(), onSettings) }
        item { HeroCard("Discover your strengths", "Find the course that fits you", "Connect your interests, KCSE performance and career goals with possible university courses.", "Take Assessment", onFindCourses, Icons.AutoMirrored.Outlined.Assignment) }
        item { ListRowCard(Icons.Outlined.Person, "My Profile", "Update your details and preferences", onClick = onSettings) }
        item { ListRowCard(Icons.Outlined.Grade, "My KCSE Results", "Add or update your subject grades", onClick = onKcse) }
        item { ListRowCard(Icons.Outlined.EmojiEvents, "My Results", results?.top?.course?.name ?: "View your recommended courses", onClick = onRecommendations) }
        item { SectionTitle("Explore your options") }
        item { ListRowCard(Icons.AutoMirrored.Outlined.MenuBook, "Courses", "Browse all courses", onClick = onCourseExplorer) }
        item { ListRowCard(Icons.Outlined.AccountBalance, "Universities", "Explore institutions and listings", onClick = onUniversities) }
        item { ListRowCard(Icons.Outlined.WorkOutline, "Careers", "Discover possible future directions", onClick = onCareers) }
        item { ListRowCard(Icons.Outlined.Map, "My Journey", "Build your course-to-career plan", onClick = onJourney) }
        item { DisclaimerCard(EFE_DISCLAIMER) }
    }
}

// ================================================================== PROFILE

// ================================================================== KCSE RESULTS

@Composable
fun KcseResultsScreen(vm: EightFourFourViewModel, startEditing: Boolean, onBack: (() -> Unit)?, onUpdateRecommendations: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val data by vm.data.collectAsState()
    val results by vm.results.collectAsState()
    var editing by rememberSaveable { mutableStateOf(startEditing) }
    val draft = remember(profile) { mutableStateListOf<KcseSubjectGrade>().apply { addAll(profile.subjectGrades) } }
    val mean = EightFourFourRecommendationEngine.estimateMean(profile.copy(subjectGrades = draft.toList()))
    val accent = LocalRoleAccent.current

    PfScreen(
        title = "My KCSE Results", onBack = onBack,
        actions = { if (!editing) TextButton(onClick = { editing = true }) { Text("Edit Results") } },
    ) {
        item {
            PfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("MEAN GRADE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(mean?.label ?: "—", style = MaterialTheme.typography.displaySmall, color = accent.main)
                        Text(
                            if (profile.meanGrade != null) "As entered in your profile" else if (draft.isNotEmpty()) "Estimated from your best 7 subjects" else "Add your subject grades",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Outlined.Grade, null, tint = accent.main, modifier = Modifier.size(40.dp))
                }
            }
        }
        val d = data ?: return@PfScreen
        if (editing) {
            item { Text("Enter the grade for each subject you sat. Leave others blank.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            d.subjects.groupBy { it.group }.forEach { (group, subjects) ->
                item { SectionTitle(group) }
                item {
                    PfCard(contentPadding = 6.dp) {
                        subjects.forEachIndexed { i, s ->
                            val current = draft.firstOrNull { it.subjectCode == s.code }?.grade
                            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(s.name + if (s.compulsory) " *" else "", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                GradeDropdown(current) { g ->
                                    draft.removeAll { it.subjectCode == s.code }
                                    if (g != null) draft.add(KcseSubjectGrade(s.code, g))
                                }
                            }
                            if (i < subjects.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            item {
                PrimaryButton("Save results", {
                    vm.saveProfile(profile.copy(subjectGrades = draft.toList())) { editing = false }
                })
            }
        } else {
            if (profile.subjectGrades.isEmpty()) {
                item { EmptyState(Icons.Outlined.Grade, "No grades yet", "Add your KCSE subject grades so JuaFit can estimate your academic alignment with courses.", "Edit Results") { editing = true } }
            } else {
                item { SectionTitle("Individual subjects") }
                item {
                    PfCard(contentPadding = 6.dp) {
                        profile.subjectGrades.sortedByDescending { it.grade.points }.forEachIndexed { i, g ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(d.subject(g.subjectCode)?.name ?: g.subjectCode, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                MatchPill(g.grade.label)
                            }
                            if (i < profile.subjectGrades.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
                item { SectionTitle("Relevant academic strengths") }
                item {
                    StrengthChips(profile.subjectGrades.sortedByDescending { it.grade.points }.take(3).map { "${d.subject(it.subjectCode)?.name} (${it.grade.label})" })
                }
            }
            item { PrimaryButton(if (results == null) "Continue to Career Assessment" else "Update My Recommendations", onUpdateRecommendations) }
        }
    }
}

@Composable
private fun GradeDropdown(current: KcseGrade?, onSelect: (KcseGrade?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }, shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
            Text(current?.label ?: "—", fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Not taken") }, onClick = { onSelect(null); open = false })
            KcseGrade.entries.forEach { g -> DropdownMenuItem(text = { Text(g.label) }, onClick = { onSelect(g); open = false }) }
        }
    }
}

// ================================================================== ASSESSMENT + ANALYSIS

@Composable
fun EfeAssessmentScreen(vm: EightFourFourViewModel, onClose: () -> Unit, onFinished: () -> Unit) {
    CareerfitQuestionFlow(draft = vm.draft, onClose = onClose, onSubmit = { vm.submitAssessment(onFinished) })
}

@Composable
fun EfeAnalysisScreen(vm: EightFourFourViewModel, onSeeResults: () -> Unit, onPremiumPreview: () -> Unit) {
    val results by vm.results.collectAsState()
    var analysing by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1800); analysing = false }
    if (analysing || results == null) { AnalysingView("Matching your KCSE results and interests to university courses…"); return }
    val set = results!!
    val top = set.top ?: return
    var step by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (step < 4) { delay(350); step++ } }

    PfScreen(title = "") {
        item {
            Column {
                Text("Your Course Profile Is Ready", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text("We found patterns in your grades, interests and career goals.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            AnimatedVisibility(step >= 1, enter = fadeIn(tween(400)) + slideInVertically { it / 3 }) {
                PfCard {
                    Text("ACADEMIC STRENGTHS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    StrengthChips(set.academicStrengths.ifEmpty { listOf("Add grades to see this") })
                }
            }
        }
        item {
            AnimatedVisibility(step >= 2, enter = fadeIn(tween(400)) + slideInVertically { it / 3 }) {
                PfCard {
                    Text("YOUR TOP MATCH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(top.course.name, style = MaterialTheme.typography.headlineSmall)
                            Text(top.course.field, style = MaterialTheme.typography.bodyMedium, color = LocalRoleAccent.current.main)
                        }
                        ScoreRing(top.percent)
                    }
                    if (!set.hasGrades) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tip: add your KCSE grades to include academic alignment.", style = MaterialTheme.typography.bodySmall, color = PfColors.Warning)
                    }
                }
            }
        }
        item {
            AnimatedVisibility(step >= 3, enter = fadeIn(tween(400))) {
                LockedPreview(
                    title = "🔒 Unlock Your Complete Path",
                    subtitle = "Top 10 matches, a university shortlist, course comparison and your career map.",
                    teaser = listOf("Course-to-Career Map", null, "University Shortlist", null, "+5 more course matches"),
                    cta = "See What's Behind Your Match",
                    onClick = onPremiumPreview,
                )
            }
        }
        item { AnimatedVisibility(step >= 4, enter = fadeIn()) { PrimaryButton("View my courses", onSeeResults) } }
    }
}

// ================================================================== COURSE RECOMMENDATIONS

@Composable
fun CourseRecommendationsScreen(
    vm: EightFourFourViewModel,
    onBack: (() -> Unit)?,
    onOpen: (String) -> Unit,
    onStart: () -> Unit,
    onCompare: () -> Unit,
    onCareerMap: () -> Unit,
    onShortlist: () -> Unit,
    onNextYears: () -> Unit,
) {
    val results by vm.results.collectAsState()
    val gemini by vm.geminiResult.collectAsState()
    val premium = LocalPremiumUi.current
    val selected = remember { mutableStateListOf<String>() }

    PfScreen(title = "Your Recommended Courses", onBack = onBack) {
        gemini?.let { ai -> item { PfCard { Text(ai.summary, style = MaterialTheme.typography.bodyLarge); Text("Analyzed by JuaGuide", style = MaterialTheme.typography.labelSmall) } } }
        val set = results
        if (set == null) {
            item { EmptyState(Icons.AutoMirrored.Outlined.Assignment, "No course matches yet", "Complete the 8-4-4 career assessment to see courses that may suit you.", "Find My Courses", onStart) }
            return@PfScreen
        }
        val limit = if (premium.has(PremiumFeature.TOP_COURSES)) 10 else 5
        if (!set.hasGrades) item {
            DisclaimerCard(
                title = "Add your KCSE grades",
                text = "These matches use your interests and goals only. Add your grades on the KCSE Results screen to include academic and subject alignment.",
            )
        }
        item {
            Text(
                "Ranked by JuaFit Match Score. Tick two courses to compare them.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        itemsIndexed(set.courses.take(limit), key = { _, m -> m.course.id }) { i, m ->
            CourseCard(i + 1, m, m.course.id in selected, onToggleCompare = {
                if (m.course.id in selected) selected.remove(m.course.id) else if (selected.size < 2) selected.add(m.course.id)
            }, onOpen = { onOpen(m.course.id) })
        }
        if (selected.size == 2) item {
            PrimaryButton("Compare selected courses", {
                vm.comparison.value = selected[0] to selected[1]
                onCompare()
            })
        }
        if (limit < 10) item {
            LockedPreview(
                title = "We found ${set.courses.count { it.percent >= 50 }} possible courses",
                subtitle = "See your Top 10 matches instead of only five.",
                teaser = set.courses.drop(5).take(5).map { null },
                cta = "Explore Top 10",
                onClick = { premium.requestUnlock(PremiumFeature.TOP_COURSES) },
            )
        }
        item { SectionTitle("Plan deeper") }
        item { PremiumToolRow(Icons.Outlined.Map, "Career-to-Course Map", "Your goal → courses → universities", PremiumFeature.CAREER_MAP, onCareerMap) }
        item { PremiumToolRow(Icons.Outlined.AccountBalance, "University Shortlist", "Built from your profile and preferences", PremiumFeature.UNIVERSITY_SHORTLIST, onShortlist) }
        item { PremiumToolRow(Icons.Outlined.AutoAwesome, "My Next 3 Years", "Your personalised development roadmap", PremiumFeature.ACTION_PLAN, onNextYears) }
        item { DisclaimerCard(EFE_DISCLAIMER) }
    }
}

@Composable
private fun CourseCard(rank: Int, m: CourseMatch, selected: Boolean, onToggleCompare: () -> Unit, onOpen: () -> Unit) {
    val accent = LocalRoleAccent.current
    PfCard(onClick = onOpen, container = accent.soft.copy(alpha = 0.5f), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = if (rank % 2 == 0) PfColors.Gold else accent.main) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text("$rank", color = if (rank % 2 == 0) PfColors.NavyDeep else Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.course.name, style = MaterialTheme.typography.titleMedium)
                Text("${m.percent}% JuaFit Match", style = MaterialTheme.typography.labelMedium, color = PfColors.Success)
            }
            Checkbox(checked = selected, onCheckedChange = { onToggleCompare() }, colors = CheckboxDefaults.colors(checkedColor = accent.main), modifier = Modifier.semantics { contentDescription = "Compare ${m.course.name}" })
            Icon(Icons.Outlined.ChevronRight, null, tint = accent.main)
        }
    }
}

// ================================================================== COURSE DETAILS

@Composable
fun CourseDetailScreen(vm: EightFourFourViewModel, courseId: String, onBack: () -> Unit, onOpenUniversityExplorer: () -> Unit) {
    val results by vm.results.collectAsState()
    val data by vm.data.collectAsState()
    val careers by vm.careers.collectAsState()
    val profile by vm.profile.collectAsState()
    val saved by vm.saved.collectAsState()
    val course = data?.course(courseId)
    val match = results?.courses?.firstOrNull { it.course.id == courseId }
    val explanation by produceState<AiExplanation?>(null, match) { value = match?.let { vm.explain(it) } }
    var section by rememberSaveable(courseId) { mutableStateOf<String?>(null) }
    BackHandler(enabled = section != null) { section = null }
    val isSaved = saved.any { it.itemId == courseId }
    val sections = listOf(
        Triple("Course Overview", "What the course involves", Icons.AutoMirrored.Outlined.MenuBook),
        Triple("Why It Fits", "How your answers match", Icons.Outlined.EmojiEvents),
        Triple("Entry Requirements", "Admission information to verify", Icons.Outlined.Grade),
        Triple("Universities", "Where you could study", Icons.Outlined.AccountBalance),
        Triple("Possible Careers", "Explore future job directions", Icons.Outlined.WorkOutline),
        Triple("Relevant Subjects", "Subjects that support this course", Icons.Outlined.School),
        Triple("Relevant Competencies", "Skills you could develop", Icons.Outlined.Psychology),
    )
    PfScreen(title = section ?: "Course Details", onBack = { if (section != null) section = null else onBack() }) {
        val d = data
        if (d == null || course == null) {
            item { EmptyState(Icons.Outlined.School, "Course unavailable", "Please return to the course list and choose another course.") }
            return@PfScreen
        }
        if (section == null) {
            item {
                PfCard(container = LocalRoleAccent.current.soft, border = false) {
                    Text(course.field, style = MaterialTheme.typography.labelMedium, color = LocalRoleAccent.current.main)
                    Text(course.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    if (match != null) MatchPill("${match.percent}% JuaFit Match")
                    else Text("Take an assessment to see your match.", style = MaterialTheme.typography.bodySmall)
                }
            }
            sections.forEach { (title, subtitle, icon) -> item { ListRowCard(icon, title, subtitle, onClick = { section = title }) } }
            if (match != null) item { PrimaryButton(if (isSaved) "Remove from My Plan" else "Add to My Plan", { vm.toggleSave(match) }) }
            item { VerifyNotice() }
        } else when (section) {
            "Course Overview" -> item { PfCard { Text(course.overview, style = MaterialTheme.typography.bodyLarge) } }
            "Why It Fits" -> {
                if (match == null) item { Text("Complete your assessment to see a personal explanation.") }
                else {
                    item {
                        PfCard {
                            explanation?.let { Text(it.summary, style = MaterialTheme.typography.bodyMedium) }
                            Spacer(Modifier.height(12.dp))
                            match.factors.sortedByDescending { it.score }.take(4).forEach { factor ->
                                CheckLine(if (factor.evidence.isEmpty()) factor.label else "${factor.label}: ${factor.evidence.joinToString()}")
                            }
                        }
                    }
                    item {
                        val premium = LocalPremiumUi.current
                        if (premium.has(PremiumFeature.DETAILED_WHY_FIT)) PfCard { match.factors.forEach { FactorBar(it.label, it.score, explanation?.factorNotes?.get(it.key)) } }
                        else LockedPreview("Detailed Match Analysis", "See how each factor contributes to your match.", match.factors.flatMap { listOf(it.label, null) }.take(6), "Unlock Detailed Analysis") { premium.requestUnlock(PremiumFeature.DETAILED_WHY_FIT) }
                    }
                }
            }
            "Entry Requirements" -> {
                val reqs = d.verifiedRequirements(course.id)
                if (reqs.isEmpty()) item { Text("Verified entry requirements are not available for this course yet.", style = MaterialTheme.typography.bodyMedium) }
                items(reqs) { requirement -> PfCard { Text(requirement.description); Text("Source: ${requirement.sourceName} ${requirement.academicYear}", style = MaterialTheme.typography.bodySmall) } }
                item { VerifyNotice() }
            }
            "Universities" -> {
                val universities = d.universitiesFor(course.id)
                if (universities.isEmpty()) item { Text("No institutions listed for this course yet.") }
                items(universities) { (university, offering) ->
                    PfCard {
                        Text(university.name, style = MaterialTheme.typography.titleMedium)
                        Text("${university.county} / ${university.type.label}", style = MaterialTheme.typography.bodySmall)
                        if (!offering.verified) Text("Unverified listing", style = MaterialTheme.typography.labelSmall, color = PfColors.Warning)
                    }
                }
                item { PrimaryButton("Explore universities", onOpenUniversityExplorer) }
                item { VerifyNotice() }
            }
            "Possible Careers" -> {
                val related = course.careerIds.mapNotNull { careers?.career(it) }
                if (related.isEmpty()) item { Text("No careers listed for this course yet.") }
                items(related) { career -> PfCard { Text(career.name, style = MaterialTheme.typography.titleMedium); Text(career.summary, style = MaterialTheme.typography.bodyMedium) } }
            }
            "Relevant Subjects" -> {
                item { Text("General subject guidance. These are not official admission requirements.", style = MaterialTheme.typography.bodySmall) }
                items(course.relevantSubjects) { code ->
                    PfCard {
                        Text(d.subject(code)?.name ?: code, style = MaterialTheme.typography.titleMedium)
                        val grade = profile.subjectGrades.firstOrNull { it.subjectCode == code }?.grade
                        Text(grade?.let { "Your grade: ${it.label}" } ?: "Grade not entered", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            "Relevant Competencies" -> item { StrengthChips(course.skills) }
        }
    }
}

// ================================================================== COURSE EXPLORER (browse)

@Composable
fun CourseExplorerScreen(vm: EightFourFourViewModel, onBack: (() -> Unit)?, onOpen: (String) -> Unit) {
    val data by vm.data.collectAsState()
    val results by vm.results.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var field by rememberSaveable { mutableStateOf<String?>(null) }
    PfScreen(title = "Course Explorer", onBack = onBack) {
        val d = data ?: return@PfScreen
        item { SearchField(query, "Search courses") { query = it } }
        item { ChipSelector(d.fields.map { it to it }, listOfNotNull(field), { field = if (field == it) null else it }) }
        val list = d.courses.filter { (field == null || it.field == field) && (query.isBlank() || it.name.contains(query, true) || it.field.contains(query, true)) }
        items(list, key = { it.id }) { c ->
            val pct = results?.courses?.firstOrNull { it.course.id == c.id }?.percent
            com.pathwayfit.app.ui.components.ListRowCard(Icons.Outlined.School, c.name, c.field, pct?.let { "$it%" }) { onOpen(c.id) }
        }
    }
}

@Composable
private fun SearchField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, onChange, label = { Text(label) }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
    )
}

// ================================================================== UNIVERSITY EXPLORER

@Composable
fun UniversityExplorerScreen(vm: EightFourFourViewModel, onBack: (() -> Unit)?, onOpenCourse: (String) -> Unit, onShortlist: () -> Unit) {
    val data by vm.data.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var county by rememberSaveable { mutableStateOf<String?>(null) }
    var courseId by rememberSaveable { mutableStateOf<String?>(null) }
    var field by rememberSaveable { mutableStateOf<String?>(null) }
    var type by rememberSaveable { mutableStateOf<String?>(null) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val accent = LocalRoleAccent.current

    PfScreen(title = "University Explorer", onBack = onBack) {
        val d = data ?: return@PfScreen
        item { SearchField(query, "Search universities") { query = it } }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showFilters = !showFilters }) { Text(if (showFilters) "Hide filters" else "Filters") }
                Spacer(Modifier.weight(1f))
                if (county != null || courseId != null || field != null || type != null)
                    TextButton(onClick = { county = null; courseId = null; field = null; type = null }) { Text("Clear") }
            }
        }
        if (showFilters) {
            item { Text("Location", style = MaterialTheme.typography.labelLarge) }
            item { ChipSelector(d.counties.map { it to it }, listOfNotNull(county), { county = if (county == it) null else it }) }
            item { Text("Institution type", style = MaterialTheme.typography.labelLarge) }
            item { ChipSelector(InstitutionType.entries.map { it.name to it.label }, listOfNotNull(type), { type = if (type == it) null else it }) }
            item { Text("Field", style = MaterialTheme.typography.labelLarge) }
            item { ChipSelector(d.fields.map { it to it }, listOfNotNull(field), { field = if (field == it) null else it }) }
            item { Text("Course", style = MaterialTheme.typography.labelLarge) }
            item { ChipSelector(d.courses.map { it.id to it.name }, listOfNotNull(courseId), { courseId = if (courseId == it) null else it }) }
        }
        item {
            PremiumToolRow(Icons.Outlined.AccountBalance, "Build your personalised shortlist", "Universities ranked for your courses and preferences", PremiumFeature.UNIVERSITY_SHORTLIST, onShortlist)
        }
        val list = d.universities.filter { u ->
            val courses = d.coursesAt(u.id)
            (query.isBlank() || u.name.contains(query, true) || u.county.contains(query, true)) &&
                (county == null || u.county == county) &&
                (type == null || u.type.name == type) &&
                (courseId == null || courses.any { it.id == courseId }) &&
                (field == null || courses.any { it.field == field })
        }
        item { Text("${list.size} universities", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(list, key = { it.id }) { u ->
            val open = openId == u.id
            PfCard(onClick = { openId = if (open) null else u.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(u.name, style = MaterialTheme.typography.titleMedium)
                        Text("${u.county} · ${u.type.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                }
                AnimatedVisibility(open) {
                    Column(Modifier.padding(top = 10.dp)) {
                        Text("COURSES LISTED (UNVERIFIED SAMPLE)", style = MaterialTheme.typography.labelSmall, color = PfColors.Warning)
                        d.coursesAt(u.id).forEach { c ->
                            TextButton(onClick = { onOpenCourse(c.id) }) { Text(c.name, color = accent.main) }
                        }
                        VerifyNotice()
                    }
                }
            }
        }
    }
}

// ================================================================== CAREER EXPLORER

@Composable
fun CareerExplorerScreen(vm: EightFourFourViewModel, onBack: (() -> Unit)?, onOpenCourse: (String) -> Unit) {
    val data by vm.data.collectAsState()
    val careers by vm.careers.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val accent = LocalRoleAccent.current
    PfScreen(title = "Career Explorer", onBack = onBack) {
        val d = data ?: return@PfScreen
        val all = careers?.careers?.filter { it.courseIds.isNotEmpty() } ?: return@PfScreen
        item { Text("Career → Relevant Courses → Universities → Relevant Subjects", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { SearchField(query, "Search careers") { query = it } }
        items(all.filter { query.isBlank() || it.name.contains(query, true) }, key = { it.id }) { c ->
            val open = openId == c.id
            PfCard(onClick = { openId = if (open) null else c.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                        Text(c.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                }
                AnimatedVisibility(open) {
                    Column(Modifier.padding(top = 10.dp)) {
                        val courses = c.courseIds.mapNotNull { d.course(it) }
                        Text("RELEVANT COURSES", style = MaterialTheme.typography.labelSmall, color = accent.main)
                        courses.forEach { co -> TextButton(onClick = { onOpenCourse(co.id) }) { Text(co.name) } }
                        Text("UNIVERSITIES (UNVERIFIED SAMPLE)", style = MaterialTheme.typography.labelSmall, color = accent.main)
                        Text(courses.flatMap { d.universitiesFor(it.id).map { p -> p.first.name } }.distinct().take(6).joinToString().ifBlank { "—" }, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("RELEVANT SUBJECTS", style = MaterialTheme.typography.labelSmall, color = accent.main)
                        Text(courses.flatMap { it.relevantSubjects }.distinct().mapNotNull { d.subject(it)?.name }.joinToString(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ================================================================== JOURNEY

@Composable
fun EfeJourneyScreen(vm: EightFourFourViewModel, onBack: (() -> Unit)?, onKcse: () -> Unit, onProfile: () -> Unit, onAssessment: () -> Unit, onCourses: () -> Unit, onCareerMap: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val results by vm.results.collectAsState()
    val premium = LocalPremiumUi.current
    val steps = journeyStatus(profile, results != null)
    val top = results?.courses?.take(3)
    PfScreen(title = "My University Journey", onBack = onBack) {
        item {
            PfCard {
                ChainFlow(
                    listOf(
                        ChainNode("KCSE", EightFourFourRecommendationEngine.estimateMean(profile)?.let { "Mean grade ${it.label}" }, steps[0], onClick = onKcse),
                        ChainNode("Academic Profile", if (profile.school.isNotBlank()) profile.school else null, steps[1], onClick = onProfile),
                        ChainNode("Career Interests", if (results != null) "Assessment complete" else null, steps[2], onClick = onAssessment),
                        ChainNode("Recommended Courses", top?.joinToString { it.course.name }, steps[3], onClick = onCourses),
                        ChainNode("University Options", top?.flatMap { it.universities }?.distinctBy { it.id }?.take(3)?.joinToString { it.name }, steps[4]),
                        ChainNode("Career", top?.firstOrNull()?.careers?.firstOrNull()?.name, steps[5]),
                    ),
                )
            }
        }
        item { SectionTitle("Your progress") }
        item {
            PfCard {
                CheckRow("KCSE grades entered", profile.subjectGrades.isNotEmpty())
                CheckRow("Academic profile ${(profile.completion() * 100).toInt()}% complete", profile.completion() >= 0.7f)
                CheckRow("Career assessment completed", results != null)
            }
        }
        if (results != null) item {
            if (premium.has(PremiumFeature.CAREER_MAP)) PrimaryButton("Open my Career Map", onCareerMap)
            else LockedPreview("Complete your journey", "Your personal Career-to-Course map with universities and skills.", listOf(null, null, null), "Complete Your Journey") {
                premium.requestUnlock(PremiumFeature.CAREER_MAP)
            }
        }
        item { DisclaimerCard(EFE_DISCLAIMER) }
    }
}

private fun journeyStatus(p: UniversityProfile, assessed: Boolean): List<JourneyStatus> {
    val done = listOf(p.subjectGrades.isNotEmpty() || p.meanGrade != null, p.school.isNotBlank(), assessed, assessed, assessed, false)
    val current = done.indexOfFirst { !it }
    return done.mapIndexed { i, d -> when { d -> JourneyStatus.DONE; i == current -> JourneyStatus.CURRENT; else -> JourneyStatus.UPCOMING } }
}

// ================================================================== PREMIUM (8-4-4)

@Composable
fun CourseComparisonScreen(vm: EightFourFourViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    val pair by vm.comparison.collectAsState()
    val premium = LocalPremiumUi.current
    PfScreen(title = "Course Comparison", onBack = onBack) {
        val set = results ?: return@PfScreen
        val a = set.courses.firstOrNull { it.course.id == pair?.first } ?: set.courses.getOrNull(0) ?: return@PfScreen
        val b = set.courses.firstOrNull { it.course.id == pair?.second } ?: set.courses.getOrNull(1) ?: return@PfScreen
        val cmp = PremiumInsightBuilder.compare(a, b)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(a, b).forEach { m ->
                    PfCard(Modifier.weight(1f)) {
                        Text(m.course.name, style = MaterialTheme.typography.titleSmall)
                        Text("${m.percent}% Match", color = LocalRoleAccent.current.main, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item { ComparisonTable(a.course.name, b.course.name, cmp.scoreRows) }
        if (premium.has(PremiumFeature.COURSE_COMPARISON)) {
            item { ComparisonTable(a.course.name, b.course.name, cmp.detailRows) }
            item { PfCard { Text("Which fits you better?", style = MaterialTheme.typography.titleMedium); Text(cmp.verdict, style = MaterialTheme.typography.bodyMedium) } }
        } else {
            item {
                LockedPreview(
                    "See which one is a stronger fit for YOUR profile", "Skills, subjects, career directions and a personalised verdict.",
                    listOf("Strength Match", null, "Subject Match", null, "Career directions"), "Unlock Comparison",
                ) { premium.requestUnlock(PremiumFeature.COURSE_COMPARISON) }
            }
        }
        item { DisclaimerCard(EFE_DISCLAIMER) }
    }
}

@Composable
fun UniversityShortlistScreen(vm: EightFourFourViewModel, onBack: () -> Unit, onOpenCourse: (String) -> Unit) {
    val results by vm.results.collectAsState()
    val profile by vm.profile.collectAsState()
    PremiumGuard(PremiumFeature.UNIVERSITY_SHORTLIST, "University Shortlist", onBack) {
        val set = results
        if (set == null) { item { EmptyState(Icons.Outlined.AccountBalance, "Shortlist not ready", "Complete the career assessment first.") }; return@PremiumGuard }
        val list = PremiumInsightBuilder.universityShortlist(set, profile)
        item { VerifyNotice() }
        items(list.entries, key = { it.university.id }) { e ->
            PfCard {
                Text(e.university.name, style = MaterialTheme.typography.titleMedium)
                Text("${e.university.county} · ${e.university.type.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                e.reasons.forEach { CheckLine(it) }
                e.courses.take(3).forEach { cm ->
                    TextButton(onClick = { onOpenCourse(cm.course.id) }) { Text("${cm.course.name} · ${cm.percent}%") }
                }
            }
        }
    }
}

@Composable
fun EfeCareerMapScreen(vm: EightFourFourViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    val profile by vm.profile.collectAsState()
    val user by vm.user.collectAsState()
    val data by vm.data.collectAsState()
    PremiumGuard(PremiumFeature.CAREER_MAP, "Your Career Map", onBack) {
        val set = results
        if (set == null) { item { EmptyState(Icons.Outlined.Map, "Map not ready", "Complete the career assessment first.") }; return@PremiumGuard }
        val shortlist = PremiumInsightBuilder.universityShortlist(set, profile)
        item { Text("Tap any stage to see more.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { PfCard { CareerMapView(PremiumInsightBuilder.universityCareerMap(user?.firstName ?: "You", set, shortlist)) } }
        data?.let { d ->
            item { SectionTitle("Subject guidance") }
            PremiumInsightBuilder.subjectGuidance(set, d, profile).forEach { ins ->
                item { PfCard { Text(ins.title, style = MaterialTheme.typography.titleSmall); Text(ins.body, style = MaterialTheme.typography.bodySmall) } }
            }
        }
        item { VerifyNotice() }
    }
}

@Composable
fun EfeNextYearsScreen(vm: EightFourFourViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    val profile by vm.profile.collectAsState()
    val data by vm.data.collectAsState()
    PremiumGuard(PremiumFeature.ACTION_PLAN, "My Next 3 Years", onBack) {
        val set = results; val d = data
        if (set == null || d == null) { item { EmptyState(Icons.Outlined.Timeline, "Roadmap not ready", "Complete the career assessment first.") }; return@PremiumGuard }
        item { DisclaimerCard(PremiumInsightBuilder.GUIDANCE_NOTE, title = "Guidance only") }
        item { YearPlanView(PremiumInsightBuilder.universityNextThreeYears(set, d, profile)) }
    }
}
