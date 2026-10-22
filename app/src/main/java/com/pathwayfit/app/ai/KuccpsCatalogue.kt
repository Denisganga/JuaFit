package com.pathwayfit.app.ai

import android.content.Context
import org.json.JSONObject

/** Printed KUCCPS facts, distinct from AI explanations and interest scores. */
object KuccpsCatalogue {
    @Volatile private var cached: JSONObject? = null
    fun facts(context: Context, courseId: String, section: String): String? {
        if (section !in listOf("universities", "requirements")) return null
        val data = cached ?: JSONObject(context.assets.open("kuccps_2025.json").bufferedReader().use { it.readText() }).also { cached = it }
        val codes = data.getJSONObject("byCourse").optJSONArray(courseId) ?: return null
        val selected = (0 until codes.length()).map { codes.getString(it) }.toSet()
        if (selected.isEmpty()) return null
        val rows = data.getJSONArray("records")
        val lines = mutableListOf("KUCCPS verified source: 2025/2026 Degree Programmes", "Listed programme variants in this course family. Availability is verified for this edition, not later intakes.")
        for (index in 0 until rows.length()) {
            val row = rows.getJSONObject(index)
            if (row.getString("code") !in selected) continue
            var text = "${row.getString("institution")} — ${row.getString("programme")} | Programme code ${row.getString("code")} | PDF page ${row.getInt("page")}"
            if (section == "requirements") {
                val requirements = row.getJSONArray("subjectRequirements")
                text += "\nPrinted subject requirements: " + if (requirements.length() == 0) "Not specified in this row. Confirm with KUCCPS or the institution." else (0 until requirements.length()).joinToString("; ") { requirements.getString(it) }
            }
            lines.add(text)
        }
        lines.add("Historical 2023/2022 cutoffs are not current entry requirements. Suitability scores and AI career guidance are not verified by KUCCPS.")
        return lines.joinToString("\n\n")
    }
}
