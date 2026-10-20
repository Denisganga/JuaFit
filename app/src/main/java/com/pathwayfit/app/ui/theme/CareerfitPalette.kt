package com.pathwayfit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Converts reference artwork colors into readable colors for either app theme. */
class CareerfitPalette(private val scheme: ColorScheme) {
    val background get() = scheme.background
    val surface get() = scheme.surface
    fun color(value: Long): Color {
        val original = Color(value)
        val dark = scheme.background.luminance() < .2f
        val lightness = original.luminance()
        return when {
            lightness > .65f -> if (dark) scheme.surfaceVariant else if (value == 0xFFFFFFFFL) scheme.background else original.copy(alpha = .85f)
            lightness < .055f -> scheme.onSurface
            value in setOf(0xFF637494L,0xFF425A80L,0xFF405D8EL,0xFF36568CL,0xFF3D5781L,0xFF425E8BL,0xFF496899L,0xFF52658CL,0xFF526CA4L,0xFF63749DL,0xFF4A5B79L,0xFF384675L,0xFF7387B2L,0xFF4D6594L) -> scheme.onSurfaceVariant
            else -> original
        }
    }
}

val careerfitPalette: CareerfitPalette
    @Composable get() = CareerfitPalette(MaterialTheme.colorScheme)
