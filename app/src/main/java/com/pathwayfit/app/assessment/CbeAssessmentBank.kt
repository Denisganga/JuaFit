package com.pathwayfit.app.assessment

import com.pathwayfit.app.core.model.Assessment
import com.pathwayfit.app.core.model.AssessmentOption
import com.pathwayfit.app.core.model.AssessmentQuestion
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.QuestionType.MULTI_SELECT
import com.pathwayfit.app.core.model.QuestionType.RATING
import com.pathwayfit.app.core.model.QuestionType.SCENARIO
import com.pathwayfit.app.core.model.QuestionType.SINGLE_CHOICE
import com.pathwayfit.app.core.model.SignalCategory

/**
 * CBE assessment: interests, competencies, strengths and learning preferences that
 * feed pathway, track and area suitability. Written for Junior Secondary learners.
 * It shares no questions with the 8-4-4 assessment.
 */
object CbeAssessmentBank {

    private fun o(id: String, label: String, vararg tags: Pair<String, Float>) =
        AssessmentOption(id, label, tags.toMap())

    val assessment = Assessment(
        id = "cbe_v1",
        system = EducationSystem.CBE,
        title = "Discover Your CBE Pathway",
        questions = listOf(
            AssessmentQuestion(
                id = "cbe_q1", type = MULTI_SELECT, category = SignalCategory.INTEREST, maxSelections = 3,
                prompt = "Which of these topics make you curious?",
                helper = "Pick up to 3.", allowOther = true,
                options = listOf(
                    o("a", "How the human body works", "health" to 1f, "science" to 1f),
                    o("b", "Computers, apps and coding", "computing" to 1f, "technology" to 1f),
                    o("c", "Machines, engines and electricity", "mechanics" to 1f, "electrical" to 1f, "engineering" to 1f),
                    o("d", "Stories, poems and languages", "literature" to 1f, "languages" to 1f),
                    o("e", "People, history and communities", "history" to 1f, "society" to 1f),
                    o("f", "Music, dance and drama", "music" to 1f, "performing" to 1f, "drama" to 1f),
                    o("g", "Drawing, design and crafts", "visual" to 1f, "design" to 1f, "crafts" to 1f),
                    o("h", "Sports and fitness", "sports" to 1f, "fitness" to 1f),
                    o("i", "Plants, animals and farming", "agriculture" to 1f, "nature" to 1f),
                    o("j", "Business and money", "business" to 1f, "money" to 1f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q2", type = SINGLE_CHOICE, category = SignalCategory.LEARNING_PREFERENCE,
                prompt = "How do you learn best?",
                options = listOf(
                    o("a", "Doing experiments and testing ideas", "research" to 1f, "science" to 0.6f, "hands_on" to 0.5f),
                    o("b", "Building or making something with my hands", "hands_on" to 1f, "practical" to 1f),
                    o("c", "Reading, writing and discussing", "discussion" to 1f, "writing" to 0.7f, "languages" to 0.5f),
                    o("d", "Watching, drawing and visualising", "visual_learning" to 1f, "visual" to 0.6f),
                    o("e", "Moving, practising and performing", "performing" to 0.7f, "fitness" to 0.7f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q3", type = RATING, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "How much do you enjoy Mathematics?",
                ratingSignals = mapOf("maths" to 1f, "analytical" to 0.6f, "data" to 0.4f),
            ),
            AssessmentQuestion(
                id = "cbe_q4", type = RATING, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "How much do you enjoy Integrated Science?",
                ratingSignals = mapOf("science" to 1f, "research" to 0.5f, "health" to 0.3f),
            ),
            AssessmentQuestion(
                id = "cbe_q5", type = RATING, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "How much do you enjoy English, Kiswahili or other languages?",
                ratingSignals = mapOf("languages" to 1f, "literature" to 0.6f, "communication" to 0.5f),
            ),
            AssessmentQuestion(
                id = "cbe_q6", type = RATING, category = SignalCategory.ACADEMIC_PREFERENCE,
                prompt = "How much do you enjoy Creative Arts and Sports?",
                ratingSignals = mapOf("arts" to 0.8f, "creative" to 0.6f, "sports" to 0.6f, "performing" to 0.4f),
            ),
            AssessmentQuestion(
                id = "cbe_q7", type = SCENARIO, category = SignalCategory.COMPETENCY,
                prompt = "Your class is organising a school open day. Which job would you volunteer for?",
                allowOther = true,
                options = listOf(
                    o("a", "Setting up the sound system and lights", "electrical" to 1f, "technology" to 0.7f, "hands_on" to 0.5f),
                    o("b", "Designing posters and decorating the hall", "visual" to 1f, "design" to 1f, "creative" to 0.6f),
                    o("c", "Being the MC and welcoming guests", "communication" to 1f, "people" to 0.7f, "leadership" to 0.4f),
                    o("d", "Running a science demonstration", "science" to 1f, "research" to 0.6f),
                    o("e", "Selling snacks to raise money", "business" to 1f, "entrepreneurial" to 1f, "money" to 0.6f),
                    o("f", "Performing a song, dance or play", "performing" to 1f, "music" to 0.6f, "drama" to 0.6f),
                    o("g", "Organising a sports competition", "sports" to 1f, "organising" to 0.6f, "leadership" to 0.4f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q8", type = SCENARIO, category = SignalCategory.STRENGTH,
                prompt = "A water pipe in your home starts leaking. What do you do first?",
                options = listOf(
                    o("a", "Work out where the leak is coming from and why", "analytical" to 1f, "problem_solving" to 1f),
                    o("b", "Get tools and try to fix it", "hands_on" to 1f, "practical" to 1f, "mechanics" to 0.5f),
                    o("c", "Calmly explain the problem and call someone who can help", "communication" to 1f, "people" to 0.5f),
                    o("d", "Organise buckets and keep everyone safe", "organising" to 1f, "care" to 0.5f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q9", type = MULTI_SELECT, category = SignalCategory.STRENGTH, maxSelections = 3,
                prompt = "What do teachers or friends say you are good at?",
                helper = "Pick up to 3.",
                options = listOf(
                    o("a", "Solving hard problems", "problem_solving" to 1f, "analytical" to 0.6f),
                    o("b", "Coming up with original ideas", "creative" to 1f),
                    o("c", "Explaining things clearly", "communication" to 1f),
                    o("d", "Leading a group", "leadership" to 1f),
                    o("e", "Working well in a team", "teamwork" to 1f),
                    o("f", "Fixing and making things", "hands_on" to 1f, "practical" to 0.6f),
                    o("g", "Being kind and patient with others", "care" to 1f, "helping" to 0.8f),
                    o("h", "Staying fit and active", "fitness" to 1f, "sports" to 0.6f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q10", type = SINGLE_CHOICE, category = SignalCategory.PRACTICAL_PREFERENCE,
                prompt = "Where would you most like to spend a school day?",
                options = listOf(
                    o("a", "In a science laboratory", "science" to 1f, "research" to 0.6f),
                    o("b", "In a workshop with tools", "hands_on" to 1f, "engineering" to 0.8f),
                    o("c", "In a computer lab", "computing" to 1f, "technology" to 0.6f),
                    o("d", "On a school farm or field trip", "agriculture" to 0.8f, "outdoor" to 1f, "nature" to 0.6f),
                    o("e", "In the library or a debate room", "literature" to 0.7f, "discussion" to 1f),
                    o("f", "In the art or music room", "arts" to 1f, "music" to 0.5f, "visual" to 0.5f),
                    o("g", "On the sports field", "sports" to 1f, "fitness" to 0.6f, "outdoor" to 0.4f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q11", type = SCENARIO, category = SignalCategory.COMPETENCY,
                prompt = "Your community has a problem with plastic waste. Which project would you start?",
                allowOther = true,
                options = listOf(
                    o("a", "Test which materials break down fastest", "science" to 1f, "research" to 1f),
                    o("b", "Build a machine that sorts or recycles plastic", "engineering" to 1f, "mechanics" to 0.6f),
                    o("c", "Write and share messages that change people's habits", "communication" to 1f, "writing" to 0.7f, "society" to 0.5f),
                    o("d", "Turn plastic into art or useful products", "crafts" to 1f, "creative" to 0.8f, "design" to 0.5f),
                    o("e", "Start a small recycling business", "entrepreneurial" to 1f, "business" to 0.8f),
                    o("f", "Make an app to track clean-ups", "computing" to 1f, "technology" to 0.6f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q12", type = RATING, category = SignalCategory.LEARNING_PREFERENCE,
                prompt = "I enjoy working with my hands more than working on paper.",
                ratingSignals = mapOf("hands_on" to 1f, "practical" to 0.8f),
                ratingLabels = "Disagree" to "Agree",
            ),
            AssessmentQuestion(
                id = "cbe_q13", type = RATING, category = SignalCategory.STRENGTH,
                prompt = "I like performing or presenting in front of others.",
                ratingSignals = mapOf("performing" to 0.8f, "communication" to 0.8f, "drama" to 0.4f),
                ratingLabels = "Disagree" to "Agree",
            ),
            AssessmentQuestion(
                id = "cbe_q14", type = MULTI_SELECT, category = SignalCategory.CAREER_INTEREST, maxSelections = 3,
                prompt = "Which kinds of work could you see yourself doing one day?",
                helper = "Pick up to 3. You can change your mind later!", allowOther = true,
                options = listOf(
                    o("a", "Treating patients and keeping people healthy", "health" to 1f, "care" to 0.7f),
                    o("b", "Designing apps, games or websites", "computing" to 1f, "creative" to 0.4f),
                    o("c", "Building roads, houses or machines", "engineering" to 1f, "building" to 0.7f),
                    o("d", "Teaching or counselling young people", "helping" to 1f, "people" to 0.8f),
                    o("e", "Law, government or leading organisations", "law" to 1f, "leadership" to 0.8f),
                    o("f", "Running a business", "business" to 1f, "entrepreneurial" to 0.8f),
                    o("g", "Writing, journalism or translation", "writing" to 1f, "languages" to 0.6f),
                    o("h", "Music, film or performing", "performing" to 1f, "film" to 0.6f),
                    o("i", "Art, fashion or design", "design" to 1f, "visual" to 0.7f),
                    o("j", "Sports or coaching", "sports" to 1f, "coaching" to 0.8f),
                    o("k", "Farming, food or the environment", "agriculture" to 1f, "nature" to 0.7f),
                ),
            ),
            AssessmentQuestion(
                id = "cbe_q15", type = SINGLE_CHOICE, category = SignalCategory.INTEREST,
                prompt = "If you could spend a free afternoon on one thing, it would be…",
                options = listOf(
                    o("a", "Figuring out how something works", "analytical" to 1f, "science" to 0.6f, "engineering" to 0.5f),
                    o("b", "Creating something new", "creative" to 1f, "visual" to 0.4f, "music" to 0.3f),
                    o("c", "Helping someone with a problem", "helping" to 1f, "people" to 0.7f),
                    o("d", "Reading or writing", "literature" to 1f, "writing" to 0.7f),
                    o("e", "Playing or training for a sport", "sports" to 1f, "fitness" to 0.6f),
                    o("f", "Planning a project or event", "organising" to 1f, "leadership" to 0.6f, "entrepreneurial" to 0.4f),
                ),
            ),
        ),
    )
}
