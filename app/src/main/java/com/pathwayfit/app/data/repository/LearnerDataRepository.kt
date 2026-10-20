package com.pathwayfit.app.data.repository

import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentRecord
import com.pathwayfit.app.core.model.CbeProfile
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.SavedRecommendation
import com.pathwayfit.app.core.model.UniversityProfile
import com.pathwayfit.app.data.local.LocalStore
import kotlinx.coroutines.flow.Flow

/**
 * Per-learner data, always keyed by user AND education system so CBE and 8-4-4
 * data can never leak into each other's workflow.
 */
class LearnerDataRepository(private val store: LocalStore) {

    private fun key(kind: String, uid: String, system: EducationSystem? = null) =
        listOfNotNull(kind, system?.name, uid).joinToString("_")

    // --- CBE profile (CBE learners only)
    fun cbeProfile(uid: String): Flow<CbeProfile> = store.objectFlow(key("profile", uid, EducationSystem.CBE), CbeProfile())
    suspend fun saveCbeProfile(uid: String, profile: CbeProfile) = store.putObject(key("profile", uid, EducationSystem.CBE), profile)

    // --- University profile (8-4-4 learners only)
    fun universityProfile(uid: String): Flow<UniversityProfile> =
        store.objectFlow(key("profile", uid, EducationSystem.EIGHT_FOUR_FOUR), UniversityProfile())
    suspend fun saveUniversityProfile(uid: String, profile: UniversityProfile) =
        store.putObject(key("profile", uid, EducationSystem.EIGHT_FOUR_FOUR), profile)

    // --- Assessment answers, per system
    fun answers(uid: String, system: EducationSystem): Flow<List<AssessmentAnswer>> =
        store.objectFlow(key("answers", uid, system), emptyList())
    suspend fun saveAnswers(uid: String, system: EducationSystem, answers: List<AssessmentAnswer>) =
        store.putObject(key("answers", uid, system), answers)

    fun completed(uid: String, system: EducationSystem): Flow<Boolean> = store.boolFlow(key("completed", uid, system))
    suspend fun setCompleted(uid: String, system: EducationSystem, done: Boolean) = store.putBool(key("completed", uid, system), done)

    // --- History & saved recommendations (shared structure, role-scoped content)
    fun history(uid: String): Flow<List<AssessmentRecord>> = store.objectFlow(key("history", uid), emptyList())
    suspend fun addHistory(uid: String, record: AssessmentRecord) {
        val current = store.getObject<List<AssessmentRecord>>(key("history", uid), emptyList())
        store.putObject(key("history", uid), (listOf(record) + current).take(20))
    }

    fun saved(uid: String): Flow<List<SavedRecommendation>> = store.objectFlow(key("saved", uid), emptyList())
    suspend fun toggleSaved(uid: String, rec: SavedRecommendation) {
        val current = store.getObject<List<SavedRecommendation>>(key("saved", uid), emptyList())
        val exists = current.any { it.system == rec.system && it.itemId == rec.itemId }
        store.putObject(key("saved", uid), if (exists) current.filterNot { it.system == rec.system && it.itemId == rec.itemId } else current + rec)
    }

    /** Called when a learner switches education system: clears the old system's results. */
    suspend fun clearSystemResults(uid: String, system: EducationSystem) {
        store.putString(key("answers", uid, system), null)
        setCompleted(uid, system, false)
    }
}
