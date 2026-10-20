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
    val academic: Float = 0.35f,
    val subject: Float = 0.25f,
    val interest: Float = 0.15f,
    val strength: Float = 0.10f,
    val career: Float = 0.10f,
    val preference: Float = 0.05f,
)

data class EightFourFourEngineInput(
    val profile: UniversityProfile,
    val questions: List<AssessmentQuestion>,
    val answers: List<AssessmentAnswer>,
    val data: UniversityData,
    val careers: CareerData,
)

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
) {
    val top: CourseMatch? get() = courses.firstOrNull()
}

/**
 * Scores university courses for an 8-4-4 learner from KCSE grades, the 8-4-4 career
 * assessment and stated preferences. Independent of the CBE engine.
 *
 * The academic factor is a PATHWAYFIT estimate of how strong the learner's grades are in
 * subjects commonly relevant to a course. It does not apply KUCCPS cluster formulas,
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
        input.profile.careerInterests.forEach { id ->
            input.careers.career(id)?.let { b.addAll(SignalCategory.CAREER_INTEREST, it.tags, 0.8f) }
        }
        input.profile.preferredFields.forEach { field ->
            input.data.courses.filter { it.field == field }.flatMap { it.tags }.distinct()
                .forEach { b.add(SignalCategory.INTEREST, it, 0.3f) }
        }
        return b.build()
    }

    override fun recommend(input: EightFourFourEngineInput): CourseRecommendationSet {
        val s = signals(input)
        val grades = input.profile.subjectGrades.associate { it.subjectCode to it.grade.points }
        val mean = estimateMean(input.profile)
        val hasGrades = grades.isNotEmpty() || mean != null

        val interest = s.of(SignalCategory.INTEREST, SignalCategory.ACADEMIC_PREFERENCE)
        val strength = s.of(SignalCategory.STRENGTH, SignalCategory.COMPETENCY)
        val career = s.of(SignalCategory.CAREER_INTEREST, SignalCategory.WORK_PREFERENCE, SignalCategory.PERSONAL_GOAL)

        val matches = input.data.courses.map { course ->
            val unis = input.data.universitiesFor(course.id).map { it.first }
            val careers = course.careerIds.mapNotNull { input.careers.career(it) }

            // 1. Academic alignment (estimate, not eligibility)
            val relevantPts = course.relevantSubjects.mapNotNull { grades[it] }
            val academic: Float? = when {
                !hasGrades -> null
                relevantPts.isEmpty() -> (mean?.points ?: 6) / 12f * 0.85f
                else -> 0.5f * ((mean?.points ?: relevantPts.average().toInt()) / 12f) + 0.5f * (relevantPts.average().toFloat() / 12f)
            }
            val academicEvidence = buildList {
                mean?.let { add("Mean grade ${it.label}") }
                course.relevantSubjects.filter { grades.containsKey(it) }
                    .sortedByDescending { grades[it] }.take(3)
                    .forEach { code -> add("${input.data.subject(code)?.name ?: code}: ${input.profile.subjectGrades.first { it.subjectCode == code }.grade.label}") }
            }

            // 2. Subject relevance
            val subject: Float? = if (grades.isEmpty() || course.relevantSubjects.isEmpty()) null else
                course.relevantSubjects.map { code -> ((grades[code] ?: 0) / 10f).coerceAtMost(1f) }.average().toFloat()
            val taken = course.relevantSubjects.filter { grades.containsKey(it) }.mapNotNull { input.data.subject(it)?.name }

            // 3–5. Interest, strength, career
            val careerCov = Matching.coverage(course.tags, career)
            val statedCareer = course.careerIds.any { it in input.profile.careerInterests }
            val careerScore = when {
                statedCareer -> maxOf(careerCov ?: 0f, 0.9f)
                else -> careerCov
            }

            // 6. Preferences
            val prefs = mutableListOf<Float>()
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
                Matching.factor("subject", "Subject match", subject, weights.subject, taken.take(4)),
                Matching.factor("interest", "Interest match", Matching.coverage(course.tags, interest), weights.interest, Matching.matchedLabels(course.tags, interest)),
                Matching.factor("strength", "Strength match", Matching.coverage(course.tags, strength), weights.strength, Matching.matchedLabels(course.tags, strength)),
                Matching.factor("career", "Career match", careerScore, weights.career,
                    (if (statedCareer) listOf("Leads toward a career you chose") else emptyList()) + Matching.matchedLabels(course.tags, career, 2)),
                Matching.factor("preference", "Preference match", if (prefs.isEmpty()) null else prefs.average().toFloat(), weights.preference, prefEvidence),
            )
            val (total, factors) = Matching.combine(raw)
            CourseMatch(course, total, factors, unis, careers)
        }.sortedByDescending { it.score }

        val strengths = input.profile.subjectGrades.sortedByDescending { it.grade.points }.take(3)
            .map { "${input.data.subject(it.subjectCode)?.name ?: it.subjectCode} (${it.grade.label})" }

        return CourseRecommendationSet(matches, strengths, mean, hasGrades)
    }
}
