package com.pathwayfit.app.ui.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.pathwayfit.app.ui.components.PfScreen
import com.pathwayfit.app.ui.components.HeroCard
import com.pathwayfit.app.ui.components.PfCard
import com.pathwayfit.app.ui.components.CheckLine
import com.pathwayfit.app.ui.components.BrandWordmark
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentQuestion
import com.pathwayfit.app.core.model.QuestionType
import com.pathwayfit.app.ui.components.PrimaryButton
import com.pathwayfit.app.ui.components.SecondaryButton
import com.pathwayfit.app.ui.theme.LocalRoleAccent
import com.pathwayfit.app.ui.viewmodel.AssessmentDraft

/**
 * Shared question-flow mechanics (progress bar, navigation, input types).
 * The CONTENT — the question bank — is supplied per education system.
 */
@Composable
fun AssessmentFlow(
    draft: AssessmentDraft,
    eyebrow: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
) {
    var started by rememberSaveable { mutableStateOf(false) }
    if (!started) {
        PfScreen(title = "Take Assessment", onBack = onClose) {
            item { HeroCard(eyebrow, "Get to know your strengths", "Answer honestly to discover directions that fit your interests and preferences.", "Start Assessment", { started = true }, Icons.AutoMirrored.Outlined.Assignment) }
            item {
                PfCard {
                    Text("Before you begin", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    CheckLine("${draft.assessment.questions.size} questions, one at a time")
                    CheckLine("Choose options, rate statements and respond to scenarios")
                    CheckLine("Some questions include an optional Other response")
                    CheckLine("Go back to review your answers before submitting")
                }
            }
            item { Text("Your answers generate guidance on this device. A match score is a starting point for exploring your options.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        return
    }
    val accent = LocalRoleAccent.current
    val index by draft.index.collectAsState()
    val answers by draft.answers.collectAsState()
    val questions = draft.assessment.questions
    val q = questions[index]
    val answer = answers[q.id] ?: AssessmentAnswer(q.id)
    val progress by animateFloatAsState((index + 1) / questions.size.toFloat(), label = "progress")
    val isLast = index == questions.lastIndex

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "Close assessment") }
            Box(Modifier.weight(1f)) { BrandWordmark() }
            Text("${index + 1}/${questions.size}", style = MaterialTheme.typography.labelMedium, color = accent.main)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(CircleShape),
            color = accent.main, trackColor = MaterialTheme.colorScheme.surfaceVariant, drawStopIndicator = {},
        )

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it / 4 * dir } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 * dir } + fadeOut())
            },
            modifier = Modifier.weight(1f),
            label = "question",
        ) { i ->
            val question = questions[i]
            val ans = answers[question.id] ?: AssessmentAnswer(question.id)
            QuestionBody(question, ans) { draft.set(it) }
        }

        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (index > 0) SecondaryButton("Back", { draft.back() }, Modifier.weight(1f))
            PrimaryButton(
                text = if (isLast) "See my results" else "Next",
                onClick = { if (isLast) onSubmit() else draft.next() },
                enabled = answer.isAnswered,
                modifier = Modifier.weight(if (index > 0) 1.6f else 1f),
            )
        }
    }
}

@Composable
private fun QuestionBody(q: AssessmentQuestion, answer: AssessmentAnswer, onChange: (AssessmentAnswer) -> Unit) {
    val accent = LocalRoleAccent.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (q.type == QuestionType.SCENARIO) {
            Surface(shape = CircleShape, color = accent.soft) {
                Text("SCENARIO", style = MaterialTheme.typography.labelSmall, color = accent.main, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(10.dp))
        }
        Text(q.prompt, style = MaterialTheme.typography.headlineSmall)
        if (q.helper.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(q.helper, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(20.dp))

        when (q.type) {
            QuestionType.RATING -> RatingInput(q, answer, onChange)
            else -> {
                val multi = q.type == QuestionType.MULTI_SELECT
                q.options.forEach { opt ->
                    val selected = opt.id in answer.selectedOptionIds
                    OptionRow(opt.label, selected, multi) {
                        val next = when {
                            !multi -> setOf(opt.id)
                            selected -> answer.selectedOptionIds - opt.id
                            answer.selectedOptionIds.size < q.maxSelections -> answer.selectedOptionIds + opt.id
                            else -> answer.selectedOptionIds
                        }
                        onChange(answer.copy(selectedOptionIds = next))
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (q.allowOther) {
                    OutlinedTextField(
                        value = answer.otherText,
                        onValueChange = { onChange(answer.copy(otherText = it.take(80))) },
                        label = { Text("Other (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, multi: Boolean, onClick: () -> Unit) {
    val accent = LocalRoleAccent.current
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) accent.soft else MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent.main else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle, null,
                tint = if (selected) accent.main else MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun RatingInput(q: AssessmentQuestion, answer: AssessmentAnswer, onChange: (AssessmentAnswer) -> Unit) {
    val accent = LocalRoleAccent.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        (1..5).forEach { r ->
            val on = answer.rating == r
            Card(
                onClick = { onChange(answer.copy(rating = r)) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (on) accent.main else MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (on) accent.main else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).height(64.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("$r", style = MaterialTheme.typography.titleLarge, color = if (on) accent.onMain else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth()) {
        Text(q.ratingLabels.first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(q.ratingLabels.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}
