package com.pathwayfit.app.core.model

import kotlinx.serialization.Serializable

/* Shared structures used by both systems. Their *content* is always role-aware. */

@Serializable
data class Career(
    val id: String,
    val name: String,
    val summary: String,
    val tags: List<String> = emptyList(),
    val cbeTrackIds: List<String> = emptyList(), // links into CBE data
    val courseIds: List<String> = emptyList(),   // links into 8-4-4 course data
)

@Serializable
data class CareerData(val careers: List<Career>) {
    fun career(id: String) = careers.firstOrNull { it.id == id }
}

// ---- Assessment ---------------------------------------------------------

@Serializable
enum class QuestionType { SINGLE_CHOICE, MULTI_SELECT, RATING, SCENARIO }

/** What a question measures. Each engine maps these onto its own weights. */
@Serializable
enum class SignalCategory {
    INTEREST, STRENGTH, COMPETENCY, LEARNING_PREFERENCE, PRACTICAL_PREFERENCE,
    ACADEMIC_PREFERENCE, CAREER_INTEREST, WORK_PREFERENCE, PERSONAL_GOAL, UNIVERSITY_PREFERENCE,
}

@Serializable
data class AssessmentOption(
    val id: String,
    val label: String,
    /** tag -> weight contributed when the option is chosen */
    val signals: Map<String, Float> = emptyMap(),
)

@Serializable
data class AssessmentQuestion(
    val id: String,
    val type: QuestionType,
    val category: SignalCategory,
    val prompt: String,
    val helper: String = "",
    val options: List<AssessmentOption> = emptyList(),
    val allowOther: Boolean = false,
    val maxSelections: Int = 1,
    /** For RATING: tags scaled by the 1..5 rating. */
    val ratingSignals: Map<String, Float> = emptyMap(),
    val ratingLabels: Pair<String, String> = "Not at all" to "Very much",
)

@Serializable
data class AssessmentAnswer(
    val questionId: String,
    val selectedOptionIds: Set<String> = emptySet(),
    val rating: Int? = null,
    val otherText: String = "",
) {
    val isAnswered: Boolean get() = selectedOptionIds.isNotEmpty() || rating != null || otherText.isNotBlank()
}

data class Assessment(
    val id: String,
    val system: EducationSystem,
    val title: String,
    val questions: List<AssessmentQuestion>,
)

@Serializable
data class AssessmentRecord(
    val system: EducationSystem,
    val completedAt: Long,
    val topLabel: String,
    val topScore: Int,
)

// ---- Recommendations -----------------------------------------------------

/** One weighted factor in a match score, e.g. "Interest match 82%". */
data class FactorScore(
    val key: String,
    val label: String,
    val score: Float,          // 0..1
    val weight: Float,         // normalised weight actually applied
    val evidence: List<String> // human-readable reasons
)

@Serializable
data class SavedRecommendation(
    val system: EducationSystem,
    val itemId: String,
    val title: String,
    val score: Int,
    val savedAt: Long,
)

/** A step in a "My Journey" timeline. */
data class JourneyStep(
    val title: String,
    val value: String?,
    val status: JourneyStatus,
    val hint: String = "",
)

enum class JourneyStatus { DONE, CURRENT, UPCOMING }
