package com.pathwayfit.app.ui.efe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.ui.viewmodel.EightFourFourViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JuaGuideScreen(vm: EightFourFourViewModel, onUpgrade: () -> Unit) {
    val user by vm.user.collectAsState()
    val trialUsed by vm.guideTrialUsed.collectAsState()
    val premium by vm.guidePremium.collectAsState()
    val locked = trialUsed && !premium.isPremium && !com.pathwayfit.app.ai.JuaGuideDemo.enabled
    val keyboard = LocalSoftwareKeyboardController.current
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val context = LocalContext.current
    key(user?.userId) {
        val messages = remember { mutableStateListOf<Pair<String, String>>() }
        var attachment by remember { mutableStateOf<JsonObject?>(null) }
        var attachmentLabel by remember { mutableStateOf<String?>(null) }
        var input by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val list = rememberLazyListState()
        fun request() {
            if (busy || locked) { if (locked) onUpgrade(); return }
            busy = true; error = null
            scope.launch {
                try { val reply = vm.askJuaGuide(messages.toList(), attachment); messages.add("model" to reply.text) }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    error = e.message ?: "Unable to get a reply. Please retry."
                } finally { busy = false }
            }
        }
        fun send(text: String) {
            if (locked) { onUpgrade(); return }
            if (text.isBlank() || busy) return
            keyboard?.hide()
            messages.add("user" to text.trim().take(4000)); input = ""; request()
        }
        val upload = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) scope.launch {
                try {
                    busy = true
                    val result = withContext(Dispatchers.IO) {
                        val type = context.contentResolver.getType(uri) ?: error("Choose a PDF or image.")
                        check(type == "application/pdf" || type in listOf("image/jpeg", "image/png", "image/webp")) { "Choose a PDF, JPG, PNG or WebP file." }
                        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            var count = stream.read(buffer)
                            while (count != -1) {
                                output.write(buffer, 0, count)
                                check(output.size() <= 3 * 1024 * 1024) { "Choose a file smaller than 3 MB." }
                                count = stream.read(buffer)
                            }
                            output.toByteArray() } ?: error("Unable to read file.")
                        check(bytes.size <= 3 * 1024 * 1024) { "Choose a file smaller than 3 MB." }
                        buildJsonObject { put("mimeType", type); put("data", android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)) }
                    }
                    attachment = result; attachmentLabel = "Academic results attached"; error = null
                } catch(e: Exception) { error = e.message }
                finally { busy = false }
            }
        }
        LaunchedEffect(messages.size, busy, error, imeBottom) { list.animateScrollToItem(if (busy || error != null) messages.size + 1 else messages.size) }
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(16.dp)) {
            Text("JuaGuide", style = MaterialTheme.typography.headlineMedium)
            if (com.pathwayfit.app.ai.JuaGuideDemo.enabled) Text("AI Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("Premium career guidance • One free guidance session", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            LazyColumn(state = list, modifier = Modifier.weight(1f).imeNestedScroll(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Hi! I’m JuaGuide. Upload your transcript and we’ll discuss your interests and goals, one question at a time. I’ll help you explore five course and university options with reasons. Your first completed guidance session is free.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            if (messages.isEmpty() && !locked) listOf("Which career suits my interests?", "Explain my course recommendations", "What should I do next?").forEach { prompt ->
                                TextButton(onClick = { send(prompt) }, enabled = !busy) { Text(prompt) }
                            }
                        }
                    }
                }
                items(messages.toList()) { (role, text) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (role == "user") Arrangement.End else Arrangement.Start) {
                        Surface(shape = RoundedCornerShape(18.dp), color = if (role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 320.dp)) {
                            Text(text, modifier = Modifier.padding(14.dp), color = if (role == "user") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    if (busy) Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Text("  JuaGuide is thinking…") }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(onClick = { request() }) { Text("Retry") } }
                }
            }
            if (locked) Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Continue with JuaGuide Premium", style = MaterialTheme.typography.titleMedium)
                    Text("Your free guidance session is complete. Pay for Premium to ask more questions and get guidance on your academic results.")
                    Button(onClick = onUpgrade) { Text("Upgrade & payment options") }
                }
            }
            TextButton(onClick = { if (locked) onUpgrade() else upload.launch(arrayOf("application/pdf", "image/jpeg", "image/png", "image/webp")) }, enabled = !busy) { Text("Upload academic results") }
            attachmentLabel?.let { Text(it); TextButton(onClick = { attachment = null; attachmentLabel = null }, enabled = !busy) { Text("Remove attachment") } }
            if (attachment != null) Text("This file will be sent to JuaGuide with your next question. Remove personal identifiers first.", style = MaterialTheme.typography.bodySmall)
            if (imeBottom == 0) Text("Explore your options; confirm official requirements with the institution.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, onValueChange = { input = it.take(4000) }, placeholder = { Text("Ask JuaGuide…") }, modifier = Modifier.weight(1f), maxLines = 4, shape = RoundedCornerShape(18.dp))
                IconButton(onClick = { send(input) }, enabled = input.isNotBlank() && !busy && error == null && !locked) { Icon(Icons.AutoMirrored.Filled.Send, "Send message") }
            }
        }
    }
}
