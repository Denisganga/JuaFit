package com.pathwayfit.app.data.repository

import android.content.Context
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.UniversityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Loads the educational reference data. CBE and university data come from separate
 * files and are exposed through separate accessors — they are never merged.
 *
 * To import verified data later, replace the asset files (or point this class at a
 * remote source) without touching the UI or the engines.
 */
class ReferenceDataRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cbe: CbeReference? = null
    private var university: UniversityData? = null
    private var careers: CareerData? = null

    suspend fun cbeReference(): CbeReference = mutex.withLock {
        cbe ?: load<CbeReference>("cbe_reference.json").also { cbe = it }
    }

    suspend fun universityData(): UniversityData = mutex.withLock {
        university ?: load<UniversityData>("kcse_university_data.json").also { university = it }
    }

    suspend fun careerData(): CareerData = mutex.withLock {
        careers ?: load<CareerData>("careers.json").also { careers = it }
    }

    private suspend inline fun <reified T> load(file: String): T = withContext(Dispatchers.IO) {
        val text = context.assets.open(file).bufferedReader().use { it.readText() }
        json.decodeFromString<T>(text)
    }
}
