package com.pathwayfit.app.ui.cbe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.ai.AiExplanation
import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.CbeSubjectKind
import com.pathwayfit.app.core.model.JourneyStatus
import com.pathwayfit.app.core.model.TagCatalog
import com.pathwayfit.app.engine.CbePathwayMatch
import com.pathwayfit.app.premium.PremiumFeature
import com.pathwayfit.app.premium.PremiumInsightBuilder
import com.pathwayfit.app.ui.components.ChainFlow
import com.pathwayfit.app.ui.components.ChainNode
import com.pathwayfit.app.ui.components.CheckLine
import com.pathwayfit.app.ui.components.ChipSelector
import com.pathwayfit.app.ui.components.DisclaimerCard
import com.pathwayfit.app.ui.components.EmptyState
import com.pathwayfit.app.ui.components.FactorBar
import com.pathwayfit.app.ui.components.HeroCard
import com.pathwayfit.app.ui.components.ListRowCard
import com.pathwayfit.app.ui.components.LockedPreview
import com.pathwayfit.app.ui.components.MatchPill
import com.pathwayfit.app.ui.components.BrandHeader
import com.pathwayfit.app.ui.components.ProfileSavedDialog
import com.pathwayfit.app.ui.components.ProfileReminder
import com.pathwayfit.app.ui.components.PathwayCard
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Palette
import com.pathwayfit.app.ui.components.PfCard
import com.pathwayfit.app.ui.components.PfScreen
import com.pathwayfit.app.ui.components.PremiumBadge
import com.pathwayfit.app.ui.components.PrimaryButton
import com.pathwayfit.app.ui.components.ScoreRing
import com.pathwayfit.app.ui.components.SecondaryButton
import com.pathwayfit.app.ui.components.SectionTitle
import com.pathwayfit.app.ui.components.TileCard
import com.pathwayfit.app.ui.shared.AssessmentFlow
import com.pathwayfit.app.ui.shared.CareerMapView
import com.pathwayfit.app.ui.shared.ComparisonTable
import com.pathwayfit.app.ui.shared.CountyPickerField
import com.pathwayfit.app.ui.shared.LocalPremiumUi
import com.pathwayfit.app.ui.shared.YearPlanView
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PfColors
import com.pathwayfit.app.ui.viewmodel.CbeViewModel
import kotlinx.coroutines.delay

const val CBE_DISCLAIMER = "JuaFit recommendations are guidance based on your answers. Your final pathway choice should be made with your school, teachers and parents."

@Composable
private fun CbeDataNotice(ref: CbeReference?) {
    if (ref != null && !ref.meta.verified) {
        DisclaimerCard(title = "CBE data pending verification", text = "Pathway, track and area information in this build is interim and is being checked against the official CBE reference. Confirm details with your school.")
    }
}

// ================================================================== HOME

@Composable
fun CbeHomeScreen(
    vm: CbeViewModel,
    onStartAssessment: () -> Unit,
    onExplore: () -> Unit,
    onRecommendations: () -> Unit,
    onJourney: () -> Unit,
    onProfile: () -> Unit,
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
            Text(profile.grade.ifBlank { "Junior Secondary School" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { ProfileReminder(profile.completion(), onProfile) }
        item {
            HeroCard("Your pathway discovery", "Let's find your best pathway", "Explore the interests, strengths and learning preferences that shape your next step.", "Take Assessment", onStartAssessment, Icons.AutoMirrored.Outlined.Assignment)
        }
        item { ListRowCard(Icons.Outlined.Person, "My Profile", "Update your details", onClick = onProfile) }
        item { ListRowCard(Icons.Outlined.EmojiEvents, "My Results", results?.top?.pathway?.name ?: "View your recommended pathways", onClick = onRecommendations) }
        item { SectionTitle("Explore your next step") }
        item { ListRowCard(Icons.Outlined.Explore, "Explore Pathways", "Discover pathways, tracks and areas", onClick = onExplore) }
        item { ListRowCard(Icons.Outlined.Timeline, "My Journey", "Your plan from Grade 9 to a future career", onClick = onJourney) }
        item { DisclaimerCard(CBE_DISCLAIMER) }
    }
}

@Composable
private fun MiniChain(parts: List<String>) {
    val accent = LocalRoleAccent.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        parts.forEachIndexed { i, p ->
            Surface(shape = RoundedCornerShape(8.dp), color = accent.soft) {
                Text(p, style = MaterialTheme.typography.labelMedium, color = accent.main, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), maxLines = 1)
            }
            if (i < parts.lastIndex) Text(" → ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ================================================================== PROFILE

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CbeProfileScreen(vm: CbeViewModel, setup: Boolean, onBack: (() -> Unit)?, onDone: () -> Unit) {
    var profileSaved by rememberSaveable { mutableStateOf(false) }
    val saved by vm.profile.collectAsState()
    val ref by vm.reference.collectAsState()
    val careers by vm.careers.collectAsState()
    var p by remember(saved) { mutableStateOf(saved) }

    fun toggle(list: List<String>, id: String) = if (id in list) list - id else list + id

    PfScreen(title = if (setup) "Set up your CBE profile" else "My CBE Profile", onBack = onBack) {
        item {
            Text(
                "This helps JuaFit understand you. Everything here is used only for your CBE pathway guidance.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { SectionTitle("Grade") }
        item {
            ChipSelector(
                options = (ref?.grades ?: emptyList()).map { it to it },
                selected = listOf(p.grade),
                onToggle = { p = p.copy(grade = it) },
            )
        }
        item {
            OutlinedTextField(p.school, { p = p.copy(school = it) }, label = { Text("School") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        }
        item { CountyPickerField("County", p.county) { p = p.copy(county = it) } }
        item { SectionTitle("Interests") }
        item { ChipSelector(TagCatalog.interestOptions.map { it.id to it.label }, p.interests, { p = p.copy(interests = toggle(p.interests, it)) }, max = 5) }
        item { SectionTitle("Strengths") }
        item { ChipSelector(TagCatalog.strengthOptions.map { it.id to it.label }, p.strengths, { p = p.copy(strengths = toggle(p.strengths, it)) }, max = 4) }
        item { SectionTitle("Favourite learning activities") }
        item { ChipSelector(TagCatalog.activityOptions.map { it.id to it.label }, p.favouriteActivities, { p = p.copy(favouriteActivities = toggle(p.favouriteActivities, it)) }, max = 4) }
        item { SectionTitle("Learning areas you enjoy") }
        item {
            ChipSelector(
                (ref?.subjects ?: emptyList()).filter { it.kind == CbeSubjectKind.PATHWAY }.map { it.id to it.name },
                p.learningAreas, { p = p.copy(learningAreas = toggle(p.learningAreas, it)) }, max = 6,
            )
        }
        item { SectionTitle("Career interests (optional)") }
        item {
            ChipSelector(
                (careers?.careers ?: emptyList()).map { it.id to it.name },
                p.careerInterests, { p = p.copy(careerInterests = toggle(p.careerInterests, it)) }, max = 3,
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
        item { PrimaryButton(if (setup) "Save and continue" else "Save profile", { vm.saveProfile(p) { profileSaved = true } }) }
        if (setup) item { SecondaryButton("Skip for now", onDone) }
    }
    if (profileSaved) ProfileSavedDialog(onDone)
}

// ================================================================== ASSESSMENT

@Composable
fun CbeAssessmentScreen(vm: CbeViewModel, onClose: () -> Unit, onFinished: () -> Unit) {
    AssessmentFlow(draft = vm.draft, eyebrow = "CBE Pathway Assessment", onClose = onClose, onSubmit = { vm.submitAssessment(onFinished) })
}

// ================================================================== AHA / ANALYSIS

@Composable
fun CbeAnalysisScreen(vm: CbeViewModel, onSeeResults: () -> Unit, onPremiumPreview: () -> Unit) {
    val results by vm.results.collectAsState()
    var analysing by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1800); analysing = false }
    val accent = LocalRoleAccent.current

    if (analysing || results == null) {
        AnalysingView("Analysing your interests, strengths and learning preferences…")
        return
    }
    val set = results!!
    val top = set.top ?: return
    var step by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (step < 4) { delay(350); step++ } }

    PfScreen(title = "") {
        item {
            Column {
                Text("Your Pathway Profile Is Ready", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text("We found patterns in your interests, strengths and academic preferences.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            AnimatedVisibility(step >= 1, enter = fadeIn(tween(400)) + slideInVertically { it / 3 }) {
                PfCard {
                    Text("STRENGTH PROFILE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    StrengthChips(set.strengthProfile.ifEmpty { listOf("Curious learner") })
                }
            }
        }
        item {
            AnimatedVisibility(step >= 2, enter = fadeIn(tween(400)) + slideInVertically { it / 3 }) {
                PfCard {
                    Text("YOUR TOP MATCH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(top.pathway.name, style = MaterialTheme.typography.headlineSmall)
                            top.bestTrack?.let { Text("Track: ${it.item.name}", style = MaterialTheme.typography.bodyMedium, color = accent.main) }
                        }
                        ScoreRing(top.percent)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("You have a strong match. Your interests and strengths show strong alignment with this direction.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            AnimatedVisibility(step >= 3, enter = fadeIn(tween(400))) {
                LockedPreview(
                    title = "🔒 Unlock Your Complete Path",
                    subtitle = "Your full pathway-to-career map, every direction we found, and What If? comparisons.",
                    teaser = listOf("Recommended Area → Learning Areas", null, "Future Study Options", null, "+${(set.directions.size - 3).coerceAtLeast(1)} more directions"),
                    cta = "See What's Behind Your Match",
                    onClick = onPremiumPreview,
                )
            }
        }
        item { AnimatedVisibility(step >= 4, enter = fadeIn()) { PrimaryButton("View my recommendations", onSeeResults) } }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StrengthChips(items: List<String>) {
    val accent = LocalRoleAccent.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.soft) {
                Text(it, color = accent.main, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
fun AnalysingView(text: String) {
    val accent = LocalRoleAccent.current
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, accent.soft))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            CircularProgressIndicator(color = accent.main, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(24.dp))
            Icon(Icons.Outlined.Psychology, null, tint = accent.main, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

// ================================================================== RESULTS

@Composable
fun CbeResultsScreen(vm: CbeViewModel, onBack: (() -> Unit)?, onOpen: (String) -> Unit, onStart: () -> Unit, onExplore: () -> Unit) {
    val results by vm.results.collectAsState()
    val ref by vm.reference.collectAsState()
    val premium = LocalPremiumUi.current
    PfScreen(title = "Your Recommended Pathways", onBack = onBack) {
        val set = results
        if (set == null) {
            item { EmptyState(Icons.AutoMirrored.Outlined.Assignment, "No recommendations yet", "Complete the CBE assessment to see the pathways that match you.", "Start Assessment", onStart) }
            return@PfScreen
        }
        item { Text("Ranked by interest and strength alignment. Percentages are guidance scores, not official Ministry placement scores.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        itemsIndexed(set.pathways) { i, m ->
            PathwayCard("${i + 1}. ${m.pathway.name}", m.pathway.description, pathwayIcon(m.pathway.id), pathwayTone(m.pathway.id), m.percent, i == 0) { onOpen(m.pathway.id) }
        }
        item { PrimaryButton("Explore all pathways", onExplore) }

        val dirs = set.directions
        item { SectionTitle("We found ${dirs.size} possible directions") }
        val free = if (premium.has(PremiumFeature.ALL_DIRECTIONS)) dirs else dirs.take(3)
        itemsIndexed(free) { i, d ->
            ListRowCard(Icons.Outlined.Explore, "${i + 1}. ${d.name}", ref?.track(d.trackId)?.name ?: "", "${d.percent}%") {
                ref?.track(d.trackId)?.pathwayId?.let(onOpen)
            }
        }
        if (dirs.size > free.size) item {
            LockedPreview(
                title = "+ ${dirs.size - free.size} more personalised directions",
                subtitle = "See every track and area that fits you, ranked.",
                teaser = List((dirs.size - free.size).coerceAtMost(4)) { null },
                cta = "Explore All ${dirs.size}",
                onClick = { premium.requestUnlock(PremiumFeature.ALL_DIRECTIONS) },
            )
        }
        item { CbeDataNotice(ref) }
        item { DisclaimerCard(CBE_DISCLAIMER) }
    }
}

// ================================================================== DETAIL

@Composable
fun CbeRecommendationDetailScreen(
    vm: CbeViewModel,
    pathwayId: String,
    onBack: () -> Unit,
    onPathwayMap: () -> Unit,
    onWhatIf: () -> Unit,
    onNextYears: () -> Unit,
) {
    val results by vm.results.collectAsState()
    val ref by vm.reference.collectAsState()
    val saved by vm.saved.collectAsState()
    val premium = LocalPremiumUi.current
    val match = results?.pathways?.firstOrNull { it.pathway.id == pathwayId }
    val explanation by produceState<AiExplanation?>(null, match) { value = match?.let { vm.explain(it) } }
    val isSaved = saved.any { it.itemId == pathwayId }

    PfScreen(
        title = match?.pathway?.name ?: "Recommendation",
        onBack = onBack,
        actions = {
            if (match != null) IconButton(onClick = { vm.toggleSave(match) }) {
                Icon(if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Save")
            }
        },
    ) {
        if (match == null) {
            item { EmptyState(Icons.Outlined.Explore, "Not available", "Complete the assessment to see this recommendation.") }
            return@PfScreen
        }
        item {
            PfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("RECOMMENDED Jua", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(match.pathway.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(match.pathway.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    ScoreRing(match.percent)
                }
            }
        }
        item { SectionTitle("Tracks in this pathway") }
        items(ref?.tracksOf(pathwayId).orEmpty(), key = { it.id }) { track ->
            var expanded by rememberSaveable(track.id) { mutableStateOf(false) }
            PfCard(onClick = { expanded = !expanded }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(track.name, style = MaterialTheme.typography.titleMedium)
                        Text(track.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, if (expanded) "Hide areas" else "Show areas")
                }
                if (expanded) {
                    Spacer(Modifier.height(12.dp))
                    Text("Learning areas", style = MaterialTheme.typography.titleSmall)
                    ref?.areasOf(track.id).orEmpty().forEach { area ->
                        Text(area.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 10.dp))
                        Text(ref?.subjectsOf(area).orEmpty().joinToString { it.name }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { SectionTitle("Why this fits you") }
        item {
            PfCard {
                explanation?.let { Text(it.summary, style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(10.dp)) }
                whyLines(match).forEach { CheckLine(it) }
            }
        }
        item { SectionTitle("Your direction") }
        item {
            PfCard {
                ChainFlow(
                    listOf(
                        ChainNode("Pathway", match.pathway.name),
                        ChainNode("Track", match.bestTrack?.item?.name),
                        ChainNode("Area", match.bestArea?.item?.name, hint = match.bestArea?.item?.let { a -> ref?.subjectsOf(a)?.joinToString { it.name } } ?: ""),
                        ChainNode("Possible Careers", match.careers.take(3).joinToString { it.item.name }.ifBlank { null }),
                    ),
                )
            }
        }
        item { SectionTitle("Why You Fit — Detailed Analysis") }
        item {
            if (premium.has(PremiumFeature.DETAILED_WHY_FIT)) {
                PfCard {
                    match.factors.forEach { f -> FactorBar(f.label, f.score, explanation?.factorNotes?.get(f.key)) }
                    explanation?.let { Text(it.generatedBy, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            } else {
                LockedPreview(
                    title = "You have a strong match. Want to understand why?",
                    subtitle = "Every factor scored and explained in plain language.",
                    teaser = match.factors.flatMap { listOf(it.label, null) }.take(6),
                    cta = "Unlock Detailed Analysis",
                    onClick = { premium.requestUnlock(PremiumFeature.DETAILED_WHY_FIT) },
                )
            }
        }
        item { SectionTitle("Go deeper") }
        item { PremiumToolRow(Icons.Outlined.Map, "Complete CBE Pathway Map", "Grade → pathway → track → area → careers", PremiumFeature.CBE_PATHWAY_MAP, onPathwayMap) }
        item { PremiumToolRow(Icons.AutoMirrored.Outlined.CompareArrows, "What If?", "Compare this pathway with another", PremiumFeature.WHAT_IF, onWhatIf) }
        item { PremiumToolRow(Icons.Outlined.AutoAwesome, "My Next 3 Years", "Your personalised development roadmap", PremiumFeature.ACTION_PLAN, onNextYears) }
        item { CbeDataNotice(ref) }
    }
}

private fun whyLines(m: CbePathwayMatch): List<String> {
    fun ev(key: String) = m.factors.firstOrNull { it.key == key }?.evidence?.joinToString(", ")?.lowercase()?.takeIf { it.isNotBlank() }
    return listOfNotNull(
        ev("interest")?.let { "Interest alignment — you showed interest in $it" } ?: "Interest alignment",
        ev("strength")?.let { "Strength alignment — $it" } ?: "Strength alignment",
        ev("learning")?.let { "Learning preference — you enjoy $it" } ?: "Learning preference",
        m.bestTrack?.item?.skills?.takeIf { it.isNotEmpty() }?.let { "Relevant competencies — ${it.joinToString().lowercase()}" },
        m.bestTrack?.item?.futureStudy?.takeIf { it.isNotEmpty() }?.let { "Possible future directions — ${it.joinToString().lowercase()}" },
    )
}

@Composable
internal fun PremiumToolRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, feature: PremiumFeature, onOpen: () -> Unit) {
    val premium = LocalPremiumUi.current
    val accent = LocalRoleAccent.current
    PfCard(onClick = { if (premium.has(feature)) onOpen() else premium.requestUnlock(feature) }, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.soft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent.main)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!premium.has(feature)) PremiumBadge()
        }
    }
}

// ================================================================== EXPLORER

@Composable
fun CbeExplorerScreen(vm: CbeViewModel, onBack: (() -> Unit)?) {
    val ref by vm.reference.collectAsState()
    val careers by vm.careers.collectAsState()
    var openPathway by rememberSaveable { mutableStateOf<String?>(null) }
    var openTrack by rememberSaveable { mutableStateOf<String?>(null) }
    val r = ref
    val pathway = r?.pathway(openPathway ?: "")
    val track = r?.track(openTrack ?: "")
    BackHandler(enabled = openTrack != null || openPathway != null) { if (openTrack != null) openTrack = null else openPathway = null }
    val goBack: (() -> Unit)? = if (openTrack != null || openPathway != null) ({
        if (openTrack != null) openTrack = null else openPathway = null
    }) else onBack
    PfScreen(title = track?.name ?: pathway?.name ?: "Explore Pathways", onBack = goBack) {
        if (r == null) return@PfScreen
        item { Text(when { track != null -> "${pathway?.name} / ${track.name}"; pathway != null -> "Pathway / Tracks"; else -> "Choose a pathway to explore its tracks and areas." }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (pathway == null) {
            items(r.pathways, key = { it.id }) { p ->
                PathwayCard(p.name, p.description, pathwayIcon(p.id), pathwayTone(p.id)) { openPathway = p.id }
            }
            item { SectionTitle("Core learning areas for all learners") }
            item { PfCard { r.coreSubjects().forEach { Text(it.name, Modifier.padding(vertical = 4.dp)) } } }
        } else if (track == null) {
            item { PfCard(container = LocalRoleAccent.current.soft, border = false) { Text(pathway.description, style = MaterialTheme.typography.bodyLarge) } }
            item { SectionTitle("Tracks in this pathway") }
            items(r.tracksOf(pathway.id), key = { it.id }) { t ->
                ListRowCard(Icons.Outlined.Science, t.name, t.description, onClick = { openTrack = t.id })
            }
        } else {
            item { Text(track.description, style = MaterialTheme.typography.bodyMedium) }
            item { SectionTitle("Areas of specialization") }
            val areas = r.areasOf(track.id)
            if (areas.isEmpty()) item { Text("No areas listed in this dataset yet.") }
            items(areas, key = { it.id }) { area ->
                PfCard {
                    Text(area.name, style = MaterialTheme.typography.titleLarge)
                    Text(area.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Learning areas", style = MaterialTheme.typography.titleSmall)
                    r.subjectsOf(area).forEach { Text(it.name, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall) }
                }
            }
            item { SectionTitle("Possible careers") }
            item {
                PfCard {
                    val related = careers?.careers?.filter { track.id in it.cbeTrackIds }.orEmpty()
                    if (related.isEmpty()) Text("No careers listed for this track yet.")
                    related.forEach { Text(it.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 5.dp)) }
                }
            }
        }
        item { CbeDataNotice(r) }
    }
}

private fun pathwayIcon(id: String) = when (id) {
    "stem" -> Icons.Outlined.Science
    "social_sciences" -> Icons.Outlined.Public
    else -> Icons.Outlined.Palette
}

private fun pathwayTone(id: String) = when (id) {
    "stem" -> PfColors.CbeTeal
    "social_sciences" -> PfColors.Gold
    else -> Color(0xFFB65B97)
}

// ================================================================== JOURNEY

@Composable
fun CbeJourneyScreen(vm: CbeViewModel, onBack: (() -> Unit)?, onStart: () -> Unit, onProfile: () -> Unit, onPathwayMap: () -> Unit) {
    val results by vm.results.collectAsState()
    val profile by vm.profile.collectAsState()
    val top = results?.top
    val premium = LocalPremiumUi.current
    fun rec(v: String?) = v?.let { "Recommended: $it" }

    PfScreen(title = "My CBE Journey", onBack = onBack) {
        item {
            Text("Where you are now, and where your recommendations point.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            PfCard {
                ChainFlow(
                    listOf(
                        ChainNode("Grade", profile.grade.ifBlank { null }, JourneyStatus.CURRENT, hint = if (profile.grade.isBlank()) "Add your grade in your profile" else "", onClick = onProfile),
                        ChainNode("Pathway", rec(top?.pathway?.name), JourneyStatus.UPCOMING),
                        ChainNode("Track", rec(top?.bestTrack?.item?.name), JourneyStatus.UPCOMING),
                        ChainNode("Area", rec(top?.bestArea?.item?.name), JourneyStatus.UPCOMING),
                        ChainNode("Future Study", top?.bestTrack?.item?.futureStudy?.firstOrNull(), JourneyStatus.UPCOMING),
                        ChainNode("Career Direction", top?.careers?.firstOrNull()?.item?.name, JourneyStatus.UPCOMING),
                    ),
                )
            }
        }
        item { SectionTitle("Your progress") }
        item {
            PfCard {
                CheckRow("Profile ${(profile.completion() * 100).toInt()}% complete", profile.completion() >= 0.75f)
                CheckRow("CBE assessment completed", results != null)
                CheckRow("Pathway recommendation received", top != null)
            }
        }
        if (results == null) item { PrimaryButton("Start Assessment", onStart) }
        else item {
            LockedOrOpen(
                feature = PremiumFeature.CBE_PATHWAY_MAP, title = "Complete your journey",
                subtitle = "See learning areas, future study options and university direction.",
                onOpen = onPathwayMap,
            )
        }
        item { DisclaimerCard(CBE_DISCLAIMER) }
    }
}

@Composable
internal fun CheckRow(text: String, done: Boolean) {
    val accent = LocalRoleAccent.current
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).background(if (done) accent.main else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { if (done) Text("✓", color = accent.onMain, style = MaterialTheme.typography.labelMedium) }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun LockedOrOpen(feature: PremiumFeature, title: String, subtitle: String, onOpen: () -> Unit) {
    val premium = LocalPremiumUi.current
    if (premium.has(feature)) PrimaryButton(title, onOpen)
    else LockedPreview(title, subtitle, listOf(null, null, null), "Complete Your Journey") { premium.requestUnlock(feature) }
}

// ================================================================== PREMIUM: CBE MAP / WHAT IF / 3 YEARS

@Composable
fun CbePathwayMapScreen(vm: CbeViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    val ref by vm.reference.collectAsState()
    val careers by vm.careers.collectAsState()
    val uni by vm.futureCourses.collectAsState()
    val profile by vm.profile.collectAsState()
    PremiumGuard(PremiumFeature.CBE_PATHWAY_MAP, "Complete CBE Pathway Map", onBack) {
        val set = results; val r = ref; val c = careers
        if (set == null || r == null || c == null) {
            item { EmptyState(Icons.Outlined.Map, "Map not ready", "Complete the CBE assessment first.") }
        } else {
            item { Text("Tap any stage to see more.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { PfCard { CareerMapView(PremiumInsightBuilder.cbePathwayMap(profile.grade, set, r, c, uni)) } }
            item { SectionTitle("Learning focus") }
            PremiumInsightBuilder.cbeSubjectGuidance(set, r).forEach { ins ->
                item { PfCard { Text(ins.title, style = MaterialTheme.typography.titleSmall); Text(ins.body, style = MaterialTheme.typography.bodySmall) } }
            }
            item { DisclaimerCard("University directions are shown as future possibilities, years ahead. " + PremiumInsightBuilder.GUIDANCE_NOTE) }
        }
    }
}

@Composable
fun CbeWhatIfScreen(vm: CbeViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    PremiumGuard(PremiumFeature.WHAT_IF, "What If?", onBack) {
        val set = results
        if (set == null || set.pathways.size < 2) {
            item { EmptyState(Icons.AutoMirrored.Outlined.CompareArrows, "Nothing to compare yet", "Complete the CBE assessment first.") }
            return@PremiumGuard
        }
        item {
            WhatIfContent(set.pathways)
        }
    }
}

@Composable
private fun WhatIfContent(pathways: List<CbePathwayMatch>) {
    var a by rememberSaveable { mutableStateOf(pathways[0].pathway.id) }
    var b by rememberSaveable { mutableStateOf(pathways[1].pathway.id) }
    val ma = pathways.first { it.pathway.id == a }
    val mb = pathways.first { it.pathway.id == b }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("What if I choose Pathway A?", style = MaterialTheme.typography.titleSmall)
        ChipSelector(pathways.map { it.pathway.id to it.pathway.name }, listOf(a), { if (it != b) a = it })
        Text("…versus Pathway B?", style = MaterialTheme.typography.titleSmall)
        ChipSelector(pathways.map { it.pathway.id to it.pathway.name }, listOf(b), { if (it != a) b = it })
        val cmp = PremiumInsightBuilder.whatIf(ma, mb)
        ComparisonTable(ma.pathway.name, mb.pathway.name, cmp.rows)
        DisclaimerCard("Both choices can lead to fulfilling futures. Use this to start a conversation with your teachers and parents.")
    }
}

@Composable
fun CbeNextYearsScreen(vm: CbeViewModel, onBack: () -> Unit) {
    val results by vm.results.collectAsState()
    val ref by vm.reference.collectAsState()
    PremiumGuard(PremiumFeature.ACTION_PLAN, "My Next 3 Years", onBack) {
        val set = results; val r = ref
        if (set == null || r == null) {
            item { EmptyState(Icons.AutoMirrored.Outlined.MenuBook, "Roadmap not ready", "Complete the CBE assessment first.") }
        } else {
            item { DisclaimerCard(PremiumInsightBuilder.GUIDANCE_NOTE, title = "Guidance only") }
            item { YearPlanView(PremiumInsightBuilder.cbeNextThreeYears(set, r)) }
        }
    }
}

/** Wraps a premium screen; shows a locked state if the feature is not available. */
@Composable
internal fun PremiumGuard(
    feature: PremiumFeature,
    title: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val premium = LocalPremiumUi.current
    PfScreen(title = title, onBack = onBack, actions = { PremiumBadge(Modifier.padding(end = 12.dp)) }) {
        if (premium.has(feature)) content()
        else item {
            LockedPreview(feature.title, feature.description, listOf(null, null, null, null), "Unlock ${feature.title}") { premium.requestUnlock(feature) }
        }
    }
}
