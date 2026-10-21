package com.pathwayfit.app.engine

import com.pathwayfit.app.assessment.EightFourFourAssessmentBank as Bank
import com.pathwayfit.app.core.model.*
import org.junit.Assert.*
import org.junit.Test

class AssessmentRulesTest {
    private val questions = Bank.assessment.questions
    private val secondary = mapOf("efe_v2_q2" to AssessmentAnswer("efe_v2_q2", setOf("secondary_school")))
    @Test fun performanceLeadsSecondaryAssessment() {
        assertEquals("efe_v3_performance", questions[0].id)
        assertEquals(9, questions.size)
        assertFalse(questions.any { it.id == "efe_v2_q2" })
    }
    @Test fun subjectsRequireThreeOrFourValidChoices() {
        val q = questions.first { it.id == "efe_v2_q3" }
        val ids = listOf("MAT", "ENG", "KIS", "BIO", "PHY")
        for (size in 0..5) assertEquals(size in 3..4, Bank.valid(q, AssessmentAnswer(q.id, ids.take(size).toSet()), secondary))
        assertFalse(Bank.valid(q, AssessmentAnswer(q.id, setOf("MAT", "ENG", "other")), secondary))
    }
    @Test fun performanceUpdatesScoringProfile() {
        val answers = secondary.values.toList() + AssessmentAnswer("efe_v3_performance", setOf("B_PLUS"))
        assertEquals(KcseGrade.B_PLUS, Bank.withPerformance(UniversityProfile(), answers).meanGrade)
    }
    @Test fun secondarySubjectsAndValuesAreSpecific() {
        val subjects = Bank.subjects(emptyMap())
        assertFalse(subjects.any { it.id in setOf("SCI", "SST", "TECH") })
        assertTrue(subjects.any { it.label == "Home Science" })
        assertEquals(3, questions.first { it.id == "efe_v2_q9" }.maxSelections)
    }
    @Test fun subjectAlignmentIsRankedAndNeverExceedsOneHundred() {
        val ranking = Bank.subjectRanking(listOf(
            AssessmentAnswer("efe_v2_q3", setOf("MAT", "BIO", "PHY")),
            AssessmentAnswer("efe_v2_q4", setOf("3", "6")),
            AssessmentAnswer("efe_v2_q5", setOf("3"))
        ))
        assertEquals(3, ranking.size)
        assertTrue(ranking.all { it.second in 0..100 })
        assertTrue(ranking.zipWithNext().all { (a,b) -> a.second >= b.second })
    }
}
