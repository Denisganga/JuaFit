package com.pathwayfit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.ui.theme.PfColors

/** The supplied walkthrough's blue and gold identity, adapted to the app's brand. */
@Composable
fun BrandWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.pathwayfit.app.R.drawable.juafit_emblem),
            contentDescription = "JuaFit emblem", modifier = Modifier.size(44.dp))
        Row {
            Text("Jua", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Fit", color = PfColors.Gold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun BrandHeader(onProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { BrandWordmark() }
        IconButton(onClick = onProfile) { Icon(Icons.Outlined.Person, "Open profile", tint = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
fun BrandBackdrop(modifier: Modifier = Modifier) {
    val blue = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    val gold = PfColors.Gold.copy(alpha = 0.13f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawPath(Path().apply {
            moveTo(w * 0.64f, 0f); lineTo(w, 0f); lineTo(w, h * 0.12f)
            quadraticTo(w * 0.75f, h * 0.08f, w * 0.64f, 0f); close()
        }, gold)
        drawPath(Path().apply {
            moveTo(0f, h * 0.95f); quadraticTo(w * 0.4f, h, w * 0.78f, h)
            lineTo(0f, h); close()
        }, blue)
        drawPath(Path().apply {
            moveTo(w * 0.4f, h); quadraticTo(w * 0.8f, h * 0.98f, w, h * 0.94f)
            lineTo(w, h); close()
        }, gold)
    }
}

@Composable
fun ProfileReminder(completion: Float, onClick: () -> Unit) {
    if (completion >= 1f) return
    PfCard(onClick = onClick, container = MaterialTheme.colorScheme.tertiaryContainer, border = false, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Complete your profile", style = MaterialTheme.typography.titleSmall)
                Text("${(completion * 100).toInt()}% complete. Add details for a more personal match.", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}

@Composable
fun PathwayCard(title: String, description: String, icon: ImageVector, tone: Color, percent: Int? = null, best: Boolean = false, onClick: () -> Unit) {
    PfCard(onClick = onClick, container = tone.copy(alpha = 0.08f), border = false) {
        if (best) {
            Surface(shape = CircleShape, color = PfColors.Gold) {
                Text("Best match", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = PfColors.NavyDeep)
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(tone.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tone, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(5.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, "Explore $title", tint = MaterialTheme.colorScheme.primary)
        }
        if (percent != null) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$percent% Match", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape), color = if (best) PfColors.Success else tone, trackColor = MaterialTheme.colorScheme.surfaceVariant, drawStopIndicator = {})
            }
        }
    }
}

@Composable
fun ProfileSavedDialog(onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        icon = { Icon(Icons.Filled.CheckCircle, null, tint = PfColors.Success, modifier = Modifier.size(48.dp)) },
        title = { Text("Profile Updated!") },
        text = { Text("Your details are saved. Your recommendations will reflect your updated profile. You can change it anytime.") },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue") } },
    )
}
