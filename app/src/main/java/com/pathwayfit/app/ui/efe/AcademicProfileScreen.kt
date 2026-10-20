package com.pathwayfit.app.ui.efe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.core.model.*
import com.pathwayfit.app.ui.components.PfScreen
import com.pathwayfit.app.ui.components.PrimaryButton
import com.pathwayfit.app.ui.shared.CountyPickerField
import com.pathwayfit.app.ui.shared.CountyDialog
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel

@Composable
fun EfeProfileScreen(vm: EightFourFourViewModel, setup: Boolean, onBack: (() -> Unit)?, onEditGrades: () -> Unit, onDone: () -> Unit) {
    val saved by vm.profile.collectAsState()
    val data by vm.data.collectAsState()
    val careers by vm.careers.collectAsState()
    var profile by remember(saved) { mutableStateOf(saved) }
    var section by rememberSaveable { mutableStateOf("Academic") }
    var county by remember { mutableStateOf(false) }
    fun toggle(list: List<String>, id: String) = if (id in list) list - id else if (list.size < 3) list + id else list
    PfScreen(title = "Academic Profile", onBack = onBack) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Academic", "Interests", "Preferences").forEach { name -> FilterChip(selected = section == name, onClick = { section = name }, label = { Text(name) }) }
        } }
        when(section) {
            "Academic" -> {
                item { OutlinedTextField(profile.school, { profile = profile.copy(school = it) }, label = { Text("School") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { CountyPickerField("Home county", profile.county) { profile = profile.copy(county = it) } }
                item { ProfileDropdown("KCSE year", (2010..2028).map { it.toString() to it.toString() }, listOfNotNull(profile.kcseYear?.toString())) { profile = profile.copy(kcseYear = it.toInt()) } }
                item { ProfileDropdown("KCSE mean grade", KcseGrade.entries.map { it.name to it.label }, listOfNotNull(profile.meanGrade?.name)) { profile = profile.copy(meanGrade = KcseGrade.valueOf(it)) } }
                item { OutlinedButton(onClick = { vm.saveProfile(profile, onEditGrades) }, modifier = Modifier.fillMaxWidth()) { Text("Subject grades • ${profile.subjectGrades.size} entered →") } }
            }
            "Interests" -> {
                item { Text("Select up to three fields and career interests.") }
                item { ProfileDropdown("Preferred fields", data?.fields.orEmpty().map { it to it }, profile.preferredFields, multi = true) { profile = profile.copy(preferredFields = toggle(profile.preferredFields, it)) } }
                item { ProfileDropdown("Career interests", careers?.careers.orEmpty().map { it.id to it.name }, profile.careerInterests, multi = true) { profile = profile.copy(careerInterests = toggle(profile.careerInterests, it)) } }
            }
            else -> {
                item { ProfileDropdown("University type", InstitutionType.entries.map { it.name to it.label }, profile.institutionTypes.map { it.name }, multi = true) { id ->
                    val type = InstitutionType.valueOf(id)
                    profile = profile.copy(institutionTypes = if(type in profile.institutionTypes) profile.institutionTypes - type else profile.institutionTypes + type)
                } }
                item { OutlinedButton(onClick = { county = true }, modifier = Modifier.fillMaxWidth()) { Text("Preferred locations: ${profile.preferredLocations.joinToString().ifBlank { "Choose counties" }}") } }
                item { profile.preferredLocations.forEach { name -> TextButton(onClick = { profile = profile.copy(preferredLocations = profile.preferredLocations - name) }) { Text("Remove $name") } } }
                item { OutlinedTextField(profile.otherPreferences, { profile = profile.copy(otherPreferences = it.take(200)) }, label = { Text("Other preferences (optional)") }, modifier = Modifier.fillMaxWidth()) }
            }
        }
        item { PrimaryButton("Save profile", { vm.saveProfile(profile, onDone) }) }
        if (setup) item { TextButton(onClick = onDone) { Text("Skip for now") } }
    }
    if(county) CountyDialog(onDismiss = { county = false }) { name -> profile = profile.copy(preferredLocations = toggle(profile.preferredLocations, name)); county = false }
}

@Composable
private fun ProfileDropdown(label: String, options: List<Pair<String,String>>, selected: List<String>, multi: Boolean = false, onPick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.labelLarge); Text(options.filter { it.first in selected }.joinToString { it.second }.ifBlank { "Select…" }) }
            Text("▾")
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            options.forEach { (id, name) -> DropdownMenuItem(text = { Text(if (multi) "${if(id in selected) "✓ " else ""}$name" else name) }, onClick = { onPick(id); if(!multi) expanded = false }) }
            if(multi) DropdownMenuItem(text = { Text("Done") }, onClick = { expanded = false })
        }
    }
}
