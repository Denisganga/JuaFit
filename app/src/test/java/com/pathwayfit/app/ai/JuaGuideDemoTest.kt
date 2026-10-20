package com.pathwayfit.app.ai

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class JuaGuideDemoTest {
    @Test fun technologyQuestionGetsRelevantGuidance() = runBlocking {
        val result = JuaGuideDemo.chat(listOf("user" to "I enjoy coding"), buildJsonObject {})
        assertTrue(result.text.contains("software development"))
        assertFalse(result.completed)
    }
    @Test fun attachmentAsksForQualificationInsteadOfPretendingToReadIt() = runBlocking {
        val result = JuaGuideDemo.chat(listOf("user" to "Help with my transcript"), buildJsonObject { put("attachment", buildJsonObject {}) })
        assertTrue(result.text.contains("qualification"))
        assertTrue(result.text.contains("demo"))
        assertFalse(result.completed)
    }
    @Test fun shortlistUsesSuppliedCatalog() = runBlocking {
        val context = buildJsonObject { put("catalog", buildJsonArray { repeat(5) { index -> add(buildJsonObject { put("course", "Course $index"); put("universities", "University $index (offering verified: false)") }) } }) }
        val result = JuaGuideDemo.chat(listOf("user" to "Show top 5"), context)
        assertTrue(result.completed)
        assertTrue(result.text.contains("Course 4"))
        assertTrue(result.text.contains("need confirmation"))
    }
}
