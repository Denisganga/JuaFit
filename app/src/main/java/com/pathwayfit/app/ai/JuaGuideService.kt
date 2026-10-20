package com.pathwayfit.app.ai

import com.pathwayfit.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

data class GuideReply(val text: String, val completed: Boolean)

object JuaGuideService {
    suspend fun reply(messages: List<Pair<String, String>>, context: JsonObject): GuideReply = withContext(Dispatchers.IO) {

        if (JuaGuideDemo.enabled) return@withContext JuaGuideDemo.chat(messages, context)
        val payload = buildJsonObject {
            put("context", context)
            put("messages", buildJsonArray { messages.takeLast(20).forEach { (role, text) -> add(buildJsonObject { put("role", role); put("text", text) }) } })
        }
        val json = GeminiConnection.call("chat", payload)
        GuideReply(json.getValue("reply").jsonPrimitive.content.also { check(it.isNotBlank()) }, json.getValue("completed").jsonPrimitive.boolean)
    }
}
