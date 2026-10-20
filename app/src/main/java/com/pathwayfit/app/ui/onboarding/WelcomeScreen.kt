package com.pathwayfit.app.ui.onboarding

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .2f
    val ink = if (dark) Color(0xFFF6F8FF) else Color(0xFF080E3C)
    val blue = if (dark) Color(0xFF3DA7FF) else Color(0xFF006AFF)
    val muted = if (dark) Color(0xFFAFC7E6) else Color(0xFF606D92)
    val context = LocalContext.current
    val photo = remember(dark) { BitmapFactory.decodeResource(context.resources, if (dark) R.drawable.welcome_dark_reference else R.drawable.welcome_light_reference).asImageBitmap() }
    BoxWithConstraints(Modifier.fillMaxSize().background(if (dark) Color(0xFF07182D) else Color(0xFFF4F8FC))) {
        val compact = maxHeight < 740.dp
        val photoRatio = if (compact) 1.85f else 1.45f
        val gap = if (compact) 12.dp else 20.dp
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawPath(Path().apply { moveTo(w*.63f,0f); lineTo(w,0f); lineTo(w,h*.16f); quadraticTo(w*.70f,h*.13f,w*.63f,0f); close() },
                if(dark) Color(0xFF102E58) else Color(0xFFE7F1FC))
            drawPath(Path().apply { moveTo(0f,h*.89f); cubicTo(w*.32f,h*.83f,w*.57f,h*1.03f,w,h*.87f); lineTo(w,h); lineTo(0f,h); close() },Color(0xFFFFCA36))
            drawPath(Path().apply { moveTo(0f,h*.88f); cubicTo(w*.34f,h*.87f,w*.57f,h*1.04f,w,h*.94f); lineTo(w,h); lineTo(0f,h); close() },Brush.horizontalGradient(listOf(Color(0xFF0056D4),Color(0xFF008AFF))))
            drawPath(Path().apply { moveTo(0f,h*.96f); cubicTo(w*.34f,h*.88f,w*.55f,h*1.05f,w,h*.97f); lineTo(w,h); lineTo(0f,h); close() },if(dark) Color(0xFF0047B7) else Color(0xFF63B8FF))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal=16.dp).padding(top=16.dp,bottom=if(compact) 24.dp else 60.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(end=55.dp),verticalAlignment=Alignment.CenterVertically) {
                Image(androidx.compose.ui.res.painterResource(R.drawable.juafit_emblem),"JuaFit logo",Modifier.size(if(compact) 56.dp else 72.dp))
                Text(buildAnnotatedString { withStyle(SpanStyle(color=if(dark) ink else Color(0xFF003AB6))) { append("Jua") }; withStyle(SpanStyle(color=Color(0xFFFFBE18))) { append("Fit") } },
                    fontSize=if(compact) 32.sp else 38.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(start=8.dp))
            }
            Spacer(Modifier.height(gap))
            Canvas(Modifier.fillMaxWidth().aspectRatio(photoRatio).clip(RoundedCornerShape(28.dp))) {
                val x=if(dark)33 else 30; val y=if(dark)302 else 322
                drawImage(photo,srcOffset=IntOffset(x,y),srcSize=if(dark)IntSize(786,539) else IntSize(792,548),dstSize=IntSize(size.width.toInt(),size.height.toInt()))
            }
            Spacer(Modifier.height(if(compact) 12.dp else if(dark)28.dp else 19.dp))
            if(!dark) {
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) { repeat(3) { Box(Modifier.width(19.dp).height(6.dp).background(if(it==0)blue else Color(0xFFDCE8F6),CircleShape)) } }
                Spacer(Modifier.height(gap))
            }
            Text(buildAnnotatedString { withStyle(SpanStyle(color=ink)) { append("Find the path\nthat ") }; withStyle(SpanStyle(color=blue)) { append("fits you") } },
                fontSize=if(compact) 29.sp else 34.sp,lineHeight=if(compact) 34.sp else 40.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
            Canvas(Modifier.width(125.dp).height(10.dp)) {
                drawPath(Path().apply { moveTo(4f,size.height*.7f); quadraticTo(size.width*.5f,0f,size.width-4f,size.height*.7f) },Color(0xFFFFC528),style=androidx.compose.ui.graphics.drawscope.Stroke(width=3.dp.toPx(),cap=StrokeCap.Round))
            }
            Spacer(Modifier.height(12.dp))
            Text("Discover your strengths and explore your next step,\nfrom CBE pathways to university courses.",color=muted,fontSize=14.sp,lineHeight=21.sp,textAlign=TextAlign.Center)
            Spacer(Modifier.height(if(compact) 16.dp else 32.dp))
            Button(onClick=onGetStarted,modifier=Modifier.fillMaxWidth().height(58.dp),shape=CircleShape,contentPadding=PaddingValues(horizontal=14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF),contentColor=Color.White)) {
                Box(Modifier.fillMaxWidth()) {
                    Text("Get Started",fontSize=20.sp,fontWeight=FontWeight.Bold,modifier=Modifier.align(Alignment.Center))
                    Box(Modifier.size(38.dp).background(Color.White,CircleShape).align(Alignment.CenterEnd),contentAlignment=Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.ArrowForward,null,tint=Color(0xFF006AFF),modifier=Modifier.size(25.dp)) }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick=onLogin) { Text("I already have an account",color=blue,fontSize=15.sp,fontWeight=FontWeight.SemiBold) }
        }
    }
}
