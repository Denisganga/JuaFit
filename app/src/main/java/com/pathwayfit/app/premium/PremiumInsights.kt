package com.pathwayfit.app.premium

import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.University
import com.pathwayfit.app.core.model.UniversityData
import com.pathwayfit.app.core.model.UniversityProfile
import com.pathwayfit.app.engine.CbePathwayMatch
import com.pathwayfit.app.engine.CbeRecommendationSet
import com.pathwayfit.app.engine.CourseMatch
import com.pathwayfit.app.engine.CourseRecommendationSet

/* Premium data structures — built from the free engine output, adding depth not new facts. */

data class PremiumInsight(val title: String, val body: String)

data class PremiumRecommendation(val rank: Int, val title: String, val percent: Int, val subtitle: String)

data class CareerMapStage(val title: String, val items: List<String>, val detail: String, val isFuture: Boolean = false)

data class PremiumCareerMap(val system: EducationSystem, val stages: List<CareerMapStage>)

data class ComparisonRow(val label: String, val a: String, val b: String)

data class PremiumCourseComparison(
    val a: CourseMatch,
    val b: CourseMatch,
    val scoreRows: List<ComparisonRow>,
    val detailRows: List<ComparisonRow>,
    val verdict: String,
)

data class ShortlistEntry(val university: University, val courses: List<CourseMatch>, val reasons: List<String>)

data class PremiumUniversityShortlist(val entries: List<ShortlistEntry>)

data class YearPlan(val title: String, val items: List<String>)

data class WhatIfComparison(val a: CbePathwayMatch, val b: CbePathwayMatch, val rows: List<ComparisonRow>)

object PremiumInsightBuilder {

    const val GUIDANCE_NOTE = "This is personal guidance to help you plan. It is not an official admission, placement or selection plan."

    // ---------------- CBE ----------------

    fun cbePathwayMap(grade: String, set: CbeRecommendationSet, ref: CbeReference, careers: CareerData, uni: UniversityData?): PremiumCareerMap {
        val top = set.top
        val area = top?.bestArea?.item
        val track = top?.bestTrack?.item
        val learningAreas = (area?.let { ref.subjectsOf(it) } ?: emptyList()).map { it.name } + ref.coreSubjects().map { "${it.name} (core)" }
        val careerItems = top?.careers?.take(4)?.map { it.item } ?: emptyList()
        val uniDirections = careerItems.flatMap { it.courseIds }.distinct()
            .mapNotNull { id -> uni?.course(id)?.name }.take(4)
        return PremiumCareerMap(
            EducationSystem.CBE,
            listOf(
                CareerMapStage("Current Grade", listOf(grade.ifBlank { "Junior Secondary" }), "Where you are today."),
                CareerMapStage("Recommended Pathway", listOfNotNull(top?.pathway?.name), top?.pathway?.description ?: ""),
                CareerMapStage("Recommended Track", listOfNotNull(track?.name), track?.description ?: ""),
                CareerMapStage("Recommended Area", listOfNotNull(area?.name).ifEmpty { listOf("Areas to be confirmed") }, area?.description ?: ""),
                CareerMapStage("Relevant Learning Areas", learningAreas, "Subjects you would study in senior school."),
                CareerMapStage("Future Study Options", track?.futureStudy ?: emptyList(), "Where this track can lead after senior school.", isFuture = true),
                CareerMapStage("Career Possibilities", careerItems.map { it.name }, "Careers connected to this direction.", isFuture = true),
                CareerMapStage("University Direction", uniDirections.ifEmpty { listOf("Explore later") }, "Possible future university courses — years away, shown as possibilities only.", isFuture = true),
            ),
        )
    }

    fun whatIf(a: CbePathwayMatch, b: CbePathwayMatch): WhatIfComparison {
        fun pct(m: CbePathwayMatch, key: String) = m.factors.firstOrNull { it.key == key }?.let { "${(it.score * 100).toInt()}%" } ?: "—"
        return WhatIfComparison(
            a, b,
            listOf(
                ComparisonRow("Overall match", "${a.percent}%", "${b.percent}%"),
                ComparisonRow("Strength alignment", pct(a, "strength"), pct(b, "strength")),
                ComparisonRow("Interest alignment", pct(a, "interest"), pct(b, "interest")),
                ComparisonRow("Best track", a.bestTrack?.item?.name ?: "—", b.bestTrack?.item?.name ?: "—"),
                ComparisonRow("Possible study directions", a.bestTrack?.item?.futureStudy?.joinToString(", ") ?: "—", b.bestTrack?.item?.futureStudy?.joinToString(", ") ?: "—"),
                ComparisonRow("Career possibilities", a.careers.take(3).joinToString(", ") { it.item.name }, b.careers.take(3).joinToString(", ") { it.item.name }),
                ComparisonRow("Skills developed", a.bestTrack?.item?.skills?.joinToString(", ") ?: "—", b.bestTrack?.item?.skills?.joinToString(", ") ?: "—"),
            ),
        )
    }

    fun cbeNextThreeYears(set: CbeRecommendationSet, ref: CbeReference): List<YearPlan> {
        val top = set.top
        val area = top?.bestArea?.item
        val subjects = area?.let { ref.subjectsOf(it).map { s -> s.name } } ?: emptyList()
        val track = top?.bestTrack?.item?.name ?: "your recommended track"
        return listOf(
            YearPlan("Year 1 — Explore", listOf(
                "Talk to your teachers and parents about ${top?.pathway?.name ?: "your pathway"}",
                "Strengthen learning areas linked to $track" + if (subjects.isNotEmpty()) " (${subjects.joinToString()})" else "",
                "Join a club or activity that uses your strengths: ${set.strengthProfile.joinToString().lowercase()}",
            )),
            YearPlan("Year 2 — Build", listOf(
                "Try a small project connected to ${area?.name ?: track}",
                "Learn about 2–3 careers: ${top?.careers?.take(3)?.joinToString { it.item.name } ?: "from your results"}",
                "Retake the JuaFit assessment to see how your interests are developing",
            )),
            YearPlan("Year 3 — Decide", listOf(
                "Confirm your senior school pathway choice with your school",
                "Compare your top two pathways using What If?",
                "Keep building skills: ${top?.bestTrack?.item?.skills?.joinToString()?.lowercase() ?: "communication and problem solving"}",
            )),
        )
    }

    fun cbeSubjectGuidance(set: CbeRecommendationSet, ref: CbeReference): List<PremiumInsight> =
        set.areas.take(3).map { a ->
            PremiumInsight(a.item.name, "Learning areas to focus on: " + ref.subjectsOf(a.item).joinToString { it.name }.ifBlank { "to be confirmed from the CBE reference" })
        }

    // ---------------- 8-4-4 ----------------

    fun universityShortlist(set: CourseRecommendationSet, profile: UniversityProfile, max: Int = 6): PremiumUniversityShortlist {
        val topCourses = set.courses.take(10)
        val byUni = topCourses.flatMap { cm -> cm.universities.map { it to cm } }.groupBy({ it.first }, { it.second })
        val entries = byUni.map { (u, courses) ->
            val reasons = buildList {
                add("${courses.size} of your top course matches listed here")
                if (u.county in profile.preferredLocations) add("In ${u.county}, a county you prefer")
                if (u.type in profile.institutionTypes) add(u.type.label)
            }
            ShortlistEntry(u, courses.sortedByDescending { it.score }, reasons)
        }.sortedWith(compareByDescending<ShortlistEntry> { e ->
            e.courses.maxOf { it.score } + (if (e.university.county in profile.preferredLocations) 0.1f else 0f) +
                (if (e.university.type in profile.institutionTypes) 0.05f else 0f) + e.courses.size * 0.02f
        })
        return PremiumUniversityShortlist(entries.take(max))
    }

    fun compare(a: CourseMatch, b: CourseMatch): PremiumCourseComparison {
        fun pct(m: CourseMatch, key: String) = m.factor(key)?.let { "${(it.score * 100).toInt()}%" } ?: "—"
        val scoreRows = listOf(
            ComparisonRow("Overall", "${a.percent}%", "${b.percent}%"),
            ComparisonRow("Interest Match", pct(a, "interest"), pct(b, "interest")),
            ComparisonRow("Academic Match", pct(a, "academic"), pct(b, "academic")),
            ComparisonRow("Career Match", pct(a, "career"), pct(b, "career")),
        )
        val detailRows = listOf(
            ComparisonRow("Strength Match", pct(a, "strength"), pct(b, "strength")),
            ComparisonRow("Subject Match", pct(a, "subject"), pct(b, "subject")),
            ComparisonRow("Skills", a.course.skills.joinToString(", "), b.course.skills.joinToString(", ")),
            ComparisonRow("Career directions", a.careers.joinToString(", ") { it.name }, b.careers.joinToString(", ") { it.name }),
            ComparisonRow("Universities listed", a.universities.size.toString(), b.universities.size.toString()),
        )
        val winner = if (a.score >= b.score) a else b
        val other = if (winner === a) b else a
        val lead = winner.factors.maxByOrNull { f -> f.score - (other.factor(f.key)?.score ?: 0f) }
        val verdict = "${winner.course.name} is the stronger fit for your profile overall" +
            (lead?.let { ", mainly because of a higher ${it.label.lowercase()}." } ?: ".") +
            " Both remain options — the gap is ${kotlin.math.abs(a.percent - b.percent)} points."
        return PremiumCourseComparison(a, b, scoreRows, detailRows, verdict)
    }

    fun universityCareerMap(name: String, set: CourseRecommendationSet, shortlist: PremiumUniversityShortlist): PremiumCareerMap {
        val top = set.courses.take(3)
        return PremiumCareerMap(
            EducationSystem.EIGHT_FOUR_FOUR,
            listOf(
                CareerMapStage("You", listOf(name), "Your KCSE profile and assessment."),
                CareerMapStage("Your Strengths", set.academicStrengths.ifEmpty { listOf("Add your KCSE grades") }, "Your strongest KCSE subjects."),
                CareerMapStage("Recommended Direction", top.map { it.course.field }.distinct(), "Fields where your matches cluster."),
                CareerMapStage("Possible Courses", top.map { "${it.course.name} · ${it.percent}%" }, "Your top JuaFit matches."),
                CareerMapStage("Universities", shortlist.entries.take(4).map { it.university.name }, "From your shortlist — verify programme availability with each institution."),
                CareerMapStage("Skills to Build", top.flatMap { it.course.skills }.distinct().take(5), "Skills these courses develop."),
                CareerMapStage("Potential Careers", top.flatMap { it.careers }.distinctBy { it.id }.take(5).map { it.name }, "Careers these courses can lead to.", isFuture = true),
            ),
        )
    }

    fun universityNextThreeYears(set: CourseRecommendationSet, data: UniversityData, profile: UniversityProfile): List<YearPlan> {
        val top = set.top
        val weak = top?.course?.relevantSubjects
            ?.filter { code -> (profile.subjectGrades.firstOrNull { it.subjectCode == code }?.grade?.points ?: 0) < 9 }
            ?.mapNotNull { data.subject(it)?.name } ?: emptyList()
        return listOf(
            YearPlan("Year 1", listOfNotNull(
                if (weak.isNotEmpty()) "Strengthen subjects relevant to ${top?.course?.name}: ${weak.joinToString()}" else "Keep your strongest subjects sharp",
                "Explore the ${top?.course?.field ?: "career"} field through talks, videos or job shadowing",
                "Develop foundational skills: ${top?.course?.skills?.take(2)?.joinToString()?.lowercase() ?: "communication and digital skills"}",
            )),
            YearPlan("Year 2", listOf(
                "Explore and compare your top courses",
                "Research universities on your shortlist and confirm programme availability with each one",
                "Build a small project related to ${top?.course?.name ?: "your field"}",
            )),
            YearPlan("Year 3", listOf(
                "Prepare for the university application process through official channels",
                "Compare courses one final time against current official requirements",
                "Prepare an application strategy with a counsellor or teacher",
            )),
        )
    }

    fun subjectGuidance(set: CourseRecommendationSet, data: UniversityData, profile: UniversityProfile): List<PremiumInsight> =
        set.courses.take(3).map { cm ->
            val lines = cm.course.relevantSubjects.mapNotNull { code ->
                val name = data.subject(code)?.name ?: return@mapNotNull null
                val g = profile.subjectGrades.firstOrNull { it.subjectCode == code }?.grade
                "$name: ${g?.label ?: "not entered"}"
            }
            PremiumInsight(cm.course.name, "Commonly relevant subjects — " + lines.joinToString("; "))
        }
}
