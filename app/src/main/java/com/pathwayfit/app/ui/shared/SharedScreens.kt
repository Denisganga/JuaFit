package com.pathwayfit.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.BuildConfig
import com.pathwayfit.app.core.model.AssessmentRecord
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.SavedRecommendation
import com.pathwayfit.app.core.model.User
import com.pathwayfit.app.premium.PremiumFeature
import com.pathwayfit.app.ui.components.DisclaimerCard
import com.pathwayfit.app.ui.components.PfCard
import com.pathwayfit.app.ui.components.PfScreen
import com.pathwayfit.app.ui.components.PremiumBadge
import com.pathwayfit.app.ui.components.SectionTitle
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.theme.PfColors
import java.text.DateFormat
import java.util.Date

// ================================================================== PROFILE & SETTINGS (shared, role-aware)

@Composable
fun SettingsScreen(
    user: User?,
    profileCompletion: Float,
    history: List<AssessmentRecord>,
    saved: List<SavedRecommendation>,
    reminders: Boolean,
    devToolsVisible: Boolean,
    onBack: (() -> Unit)?,
    onEditProfile: () -> Unit,
    onOpenSaved: (SavedRecommendation) -> Unit,
    onReminders: (Boolean) -> Unit,
    onPremium: () -> Unit,
    onHelp: () -> Unit,
    onDeveloper: () -> Unit,
    onUpdateName: (String) -> Unit,
    onSwitchSystem: () -> Unit,
    onLogout: () -> Unit,
) {
    val accent = LocalRoleAccent.current
    val system = user?.educationSystem
    var editName by remember { mutableStateOf(false) }
    var confirmSwitch by remember { mutableStateOf(false) }
    var versionTaps by remember { mutableIntStateOf(0) }
    val df = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    PfScreen(title = "Profile & Settings", onBack = onBack) {
        item {
            PfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
                        Text(user?.firstName?.take(1)?.uppercase() ?: "?", style = MaterialTheme.typography.headlineSmall, color = accent.main)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.name ?: "", style = MaterialTheme.typography.titleLarge)
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${system?.label ?: "No education path"} · ${user?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}", style = MaterialTheme.typography.labelMedium, color = accent.main)
                    }
                    IconButton(onClick = { editName = true }) { Icon(Icons.Outlined.Edit, "Edit name") }
                }
            }
        }
        item {
            SettingRow(
                Icons.Outlined.Edit,
                if (system == EducationSystem.CBE) "My CBE Profile" else "Academic Profile & KCSE",
                "${(profileCompletion * 100).toInt()}% complete", onClick = onEditProfile,
            )
        }
        item { SettingRow(Icons.Outlined.WorkspacePremium, "JuaFit Premium", "Deeper personalised guidance", trailing = { PremiumBadge() }, onClick = onPremium) }

        item { SectionTitle("Saved recommendations") }
        if (saved.isEmpty()) item { Text("Tap the bookmark on any recommendation to save it here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(saved) { s -> SettingRow(Icons.Outlined.Bookmark, s.title, "${s.score}% match", onClick = { onOpenSaved(s) }) }

        item { SectionTitle("Assessment history") }
        val mine = history.filter { it.system == system }
        if (mine.isEmpty()) item { Text("No assessments yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(mine.take(5)) { h -> SettingRow(Icons.Outlined.History, h.topLabel, "${df.format(Date(h.completedAt))} · top match ${h.topScore}%", onClick = null) }

        item { SectionTitle("Preferences") }
        item {
            SettingRow(Icons.Outlined.Notifications, "Study reminders", "Gentle nudges to keep your journey moving", trailing = {
                Switch(checked = reminders, onCheckedChange = onReminders)
            }, onClick = { onReminders(!reminders) })
        }
        item { SettingRow(Icons.Outlined.SwapHoriz, "Change education path", "Switch between CBE and 8-4-4", onClick = { confirmSwitch = true }) }
        item { SettingRow(Icons.AutoMirrored.Outlined.HelpOutline, "Help & disclaimer", "How JuaFit works", onClick = onHelp) }
        if (devToolsVisible && versionTaps >= 7) {
            item { SettingRow(Icons.Outlined.Code, "Developer settings", "Debug builds only", onClick = onDeveloper) }
        }
        item { SettingRow(Icons.AutoMirrored.Outlined.Logout, "Log out", "", onClick = onLogout) }
        item {
            Text(
                "JuaFit ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().clickable { versionTaps++ }.padding(vertical = 12.dp),
            )
        }
    }

    if (editName) {
        var name by remember { mutableStateOf(user?.name ?: "") }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text("Your name") },
            text = { OutlinedTextField(name, { name = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { onUpdateName(name); editName = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancel") } },
        )
    }
    if (confirmSwitch) {
        AlertDialog(
            onDismissRequest = { confirmSwitch = false },
            title = { Text("Change education path?") },
            text = { Text("Your ${system?.shortLabel ?: ""} assessment results will be cleared so the two experiences don't mix. Your profile details are kept.") },
            confirmButton = { TextButton(onClick = { confirmSwitch = false; onSwitchSystem() }) { Text("Change path") } },
            dismissButton = { TextButton(onClick = { confirmSwitch = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, trailing: @Composable () -> Unit = {}, onClick: (() -> Unit)?) {
    PfCard(onClick = onClick, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}

// ================================================================== PREMIUM PREVIEW

@Composable
fun PremiumPreviewScreen(system: EducationSystem?, freeTitle: String?, freePercent: Int?, onBack: () -> Unit, onSeePremium: () -> Unit) {
    val premiumItems = buildList {
        add("Your Top 5 Matches" to "#2 ●●●●●●  ·  #3 ●●●●●")
        add("Detailed \"Why You Fit\" Analysis" to "Interest ●●% · Strength ●●%")
        add("Alternative Career Paths" to "●●●●●●, ●●●●●●●")
        if (system == EducationSystem.EIGHT_FOUR_FOUR) {
            add("Course-to-Career Map" to "Career → ●●●●● → ●●●●●")
            add("University Shortlist" to "●●●●●● University, ●●●●● University")
        } else {
            add("Complete CBE Pathway Map" to "Track → Area → ●●●●●●")
            add("What If? Pathway Comparison" to "Pathway A vs Pathway B")
        }
        add("Subject Improvement Guidance" to "Focus on ●●●●●●")
        add("Personalised Action Plan" to "Year 1 · Year 2 · Year 3")
        add("What To Do Next" to "●●●●●●●●●●")
    }
    PfScreen(title = "", onBack = onBack) {
        item {
            Column {
                Text("Your Future Has More Than One Possibility.", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text("We found several directions that could fit you.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PfCard {
                Text("FREE — YOURS ALREADY", style = MaterialTheme.typography.labelSmall, color = PfColors.Success)
                Spacer(Modifier.height(8.dp))
                Text(freeTitle ?: "Top recommendation", style = MaterialTheme.typography.titleLarge)
                freePercent?.let { Text("$it% match · basic match score", style = MaterialTheme.typography.bodyMedium, color = LocalRoleAccent.current.main) }
                Text("Short explanation and basic career direction", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PfCard(container = PfColors.NavyDeep, border = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("PREMIUM", style = MaterialTheme.typography.labelSmall, color = PfColors.GoldBright, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Lock, null, tint = PfColors.GoldBright, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(8.dp))
                premiumItems.forEach { (title, peek) ->
                    Column(Modifier.padding(vertical = 7.dp)) {
                        Text("🔒 $title", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        Text(peek, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.55f))
                    }
                }
            }
        }
        item {
            Button(
                onClick = onSeePremium, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PfColors.Gold, contentColor = PfColors.NavyDeep),
            ) { Text("See JuaFit Premium", fontWeight = FontWeight.Bold) }
        }
        item { Text("Your free recommendation stays yours either way.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

// ================================================================== PREMIUM

@Composable
fun PremiumScreen(system: EducationSystem?, notifyMe: Boolean, onNotify: () -> Unit, onBack: () -> Unit) {
    val features = PremiumFeature.entries.filter { it.appliesTo(system) }
    LazyColumn(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PfColors.NavyDeep, PfColors.Navy))).statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } }
        item {
            Column {
                Text("JuaFit PREMIUM", style = MaterialTheme.typography.labelSmall, color = PfColors.GoldBright)
                Spacer(Modifier.height(10.dp))
                Text("Don't Just Choose a Course.\nUnderstand Your Path.", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(10.dp))
                Text("Get deeper personalised guidance from your strengths to your future career.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            }
        }
        items(features) { f ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.06f)).padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(PfColors.Gold), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = PfColors.NavyDeep, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(f.title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(f.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.08f)).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Premium Coming Soon", style = MaterialTheme.typography.titleLarge, color = PfColors.GoldBright)
                Spacer(Modifier.height(6.dp))
                Text("We're preparing a better way to help you plan your future.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onNotify, enabled = !notifyMe, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PfColors.Gold, contentColor = PfColors.NavyDeep,
                        disabledContainerColor = Color.White.copy(alpha = 0.15f), disabledContentColor = Color.White),
                ) { Text(if (notifyMe) "✓ We'll notify you" else "Notify Me", fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Text(
                "Premium gives deeper guidance and analysis. It does not guarantee university admission, KUCCPS placement, a specific career or job, or better grades.",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

// ================================================================== DEVELOPER SETTINGS (debug only)

@Composable
fun DeveloperSettingsScreen(premiumPreview: Boolean, onTogglePreview: (Boolean) -> Unit, onBack: () -> Unit) {
    PfScreen(title = "Developer settings", onBack = onBack) {
        item { DisclaimerCard("Visible only in debug builds. Not shown to normal users.", title = "Developer only") }
        item {
            PfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Enable Premium Preview", style = MaterialTheme.typography.titleSmall)
                        Text("Simulates Premium access for testing. Subscription status stays FREE.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = premiumPreview, onCheckedChange = onTogglePreview)
                }
            }
        }
        item {
            PfCard {
                Text("Payment", style = MaterialTheme.typography.titleSmall)
                Text("paymentEnabled = ${BuildConfig.PAYMENT_ENABLED}. No payment provider is connected.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ================================================================== HELP

@Composable
fun HelpScreen(system: EducationSystem?, onBack: () -> Unit) {
    PfScreen(title = "Help & disclaimer", onBack = onBack) {
        item { SectionTitle("How JuaFit works") }
        item {
            PfCard {
                if (system == EducationSystem.CBE) {
                    Text("1. Complete your CBE profile.\n2. Take the CBE Pathway Assessment (15 questions).\n3. See your recommended pathway, track and area, with reasons.\n4. Explore pathways and follow your journey.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("1. Complete your academic profile and enter your KCSE grades.\n2. Take the 8-4-4 Career Assessment (15 questions).\n3. See courses ranked by JuaFit Match Score, with reasons.\n4. Explore universities and careers.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { SectionTitle("How match scores are calculated") }
        item {
            PfCard {
                Text(
                    if (system == EducationSystem.CBE)
                        "Interest 30% · Strength 25% · Learning preference 20% · Academic preference 15% · Career interest 10%. Factors without data are left out and the rest re-weighted."
                    else
                        "Academic 35% · Subject 25% · Interest 15% · Strength 10% · Career 10% · Preferences 5%. The academic factor is a JuaFit estimate — not a KUCCPS cluster calculation.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            DisclaimerCard(
                "JuaFit provides educational guidance only. It does not guarantee admission, KUCCPS placement, a career or a job. " +
                    "Always confirm pathway, programme and admission requirements with your school, KUCCPS, KICD or the relevant institution.",
                title = "Educational disclaimer",
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("Your data is stored on this device in this version.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
