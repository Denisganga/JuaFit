package com.pathwayfit.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.ui.components.BrandWordmark
import com.pathwayfit.app.ui.components.BrandBackdrop
import com.pathwayfit.app.ui.components.CheckLine
import com.pathwayfit.app.ui.components.PrimaryButton
import com.pathwayfit.app.ui.theme.PfColors
import kotlinx.coroutines.delay

/** The PATHWAYFIT mark: a path rising through three waypoints. */
@Composable
fun PathwayMark(size: Dp, progress: Float = 1f) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val p = Path().apply {
            moveTo(w * 0.18f, h * 0.78f)
            cubicTo(w * 0.40f, h * 0.78f, w * 0.38f, h * 0.50f, w * 0.50f, h * 0.50f)
            cubicTo(w * 0.62f, h * 0.50f, w * 0.60f, h * 0.22f, w * 0.82f, h * 0.22f)
        }
        drawPath(p, Color.White.copy(alpha = 0.9f * progress), style = Stroke(w * 0.06f, cap = StrokeCap.Round))
        drawCircle(PfColors.CbeTealDark, w * 0.07f, Offset(w * 0.18f, h * 0.78f))
        if (progress > 0.4f) drawCircle(Color.White, w * 0.07f, Offset(w * 0.50f, h * 0.50f))
        if (progress > 0.8f) drawCircle(PfColors.GoldBright, w * 0.085f, Offset(w * 0.82f, h * 0.22f))
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(1100))
        delay(350)
        onFinished()
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PfColors.NavyDeep, PfColors.Navy))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.pathwayfit.app.R.drawable.juafit_emblem),
                contentDescription = "JuaFit emblem", modifier = Modifier.size(140.dp))
            Spacer(Modifier.height(20.dp))
            Text("JuaFit", color = Color.White, style = MaterialTheme.typography.headlineMedium, letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing)
            Spacer(Modifier.height(6.dp))
            Text("Your Path. Your Course. Your Future.", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WelcomePoint(text: String) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(PfColors.GoldBright))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AuthFrame(title: String, subtitle: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandBackdrop(Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(top = 4.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) { BrandWordmark() }
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PfField(value: String, onChange: (String) -> Unit, label: String, password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), shape = RoundedCornerShape(14.dp),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        trailingIcon = if (password) {
            { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Toggle password") } }
        } else null,
    )
}

@Composable
fun ChooseSystemScreen(name: String, onChoose: (EducationSystem) -> Unit) {
    var comingSoon by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        RegistrationBackdrop(Modifier.fillMaxSize())
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = { Text("Choose Your Education Path") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select CBE or 8-4-4 to continue.")
                Button(onClick = { comingSoon = true }, modifier = Modifier.fillMaxWidth()) { Text("CBE") }
                Button(onClick = { onChoose(EducationSystem.EIGHT_FOUR_FOUR) }, modifier = Modifier.fillMaxWidth()) { Text("8-4-4") }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
    )
    if (comingSoon) androidx.compose.material3.AlertDialog(
        onDismissRequest = { comingSoon = false },
        title = { Text("Coming soon") },
        text = { Text("The CBE experience is coming soon.") },
        confirmButton = { TextButton(onClick = { comingSoon = false }) { Text("OK") } },
    )
}

@Composable
private fun SystemCard(icon: ImageVector, color: Color, title: String, tag: String, body: String, button: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(22.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(color), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(tag, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
            ) { Text(button, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) }
        }
    }
}
