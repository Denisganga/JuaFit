package com.pathwayfit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pathwayfit.app.core.model.EducationSystem

// ---- Brand palette -------------------------------------------------------
object PfColors {
    val Navy = Color(0xFF101C70)
    val NavyDeep = Color(0xFF0C154E)
    val Ink = Color(0xFF14215F)
    val Mist = Color(0xFFF1F8FF)

    // Shared blue identity from the supplied walkthrough.
    val CbeTeal = Color(0xFF006BFF)
    val CbeTealSoft = Color(0xFFE7F4FF)
    val CbeTealDark = Color(0xFF8AC4FF)

    // Keep role tokens separate while sharing the visual identity.
    val EfeIndigo = Color(0xFF006BFF)
    val EfeIndigoSoft = Color(0xFFEAF3FF)
    val EfeIndigoDark = Color(0xFF9DB1F2)

    // Premium identity: gold on deep blue
    val Gold = Color(0xFFFFB800)
    val GoldSoft = Color(0xFFFFF7DA)
    val GoldBright = Color(0xFFFFCD38)

    val Success = Color(0xFF2E8B57)
    val Warning = Color(0xFFB7791F)
}

/** Accent colours that change with the learner's education system. */
data class RoleAccent(val main: Color, val soft: Color, val onMain: Color = Color.White)

val LocalRoleAccent = staticCompositionLocalOf { RoleAccent(PfColors.CbeTeal, PfColors.CbeTealSoft) }

fun accentFor(system: EducationSystem?, dark: Boolean): RoleAccent = when (system) {
    EducationSystem.CBE -> if (dark) RoleAccent(PfColors.CbeTealDark, Color(0xFF12322E), Color(0xFF00201C))
    else RoleAccent(PfColors.CbeTeal, PfColors.CbeTealSoft)
    EducationSystem.EIGHT_FOUR_FOUR -> if (dark) RoleAccent(PfColors.EfeIndigoDark, Color(0xFF1B2547), Color(0xFF0A1440))
    else RoleAccent(PfColors.EfeIndigo, PfColors.EfeIndigoSoft)
    null -> if (dark) RoleAccent(Color(0xFFB8CBE6), Color(0xFF1C2A3D), PfColors.NavyDeep)
    else RoleAccent(PfColors.CbeTeal, PfColors.CbeTealSoft)
}

private val LightScheme = lightColorScheme(
    primary = PfColors.CbeTeal,
    onPrimary = Color.White,
    primaryContainer = PfColors.CbeTealSoft,
    onPrimaryContainer = PfColors.NavyDeep,
    secondary = PfColors.CbeTeal,
    tertiary = PfColors.Gold,
    tertiaryContainer = PfColors.GoldSoft,
    onTertiaryContainer = PfColors.NavyDeep,
    background = Color(0xFFEAF0F5),
    onBackground = PfColors.Ink,
    surface = Color(0xFFF1F5F8),
    onSurface = PfColors.Ink,
    surfaceVariant = Color(0xFFF0F6FF),
    onSurfaceVariant = Color(0xFF53638D),
    outline = Color(0xFFADC8EF),
    outlineVariant = Color(0xFFDCEBFF),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB8CBE6),
    onPrimary = PfColors.NavyDeep,
    primaryContainer = Color(0xFF233F63),
    onPrimaryContainer = Color(0xFFD6E3F5),
    secondary = PfColors.CbeTealDark,
    tertiary = PfColors.GoldBright,
    background = Color(0xFF0C1522),
    onBackground = Color(0xFFE3E8EF),
    surface = Color(0xFF131E2D),
    onSurface = Color(0xFFE3E8EF),
    surfaceVariant = Color(0xFF1C2838),
    onSurfaceVariant = Color(0xFFAAB5C4),
    outline = Color(0xFF3A4759),
    outlineVariant = Color(0xFF263345),
)

private val PfTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp),
)

private val PfShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun PathwayFitTheme(
    system: EducationSystem? = null,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val accent = accentFor(system, darkTheme)
    CompositionLocalProvider(LocalRoleAccent provides accent) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = PfTypography,
            shapes = PfShapes,
            content = content,
        )
    }
}
