package com.pathwayfit.app.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.pathwayfit.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object GeminiConnection {
    private lateinit var context: Context
    fun initialize(value: Context) { context = value }
    private val preferences get() = context.getSharedPreferences("gemini_connection", Context.MODE_PRIVATE)
    val configured get() = preferences.contains("encrypted_key")
    val model get() = preferences.getString("model", "gemini-3.6-flash")!!
    private fun secret(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("juafit_gemini", null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("juafit_gemini", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun save(apiKey: String, model: String) {
        require(apiKey.isNotBlank() && model.matches(Regex("gemini-[a-zA-Z0-9.\\-]+"))) { "Enter a JuaGuide key and valid model name." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, secret()) }
        preferences.edit().putString("encrypted_key", Base64.encodeToString(cipher.doFinal(apiKey.trim().toByteArray()), Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP)).putString("model", model.trim()).apply()
    }
    fun clear() { preferences.edit().clear().apply() }
    private fun apiKey(): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, secret(), GCMParameterSpec(128, Base64.decode(preferences.getString("iv", ""), Base64.NO_WRAP))) }
        return String(cipher.doFinal(Base64.decode(preferences.getString("encrypted_key", ""), Base64.NO_WRAP)))
    }
    suspend fun test(): String = withContext(Dispatchers.IO) {
        check(configured) { "Save your JuaGuide API key first." }
        request("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent", buildJsonObject {
            put("contents", buildJsonArray { add(buildJsonObject { put("parts", buildJsonArray { add(buildJsonObject { put("text", "Reply with OK only.") }) }) }) })
        }, apiKey())
        "JuaGuide is connected"
    }
    suspend fun call(path: String, payload: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        val base = BuildConfig.GEMINI_ANALYSIS_URL.trimEnd('/')
        check(base.startsWith("https://")) { "JuaGuide is being connected. Please try again once the service is available." }
        val connection = URL("$base/$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"; connection.doOutput = true
            connection.connectTimeout = 15000; connection.readTimeout = 75000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            check(code == 200) { if(code == 429) "JuaGuide is busy. Please try again shortly." else "JuaGuide could not complete this request. Please retry." }
            Json.parseToJsonElement(connection.inputStream.bufferedReader().use { it.readText() }).jsonObject
        } finally { connection.disconnect() }
    }

    private fun request(url: String, body: JsonObject, key: String): JsonObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"; connection.doOutput = true
            connection.connectTimeout = 15000; connection.readTimeout = 75000
            connection.setRequestProperty("Content-Type", "application/json"); connection.setRequestProperty("x-goog-api-key", key)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            check(code == 200) { when(code) {
                400, 401 -> "JuaGuide could not verify the connection key. Check JuaGuide setup."
                403 -> "This API key does not have permission to use JuaGuide."
                404 -> "The selected JuaGuide model is unavailable. Change the model in JuaGuide setup."
                429 -> "JuaGuide usage limit reached. Please try again later."
                else -> "JuaGuide connection failed (HTTP $code). Please retry."
            } }
            return Json.parseToJsonElement(connection.inputStream.bufferedReader().use { it.readText() }).jsonObject
        } finally { connection.disconnect() }
    }
}
