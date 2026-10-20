package com.pathwayfit.app.ui.efe

import com.pathwayfit.app.ui.theme.careerfitPalette
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.pathwayfit.app.assessment.EightFourFourAssessmentBank
import com.pathwayfit.app.R
import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.QuestionType
import com.pathwayfit.app.ui.viewmodel.AssessmentDraft

@Composable
fun CareerfitQuestionFlow(draft: AssessmentDraft, onClose: () -> Unit, onSubmit: () -> Unit) {
    val palette = careerfitPalette

    val index by draft.index.collectAsState()
    val answers by draft.answers.collectAsState()
    val questions = draft.assessment.questions
    val question = questions[index]
    val answer = answers[question.id] ?: AssessmentAnswer(question.id)
    val last = index == questions.lastIndex
    val primary = EightFourFourAssessmentBank.isPrimary(answers)
    val performance = question.id == "efe_v3_performance"
    var gradeMenu by remember { mutableStateOf(false) }
    val multi = question.type == QuestionType.MULTI_SELECT
    val blue = palette.color(0xFF0064F5)
    val ink = palette.color(0xFF080C35)
    val muted = palette.color(0xFF405D8E)
    val focus = LocalFocusManager.current
    var submitting by remember { mutableStateOf(false) }
    val back = { focus.clearFocus(); if (index == 0) onClose() else draft.back() }
    BackHandler(onBack = back)
    val context = LocalContext.current
    val logo = remember { BitmapFactory.decodeResource(context.resources, R.drawable.juafit_emblem).asImageBitmap() }
    Box(Modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawPath(Path().apply { moveTo(0f,0f); lineTo(w*.38f,0f); cubicTo(w*.29f,h*.09f,w*.15f,h*.13f,0f,h*.16f); close() },
                Brush.linearGradient(listOf(palette.color(0xFFD6EDFF),palette.color(0xFFF0FAFF))))
            drawPath(Path().apply { moveTo(w*.62f,0f); lineTo(w,0f); lineTo(w,h*.155f); quadraticTo(w*.70f,h*.10f,w*.62f,0f); close() },
                Brush.horizontalGradient(listOf(palette.color(0xFFFFF8DF),palette.color(0xFFFFE797))))
            drawPath(Path().apply { moveTo(0f,h*.91f); quadraticTo(w*.30f,h*.98f,w*.75f,h); lineTo(0f,h); close() },
                Brush.horizontalGradient(listOf(palette.color(0xFFE2F3FF),palette.color(0xFFB6DFFF))))
            drawPath(Path().apply { moveTo(w,h*.91f); quadraticTo(w*.76f,h*.97f,w*.43f,h); lineTo(w,h); close() },
                Brush.horizontalGradient(listOf(palette.color(0xFFFFF6D3),palette.color(0xFFFFE17D))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
                Spacer(Modifier.weight(1f))
                Canvas(Modifier.width(60.dp).height(70.dp)) {
                    drawImage(logo, srcOffset = IntOffset(0, 0), srcSize = IntSize(1254, 1254), dstSize = IntSize(size.width.toInt(),size.height.toInt()))
                }
                Column(Modifier.padding(start = 7.dp)) {
                    Row {
                        Text("Jua", color = palette.color(0xFF0060B9), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                        Text("Fit", color = palette.color(0xFFEFB52A), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
                    }
                    Spacer(Modifier.height(3.dp))
                    Text("Not Just What You Qualify For —\nWhat You Are Best Suited For", color = ink, fontSize = 11.sp, lineHeight = 14.sp)
                }
                Spacer(Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(progress = { (index + 1) / questions.size.toFloat() }, color = blue, trackColor = palette.color(0xFFE1ECFA),
                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape), drawStopIndicator = {})
                Text("${index + 1}/${questions.size}", color = ink, fontSize = 15.sp, modifier = Modifier.padding(start = 16.dp))
            }
            Spacer(Modifier.height(18.dp))
            val scroll = rememberScrollState()
            LaunchedEffect(index) { scroll.scrollTo(0) }
            Column(Modifier.weight(1f).verticalScroll(scroll)) {
                Surface(color = palette.color(0xFFF3F9FE), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 7.dp, vertical = 14.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.size(43.dp).background(palette.color(0xFFDEEDFF), CircleShape), contentAlignment = Alignment.Center) {
                                val questionIcon = when (index) {
                                    0 -> Icons.Outlined.MenuBook
                                    1 -> Icons.Outlined.BarChart
                                    2 -> Icons.Outlined.Psychology
                                    3 -> Icons.Outlined.Settings
                                    4 -> Icons.Outlined.FavoriteBorder
                                    5 -> Icons.Outlined.TrackChanges
                                    6 -> Icons.Outlined.LocationOn
                                    7 -> Icons.Outlined.Groups
                                    8 -> Icons.Outlined.StarBorder
                                    else -> Icons.Outlined.ChatBubbleOutline
                                }
                                Icon(questionIcon, null, tint = blue, modifier = Modifier.size(26.dp))
                            }
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(question.prompt, color = ink, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(5.dp))
                                Text(if (performance) { if (primary) "Primary school marks out of 500." else "Select your KCSE mean grade." } else question.helper, color = muted, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        if (performance) {
                            if (primary) OutlinedTextField(value = answer.otherText, onValueChange = { value ->
                                draft.set(answer.copy(selectedOptionIds = setOf("primary_marks"), otherText = value.filter { it.isDigit() }.take(3)))
                            }, label = { Text("Marks out of 500") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                            else Box {
                                OutlinedButton(onClick = { gradeMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(question.options.firstOrNull { it.id in answer.selectedOptionIds }?.label ?: "Select KCSE mean grade")
                                }
                                DropdownMenu(expanded = gradeMenu, onDismissRequest = { gradeMenu = false }) {
                                    question.options.forEach { option -> DropdownMenuItem(text = { Text(option.label) }, onClick = {
                                        draft.set(answer.copy(selectedOptionIds = setOf(option.id), otherText = "")); gradeMenu = false
                                    }) }
                                }
                            }
                        } else if (!last) {
                            val other = "other" in answer.selectedOptionIds
                            ((if (question.id == "efe_v2_q3") EightFourFourAssessmentBank.subjects(answers) else question.options).map { it.id to it.label } + if (question.allowOther) listOf("other" to "Other (please specify)") else emptyList()).forEach { (id, label) ->
                                val selected = id in answer.selectedOptionIds
                                val toggle = {
                                    if (!submitting) {
                                        val ids = if (multi) { if (selected) answer.selectedOptionIds - id else if (answer.selectedOptionIds.size < question.maxSelections) answer.selectedOptionIds + id else answer.selectedOptionIds }
                                            else setOf(id)
                                        if (question.id == "efe_v2_q2" && ids != answer.selectedOptionIds) {
                                            draft.set(AssessmentAnswer("efe_v3_performance")); draft.set(AssessmentAnswer("efe_v2_q3"))
                                        }
                                        draft.set(answer.copy(selectedOptionIds = ids, otherText = if ("other" in ids) answer.otherText else ""))
                                    }
                                }
                                Surface(onClick = toggle, enabled = !submitting, shape = RoundedCornerShape(8.dp), color = palette.surface.copy(alpha = .6f),
                                    border = BorderStroke(1.dp,palette.color(0xFFDCE8FA)), modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)) {
                                    Row(Modifier.padding(end = 12.dp).heightIn(min = 39.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (multi) Checkbox(selected, { toggle() }, enabled = !submitting, colors = CheckboxDefaults.colors(checkedColor = blue, uncheckedColor = muted))
                                        else RadioButton(selected, { toggle() }, enabled = !submitting, colors = RadioButtonDefaults.colors(selectedColor = blue, unselectedColor = muted))
                                        Text(label, color = if (selected) ink else palette.color(0xFF384675), fontSize = 14.sp, lineHeight = 18.sp)
                                    }
                                }
                            }
                            if (other) OutlinedTextField(value = answer.otherText, onValueChange = { draft.set(answer.copy(otherText = it)) }, enabled = !submitting,
                                placeholder = { Text("Type your answer here…", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = blue, unfocusedBorderColor = palette.color(0xFFDCE8FA), focusedContainerColor = palette.surface, unfocusedContainerColor = palette.surface))
                        } else OutlinedTextField(value = answer.otherText, onValueChange = { draft.set(answer.copy(otherText = it)) }, enabled = !submitting,
                            placeholder = { Text("Type your answer here…\n(e.g. specific interests, challenges,\nor anything important)", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = blue, unfocusedBorderColor = palette.color(0xFFDCE8FA), focusedContainerColor = palette.surface, unfocusedContainerColor = palette.surface))
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            val valid = EightFourFourAssessmentBank.valid(question, answer, answers)
            if (question.id == "efe_v2_q3") Text("${answer.selectedOptionIds.size}/4 selected · choose at least 3", color = muted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = back, enabled = !submitting, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, palette.color(0xFF9AC1FF)),
                    modifier = Modifier.weight(1f).height(47.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = blue, containerColor = palette.color(0xFFEEF6FF))) { Text("Back", fontSize = 17.sp) }
                Button(onClick = { focus.clearFocus(); if (last) { submitting = true; onSubmit() } else draft.next() }, enabled = valid && !submitting,
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(if (last) 1.5f else 1.1f).height(47.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue, contentColor = Color.White)) {
                    Text(if (submitting) "Finishing…" else if (last) "Finish Assessment" else "Next", fontSize = if (last) 12.sp else 15.sp)
                    if (!submitting) { Spacer(Modifier.width(10.dp)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(20.dp)) }
                }
            }
            Text("Your journey. A brighter future.", color = palette.color(0xFF496899), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 26.dp, bottom = 20.dp))
        }
    }
}
