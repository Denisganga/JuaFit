package com.pathwayfit.app.engine

import com.pathwayfit.app.assessment.CbeAssessmentBank
import com.pathwayfit.app.assessment.EightFourFourAssessmentBank
import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.CbeProfile
import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.KcseGrade
import com.pathwayfit.app.core.model.KcseSubjectGrade
import com.pathwayfit.app.core.model.UniversityData
import com.pathwayfit.app.core.model.UniversityProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecommendationEngineTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val ref = json.decodeFromString<CbeReference>(File("src/main/assets/cbe_reference.json").readText())
    private val uni = json.decodeFromString<UniversityData>(File("src/main/assets/kcse_university_data.json").readText())
    private val careers = json.decodeFromString<CareerData>(File("src/main/assets/careers.json").readText())

    @Test fun referenceDataIsConsistent() {
        ref.tracks.forEach { assertTrue(ref.pathway(it.pathwayId) != null) }
        ref.areas.forEach { a -> assertTrue(ref.track(a.trackId) != null); a.subjectIds.forEach { assertTrue(it, ref.subject(it) != null) } }
        careers.careers.forEach { c ->
            c.cbeTrackIds.forEach { assertTrue(it, ref.track(it) != null) }
            c.courseIds.forEach { assertTrue(it, uni.course(it) != null) }
        }
        uni.offerings.forEach { assertTrue(uni.university(it.universityId) != null && uni.course(it.courseId) != null) }
    }

    @Test fun assessmentsAreSeparateAndUseTheirOwnQuestionCounts() {
        val cbe = CbeAssessmentBank.assessment.questions
        val efe = EightFourFourAssessmentBank.assessment.questions
        assertEquals(15, cbe.size)
        assertEquals(10, efe.size)
        assertTrue(cbe.map { it.prompt }.intersect(efe.map { it.prompt }.toSet()).isEmpty())
    }

    @Test fun creativeLearnerGetsArtsPathway() {
        val answers = listOf(
            AssessmentAnswer("cbe_q1", setOf("f", "g")), AssessmentAnswer("cbe_q6", rating = 5),
            AssessmentAnswer("cbe_q7", setOf("f")), AssessmentAnswer("cbe_q10", setOf("f")),
            AssessmentAnswer("cbe_q13", rating = 5), AssessmentAnswer("cbe_q15", setOf("b")),
        )
        val set = CbeRecommendationEngine().recommend(CbeEngineInput(CbeProfile(), CbeAssessmentBank.assessment.questions, answers, ref, careers))
        assertEquals("arts_sports", set.top?.pathway?.id)
    }

    @Test fun technicalLearnerGetsStem() {
        val answers = listOf(
            AssessmentAnswer("cbe_q1", setOf("b", "c")), AssessmentAnswer("cbe_q3", rating = 5),
            AssessmentAnswer("cbe_q8", setOf("b")), AssessmentAnswer("cbe_q10", setOf("b")), AssessmentAnswer("cbe_q14", setOf("c")),
        )
        val set = CbeRecommendationEngine().recommend(CbeEngineInput(CbeProfile(), CbeAssessmentBank.assessment.questions, answers, ref, careers))
        assertEquals("stem", set.top?.pathway?.id)
        assertTrue(set.top!!.percent in 0..100)
    }

    @Test fun courseEngineUsesGradesAndNeverExceeds100() {
        val profile = UniversityProfile(subjectGrades = listOf(
            KcseSubjectGrade("MAT", KcseGrade.A), KcseSubjectGrade("PHY", KcseGrade.A_MINUS), KcseSubjectGrade("CMP", KcseGrade.A),
        ))
        val answers = listOf(AssessmentAnswer("efe_v2_q1", setOf("3")), AssessmentAnswer("efe_v2_q5", setOf("3")))
        val set = EightFourFourRecommendationEngine().recommend(
            EightFourFourEngineInput(profile, EightFourFourAssessmentBank.assessment.questions, answers, uni, careers),
        )
        assertTrue(set.hasGrades)
        assertEquals("Computing & IT", set.top?.course?.field)
        set.courses.forEach { assertTrue(it.percent in 0..100) }
    }
}
