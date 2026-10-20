package com.pathwayfit.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.ai.GeminiConnection
import kotlinx.coroutines.launch

@Composable
fun AiSetupDialog(onDismiss: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(GeminiConnection.model) }
    var status by remember { mutableStateOf(if (GeminiConnection.configured) "A key is saved securely on this phone." else "Add your connection key to enable JuaGuide.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("JuaGuide connection") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Connect JuaGuide on this phone.")
            OutlinedTextField(key, { key = it }, label = { Text("JuaGuide API key") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !busy)
            Text(status)
            TextButton(onClick = {
                try {
                    if (key.isNotBlank()) { GeminiConnection.save(key, model); key = "" }
                    busy = true
                    scope.launch {
                        try { status = GeminiConnection.test() }
                        catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; status = e.message ?: "Could not connect. Check internet access." }
                        finally { busy = false }
                    }
                } catch (e: Exception) { status = e.message ?: "Check the key and model." }
            }, enabled = !busy && (key.isNotBlank() || GeminiConnection.configured)) { Text(if (busy) "Testing…" else "Save & test connection") }
            TextButton(onClick = { GeminiConnection.clear(); key = ""; status = "Saved key removed." }, enabled = !busy) { Text("Remove saved key") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Done") } })
}
