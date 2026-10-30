package com.pathwayfit.app.ui.onboarding

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.pathwayfit.app.R

private val RegisterInk
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val RegisterMuted
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val RegisterBlue = Color(0xFF0064D5)

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onRegister: (name: String, email: String, password: String, consentAccepted: Boolean, (String?) -> Unit) -> Unit,
) {
    val palette = careerfitPalette

    BackHandler(onBack = onBack)
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var consentAccepted by rememberSaveable { mutableStateOf(false) }
    var showConsent by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val submit = {
        if (!busy) {
            if (!consentAccepted) error = "Please read and agree to the data use and consent notice."
            else if (password != confirmPassword) error = "Passwords do not match"
            else {
                focus.clearFocus()
                busy = true
                onRegister(name, email, password, consentAccepted) { result -> busy = false; error = result }
            }
        }
    }
    if (showConsent) AlertDialog(
        onDismissRequest = { showConsent = false },
        title = { Text("Data use and consent") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text("Notice version " + com.pathwayfit.app.core.model.DataConsent.VERSION)
                com.pathwayfit.app.core.model.DataConsent.sections.forEach { (title, body) ->
                    Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                    Text(body, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = { showConsent = false }) { Text("Back to registration") } }
    )
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.background)) {
        RegistrationBackdrop(Modifier.fillMaxSize())
        val compact = maxHeight < 740.dp
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp)
                .padding(top = if (compact) 14.dp else 34.dp, bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RegistrationLogo(if (compact) 112.dp else 132.dp)
            Spacer(Modifier.height(6.dp))
            Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = RegisterInk,
                fontSize = 14.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(if (compact) 24.dp else 32.dp))
            Text("Create Your Account", color = RegisterInk, fontWeight = FontWeight.Bold,
                fontSize = if (compact) 24.sp else 26.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Start your journey to a brighter future", color = RegisterMuted,
                fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            RegistrationField(name, { name = it; error = null }, "Full Name", Icons.Outlined.PersonOutline, enabled = !busy)
            Spacer(Modifier.height(12.dp))
            RegistrationField(email, { email = it.trim(); error = null }, "Email Address", Icons.Outlined.MailOutline, keyboard = KeyboardType.Email, enabled = !busy)
            Spacer(Modifier.height(12.dp))
            RegistrationField(password, { password = it; error = null }, "Password", Icons.Outlined.Lock, password = true, enabled = !busy)
            Spacer(Modifier.height(12.dp))
            RegistrationField(confirmPassword, { confirmPassword = it; error = null }, "Confirm Password", Icons.Outlined.Lock, password = true, enabled = !busy, last = true, onDone = submit)
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                Checkbox(checked = consentAccepted, onCheckedChange = { consentAccepted = it; error = null }, enabled = !busy,
                    modifier = Modifier.semantics { contentDescription = "I agree to the data use described in the consent notice" })
                Column(Modifier.weight(1f).padding(top = 10.dp)) {
                    Text("I have read and agree to the data use described in the consent notice.", color = RegisterInk, fontSize = 14.sp)
                    TextButton(onClick = { showConsent = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("Read data use and consent", textDecoration = TextDecoration.Underline)
                    }
                }
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = palette.color(0xFFB3261E), fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = submit, enabled = !busy && consentAccepted, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(13.dp), contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RegisterBlue, contentColor = Color.White)) {
                if (busy) { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
                Text("Register", fontSize = 19.sp, fontWeight = FontWeight.Medium)
            }
            Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                HorizontalDivider(Modifier.width(88.dp), color = palette.color(0xFFBAC8DD))
                Text("OR", Modifier.padding(horizontal = 16.dp), color = RegisterMuted, fontSize = 15.sp)
                HorizontalDivider(Modifier.width(88.dp), color = palette.color(0xFFBAC8DD))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = RegisterInk, fontSize = 15.sp)
                TextButton(onClick = onLogin, enabled = !busy, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Login", color = RegisterBlue, textDecoration = TextDecoration.Underline, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Text("EXPLORE  •  DISCOVER  •  BELONG", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp,
            lineHeight = 14.sp, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp))
    }
}

@Composable
internal fun RegistrationField(
    value: String, onChange: (String) -> Unit, placeholder: String, icon: ImageVector,
    password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text, enabled: Boolean,
    last: Boolean = false, onDone: () -> Unit = {},
) {
    val palette = careerfitPalette

    var visible by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Surface(Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
        color = palette.surface.copy(alpha = 0.92f), border = BorderStroke(1.dp, palette.color(0xFFD8E1EF))) {
        BasicTextField(value, onValueChange = onChange, enabled = enabled, singleLine = true,
            textStyle = TextStyle(color = RegisterInk, fontSize = 16.sp),
            cursorBrush = SolidColor(RegisterBlue),
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard, imeAction = if (last) ImeAction.Done else ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }, onDone = { onDone() }),
            modifier = Modifier.fillMaxSize().semantics { contentDescription = placeholder },
            decorationBox = { input ->
                Row(Modifier.fillMaxSize().padding(start = 16.dp, end = if (password) 0.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = palette.color(0xFF4A5B79), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(placeholder, color = RegisterMuted, fontSize = 16.sp)
                        input()
                    }
                    if (password) IconButton(onClick = { visible = !visible }, enabled = enabled) {
                        Icon(if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            if (visible) "Hide $placeholder" else "Show $placeholder", tint = palette.color(0xFF4A5B79), modifier = Modifier.size(20.dp))
                    }
                }
            },
        )
    }
}

@Composable
internal fun RegistrationLogo(emblemHeight: Dp) {
    val palette = careerfitPalette

    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(R.drawable.juafit_logo),
        contentDescription = "JuaFit logo",
        modifier = Modifier.size(emblemHeight + 38.dp),
    )
}

@Composable
internal fun RegistrationBackdrop(modifier: Modifier) {
    val palette = careerfitPalette

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.linearGradient(listOf(palette.background, palette.background, palette.background)))
        drawPath(Path().apply {
            moveTo(0f, 0f); lineTo(w * 0.46f, 0f)
            cubicTo(w * 0.37f, h * 0.10f, w * 0.22f, h * 0.16f, 0f, h * 0.20f); close()
        }, Brush.linearGradient(listOf(palette.color(0xFF50A7FC), palette.color(0xFFB5EAFF))))
        drawPath(Path().apply {
            moveTo(w * 0.72f, 0f); lineTo(w, 0f); lineTo(w, h * 0.16f)
            quadraticTo(w * 0.77f, h * 0.10f, w * 0.72f, 0f); close()
        }, Brush.linearGradient(listOf(palette.color(0xFFFFD338), palette.color(0xFFFFE994))))
        drawPath(Path().apply {
            moveTo(0f, h * 0.87f); quadraticTo(w * 0.28f, h * 0.99f, w * 0.58f, h * 0.94f)
            lineTo(w, h); lineTo(0f, h); close()
        }, palette.color(0xFFFFD248))
        drawPath(Path().apply {
            moveTo(0f, h * 0.915f); cubicTo(w * 0.30f, h * 0.995f, w * 0.65f, h * 0.925f, w, h * 0.855f)
            lineTo(w, h); lineTo(0f, h); close()
        }, Brush.horizontalGradient(listOf(palette.color(0xFF0051AF), palette.color(0xFF2B96F2))))
    }
}
