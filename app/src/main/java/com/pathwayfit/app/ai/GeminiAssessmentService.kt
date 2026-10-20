package com.pathwayfit.app.ai

import com.pathwayfit.app.BuildConfig
import com.pathwayfit.app.core.model.*
import com.pathwayfit.app.engine.CourseRecommendationSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

@Serializable
 data class GeminiCourseInsight(val courseId: String, val overview: String, val reasons: List<String>, val potential: String,
    val quote: String, val furtherEducation: List<String> = emptyList(), val employmentSectors: List<String> = emptyList(), val careerProgression: List<String> = emptyList())

@Serializable
data class GeminiAssessmentResult(val summary: String, val courseIds: List<String>, val model: String, val insights: List<GeminiCourseInsight> = emptyList())

class GeminiAssessmentService {
    suspend fun analyze(profile: UniversityProfile, questions: List<AssessmentQuestion>, answers: List<AssessmentAnswer>, candidates: CourseRecommendationSet): GeminiAssessmentResult = withContext(Dispatchers.IO) {

        val payload = buildJsonObject {
            put("profile", buildJsonObject {
                put("meanGrade", profile.meanGrade?.name ?: "Not supplied")
                put("subjectGrades", Json.encodeToJsonElement(profile.subjectGrades))
                put("preferredFields", Json.encodeToJsonElement(profile.preferredFields))
                put("preferredLocations", Json.encodeToJsonElement(profile.preferredLocations))
            })
            put("answers", buildJsonArray {
                questions.forEach { q ->
                    val a = answers.firstOrNull { it.questionId == q.id }
                    add(buildJsonObject {
                        put("questionId", q.id); put("question", q.prompt)
                        put("selectedAnswers", buildJsonArray { q.options.filter { it.id in (a?.selectedOptionIds ?: emptySet()) }.forEach { add(it.label) } })
                        put("typedAnswer", if (q.id == "efe_v3_performance" && com.pathwayfit.app.assessment.EightFourFourAssessmentBank.isPrimary(answers.associateBy { it.questionId })) "Primary school marks: ${a?.otherText ?: ""}/500" else a?.otherText ?: "")
                    })
                }
            })
            put("courses", buildJsonArray { candidates.courses.forEach { m -> add(buildJsonObject {
                put("id", m.course.id); put("name", m.course.name); put("field", m.course.field)
                put("careers", buildJsonArray { m.careers.forEach { add(it.name) } })
            }) } })
        }
        if (JuaGuideDemo.enabled) return@withContext JuaGuideDemo.analyze(questions, answers, candidates)
        val result = Json.decodeFromString<GeminiAssessmentResult>(GeminiConnection.call("analyze", payload).toString())
            val validIds = candidates.courses.map { it.course.id }.toSet()
            check(result.summary.isNotBlank() && result.courseIds.isNotEmpty() && result.courseIds.distinct().size == result.courseIds.size && result.courseIds.all { it in validIds }) { "JuaGuide returned an invalid recommendation. Please retry." }
            check(result.insights.map { it.courseId }.toSet() == result.courseIds.toSet() && result.insights.size == result.courseIds.size) { "JuaGuide returned incomplete course explanations. Please retry." }
            result

    }
}
