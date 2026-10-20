package com.pathwayfit.app.ui.efe

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R

@Composable
fun CareerfitHomeScreen(name: String, profileComplete: Boolean, onProfile: () -> Unit, onAssessment: () -> Unit) {
    val palette = careerfitPalette

    val ink = palette.color(0xFF080C35)
    val muted = palette.color(0xFF3D5781)
    val blue = palette.color(0xFF0064E7)
    val context = LocalContext.current
    val logo = remember { BitmapFactory.decodeResource(context.resources, R.drawable.juafit_emblem).asImageBitmap() }
    Box(Modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val w = size.width; val h = size.height
            drawPath(Path().apply {
                moveTo(0f, 0f); lineTo(w * 0.39f, 0f)
                cubicTo(w * 0.30f, h * 0.56f, w * 0.16f, h * 0.82f, 0f, h); close()
            }, Brush.linearGradient(listOf(palette.color(0xFFB8E4FF), palette.color(0xFFE3F5FF))))
            drawPath(Path().apply {
                moveTo(w * 0.51f, 0f); lineTo(w, 0f); lineTo(w, h * 0.78f)
                cubicTo(w * 0.84f, h * 0.35f, w * 0.59f, h * 0.64f, w * 0.51f, 0f); close()
            }, Brush.horizontalGradient(listOf(palette.color(0xFFFFF6D9), palette.color(0xFFFFE68A))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp).padding(top = 26.dp, bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.width(66.dp).height(78.dp)) {
                    drawImage(logo, srcOffset = IntOffset(0, 0), srcSize = IntSize(1254, 1254), dstSize = IntSize(size.width.toInt(), size.height.toInt()))
                }
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    Row {
                        Text("Jua", color = palette.color(0xFF0060B9), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                        Text("Fit", color = palette.color(0xFFEFB52A), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                    }
                    Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ink, fontSize = 12.sp, lineHeight = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(onClick = onProfile, shape = CircleShape, color = palette.color(0xFFF0F6FF), border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)) {
                        Icon(Icons.Filled.Person, "Profile", tint = muted, modifier = Modifier.size(42.dp).padding(5.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Hello,", color = ink, fontSize = 14.sp)
                    Text(name, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
            if (!profileComplete) {
                Surface(onClick = onProfile, shape = RoundedCornerShape(15.dp), color = palette.color(0xFFFCE9EE), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 19.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(49.dp).background(palette.color(0xFFFF0032), CircleShape), contentAlignment = Alignment.Center) {
                            Text("!", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f).padding(start = 15.dp)) {
                            Text("Update Your Profile", color = palette.color(0xFFEF0035), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Complete your profile to unlock assessments and get personalized recommendations.", color = muted, fontSize = 14.sp, lineHeight = 19.sp)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = palette.color(0xFFEF0035), modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Surface(onClick = onAssessment, shape = RoundedCornerShape(16.dp),
                color = palette.color(0xFFF0F8FF), modifier = Modifier.fillMaxWidth()) {
                Box {
                    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(92.dp).background(palette.color(0xFFDEEEFF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.Assignment, null, tint = blue, modifier = Modifier.size(55.dp))
                            Icon(Icons.Outlined.Edit, null, tint = palette.color(0xFFFFBD14), modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp).size(30.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Take Assessment", color = ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text("Discover your strengths, interests\nand get career recommendations.", color = muted, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = palette.color(0xFF004BAF), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp).size(24.dp))
                }
            }
        }
    }
}
