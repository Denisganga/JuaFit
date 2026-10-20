package com.pathwayfit.app.ui.efe

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R

@Composable
fun AssessmentIntroScreen(onBack: () -> Unit, onStart: () -> Unit) {
    val palette = careerfitPalette

    val context = LocalContext.current
    val logo = remember { BitmapFactory.decodeResource(context.resources, R.drawable.juafit_emblem).asImageBitmap() }
    val illustration = remember { BitmapFactory.decodeResource(context.resources, R.drawable.assessment_reference).asImageBitmap() }
    val ink = palette.color(0xFF080C35)
    val muted = palette.color(0xFF3D5781)
    Box(Modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxWidth().height(156.dp)) {
            val w = size.width; val h = size.height
            drawPath(Path().apply {
                moveTo(0f,0f); lineTo(w * .36f,0f)
                cubicTo(w * .27f,h * .62f,w * .14f,h * .87f,0f,h); close()
            }, Brush.linearGradient(listOf(palette.color(0xFFD3EDFF),palette.color(0xFFECF8FF))))
            drawPath(Path().apply {
                moveTo(w * .64f,0f); lineTo(w,0f); lineTo(w,h * .78f)
                cubicTo(w * .88f,h * .41f,w * .71f,h * .42f,w * .64f,0f); close()
            }, Brush.horizontalGradient(listOf(palette.color(0xFFFFF4D2),palette.color(0xFFFFE487))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink)
                }
                Spacer(Modifier.weight(1f))
                Canvas(Modifier.width(65.dp).height(77.dp)) {
                    drawImage(logo, srcOffset = IntOffset(0, 0), srcSize = IntSize(1254, 1254), dstSize = IntSize(size.width.toInt(),size.height.toInt()))
                }
                Column(Modifier.padding(start = 6.dp)) {
                    Row {
                        Text("Jua", color = palette.color(0xFF0060B9), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                        Text("Fit", color = palette.color(0xFFEFB52A), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                    }
                    Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ink, fontSize = 11.sp, lineHeight = 15.sp)
                }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Text("Take Assessment", color = ink, fontSize = 30.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Answer a few questions to help us understand you better and recommend the best career and course options.",
                color = muted, fontSize = 17.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            // Display the illustration from the supplied reference without its phone frame or UI.
            Canvas(Modifier.fillMaxWidth().aspectRatio(480f / 315f)) {
                drawImage(illustration, srcOffset = IntOffset(110,410), srcSize = IntSize(480,315),
                    dstSize = IntSize(size.width.toInt(),size.height.toInt()))
            }
            Surface(shape = RoundedCornerShape(16.dp), color = palette.color(0xFFEEF8FF), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IntroDetail(Icons.AutoMirrored.Outlined.Assignment, "Question types", "Multiple choice, text input and selection (e.g. choose or type your answer).")
                    IntroDetail(Icons.Outlined.FormatListBulleted, "Covers different areas", "Your background, interests, skills and preferences.")
                    IntroDetail(Icons.Outlined.TrackChanges, "Get personalized results", "Your answers help us recommend the best career and course options for you.")
                }
            }
            Spacer(Modifier.height(9.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.color(0xFF0064E7), contentColor = Color.White)) {
                Text("Start Assessment", fontSize = 19.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(14.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(23.dp))
            }
        }
    }
}

@Composable
private fun IntroDetail(icon: ImageVector, title: String, body: String) {
    val palette = careerfitPalette

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = palette.color(0xFF0064E7), modifier = Modifier.size(30.dp))
        Column(Modifier.padding(start = 15.dp)) {
            Text(title, color = palette.color(0xFF080C35), fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(body, color = palette.color(0xFF425E8B), fontSize = 13.sp, lineHeight = 17.sp)
        }
    }
}
