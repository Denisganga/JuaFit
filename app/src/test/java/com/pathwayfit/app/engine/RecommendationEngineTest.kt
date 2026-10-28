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
        assertEquals(9, efe.size)
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

    @Test fun holisticWeightsAreConfigurableAndInvalidWeightsAreRejected() {
        val w = EightFourFourScoringWeights()
        assertEquals(listOf(.25f,.15f,.15f,.20f,.15f,.10f), listOf(w.academic,w.subject,w.interest,w.aptitude,w.career,w.preference))
        assertTrue(runCatching { EightFourFourScoringWeights(aptitude = -1f) }.isFailure)
        assertTrue(runCatching { EightFourFourScoringWeights(academic = Float.NaN) }.isFailure)
    }

    @Test fun academicOnlyIsProvisionalAndCannotSupplySuitabilityEvidence() {
        val set = EightFourFourRecommendationEngine().recommend(EightFourFourEngineInput(
            UniversityProfile(meanGrade = KcseGrade.A), EightFourFourAssessmentBank.assessment.questions, emptyList(), uni, careers))
        assertTrue(set.provisional)
        assertTrue(!set.hasSuitabilityEvidence)
        assertTrue(set.outstandingAssessments.any { it.contains("aptitude") })
        assertTrue(set.outstandingAssessments.contains("Interest questions"))
        assertEquals(1, set.courses.map { it.score }.distinct().size)
    }

    @Test fun selfReportedStrengthsDoNotCountAsAptitudeOrChangeOtherFactors() {
        val input = EightFourFourEngineInput(UniversityProfile(), EightFourFourAssessmentBank.assessment.questions,
            listOf(AssessmentAnswer("efe_v2_q5", setOf("3"))), uni, careers)
        val baseline = EightFourFourRecommendationEngine().recommend(input)
        val changed = EightFourFourRecommendationEngine().recommend(input.copy(answers = input.answers + AssessmentAnswer("efe_v2_q4", setOf("3", "5", "6"))))
        assertEquals(baseline.courses, changed.courses)
        assertTrue(changed.courses.all { it.factor("aptitude") == null })
    }

    @Test fun subjectEnjoymentIsSeparateFromAcademicGradesAndZeroAptitudeIsObserved() {
        val input = EightFourFourEngineInput(UniversityProfile(meanGrade = KcseGrade.A), EightFourFourAssessmentBank.assessment.questions,
            listOf(AssessmentAnswer("efe_v2_q3", setOf("MAT", "PHY", "CMP")), AssessmentAnswer("efe_v2_q5", setOf("3"))), uni, careers,
            aptitudeScores = mapOf("c_cs" to 0f))
        val a = EightFourFourRecommendationEngine().recommend(input)
        val b = EightFourFourRecommendationEngine().recommend(input.copy(profile = UniversityProfile(meanGrade = KcseGrade.C)))
        assertEquals(a.courses.first { it.course.id == "c_cs" }.factor("subject")!!.score,
            b.courses.first { it.course.id == "c_cs" }.factor("subject")!!.score)
        val cs = a.courses.first { it.course.id == "c_cs" }
        assertEquals(0f, cs.factor("aptitude")!!.score)
        assertEquals(1f, cs.factors.sumOf { it.weight.toDouble() }.toFloat(), .0001f)
    }
}
