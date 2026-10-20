package com.pathwayfit.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.pathwayFitDataStore: DataStore<Preferences> by preferencesDataStore(name = "pathwayfit")

/**
 * Small key-value persistence layer for the prototype. The repositories above it are
 * interfaces, so this can be swapped for Room + a remote backend without UI changes.
 */
class LocalStore(private val dataStore: DataStore<Preferences>) {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        allowStructuredMapKeys = true
    }

    fun stringFlow(key: String): Flow<String?> =
        dataStore.data.map { it[stringPreferencesKey(key)] }

    suspend fun getString(key: String): String? = stringFlow(key).first()

    suspend fun putString(key: String, value: String?) {
        dataStore.edit { prefs ->
            val k = stringPreferencesKey(key)
            if (value == null) prefs.remove(k) else prefs[k] = value
        }
    }

    fun boolFlow(key: String, default: Boolean = false): Flow<Boolean> =
        dataStore.data.map { it[booleanPreferencesKey(key)] ?: default }

    suspend fun putBool(key: String, value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    inline fun <reified T> objectFlow(key: String, default: T): Flow<T> =
        stringFlow(key).map { raw -> raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() } ?: default }

    suspend inline fun <reified T> getObject(key: String, default: T): T = objectFlow(key, default).first()

    suspend inline fun <reified T> putObject(key: String, value: T) =
        putString(key, json.encodeToString(kotlinx.serialization.serializer<T>(), value))
}
