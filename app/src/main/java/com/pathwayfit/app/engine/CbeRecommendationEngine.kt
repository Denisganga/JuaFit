package com.pathwayfit.app.engine

import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentQuestion
import com.pathwayfit.app.core.model.Career
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.CbeArea
import com.pathwayfit.app.core.model.CbePathway
import com.pathwayfit.app.core.model.CbeProfile
import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.CbeTrack
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.FactorScore
import com.pathwayfit.app.core.model.SignalCategory
import com.pathwayfit.app.core.model.TagCatalog

/** Configurable CBE weights (defaults from the product spec). Re-normalised at runtime. */
data class CbeScoringWeights(
    val interest: Float = 0.30f,
    val strength: Float = 0.25f,
    val learningPreference: Float = 0.20f,
    val academicPreference: Float = 0.15f,
    val careerInterest: Float = 0.10f,
)

data class CbeEngineInput(
    val profile: CbeProfile,
    val questions: List<AssessmentQuestion>,
    val answers: List<AssessmentAnswer>,
    val reference: CbeReference,
    val careers: CareerData,
)

data class CbePathwayMatch(
    val pathway: CbePathway,
    val score: Float,
    val factors: List<FactorScore>,
    val bestTrack: ScoredItem<CbeTrack>?,
    val bestArea: ScoredItem<CbeArea>?,
    val careers: List<ScoredItem<Career>>,
) {
    val percent: Int get() = (score * 100).toInt().coerceIn(0, 100)
}

data class CbeRecommendationSet(
    val pathways: List<CbePathwayMatch>,
    val tracks: List<ScoredItem<CbeTrack>>,
    val areas: List<ScoredItem<CbeArea>>,
    val careers: List<ScoredItem<Career>>,
    val strengthProfile: List<String>,
    val interestHighlights: List<String>,
    val generatedAt: Long = System.currentTimeMillis(),
) {
    val top: CbePathwayMatch? get() = pathways.firstOrNull()

    /** Every track/area direction with a reasonable fit — used for "We found N directions". */
    val directions: List<Direction>
        get() {
            val withAreas = areas.filter { it.percent >= 45 }.map { Direction(it.item.name, it.item.trackId, it.item.id, it.percent) }
            val trackOnly = tracks.filter { t -> t.percent >= 45 && areas.none { it.item.trackId == t.item.id } }
                .map { Direction(it.item.name, it.item.id, null, it.percent) }
            return (withAreas + trackOnly).sortedByDescending { it.percent }
        }

    data class Direction(val name: String, val trackId: String, val areaId: String?, val percent: Int)
}

/**
 * Scores CBE pathways, tracks, areas and related careers from the learner's CBE profile
 * and CBE assessment. Independent of the 8-4-4 engine: it never reads grades, courses
 * or universities.
 */
class CbeRecommendationEngine(
    private val weights: CbeScoringWeights = CbeScoringWeights(),
) : RecommendationEngine<CbeEngineInput, CbeRecommendationSet> {

    override val system = EducationSystem.CBE

    fun signals(input: CbeEngineInput): SignalProfile {
        val b = SignalProfile.Builder().addAnswers(input.questions, input.answers)
        val p = input.profile
        b.addAll(SignalCategory.INTEREST, TagCatalog.tagsFor(p.interests, TagCatalog.interestOptions), 0.8f)
        b.addAll(SignalCategory.STRENGTH, TagCatalog.tagsFor(p.strengths, TagCatalog.strengthOptions), 0.8f)
        b.addAll(SignalCategory.LEARNING_PREFERENCE, TagCatalog.tagsFor(p.favouriteActivities, TagCatalog.activityOptions), 0.6f)
        p.learningAreas.forEach { subjectId ->
            input.reference.areas.filter { subjectId in it.subjectIds }.forEach { b.addAll(SignalCategory.ACADEMIC_PREFERENCE, it.tags, 0.5f) }
        }
        p.careerInterests.forEach { id -> input.careers.career(id)?.let { b.addAll(SignalCategory.CAREER_INTEREST, it.tags, 0.8f) } }
        return b.build()
    }

    private fun score(tags: List<String>, s: SignalProfile): ScoredItem<List<String>> {
        val interest = s.of(SignalCategory.INTEREST)
        val strength = s.of(SignalCategory.STRENGTH, SignalCategory.COMPETENCY)
        val learning = s.of(SignalCategory.LEARNING_PREFERENCE, SignalCategory.PRACTICAL_PREFERENCE)
        val academic = s.of(SignalCategory.ACADEMIC_PREFERENCE)
        val career = s.of(SignalCategory.CAREER_INTEREST)
        val raw = listOf(
            Matching.factor("interest", "Interest match", Matching.coverage(tags, interest), weights.interest, Matching.matchedLabels(tags, interest)),
            Matching.factor("strength", "Strength match", Matching.coverage(tags, strength), weights.strength, Matching.matchedLabels(tags, strength)),
            Matching.factor("learning", "Learning preference", Matching.coverage(tags, learning), weights.learningPreference, Matching.matchedLabels(tags, learning)),
            Matching.factor("academic", "Academic preference", Matching.coverage(tags, academic), weights.academicPreference, Matching.matchedLabels(tags, academic)),
            Matching.factor("career", "Career interest", Matching.coverage(tags, career), weights.careerInterest, Matching.matchedLabels(tags, career)),
        )
        val (total, factors) = Matching.combine(raw)
        return ScoredItem(tags, total, factors)
    }

    override fun recommend(input: CbeEngineInput): CbeRecommendationSet {
        val s = signals(input)
        val ref = input.reference

        val tracks = Matching.calibrate(ref.tracks.map { t -> score(t.tags, s).let { ScoredItem(t, it.score, it.factors) } })
            .sortedByDescending { it.score }

        val ownAreas = Matching.calibrate(ref.areas.filter { a -> ref.track(a.trackId) != null }.map { a ->
            score(a.tags + ref.track(a.trackId)!!.tags.take(3), s).let { ScoredItem(a, it.score, it.factors) }
        })
        val areas = ownAreas.map { own ->
            val trackScore = tracks.firstOrNull { it.item.id == own.item.trackId }?.score ?: 0f
            ScoredItem(own.item, 0.6f * own.score + 0.4f * trackScore, own.factors)
        }.sortedByDescending { it.score }

        val ownCareers = Matching.calibrate(input.careers.careers.map { c -> score(c.tags, s).let { ScoredItem(c, it.score, it.factors) } })
        val careers = ownCareers.map { own ->
            val c = own.item
            val bestTrack = tracks.filter { it.item.id in c.cbeTrackIds }.maxOfOrNull { it.score } ?: 0f
            val bonus = if (c.id in input.profile.careerInterests) 0.05f else 0f
            ScoredItem(c, (0.55f * own.score + 0.45f * bestTrack + bonus).coerceAtMost(1f), own.factors)
        }.sortedByDescending { it.score }

        val pathways = ref.pathways.map { p ->
            val pTracks = tracks.filter { it.item.pathwayId == p.id }
            val best = pTracks.firstOrNull()
            val mean = if (pTracks.isEmpty()) 0f else pTracks.map { it.score }.average().toFloat()
            val score = if (best == null) 0f else 0.85f * best.score + 0.15f * mean
            val bestArea = best?.let { b -> areas.firstOrNull { it.item.trackId == b.item.id } }
            val pCareers = careers.filter { c -> pTracks.any { it.item.id in c.item.cbeTrackIds } }.take(5)
            CbePathwayMatch(p, score, best?.factors ?: emptyList(), best, bestArea, pCareers)
        }.sortedByDescending { it.score }

        return CbeRecommendationSet(
            pathways = pathways,
            tracks = tracks,
            areas = areas,
            careers = careers,
            strengthProfile = s.topTags(3, SignalCategory.STRENGTH, SignalCategory.COMPETENCY).map(TagCatalog::label),
            interestHighlights = s.topTags(4, SignalCategory.INTEREST, SignalCategory.ACADEMIC_PREFERENCE).map(TagCatalog::label),
        )
    }
}
