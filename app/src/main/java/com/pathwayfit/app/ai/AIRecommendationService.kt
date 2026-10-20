package com.pathwayfit.app.ai

import com.pathwayfit.app.core.model.FactorScore

/*
 * AI layer. Each education system has its own context type, so a CBE request can only
 * ever carry CBE data and an 8-4-4 request can only carry KCSE/course data.
 */

/** CBE learner data → CBE recommendation context. No KCSE, course or university fields exist here. */
data class CbeAiContext(
    val firstName: String,
    val grade: String,
    val pathwayName: String,
    val trackName: String?,
    val areaName: String?,
    val percent: Int,
    val factors: List<FactorScore>,
    val strengths: List<String>,
    val interests: List<String>,
    val careerNames: List<String>,
)

/** KCSE/course data → university recommendation context. No CBE fields exist here. */
data class UniversityAiContext(
    val firstName: String,
    val courseName: String,
    val field: String,
    val percent: Int,
    val factors: List<FactorScore>,
    val academicStrengths: List<String>,
    val careerNames: List<String>,
)

data class AiExplanation(
    val headline: String,
    val summary: String,
    val factorNotes: Map<String, String>,
    val generatedBy: String,
)

interface AIRecommendationService {
    suspend fun explainCbe(context: CbeAiContext): AiExplanation
    suspend fun explainCourse(context: UniversityAiContext): AiExplanation
}

/** Rules every AI provider must follow. Sent as the system prompt to any remote model. */
object AiGuardrails {
    const val SYSTEM_PROMPT = """
You are JuaFit's guidance explainer for Kenyan learners.
- Explain WHY a recommendation fits using only the scores and evidence provided.
- Never state or invent KUCCPS cluster points, cut-off points, minimum grades, fees,
  programme availability or any official admission requirement.
- Never promise admission, placement, a job or better grades.
- Use the exact CBE pathway, track and area names you are given; do not invent new ones.
- Speak warmly and simply to a secondary-school learner. Present options, not orders.
"""
}

/**
 * Offline, deterministic explainer used by the prototype. It only rephrases engine
 * output, so it cannot introduce facts. A remote LLM implementation can replace it later
 * by implementing [AIRecommendationService] and sending [AiGuardrails.SYSTEM_PROMPT].
 */
class LocalExplanationService : AIRecommendationService {

    override suspend fun explainCbe(context: CbeAiContext): AiExplanation {
        val strongest = context.factors.maxByOrNull { it.score * it.weight }
        val route = listOfNotNull(context.pathwayName, context.trackName, context.areaName).joinToString(" → ")
        val summary = buildString {
            append("${context.firstName}, your answers point toward $route. ")
            if (context.strengths.isNotEmpty()) append("Your strongest signals were ${context.strengths.joinToString(", ").lowercase()}. ")
            strongest?.let { append("The biggest reason for this match is your ${it.label.lowercase()}") }
            strongest?.evidence?.takeIf { it.isNotEmpty() }?.let { append(" — especially ${it.joinToString(" and ").lowercase()}") }
            append(".")
        }
        return AiExplanation(
            headline = "Why ${context.pathwayName} may fit you",
            summary = summary,
            factorNotes = context.factors.associate { it.key to noteFor(it, cbe = true) },
            generatedBy = "JuaFit explainer (on-device)",
        )
    }

    override suspend fun explainCourse(context: UniversityAiContext): AiExplanation {
        val strongest = context.factors.maxByOrNull { it.score * it.weight }
        val summary = buildString {
            append("${context.firstName}, ${context.courseName} lines up with your profile. ")
            if (context.academicStrengths.isNotEmpty()) append("Your strongest results include ${context.academicStrengths.joinToString(", ")}. ")
            strongest?.let { append("Your strongest indicator for this course is ${it.label.lowercase()}.") }
        }
        return AiExplanation(
            headline = "Why ${context.courseName} may suit you",
            summary = summary,
            factorNotes = context.factors.associate { it.key to noteFor(it, cbe = false) },
            generatedBy = "JuaFit explainer (on-device)",
        )
    }

    private fun noteFor(f: FactorScore, cbe: Boolean): String {
        val level = when {
            f.score >= 0.8f -> "very strong"
            f.score >= 0.6f -> "strong"
            f.score >= 0.4f -> "moderate"
            else -> "lighter"
        }
        val ev = f.evidence.joinToString(", ")
        return when (f.key) {
            "interest" -> "Your interest alignment is $level." + if (ev.isNotBlank()) " Your answers showed interest in ${ev.lowercase()}." else ""
            "strength" -> "Strength alignment is $level." + if (ev.isNotBlank()) " You showed ${ev.lowercase()}, which this direction uses a lot." else ""
            "learning" -> "How you like to learn is a $level fit." + if (ev.isNotBlank()) " You enjoy ${ev.lowercase()}." else ""
            "academic" -> {
                if (cbe) {
                    "Your favourite learning areas are a $level fit." + (if (ev.isNotBlank()) " Especially ${ev.lowercase()}." else "")
                } else {
                    "Your grades give a $level academic alignment (a JuaFit estimate — not a KUCCPS cluster score)." +
                        (if (ev.isNotBlank()) " Based on: $ev." else "")
                }
            }
            "subject" -> "Subject relevance is $level." + if (ev.isNotBlank()) " Relevant subjects you took: $ev." else ""
            "career" -> "Career alignment is $level." + if (ev.isNotBlank()) " Linked to ${ev.lowercase()}." else ""
            "preference" -> "Your preferences are a $level fit." + if (ev.isNotBlank()) " $ev." else ""
            else -> "${f.label} is $level."
        }
    }
}
