package com.pathwayfit.app.engine

import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentQuestion
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.FactorScore
import com.pathwayfit.app.core.model.QuestionType
import com.pathwayfit.app.core.model.SignalCategory
import com.pathwayfit.app.core.model.TagCatalog
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Common contract. Each education system has its own engine with its own rules. */
interface RecommendationEngine<in I, out R> {
    val system: EducationSystem
    fun recommend(input: I): R
}

/** A scored candidate (track, area, course, career …) with an explainable breakdown. */
data class ScoredItem<T>(val item: T, val score: Float, val factors: List<FactorScore>) {
    val percent: Int get() = (score * 100).roundToInt().coerceIn(0, 100)
    fun factor(key: String) = factors.firstOrNull { it.key == key }
}

/**
 * The learner's answers turned into weighted tags, grouped by what each question measures.
 * Keys starting with "pref:" are preferences rather than interests.
 */
class SignalProfile(
    private val byCategory: Map<SignalCategory, Map<String, Float>>,
) {
    fun of(vararg categories: SignalCategory): Map<String, Float> {
        val out = HashMap<String, Float>()
        categories.forEach { c -> byCategory[c]?.forEach { (t, w) -> if (!t.startsWith("pref:")) out[t] = (out[t] ?: 0f) + w } }
        return out
    }

    fun preference(key: String): Float =
        byCategory.values.sumOf { (it["pref:$key"] ?: 0f).toDouble() }.toFloat()

    fun all(): Map<String, Float> = of(*SignalCategory.entries.toTypedArray())

    fun topTags(n: Int, vararg categories: SignalCategory): List<String> {
        val source = if (categories.isEmpty()) all() else of(*categories)
        return source.entries.sortedByDescending { it.value }.take(n).map { it.key }
    }

    val isEmpty: Boolean get() = byCategory.values.all { it.isEmpty() }

    class Builder {
        private val data = HashMap<SignalCategory, HashMap<String, Float>>()

        fun add(category: SignalCategory, tag: String, weight: Float) {
            if (weight <= 0f) return
            val bucket = data.getOrPut(category) { HashMap() }
            bucket[tag] = (bucket[tag] ?: 0f) + weight
        }

        fun addAll(category: SignalCategory, tags: Collection<String>, weight: Float = 1f) =
            tags.forEach { add(category, it, weight) }

        fun addAnswers(questions: List<AssessmentQuestion>, answers: List<AssessmentAnswer>): Builder {
            val byId = answers.associateBy { it.questionId }
            for (q in questions) {
                val a = byId[q.id] ?: continue
                when (q.type) {
                    QuestionType.RATING -> {
                        val r = a.rating ?: continue
                        val factor = ((r - 1).coerceIn(0, 4)) / 4f
                        q.ratingSignals.forEach { (t, w) -> add(q.category, t, w * factor) }
                    }
                    else -> q.options.filter { it.id in a.selectedOptionIds }.forEach { opt ->
                        opt.signals.forEach { (t, w) -> add(q.category, t, w) }
                    }
                }
            }
            return this
        }

        fun build() = SignalProfile(data)
    }
}

object Matching {
    /**
     * Share of the learner's expressed signal (in one category) that a candidate's tags
     * explain, passed through a square root so partial overlap is not over-penalised.
     * Returns null when the learner gave no signal in that category — the factor is then
     * left out and the remaining weights are re-normalised.
     */
    fun coverage(tags: Collection<String>, signals: Map<String, Float>): Float? {
        val total = signals.values.sum()
        if (total <= 0f) return null
        val matched = tags.toSet().sumOf { (signals[it] ?: 0f).toDouble() }.toFloat()
        return sqrt((matched / total).coerceIn(0f, 1f))
    }

    fun matchedLabels(tags: Collection<String>, signals: Map<String, Float>, max: Int = 3): List<String> =
        tags.toSet().filter { (signals[it] ?: 0f) > 0f }
            .sortedByDescending { signals[it] }
            .take(max)
            .map { TagCatalog.label(it) }

    /** Weighted average over the factors that have data. */
    fun combine(raw: List<FactorScore>): Pair<Float, List<FactorScore>> {
        val available = raw.filter { it.score >= 0f }
        val weightSum = available.sumOf { it.weight.toDouble() }.toFloat()
        if (weightSum <= 0f) return 0f to emptyList()
        val normalised = available.map { it.copy(weight = it.weight / weightSum) }
        return normalised.sumOf { (it.score * it.weight).toDouble() }.toFloat() to normalised
    }

    /**
     * Relative calibration used by the CBE engine: each factor becomes half its absolute
     * coverage and half its coverage relative to the strongest option in the same group.
     * This keeps scores honest (an option nobody fits stays low) while letting a clear
     * best option read as a strong match rather than being diluted by broad interests.
     */
    fun <T> calibrate(items: List<ScoredItem<T>>): List<ScoredItem<T>> {
        val maxByKey = items.flatMap { it.factors }.groupBy { it.key }.mapValues { (_, v) -> v.maxOf { it.score } }
        return items.map { item ->
            val adjusted = item.factors.map { f ->
                val max = maxByKey[f.key] ?: 0f
                if (max <= 0f) f else f.copy(score = (0.5f * f.score + 0.5f * (f.score / max)).coerceIn(0f, 1f))
            }
            ScoredItem(item.item, adjusted.sumOf { (it.score * it.weight).toDouble() }.toFloat(), adjusted)
        }
    }

    /** Helper: build a factor, using -1 to mark "no data" so combine() can skip it. */
    fun factor(key: String, label: String, score: Float?, weight: Float, evidence: List<String>) =
        FactorScore(key, label, score ?: -1f, weight, evidence)
}
