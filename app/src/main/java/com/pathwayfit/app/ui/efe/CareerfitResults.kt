package com.pathwayfit.app.ui.efe

import kotlinx.serialization.json.*

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel

private val ResultInk
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val ResultMuted
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ResultBlue = Color(0xFF0064F5)

@Composable
private fun ResultFrame(onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val palette = careerfitPalette

    val context = LocalContext.current
    val logo = remember { BitmapFactory.decodeResource(context.resources, R.drawable.juafit_emblem).asImageBitmap() }
    Box(Modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawPath(Path().apply { moveTo(0f,0f); lineTo(w*.39f,0f); quadraticTo(w*.22f,h*.12f,0f,h*.18f); close() }, Brush.horizontalGradient(listOf(palette.color(0xFFCEE9FF),palette.color(0xFFF1FAFF))))
            drawPath(Path().apply { moveTo(w*.67f,0f); lineTo(w,0f); lineTo(w,h*.17f); quadraticTo(w*.70f,h*.08f,w*.67f,0f); close() }, Brush.horizontalGradient(listOf(palette.color(0xFFFFF5D5),palette.color(0xFFFFE68A))))
            drawPath(Path().apply { moveTo(0f,h*.93f); quadraticTo(w*.3f,h*.99f,w*.73f,h); lineTo(0f,h); close() }, palette.color(0xFFD5EDFF))
            drawPath(Path().apply { moveTo(w,h*.91f); quadraticTo(w*.7f,h*.98f,w*.61f,h); lineTo(w,h); close() }, palette.color(0xFFFFE9A0))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 18.dp, bottom = 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ResultInk) }
                Spacer(Modifier.weight(1f))
                Canvas(Modifier.width(59.dp).height(69.dp)) { drawImage(logo, srcOffset = IntOffset(0, 0), srcSize = IntSize(1254, 1254), dstSize = IntSize(size.width.toInt(),size.height.toInt())) }
                Column(Modifier.padding(start = 5.dp)) {
                    Row {
                        Text("Jua", color = palette.color(0xFF0060B9), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                        Text("Fit", color = palette.color(0xFFEFB52A), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                    }
                    Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ResultInk, fontSize = 10.sp, lineHeight = 13.sp)
                }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun CareerfitRecommendationsScreen(vm: EightFourFourViewModel, onOpen: (String) -> Unit, onStart: () -> Unit) {
    val palette = careerfitPalette

    val results by vm.results.collectAsState()
    val ai by vm.geminiResult.collectAsState()
    ResultFrame {
        Text("Your Personalized\nRecommendations", color = ResultInk, fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        if (com.pathwayfit.app.ai.JuaGuideDemo.enabled) Text("AI Preview • Personalized guidance", color = ResultMuted, fontSize = 12.sp)
        Spacer(Modifier.height(9.dp))
        Text("Based on your profile and assessment, here are your top 5 courses, ranked by suitability.", color = ResultMuted, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(15.dp))
        ai?.summary?.takeIf { it.isNotBlank() }?.let { summary ->
            Surface(color = palette.color(0xFFEAF4FF), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Text(summary, color = ResultInk, fontSize = 16.sp, lineHeight = 23.sp, modifier = Modifier.padding(14.dp))
            }
        }
        results?.takeIf { it.provisional }?.let { current ->
            Surface(color = palette.color(0xFFFFF5DB), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Provisional recommendations", color = ResultInk, fontWeight = FontWeight.Bold)
                    Text("Outstanding: " + current.outstandingAssessments.joinToString("; "), color = ResultMuted)
                    Text("Complete the missing assessments for a fuller picture. A separate scored aptitude test is not yet available in this version; self-reported strengths do not replace it.", color = ResultMuted)
                    TextButton(onClick = onStart) { Text("Complete or update interest questions") }
                    Text("Pilot weights: academic 25%, subject enjoyment 15%, interests 15%, aptitude 20%, career goals 15%, preferences 10%. Missing factors are omitted and remaining weights re-normalised.", color = ResultMuted, fontSize = 12.sp)
                }
            }
        }
        val courses = results?.courses.orEmpty().sortedByDescending { it.score }.take(5)
        if (courses.isEmpty()) Button(onClick = onStart) { Text("Take Assessment") }
        courses.forEachIndexed { i, match ->
            Surface(onClick = { onOpen(match.course.id) }, color = palette.color(0xFFFAFDFF), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp,palette.color(0xFFDEEBFB)), modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(if (i % 2 == 0) palette.color(0xFFE0F1FF) else palette.color(0xFFFFF5DB), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(28.dp).background(if (i % 2 == 0) ResultBlue else palette.color(0xFFFFBE12),CircleShape), contentAlignment = Alignment.Center) { Text("${i+1}",color=Color.White,fontSize = 18.sp,fontWeight=FontWeight.Bold) }
                    }
                    Icon(if (i == 0) Icons.Outlined.MenuBook else if (i == 2) Icons.Outlined.Computer else if (i == 4) Icons.Outlined.WorkOutline else Icons.Outlined.BarChart, null, tint = ResultBlue, modifier = Modifier.padding(horizontal = 12.dp).size(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text(match.course.name, color = ResultInk, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                        Text(if (match.factors.any { it.key in listOf("interest", "aptitude") && it.weight > 0f }) "${match.percent}% ${if (results?.provisional == true) "provisional suitability" else "suitability"}" else "More assessment data needed", color = palette.color(0xFF008448), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Outlined.ChevronRight,null,tint=ResultBlue,modifier=Modifier.size(20.dp))
                }
            }
        }
        Surface(color=palette.color(0xFFE9F5FF),shape=RoundedCornerShape(14.dp)) {
            Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.EmojiEvents,null,tint=ResultBlue,modifier=Modifier.size(32.dp))
                Text("Suitability reflects your academic profile, interests and strengths. It is not an admission probability.",color=ResultMuted,fontSize = 13.sp,lineHeight = 18.sp,modifier=Modifier.padding(start=14.dp))
            }
        }
        if (ai != null) Text("Personalized by JuaGuide. Recommendations are ranked guidance, not admission guarantees.",color=ResultMuted,fontSize = 11.sp,modifier=Modifier.padding(top=8.dp))
    }
}

@Composable
fun CareerfitCourseScreen(vm: EightFourFourViewModel, courseId: String, onBack: () -> Unit) {
    val palette = careerfitPalette

    val results by vm.results.collectAsState()
    val ai by vm.geminiResult.collectAsState()
    val saved by vm.saved.collectAsState()
    val data by vm.data.collectAsState()
    val match = results?.courses?.firstOrNull { it.course.id == courseId }
    var page by rememberSaveable(courseId) { mutableStateOf("Menu") }
    var facts by remember(courseId, page) { mutableStateOf("") }
    var factSources by remember(courseId, page) { mutableStateOf<List<Pair<String,String>>>(emptyList()) }
    var factsBusy by remember(courseId, page) { mutableStateOf(false) }
    var factsError by remember(courseId, page) { mutableStateOf<String?>(null) }
    val factContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(courseId, page) {
        val section = when(page) { "Entry requirements" -> "requirements"; "Universities offering it" -> "universities"; "Career progression" -> "progression"; else -> null }
        if(section != null && match != null) {
            factsBusy = true
            try {
                val printed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.pathwayfit.app.ai.KuccpsCatalogue.facts(factContext, courseId, section)
                }
                if (printed != null) {
                    facts = printed
                    factSources = listOf("KUCCPS 2025/2026 source PDF" to "https://juafit-website.vercel.app/assets/kuccps-degree-programmes-2025.pdf")
                    return@LaunchedEffect
                }
                val result = com.pathwayfit.app.ai.GeminiConnection.stream("course-facts", kotlinx.serialization.json.buildJsonObject {
                    put("course", kotlinx.serialization.json.JsonPrimitive(match.course.name)); put("section", kotlinx.serialization.json.JsonPrimitive(section))
                }) { event ->
                    if(event["type"]?.jsonPrimitive?.content == "text") facts = "Checking sources — provisional until complete:\n" + event["text"]?.jsonPrimitive?.content.orEmpty()
                }
                facts = result["text"]?.jsonPrimitive?.content.orEmpty()
                factSources = result["sources"]?.jsonArray.orEmpty().mapNotNull { source ->
                    val web = source.jsonObject
                    val uri = web["uri"]?.jsonPrimitive?.content.orEmpty()
                    if(uri.startsWith("https://")) (web["title"]?.jsonPrimitive?.content ?: "Source") to uri else null
                }
            } catch(e: Exception) { if(e is kotlinx.coroutines.CancellationException) throw e; factsError = "Unable to verify current information. Please reopen this section to retry." }
            finally { factsBusy = false }
        }
    }
    val back = { if (page == "Menu") onBack() else page = "Menu" }
    BackHandler(onBack = back)
    ResultFrame(onBack = back) {
        if (match == null) { Text("This recommendation is unavailable. Please repeat the assessment."); return@ResultFrame }
        val insight = ai?.insights?.firstOrNull { it.courseId == courseId }
        val sections = listOf("Overview", "Why it’s recommended", "Entry requirements", "Universities offering it", "Career progression", "Further education opportunities", "Relevant competencies", "Potential employment sectors")
        val subtitles = listOf("What the course involves", "How your answers match", "Minimum grades and subjects", "Where you can study", "Job opportunities after graduation", "What you can study next", "Key skills you’ll develop", "Industries that hire graduates")
        val icons = listOf(Icons.AutoMirrored.Outlined.Assignment,Icons.Outlined.TrackChanges,Icons.Outlined.ContentPaste,Icons.Outlined.AccountBalance,Icons.Outlined.WorkOutline,Icons.Outlined.School,Icons.Outlined.BarChart,Icons.Outlined.Groups)
        when (page) {
            "Menu" -> {
                Surface(color=palette.color(0xFF005AC8),shape=RoundedCornerShape(13.dp),modifier=Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text(match.course.name,color=Color.White,fontSize = 23.sp,lineHeight = 29.sp,fontWeight=FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Surface(color=palette.color(0xFFABF5D5),shape=CircleShape) { Text("Rank #${results?.courses?.indexOfFirst { it.course.id == courseId }?.plus(1) ?: 1}",color=palette.color(0xFF00683D),fontSize = 15.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=12.dp,vertical=4.dp)) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                sections.forEachIndexed { i, title ->
                    Surface(onClick={page=title},shape=RoundedCornerShape(10.dp),color=palette.color(0xFFFAFDFF),border=BorderStroke(1.dp,palette.color(0xFFDEEBFB)),modifier=Modifier.fillMaxWidth().padding(bottom=6.dp)) {
                        Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically) {
                            SectionIcon(icons[i],36.dp)
                            Column(Modifier.weight(1f).padding(start=12.dp)) { Text(title,color=ResultInk,fontSize = 15.sp,fontWeight=FontWeight.SemiBold); Text(subtitles[i],color=ResultMuted,fontSize = 13.sp) }
                            Icon(Icons.Outlined.ChevronRight,null,tint=ResultBlue,modifier=Modifier.size(20.dp))
                        }
                    }
                }
                Button(onClick={vm.toggleSave(match)},colors=ButtonDefaults.buttonColors(containerColor=ResultBlue),shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().height(46.dp)) { Text(if(saved.any { it.itemId==courseId }) "Added to My Plan ✓" else "Add to My Plan  +") }
            }
            "Overview" -> {
                SectionTitle("Course Overview",icons[0])
                Text("What the course involves",color=ResultInk,fontSize = 19.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(insight?.overview ?: match.course.overview,color=ResultMuted,fontSize = 16.sp,lineHeight = 23.sp)
                Spacer(Modifier.height(15.dp))
                if(match.course.field.contains("Education",true)) ClassroomPhoto()
                if(!insight?.quote.isNullOrBlank()) { Spacer(Modifier.height(12.dp)); Surface(color=palette.color(0xFFFFF3D0),shape=RoundedCornerShape(14.dp)) { Text("“ ${insight?.quote}",color=ResultInk,fontSize = 15.sp,lineHeight = 22.sp,modifier=Modifier.padding(16.dp)) } }
            }
            "Why it’s recommended" -> {
                SectionTitle(page,icons[1])
                Text("This course matches your profile because:",color=ResultMuted,fontSize = 16.sp)
                Spacer(Modifier.height(18.dp))
                val reasons = insight?.reasons ?: match.factors.filter { it.evidence.isNotEmpty() }.map { "${it.label}: ${it.evidence.joinToString(", ")}" }
                reasons.forEach { reason -> Row(Modifier.padding(bottom=20.dp),verticalAlignment=Alignment.Top) {
                    Box(Modifier.size(22.dp).background(ResultBlue,CircleShape),contentAlignment=Alignment.Center) { Icon(Icons.Filled.Check,null,tint=Color.White,modifier=Modifier.size(16.dp)) }
                    Text(reason,color=ResultMuted,fontSize = 15.sp,lineHeight = 23.sp,modifier=Modifier.padding(start=16.dp))
                } }
                if(insight!=null) Surface(color=palette.color(0xFFE7F4FF),shape=RoundedCornerShape(14.dp)) { Column(Modifier.padding(18.dp)) { Text("Your potential",color=ResultInk,fontSize = 16.sp,fontWeight=FontWeight.Bold); Text(insight.potential,color=ResultMuted,fontSize = 15.sp,lineHeight = 22.sp) } }
            }
            else -> {
                SectionTitle(page,icons[sections.indexOf(page)])
                if(page in listOf("Entry requirements", "Universities offering it", "Career progression")) {
                    if(factsBusy) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Checking current official sources…", color=ResultMuted) }
                    factsError?.let { Text(it, color=MaterialTheme.colorScheme.error) }
                    if(facts.isNotBlank()) Text(facts, color=ResultMuted, fontSize=16.sp, lineHeight=23.sp)
                    factSources.forEach { (title, url) -> TextButton(onClick={ factContext.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }) { Text(title) } }
                    return@ResultFrame
                }
                val entries = when(page) {
                    "Entry requirements" -> data?.admissionRequirements.orEmpty().filter { it.courseId==courseId && it.verified }.map { "${it.description}\nSource: ${it.sourceName}" }.ifEmpty { listOf("Verified entry requirements are not available yet. Confirm current requirements with the university or KUCCPS.") }
                    "Universities offering it" -> listOf("Confirm current programme availability directly with the universities. Listings in this prototype are not verified.")
                    "Career progression" -> insight?.careerProgression.orEmpty().ifEmpty { match.careers.map { it.name } }
                    "Further education opportunities" -> insight?.furtherEducation.orEmpty()
                    "Relevant competencies" -> match.course.skills
                    else -> insight?.employmentSectors.orEmpty()
                }
                entries.ifEmpty { listOf("More information will be added soon.") }.forEach { Text(it,color=ResultMuted,fontSize = 16.sp,lineHeight = 23.sp,modifier=Modifier.padding(bottom=14.dp)) }
            }
        }
    }
}

@Composable private fun SectionIcon(icon: ImageVector, size: Dp = 52.dp) {
    val palette = careerfitPalette
 Box(Modifier.size(size).background(palette.color(0xFFE0F1FF),CircleShape),contentAlignment=Alignment.Center) { Icon(icon,null,tint=ResultBlue,modifier=Modifier.size(size*.56f)) } }
@Composable private fun SectionTitle(title:String,icon:ImageVector) {
    val palette = careerfitPalette
 Row(Modifier.padding(bottom=24.dp),verticalAlignment=Alignment.CenterVertically) { SectionIcon(icon); Text(title,color=ResultInk,fontSize = 23.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(start=16.dp)) } }
@Composable private fun ClassroomPhoto() {
    val palette = careerfitPalette

    val context=LocalContext.current
    val image=remember { BitmapFactory.decodeResource(context.resources,R.drawable.results_reference).asImageBitmap() }
    Canvas(Modifier.fillMaxWidth().height(155.dp)) { drawImage(image,srcOffset=IntOffset(705,406),srcSize=IntSize(285,134),dstSize=IntSize(size.width.toInt(),size.height.toInt())) }
}
