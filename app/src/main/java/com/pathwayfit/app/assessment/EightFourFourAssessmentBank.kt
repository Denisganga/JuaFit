package com.pathwayfit.app.assessment

import com.pathwayfit.app.core.model.*

/** Secondary-school JuaFit questionnaire; option signals feed the existing course engine. */
object EightFourFourAssessmentBank {
    val assessment = Assessment(
        id = "efe_v2", system = EducationSystem.EIGHT_FOUR_FOUR, title = "Take Assessment",
        questions = listOf(
            AssessmentQuestion(id = "efe_v3_performance", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "How did you perform academically?", helper = "Enter your academic performance.", allowOther = false, maxSelections = 1,
                options = KcseGrade.entries.map { AssessmentOption(it.name, "KCSE mean grade: ${it.label}", emptyMap()) }),
            AssessmentQuestion(id = "efe_v2_q3", type = QuestionType.MULTI_SELECT, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "Which subjects do you enjoy the most?", helper = "Select 3–4 subjects.", allowOther = false, maxSelections = 4,
                options = listOf(
                    AssessmentOption("MAT", "Mathematics", mapOf("maths" to 1f)),
                    AssessmentOption("ENG", "English", mapOf("languages" to 1f)),
                    AssessmentOption("KIS", "Kiswahili", mapOf("languages" to 1f)),
                    AssessmentOption("BIO", "Biology", mapOf("science" to 1f, "health" to 1f)),
                    AssessmentOption("CHE", "Chemistry", mapOf("science" to 1f)),
                    AssessmentOption("PHY", "Physics", mapOf("science" to 1f)),
                    AssessmentOption("BUS", "Business Studies", mapOf("business" to 1f)),
                    AssessmentOption("HIS", "History and Government", mapOf("society" to 1f)),
                    AssessmentOption("GEO", "Geography", mapOf("society" to 1f, "nature" to 1f)),
                    AssessmentOption("CRE", "Christian Religious Education", mapOf("society" to 1f)),
                    AssessmentOption("CMP", "Computer Studies", mapOf("computing" to 1f)),
                    AssessmentOption("AGR", "Agriculture", mapOf("nature" to 1f)),
                    AssessmentOption("ART", "Art & Design", mapOf("creative" to 1f)),
                    AssessmentOption("MUS", "Music", mapOf("creative" to 1f)),
                    AssessmentOption("IRE", "Islamic Religious Education", mapOf("society" to 1f)),
                    AssessmentOption("HRE", "Hindu Religious Education", mapOf("society" to 1f)),
                    AssessmentOption("HSC", "Home Science", mapOf("practical" to 1f)),
                    AssessmentOption("WOOD", "Woodwork", mapOf("hands_on" to 1f)),
                    AssessmentOption("METAL", "Metalwork", mapOf("hands_on" to 1f)),
                    AssessmentOption("BUILD", "Building Construction", mapOf("engineering" to 1f)),
                    AssessmentOption("POWER", "Power Mechanics", mapOf("engineering" to 1f)),
                    AssessmentOption("ELEC", "Electricity", mapOf("engineering" to 1f)),
                    AssessmentOption("DRAW", "Drawing and Design", mapOf("creative" to 1f)),
                    AssessmentOption("AVIA", "Aviation Technology", mapOf("engineering" to 1f)),
                    AssessmentOption("FRE", "French", mapOf("languages" to 1f)),
                    AssessmentOption("GER", "German", mapOf("languages" to 1f)),
                    AssessmentOption("ARA", "Arabic", mapOf("languages" to 1f)),
                    AssessmentOption("KSL", "Kenyan Sign Language", mapOf("languages" to 1f)),
                    AssessmentOption("GSCI", "General Science", mapOf("science" to 1f)),
                    AssessmentOption("BBL", "Biology for the Blind", mapOf("science" to 1f)),
                )),
            AssessmentQuestion(id = "efe_v2_q4", type = QuestionType.MULTI_SELECT, category = SignalCategory.STRENGTH,
                prompt = "What are you good at?", helper = "You can select one or more options or choose “Other” and type your answer.", allowOther = true, maxSelections = 8,
                options = listOf(
                    AssessmentOption("1", "Communication", mapOf("communication" to 1f)),
                    AssessmentOption("2", "Leadership", mapOf("leadership" to 1f)),
                    AssessmentOption("3", "Problem solving", mapOf("problem_solving" to 1f)),
                    AssessmentOption("4", "Creativity", mapOf("creative" to 1f)),
                    AssessmentOption("5", "Technical skills (e.g. computer skills)", mapOf("computing" to 1f)),
                    AssessmentOption("6", "Analytical thinking", mapOf("analytical" to 1f)),
                    AssessmentOption("7", "Teamwork", mapOf("people" to 1f)),
                )),
            AssessmentQuestion(id = "efe_v2_q5", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.CAREER_INTEREST,
                prompt = "Which of the following areas interest you most?", helper = "You can select one option or choose “Other” and type your answer.", allowOther = true, maxSelections = 1,
                options = listOf(
                    AssessmentOption("1", "Health and medicine", mapOf("health" to 1f)),
                    AssessmentOption("2", "Business and entrepreneurship", mapOf("business" to 1f)),
                    AssessmentOption("3", "Technology and computers", mapOf("computing" to 1f)),
                    AssessmentOption("4", "Education and training", mapOf("helping" to 1f)),
                    AssessmentOption("5", "Engineering and construction", mapOf("engineering" to 1f)),
                    AssessmentOption("6", "Environment and agriculture", mapOf("nature" to 1f)),
                    AssessmentOption("7", "Arts, media and design", mapOf("creative" to 1f)),
                )),
            AssessmentQuestion(id = "efe_v2_q6", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.PERSONAL_GOAL,
                prompt = "What are your career goals?", helper = "You can select one option or choose “Other” and type your answer.", allowOther = true, maxSelections = 1,
                options = listOf(
                    AssessmentOption("1", "Get a job quickly", mapOf("practical" to 1f)),
                    AssessmentOption("2", "Start my own business", mapOf("entrepreneurial" to 1f)),
                    AssessmentOption("3", "Continue to higher studies", mapOf("research" to 1f)),
                    AssessmentOption("4", "Gain practical skills", mapOf("hands_on" to 1f)),
                    AssessmentOption("5", "Make a positive impact in my community", mapOf("helping" to 1f)),
                    AssessmentOption("6", "I am not yet sure", emptyMap()),
                )),
            AssessmentQuestion(id = "efe_v2_q7", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.UNIVERSITY_PREFERENCE,
                prompt = "Where would you like to study?", helper = "Select one option.", allowOther = false, maxSelections = 1,
                options = listOf(
                    AssessmentOption("1", "In Kenya", mapOf("pref:ANYWHERE" to 1f)),
                    AssessmentOption("2", "Outside Kenya", emptyMap()),
                    AssessmentOption("3", "Either Kenya or abroad", mapOf("pref:ANY" to 1f)),
                    AssessmentOption("4", "Not sure yet", emptyMap()),
                )),
            AssessmentQuestion(id = "efe_v2_q8", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.WORK_PREFERENCE,
                prompt = "Which workplace setting appeals to you most?", helper = "Select one option.", allowOther = false, maxSelections = 1,
                options = listOf(
                    AssessmentOption("1", "Office or business setting", mapOf("organising" to 1f)),
                    AssessmentOption("2", "Laboratory or research setting", mapOf("research" to 1f)),
                    AssessmentOption("3", "Hospital or care setting", mapOf("health" to 1f)),
                    AssessmentOption("4", "School or training setting", mapOf("helping" to 1f)),
                    AssessmentOption("5", "Workshop, studio or production setting", mapOf("hands_on" to 1f)),
                    AssessmentOption("6", "Outdoors or field setting", mapOf("outdoor" to 1f)),
                    AssessmentOption("7", "Not sure yet", emptyMap()),
                )),
            AssessmentQuestion(id = "efe_v2_q9", type = QuestionType.MULTI_SELECT, category = SignalCategory.PERSONAL_GOAL,
                prompt = "Which personal values guide your choices?", helper = "Select up to 3 values.", allowOther = false, maxSelections = 3,
                options = listOf(
                    AssessmentOption("1", "Integrity — being honest and ethical", emptyMap()),
                    AssessmentOption("2", "Compassion — caring about others", mapOf("helping" to 1f)),
                    AssessmentOption("3", "Fairness — treating people equitably", mapOf("society" to 1f)),
                    AssessmentOption("4", "Responsibility — being dependable", emptyMap()),
                    AssessmentOption("5", "Respect — valuing people and differences", mapOf("people" to 1f)),
                    AssessmentOption("6", "Service — contributing to the community", mapOf("helping" to 1f)),
                    AssessmentOption("7", "Stewardship — caring for the environment", mapOf("nature" to 1f)),
                    AssessmentOption("8", "Excellence — doing your best", emptyMap()),
                )),
            AssessmentQuestion(id = "efe_v2_q10", type = QuestionType.SINGLE_CHOICE, category = SignalCategory.PERSONAL_GOAL,
                prompt = "You’re almost done!", helper = "Is there anything else you would like us to know about you? (Optional)", allowOther = true, maxSelections = 1,
                options = listOf(
                )),
        ),
    )
    /** Interest alignment, never an academic mark or admission probability. */
    fun subjectRanking(answers: List<AssessmentAnswer>): List<Pair<String, Int>> {
        val map = answers.associateBy { it.questionId }
        val signals = mutableMapOf<String, Float>()
        assessment.questions.filter { it.id != "efe_v2_q3" }.forEach { q ->
            q.options.filter { it.id in map[q.id]?.selectedOptionIds.orEmpty() }.forEach { option ->
                option.signals.forEach { (tag, value) -> signals[tag] = (signals[tag] ?: 0f) + value }
            }
        }
        val total = signals.values.sum()
        return subjects(map).filter { it.id in map["efe_v2_q3"]?.selectedOptionIds.orEmpty() }.map { option ->
            val score = option.signals.keys.sumOf { (signals[it] ?: 0f).toDouble() }.toFloat()
            option.label to (if(total > 0f) kotlin.math.round(100f * score / total).toInt() else 0)
        }.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
    }

    fun isPrimary(answers: Map<String, AssessmentAnswer>) = false

    fun subjects(answers: Map<String, AssessmentAnswer>): List<AssessmentOption> = assessment.questions.first { it.id == "efe_v2_q3" }.options

    fun valid(question: AssessmentQuestion, answer: AssessmentAnswer, answers: Map<String, AssessmentAnswer>): Boolean {
        if (question.id == "efe_v2_q10") return true
        if (question.id == "efe_v3_performance" && isPrimary(answers)) return answer.otherText.toIntOrNull()?.let { it in 0..500 } == true
        val allowed = (if (question.id == "efe_v2_q3") subjects(answers) else question.options).map { it.id }.toSet() + if (question.allowOther) setOf("other") else emptySet()
        val minimum = if (question.id == "efe_v2_q3") 3 else 1
        return answer.selectedOptionIds.size in minimum..question.maxSelections && answer.selectedOptionIds.all { it in allowed } && ("other" !in answer.selectedOptionIds || answer.otherText.isNotBlank())
    }

    fun withPerformance(profile: UniversityProfile, answers: List<AssessmentAnswer>): UniversityProfile {
        val map = answers.associateBy { it.questionId }
        if (isPrimary(map)) return profile.copy(meanGrade = null, subjectGrades = emptyList())
        val grade = map["efe_v3_performance"]?.selectedOptionIds?.singleOrNull()?.let { value -> KcseGrade.entries.firstOrNull { it.name == value } }
        return if (grade != null) profile.copy(meanGrade = grade) else profile
    }

}
