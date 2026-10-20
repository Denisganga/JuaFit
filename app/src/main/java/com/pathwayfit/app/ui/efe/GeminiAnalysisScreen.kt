package com.pathwayfit.app.ui.efe

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel

@Composable
fun GeminiAnalysisScreen(vm: EightFourFourViewModel, onReady: () -> Unit) {
    val palette = careerfitPalette

    val stage by vm.analysisStage.collectAsState()
    val error by vm.analysisError.collectAsState()
    LaunchedEffect(Unit) { vm.analyzeAnswers() }
    LaunchedEffect(stage) { if (stage == 4) onReady() }
    val ink = palette.color(0xFF080C35)
    val muted = palette.color(0xFF405D8E)
    val blue = palette.color(0xFF0064F5)
    val context = LocalContext.current
    val logo = remember { BitmapFactory.decodeResource(context.resources, R.drawable.juafit_emblem).asImageBitmap() }
    val robot = remember { BitmapFactory.decodeResource(context.resources, R.drawable.analysis_reference).asImageBitmap() }
    Box(Modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawPath(Path().apply { moveTo(0f,0f); lineTo(w*.37f,0f); cubicTo(w*.28f,h*.1f,w*.15f,h*.15f,0f,h*.18f); close() }, Brush.linearGradient(listOf(palette.color(0xFFD5EDFF),palette.color(0xFFF1FAFF))))
            drawPath(Path().apply { moveTo(w*.63f,0f); lineTo(w,0f); lineTo(w,h*.16f); quadraticTo(w*.7f,h*.1f,w*.63f,0f); close() }, Brush.horizontalGradient(listOf(palette.color(0xFFFFF6DA),palette.color(0xFFFFE38A))))
            drawPath(Path().apply { moveTo(0f,h*.93f); quadraticTo(w*.28f,h*.99f,w*.75f,h); lineTo(0f,h); close() }, Brush.horizontalGradient(listOf(palette.color(0xFFDBF0FF),palette.color(0xFFB8E0FF))))
            drawPath(Path().apply { moveTo(w,h*.92f); quadraticTo(w*.72f,h*.99f,w*.60f,h); lineTo(w,h); close() }, Brush.horizontalGradient(listOf(palette.color(0xFFFFF6D4),palette.color(0xFFFFE38A))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 22.dp, bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.width(63.dp).height(75.dp)) { drawImage(logo, srcOffset = IntOffset(0, 0), srcSize = IntSize(1254, 1254), dstSize = IntSize(size.width.toInt(),size.height.toInt())) }
                Column(Modifier.padding(start = 7.dp)) {
                    Row {
                        Text("Jua", color = palette.color(0xFF0060B9), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                        Text("Fit", color = palette.color(0xFFEFB52A), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                    }
                    Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ink, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Analyzing Your Answers", color = ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (com.pathwayfit.app.ai.JuaGuideDemo.enabled) Text("AI Preview • Reviewing your answers", color = muted, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Text("Our AI is carefully reviewing your profile and assessment results to find the best career and course recommendations for you.", color = muted, fontSize = 15.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(9.dp))
            Canvas(Modifier.fillMaxWidth().aspectRatio(610f/275f)) { drawImage(robot, srcOffset = IntOffset(50,380), srcSize = IntSize(610,275), dstSize = IntSize(size.width.toInt(),size.height.toInt())) }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                if (stage < 4 && error == null) LinearProgressIndicator(modifier = Modifier.weight(1f).height(11.dp).clip(CircleShape), color = blue, trackColor = palette.color(0xFFE3F0FF))
                else LinearProgressIndicator(progress = { if (stage == 4) 1f else stage / 4f }, modifier = Modifier.weight(1f).height(11.dp).clip(CircleShape), color = blue, trackColor = palette.color(0xFFE3F0FF), drawStopIndicator = {})
                Text(if (stage == 4) "100%" else "${stage.coerceAtMost(3)}/4", color = ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            }
            Spacer(Modifier.height(14.dp))
            Surface(color = palette.color(0xFFF2F9FE), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Reading your profile information", "Analyzing your assessment answers", "Matching with career and course options", "Preparing your personalized recommendations").forEachIndexed { i, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (i + 1 < stage || stage == 4) Box(Modifier.size(19.dp).background(blue, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            else if (i + 1 == stage && error == null) CircularProgressIndicator(Modifier.size(19.dp), color = blue, strokeWidth = 2.dp)
                            else Box(Modifier.size(19.dp).border(1.dp,palette.color(0xFFCCDEFA),CircleShape))
                            Text(label, color = if (i + 1 < stage) ink else muted, fontSize = 14.sp, lineHeight = 18.sp, modifier = Modifier.padding(start = 18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(11.dp))
            Surface(color = palette.color(0xFFEAF6FF), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = palette.color(0xFFFFC318), modifier = Modifier.size(35.dp))
                    Column(Modifier.padding(start = 15.dp)) {
                        Text(if (error == null) "Good things take a moment!" else "Analysis could not finish", color = ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(error ?: "We’re using AI to give you personalized and relevant recommendations.", color = muted, fontSize = 13.sp, lineHeight = 16.sp)
                        if (error != null) TextButton(onClick = { vm.analyzeAnswers() }) { Text("Retry") }
                    }
                }
            }
            Text("Your journey. A brighter future.", color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 24.dp))
        }
    }
}
