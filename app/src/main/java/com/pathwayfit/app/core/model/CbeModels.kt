package com.pathwayfit.app.core.model

import kotlinx.serialization.Serializable

/*
 * CBE "tables". These are deliberately separate from the 8-4-4 Course/University
 * structures: there is no generic "Pathway" type shared by both systems.
 * Content is loaded from assets/cbe_reference.json (the CBE source of truth).
 */

@Serializable
data class CbeReferenceMeta(
    val title: String,
    val source: String,
    val version: String,
    val verified: Boolean,
    val note: String = "",
)

@Serializable
data class CbePathway(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val verified: Boolean = false,
)

@Serializable
data class CbeTrack(
    val id: String,
    val pathwayId: String,
    val name: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val futureStudy: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val verified: Boolean = false,
)

@Serializable
data class CbeArea(
    val id: String,
    val trackId: String,
    val name: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val subjectIds: List<String> = emptyList(),
    val verified: Boolean = false,
)

@Serializable
enum class CbeSubjectKind { CORE, PATHWAY }

@Serializable
data class CbeSubject(
    val id: String,
    val name: String,
    val kind: CbeSubjectKind = CbeSubjectKind.PATHWAY,
    val verified: Boolean = false,
)

@Serializable
data class CbeReference(
    val meta: CbeReferenceMeta,
    val grades: List<String> = emptyList(),
    val pathways: List<CbePathway> = emptyList(),
    val tracks: List<CbeTrack> = emptyList(),
    val areas: List<CbeArea> = emptyList(),
    val subjects: List<CbeSubject> = emptyList(),
) {
    fun tracksOf(pathwayId: String) = tracks.filter { it.pathwayId == pathwayId }
    fun areasOf(trackId: String) = areas.filter { it.trackId == trackId }
    fun pathway(id: String) = pathways.firstOrNull { it.id == id }
    fun track(id: String) = tracks.firstOrNull { it.id == id }
    fun area(id: String) = areas.firstOrNull { it.id == id }
    fun subject(id: String) = subjects.firstOrNull { it.id == id }
    fun subjectsOf(area: CbeArea) = area.subjectIds.mapNotNull { subject(it) }
    fun coreSubjects() = subjects.filter { it.kind == CbeSubjectKind.CORE }
}
