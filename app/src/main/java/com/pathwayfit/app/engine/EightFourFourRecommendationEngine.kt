package com.pathwayfit.app.engine

import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentQuestion
import com.pathwayfit.app.core.model.Career
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.Course
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.FactorScore
import com.pathwayfit.app.core.model.InstitutionType
import com.pathwayfit.app.core.model.KcseGrade
import com.pathwayfit.app.core.model.SignalCategory
import com.pathwayfit.app.core.model.UniversityData
import com.pathwayfit.app.core.model.University
import com.pathwayfit.app.core.model.UniversityProfile

/** Configurable 8-4-4 weights (defaults from the product spec). Re-normalised at runtime. */
data class EightFourFourScoringWeights(
    val academic: Float = 0.25f,
    val subject: Float = 0.15f,
    val interest: Float = 0.15f,
    val aptitude: Float = 0.20f,
    val career: Float = 0.15f,
    val preference: Float = 0.10f,
) {
    init {
        val values = listOf(academic, subject, interest, aptitude, career, preference)
        require(values.all { it.isFinite() && it >= 0f } && values.sum() > 0f) { "Weights must be finite, non-negative and have a positive total." }
    }
}

data class EightFourFourEngineInput(
    val profile: UniversityProfile,
    val questions: List<AssessmentQuestion>,
    val answers: List<AssessmentAnswer>,
    val data: UniversityData,
    val careers: CareerData,
    // Results from a separate scored activity, never self-reported strengths.
    val aptitudeScores: Map<String, Float> = emptyMap(),
) {
    init { require(aptitudeScores.values.all { it.isFinite() && it in 0f..1f }) }
}

data class CourseMatch(
    val course: Course,
    val score: Float,
    val factors: List<FactorScore>,
    val universities: List<University>,
    val careers: List<Career>,
) {
    /** "JuaFit Match Score". This is NOT an official KUCCPS score. */
    val percent: Int get() = (score * 100).toInt().coerceIn(0, 100)
    fun factor(key: String) = factors.firstOrNull { it.key == key }
}

data class CourseRecommendationSet(
    val courses: List<CourseMatch>,
    val academicStrengths: List<String>,
    val estimatedMeanGrade: KcseGrade?,
    val hasGrades: Boolean,
    val generatedAt: Long = System.currentTimeMillis(),
    val outstandingAssessments: List<String> = emptyList(),
    val hasSuitabilityEvidence: Boolean = false,
) {
    val provisional: Boolean get() = outstandingAssessments.isNotEmpty()
    val top: CourseMatch? get() = courses.firstOrNull()
}

/**
 * Scores university courses for an 8-4-4 learner from KCSE grades, the 8-4-4 career
 * assessment and stated preferences. Independent of the CBE engine.
 *
 * Academic readiness uses the mean grade once; subject enjoyment uses separate questions.
 * Self-reported strengths are not a scored aptitude test. This pilot does not apply KUCCPS cluster formulas,
 * minimum requirements or cut-off points, and must never be presented as eligibility.
 */
class EightFourFourRecommendationEngine(
    private val weights: EightFourFourScoringWeights = EightFourFourScoringWeights(),
) : RecommendationEngine<EightFourFourEngineInput, CourseRecommendationSet> {

    override val system = EducationSystem.EIGHT_FOUR_FOUR

    companion object {
        /** KCSE mean grade is conventionally based on the best seven subjects. */
        fun estimateMean(profile: UniversityProfile): KcseGrade? {
            profile.meanGrade?.let { return it }
            if (profile.subjectGrades.isEmpty()) return null
            val best = profile.subjectGrades.map { it.grade.points }.sortedDescending().take(7)
            return KcseGrade.fromPoints(best.average())
        }
    }

    fun signals(input: EightFourFourEngineInput): SignalProfile {
        val b = SignalProfile.Builder().addAnswers(input.questions, input.answers)
        return b.build()
    }

    override fun recommend(input: EightFourFourEngineInput): CourseRecommendationSet {
        val s = signals(input)
        val grades = input.profile.subjectGrades.associate { it.subjectCode to it.grade.points }
        val mean = estimateMean(input.profile)
        val hasGrades = grades.isNotEmpty() || mean != null

        val interest = s.of(SignalCategory.INTEREST, SignalCategory.CAREER_INTEREST)
        val subjectSignals = s.of(SignalCategory.ACADEMIC_PREFERENCE)
        val careerBuilder = SignalProfile.Builder().addAnswers(input.questions.filter { it.id != "efe_v2_q9" && it.id != "efe_v2_q10" }, input.answers)
        input.profile.careerInterests.forEach { id -> input.careers.career(id)?.let { careerBuilder.addAll(SignalCategory.PERSONAL_GOAL, it.tags) } }
        val career = careerBuilder.build().of(SignalCategory.PERSONAL_GOAL)
        val preferenceSignals = s.of(SignalCategory.WORK_PREFERENCE)

        val matches = input.data.courses.map { course ->
            val unis = input.data.universitiesFor(course.id).map { it.first }
            val careers = course.careerIds.mapNotNull { input.careers.career(it) }

            // Mean grade is used once. Subject enjoyment is a separate questionnaire measure.
            val academic: Float? = mean?.points?.div(12f)
            val academicEvidence = mean?.let { listOf("Mean grade ${it.label}; readiness estimate only") }.orEmpty()
            val subject = Matching.coverage(course.tags, subjectSignals)
            val taken = Matching.matchedLabels(course.tags, subjectSignals)
            val aptitude = input.aptitudeScores[course.id]

            // 3–5. Interest, strength, career
            val careerCov = Matching.coverage(course.tags, career)
            val statedCareer = course.careerIds.any { it in input.profile.careerInterests }
            val careerScore = when {
                statedCareer -> maxOf(careerCov ?: 0f, 0.9f)
                else -> careerCov
            }

            // 6. Preferences
            val prefs = mutableListOf<Float>()
            Matching.coverage(course.tags, preferenceSignals)?.let { prefs += it }
            val prefEvidence = mutableListOf<String>()
            if (input.profile.preferredFields.isNotEmpty()) {
                val hit = course.field in input.profile.preferredFields
                prefs += if (hit) 1f else 0.3f
                if (hit) prefEvidence += "In your preferred field: ${course.field}"
            }
            val wantsPublic = InstitutionType.PUBLIC in input.profile.institutionTypes || s.preference("PUBLIC") > 0f
            val wantsPrivate = InstitutionType.PRIVATE in input.profile.institutionTypes || s.preference("PRIVATE") > 0f
            if (wantsPublic != wantsPrivate) {
                val type = if (wantsPublic) InstitutionType.PUBLIC else InstitutionType.PRIVATE
                val hit = unis.any { it.type == type }
                prefs += if (hit) 1f else 0.3f
                if (hit) prefEvidence += "Listed at a ${type.label.lowercase()}"
            }
            if (input.profile.preferredLocations.isNotEmpty() && s.preference("ANYWHERE") <= 0f) {
                val hit = unis.any { it.county in input.profile.preferredLocations }
                prefs += if (hit) 1f else 0.3f
                if (hit) prefEvidence += "Listed in a county you prefer"
            }

            val raw = listOf(
                Matching.factor("academic", "Academic match", academic, weights.academic, academicEvidence),
                Matching.factor("subject", "Subject enjoyment", subject, weights.subject, taken.take(4)),
                Matching.factor("interest", "Interest match", Matching.coverage(course.tags, interest), weights.interest, Matching.matchedLabels(course.tags, interest)),
                Matching.factor("aptitude", "Separate aptitude assessment", aptitude, weights.aptitude, if (aptitude == null) emptyList() else listOf("Separate scored aptitude assessment")),
                Matching.factor("career", "Career match", careerScore, weights.career,
                    (if (statedCareer) listOf("Leads toward a career you chose") else emptyList()) + Matching.matchedLabels(course.tags, career, 2)),
                Matching.factor("preference", "Preference match", if (prefs.isEmpty()) null else prefs.average().toFloat(), weights.preference, prefEvidence),
            )
            val (total, factors) = Matching.combine(raw)
            CourseMatch(course, total, factors, unis, careers)
        }.sortedByDescending { it.score }

        val strengths = input.profile.subjectGrades.sortedByDescending { it.grade.points }.take(3)
            .map { "${input.data.subject(it.subjectCode)?.name ?: it.subjectCode} (${it.grade.label})" }

        val outstanding = buildList {
            if (mean == null) add("Academic readiness (KCSE mean grade)")
            if (subjectSignals.isEmpty()) add("Subject enjoyment questions")
            if (interest.isEmpty()) add("Interest questions")
            if (input.data.courses.any { it.id !in input.aptitudeScores }) add("Separate aptitude assessment (self-reported strengths do not count)")
            if (career.isEmpty() && input.profile.careerInterests.isEmpty()) add("Career goals")
            if (preferenceSignals.isEmpty() && input.profile.preferredFields.isEmpty() && input.profile.institutionTypes.isEmpty() && input.profile.preferredLocations.isEmpty()) add("Study and environment preferences")
        }
        return CourseRecommendationSet(matches, strengths, mean, hasGrades,
            outstandingAssessments = outstanding, hasSuitabilityEvidence = interest.isNotEmpty() || input.aptitudeScores.isNotEmpty())
    }
}
