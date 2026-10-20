package com.pathwayfit.app.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.core.model.TagCatalog
import com.pathwayfit.app.premium.ComparisonRow
import com.pathwayfit.app.premium.PremiumAccess
import com.pathwayfit.app.premium.PremiumCareerMap
import com.pathwayfit.app.premium.PremiumFeature
import com.pathwayfit.app.premium.YearPlan
import com.pathwayfit.app.ui.components.PfCard
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PfColors

/** Premium hooks available to every screen. */
class PremiumUi(
    val access: PremiumAccess,
    /** Called by locked feature CTAs — shows "Premium Coming Soon" while payment is disabled. */
    val requestUnlock: (PremiumFeature) -> Unit,
    val openPremium: () -> Unit,
    val openPreview: () -> Unit,
) {
    fun has(feature: PremiumFeature) = access.has(feature)
}

val LocalPremiumUi = compositionLocalOf { PremiumUi(PremiumAccess(), {}, {}, {}) }

// ------------------------------------------------------------- county pickers

@Composable
fun CountyPickerField(label: String, value: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { Icon(Icons.Outlined.LocationOn, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        )
        // Transparent overlay captures taps on the read-only field.
        Box(Modifier.matchParentSize().clickable { open = true })
    }
    if (open) CountyDialog(onDismiss = { open = false }) { onSelect(it); open = false }
}

@Composable
fun CountyDialog(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val list = TagCatalog.kenyanCounties.filter { it.contains(query, ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose county") },
        text = {
            Column {
                OutlinedTextField(query, { query = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(list) { c ->
                        Text(c, Modifier.fillMaxWidth().clickable { onPick(c) }.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyLarge)
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

// ------------------------------------------------------------- career map

/** Signature premium visual: tappable stages from "You" to careers. */
@Composable
fun CareerMapView(map: PremiumCareerMap) {
    val accent = LocalRoleAccent.current
    var openIndex by rememberSaveable { mutableStateOf(-1) }
    Column {
        map.stages.forEachIndexed { i, stage ->
            val open = openIndex == i
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(if (stage.isFuture) PfColors.GoldSoft else accent.main),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${i + 1}", color = if (stage.isFuture) PfColors.Gold else accent.onMain, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    if (i < map.stages.lastIndex) Box(Modifier.width(2.dp).height(if (open) 140.dp else 56.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { openIndex = if (open) -1 else i }.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stage.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        stage.items.take(if (open) 20 else 2).joinToString(" · ").ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium, color = accent.main,
                    )
                    AnimatedVisibility(open, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(Modifier.padding(top = 6.dp)) {
                            Text(stage.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (stage.isFuture) Text("Future possibility", style = MaterialTheme.typography.labelSmall, color = PfColors.Gold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------- comparison table

@Composable
fun ComparisonTable(headerA: String, headerB: String, rows: List<ComparisonRow>) {
    val accent = LocalRoleAccent.current
    PfCard(contentPadding = 0.dp) {
        Row(Modifier.fillMaxWidth().background(accent.soft).padding(12.dp)) {
            Spacer(Modifier.weight(1f))
            Text(headerA, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = accent.main)
            Text(headerB, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = accent.main)
        }
        rows.forEachIndexed { i, r ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(r.label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(r.a, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(r.b, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            if (i < rows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

// ------------------------------------------------------------- year plan

@Composable
fun YearPlanView(plans: List<YearPlan>) {
    val accent = LocalRoleAccent.current
    plans.forEach { plan ->
        PfCard {
            Text(plan.title, style = MaterialTheme.typography.titleMedium, color = accent.main)
            Spacer(Modifier.height(8.dp))
            plan.items.forEach {
                Row(Modifier.padding(vertical = 4.dp)) {
                    Box(Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape).background(accent.main))
                    Spacer(Modifier.width(10.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
