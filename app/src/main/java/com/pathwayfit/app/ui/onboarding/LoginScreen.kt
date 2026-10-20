package com.pathwayfit.app.ui.onboarding

import com.pathwayfit.app.ui.theme.careerfitPalette
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onLogin: (email: String, password: String, (String?) -> Unit) -> Unit,
) {
    val palette = careerfitPalette

    BackHandler(onBack = onBack)
    val ink = palette.color(0xFF080C35)
    val muted = palette.color(0xFF425A80)
    val blue = palette.color(0xFF0064D5)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var recovery by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val submit = {
        if (!busy) {
            focus.clearFocus()
            if (email.isBlank() || password.isBlank()) error = "Enter your email address and password."
            else {
                busy = true
                onLogin(email, password) { result -> busy = false; error = result }
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.background)) {
        RegistrationBackdrop(Modifier.fillMaxSize())
        val compact = maxHeight < 740.dp
        Column(Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 38.dp, end = 18.dp),
            horizontalAlignment = Alignment.End) {
            Text("Your Future\nStarts Here", color = blue, fontSize = 13.sp, lineHeight = 17.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(38.dp).height(3.dp).background(palette.color(0xFFFFC72C)))
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp)
                .padding(top = if (compact) 36.dp else 48.dp, bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RegistrationLogo(if (compact) 120.dp else 132.dp)
            Spacer(Modifier.height(6.dp))
            Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ink,
                fontSize = 14.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(if (compact) 28.dp else 36.dp))
            Text("Welcome Back", color = ink, fontSize = 31.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Login to continue your journey", color = muted, fontSize = 17.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(24.dp))
            RegistrationField(email, { email = it.trim(); error = null }, "Email Address", Icons.Outlined.MailOutline,
                keyboard = KeyboardType.Email, enabled = !busy)
            Spacer(Modifier.height(12.dp))
            RegistrationField(password, { password = it; error = null }, "Password", Icons.Outlined.Lock,
                password = true, enabled = !busy, last = true, onDone = submit)
            TextButton(onClick = { recovery = true }, enabled = !busy,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp), contentPadding = PaddingValues(0.dp)) {
                Text("Forgot Password?", color = blue, fontSize = 15.sp, textDecoration = TextDecoration.Underline)
            }
            error?.let { Text(it, color = palette.color(0xFFB3261E), fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth()) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = submit, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = blue, contentColor = Color.White)) {
                if (busy) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else {
                    Text("Login", fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(20.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                HorizontalDivider(Modifier.width(88.dp), color = palette.color(0xFFBAC8DD))
                Text("OR", Modifier.padding(horizontal = 12.dp), color = muted, fontSize = 15.sp)
                HorizontalDivider(Modifier.width(88.dp), color = palette.color(0xFFBAC8DD))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don’t have an account?", color = ink, fontSize = 15.sp)
                TextButton(onClick = onRegister, enabled = !busy, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Register", color = blue, fontSize = 16.sp, textDecoration = TextDecoration.Underline)
                }
            }
        }
        Text("EXPLORE  •  DISCOVER  •  BELONG", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp,
            lineHeight = 14.sp, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp))
    }
    if (recovery) AlertDialog(onDismissRequest = { recovery = false }, title = { Text("Forgot Password?") },
        text = { Text("Password recovery is not available yet. You can register with another email address to create a new account.") },
        confirmButton = { TextButton(onClick = { recovery = false }) { Text("Close") } })
}
