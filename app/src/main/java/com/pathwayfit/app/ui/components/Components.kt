package com.pathwayfit.app.ui.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathwayfit.app.core.model.JourneyStatus
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PfColors

// ------------------------------------------------------------------ scaffolding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PfScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { BrandWordmark() },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        BrandBackdrop(Modifier.fillMaxSize())
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (title.isNotBlank()) item { Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 6.dp)) }
            content()
        }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun PfCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surface,
    border: Boolean = true,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val base = modifier.fillMaxWidth()
    val colors = CardDefaults.cardColors(containerColor = container)
    val stroke = if (border) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    if (onClick != null) {
        Card(onClick = onClick, modifier = base, shape = shape, colors = colors, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = base, shape = shape, colors = colors, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color? = null) {
    val accent = LocalRoleAccent.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color ?: accent.main, contentColor = accent.onMain),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ------------------------------------------------------------------ hero & tiles

@Composable
fun HeroCard(
    eyebrow: String,
    title: String,
    body: String,
    button: String,
    onClick: () -> Unit,
    icon: ImageVector,
) {
    val accent = LocalRoleAccent.current
    PfCard(container = accent.soft, border = false, contentPadding = 22.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = accent.main)
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent.main, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        PrimaryButton(button, onClick)
    }
}

@Composable
fun TileCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = LocalRoleAccent.current
    PfCard(modifier = modifier, onClick = onClick, contentPadding = 16.dp) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.soft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent.main, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ListRowCard(icon: ImageVector, title: String, subtitle: String, trailing: String? = null, onClick: () -> Unit) {
    val accent = LocalRoleAccent.current
    PfCard(onClick = onClick, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.soft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent.main, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (trailing != null) MatchPill(trailing)
            else Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = accent.main, modifier = Modifier.size(20.dp))
        }
    }
}

// ------------------------------------------------------------------ scores

@Composable
fun MatchPill(text: String, color: Color? = null) {
    val accent = LocalRoleAccent.current
    Surface(shape = CircleShape, color = (color ?: accent.main).copy(alpha = 0.12f)) {
        Text(
            text, color = color ?: accent.main, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun ScoreRing(percent: Int, size: Dp = 84.dp, stroke: Dp = 8.dp, label: String = "match") {
    val accent = LocalRoleAccent.current
    val track = MaterialTheme.colorScheme.surfaceVariant
    val anim = remember { Animatable(0f) }
    LaunchedEffect(percent) { anim.animateTo(percent / 100f, tween(900, easing = FastOutSlowInEasing)) }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = Stroke(stroke.toPx(), cap = StrokeCap.Round)
            drawArc(track, -90f, 360f, false, style = s)
            drawArc(accent.main, -90f, 360f * anim.value, false, style = s)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(anim.value * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FactorBar(label: String, score: Float, note: String? = null) {
    val accent = LocalRoleAccent.current
    val progress by animateFloatAsState(score.coerceIn(0f, 1f), tween(800), label = "factor")
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text("${(score * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, color = accent.main, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = accent.main,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {},
        )
        if (!note.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CheckLine(text: String) {
    val accent = LocalRoleAccent.current
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.Check, null, tint = accent.main, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

// ------------------------------------------------------------------ chain / journey

data class ChainNode(val title: String, val value: String?, val status: JourneyStatus = JourneyStatus.DONE, val hint: String = "", val onClick: (() -> Unit)? = null)

/** Vertical step-by-step flow used for Pathway→Track→Area, journeys and career maps. */
@Composable
fun ChainFlow(nodes: List<ChainNode>) {
    val accent = LocalRoleAccent.current
    Column {
        nodes.forEachIndexed { i, n ->
            val dotColor = when (n.status) {
                JourneyStatus.DONE -> accent.main
                JourneyStatus.CURRENT -> PfColors.GoldBright
                JourneyStatus.UPCOMING -> MaterialTheme.colorScheme.outline
            }
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
                    Box(
                        Modifier.size(if (n.status == JourneyStatus.CURRENT) 20.dp else 16.dp).clip(CircleShape)
                            .background(if (n.status == JourneyStatus.UPCOMING) Color.Transparent else dotColor)
                            .border(2.dp, dotColor, CircleShape),
                    )
                    if (i < nodes.lastIndex) Box(Modifier.width(2.dp).height(58.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .then(n.onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
                        .padding(bottom = 14.dp, end = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(n.title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (n.status == JourneyStatus.CURRENT) {
                            Spacer(Modifier.width(8.dp)); MatchPill("You are here", PfColors.Gold)
                        }
                    }
                    Text(
                        n.value ?: "Not yet decided",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (n.value == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    if (n.hint.isNotBlank()) Text(n.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ chips

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipSelector(
    options: List<Pair<String, String>>,
    selected: Collection<String>,
    onToggle: (String) -> Unit,
    max: Int = Int.MAX_VALUE,
) {
    val accent = LocalRoleAccent.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { (id, label) ->
            val isOn = id in selected
            FilterChip(
                selected = isOn,
                onClick = { if (isOn || selected.size < max) onToggle(id) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accent.soft, selectedLabelColor = accent.main,
                ),
            )
        }
    }
}

// ------------------------------------------------------------------ notices

@Composable
fun DisclaimerCard(text: String, title: String = "Guidance, not a guarantee") {
    PfCard(container = MaterialTheme.colorScheme.surfaceVariant, border = false, contentPadding = 14.dp) {
        Row {
            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun VerifyNotice(text: String = VERIFY_TEXT) {
    PfCard(container = PfColors.GoldSoft.copy(alpha = if (MaterialTheme.colorScheme.background.luminanceIsDark()) 0.12f else 1f), border = false, contentPadding = 14.dp) {
        Row {
            Icon(Icons.Outlined.WarningAmber, null, tint = PfColors.Warning, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

const val VERIFY_TEXT = "Verify current requirements and programme availability with the relevant official institution."

private fun Color.luminanceIsDark(): Boolean = (0.299 * red + 0.587 * green + 0.114 * blue) < 0.5

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, button: String? = null, onClick: () -> Unit = {}) {
    val accent = LocalRoleAccent.current
    PfCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent.main)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (button != null) {
                Spacer(Modifier.height(16.dp))
                PrimaryButton(button, onClick)
            }
        }
    }
}

// ------------------------------------------------------------------ premium visuals

@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(6.dp), color = PfColors.NavyDeep) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(PfColors.GoldBright))
            Spacer(Modifier.width(5.dp))
            Text("PREMIUM", color = PfColors.GoldBright, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        }
    }
}

/**
 * A locked preview: a title the learner can read, teaser rows where null entries render as
 * redacted bars (so nothing is actually leaked on devices without blur), and a CTA.
 */
@Composable
fun LockedPreview(
    title: String,
    subtitle: String,
    teaser: List<String?>,
    cta: String,
    onClick: () -> Unit,
) {
    PfCard(container = MaterialTheme.colorScheme.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            PremiumBadge()
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Box {
            Column(
                Modifier.fillMaxWidth().then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(5.dp) else Modifier),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                teaser.forEach { line ->
                    if (line == null) {
                        Box(Modifier.fillMaxWidth(0.85f).height(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    } else {
                        Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box(
                Modifier.align(Alignment.Center).size(40.dp).clip(CircleShape).background(PfColors.NavyDeep),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Lock, "Locked", tint = PfColors.GoldBright, modifier = Modifier.size(18.dp)) }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PfColors.NavyDeep, contentColor = PfColors.GoldBright),
        ) { Text(cta, style = MaterialTheme.typography.labelLarge) }
    }
}

/** Shown for any Premium CTA while payment is disabled. Never a fake payment screen. */
@Composable
fun PremiumComingSoonDialog(notifyMe: Boolean, onNotify: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Lock, null, tint = PfColors.Gold) },
        title = { Text("Premium Coming Soon") },
        text = {
            Column {
                Text("We're preparing a better way to help you plan your future.")
                if (notifyMe) {
                    Spacer(Modifier.height(10.dp))
                    Text("✓ We'll let you know when Premium is ready.", color = PfColors.Success, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (!notifyMe) TextButton(onClick = onNotify) { Text("Notify Me") }
            else TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = { if (!notifyMe) TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}

@Composable
fun Pad(h: Dp) = Spacer(Modifier.height(h))
