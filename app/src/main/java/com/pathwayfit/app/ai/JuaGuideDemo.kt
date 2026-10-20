package com.pathwayfit.app.ai

import com.pathwayfit.app.BuildConfig
import com.pathwayfit.app.core.model.*
import com.pathwayfit.app.engine.CourseRecommendationSet
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*

/** Offline pitch experience. All screens disclose that this is an AI demonstration. */
object JuaGuideDemo {
    val enabled get() = BuildConfig.JUA_DEMO_MODE

    suspend fun analyze(questions: List<AssessmentQuestion>, answers: List<AssessmentAnswer>, candidates: CourseRecommendationSet): GeminiAssessmentResult {
        delay(2200)
        val evidence = questions.mapNotNull { q ->
            answers.firstOrNull { it.questionId == q.id }?.let { a ->
                (q.options.filter { it.id in a.selectedOptionIds }.map { it.label } + listOf(a.otherText).filter { it.isNotBlank() }).takeIf { it.isNotEmpty() }?.joinToString()
            }
        }
        val interests = evidence.take(4).joinToString("; ").ifBlank { "your academic and career preferences" }
        val top = candidates.courses.take(5)
        return GeminiAssessmentResult(
            summary = "In this AI demo, your answers highlight $interests. Your five options below come from JuaFit’s course catalog and are ranked by the local matching engine. Explore the reasons and compare the practical next steps before choosing.",
            courseIds = top.map { it.course.id }, model = "JuaGuide AI Preview",
            insights = top.map { match -> GeminiCourseInsight(
                courseId = match.course.id, overview = match.course.overview,
                reasons = listOf("You told us: $interests.") + match.factors.filter { it.evidence.isNotEmpty() }.take(4).map { "${it.label}: ${it.evidence.joinToString(", ")}." },
                potential = "Explore ${match.course.field} through an introductory course, a small project and conversations with people in the field. Your interest is a starting point; compare it with your grades, study preferences and goals.",
                quote = "A good next step combines what interests you with skills you’re ready to develop.",
                furtherEducation = listOf("Explore related postgraduate or professional qualifications after completing the required entry qualification."),
                employmentSectors = match.careers.map { it.name }.ifEmpty { listOf(match.course.field) },
            ) }
        )
    }

    suspend fun chat(messages: List<Pair<String,String>>, context: JsonObject): GuideReply {
        delay(900)
        val text = messages.lastOrNull { it.first == "user" }?.second.orEmpty().lowercase()
        val turns = messages.count { it.first == "user" }
        fun reply(value: String, complete: Boolean = false) = GuideReply(value, complete)
        if (context["attachment"] != null && turns == 1) return reply("Your academic-results file is attached. In this AI demo, please tell me the qualification shown and your strongest subjects so we can demonstrate a personalized discussion. What did you study, and what type of work interests you?")
        if (text.contains("university") || text.contains("top 5") || text.contains("recommend") || text.contains("course") || (context["attachment"] != null && turns >= 3)) {
            val catalog = context["catalog"]?.jsonArray.orEmpty()
            val terms = listOf("computer", "technology", "business", "education", "health", "engineering", "science", "agriculture", "design", "math").filter { it in text }
            val ranked = catalog.sortedByDescending { entry -> terms.count { it in entry.jsonObject["course"]?.jsonPrimitive?.content.orEmpty().lowercase() } }.take(5)
            val options = ranked.mapIndexed { i, entry ->
                val course = entry.jsonObject["course"]?.jsonPrimitive?.content.orEmpty()
                val universities = entry.jsonObject["universities"]?.jsonPrimitive?.content.orEmpty().split(", ").firstOrNull().orEmpty()
                "${i+1}. $course\nUniversity option: ${universities.ifBlank { "Confirm an institution offering this programme" }}.\nReason: explore the subject content, related careers and how this direction fits your stated interests and qualification."
            }.joinToString("\n\n")
            return reply("Here are five illustrative course directions from our catalog:\n\n$options\n\nUniversity listings need confirmation; these are demonstration suggestions, not admission decisions. Which option would you like to explore further?", true)
        }
        val topic = listOf(
            listOf("scholarship", "fees", "fund", "budget") to "Start with a realistic budget: tuition, accommodation, transport and learning materials. Compare public and private institutions, then check official scholarship and funding opportunities. Deadlines and eligibility vary, so confirm them directly. What budget range are you planning for?",
            listOf("grade", "kcse", "result", "transcript") to "Your results help us understand academic strengths, but a grade alone doesn’t define your future. Consider degree, diploma and certificate routes that fit your qualification, then verify each institution’s current entry requirements. Which qualification and subjects are on your results?",
            listOf("computer", "software", "coding", "technology", "data") to "Technology has several directions: software development, information systems, networking and data analysis. If you enjoy building things, try a small coding project; if you enjoy patterns, try analyzing a spreadsheet. Pair that experience with your mathematics and computing results. Would you rather build applications or analyze information?",
            listOf("business", "entrepreneur", "finance", "account") to "Business routes include accounting, economics, business management and information systems. Compare whether you enjoy numbers, organizing people or developing ideas. A practical starting point is a simple business plan or budget project. Which of those tasks feels most natural to you?",
            listOf("doctor", "nurse", "health", "medicine") to "Healthcare includes clinical care, public health, laboratory work and health administration. These paths have different subject and licensing requirements. Think about whether you prefer direct patient contact or scientific investigation. Which subjects did you enjoy, and are you comfortable working closely with patients?",
            listOf("engineer", "physics", "build") to "Engineering combines mathematics, scientific reasoning and practical problem solving. Compare civil, electrical, mechanical and related technical pathways. Try a hands-on project and confirm the programme’s recognition and entry requirements. What kinds of systems or structures would you like to build?",
            listOf("teach", "education", "community") to "Education can suit someone who enjoys explaining ideas and helping others learn. Compare subject teaching, training and related community roles. Try tutoring a topic you understand well and reflect on whether you enjoy the process. Which subjects would you feel excited to teach?",
            listOf("agricultur", "environment", "nature") to "Environmental and agricultural pathways combine science with practical work and community needs. Explore agriculture, environmental science and resource management, then compare laboratory, field and business roles. Are you most interested in conservation, food production or agricultural enterprise?",
            listOf("art", "design", "creative", "music") to "Creative pathways reward practice and a portfolio. Explore design, media, performing arts and related business skills. Build a small sample project and get feedback before committing to a course. What do you enjoy creating: visual work, stories, music or digital experiences?",
            listOf("job", "employment", "salary") to "A useful career plan combines a suitable qualification, practical skills and evidence of experience. Research real vacancies, note recurring skills, and build projects or placements around them. Avoid choosing on salary alone. What kind of day-to-day work would you enjoy?",
            listOf("next", "plan", "start") to "Here’s a practical next-step plan:\n1. Review your strongest subjects and interests.\n2. Shortlist three to five course directions.\n3. Verify admission requirements and costs.\n4. Speak with a student or professional.\n5. Try a small project in your preferred field.\nWhich step would you like us to work through together?",
            listOf("hello", "hi", "hey") to "Hi! I’m JuaGuide. We can explore courses, careers and academic pathways together. Tell me your qualification and one subject or activity you enjoy, and we’ll take it from there.",
        ).firstOrNull { (keywords, _) -> keywords.any { it in text } }
        return reply(topic?.second ?: "Let’s narrow this down together. Think about three things: what you enjoy, what you do well, and the kind of work you want to do. Your assessment answers can help us compare options. Tell me your strongest subject and one career you’re curious about, or ask for your top five course directions.")
    }
}
