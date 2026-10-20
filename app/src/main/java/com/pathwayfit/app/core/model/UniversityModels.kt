package com.pathwayfit.app.core.model

import kotlinx.serialization.Serializable

/*
 * 8-4-4 / KCSE / university "tables". Separate from CBE structures.
 * Loaded from assets/kcse_university_data.json. Anything that is not verified
 * against an official source carries verified = false and is labelled in the UI.
 */

/** KCSE letter grades with their conventional points (A = 12 ... E = 1). */
@Serializable
enum class KcseGrade(val label: String, val points: Int) {
    A("A", 12), A_MINUS("A-", 11), B_PLUS("B+", 10), B("B", 9), B_MINUS("B-", 8),
    C_PLUS("C+", 7), C("C", 6), C_MINUS("C-", 5), D_PLUS("D+", 4), D("D", 3), D_MINUS("D-", 2), E("E", 1);

    companion object {
        fun fromPoints(avg: Double): KcseGrade =
            entries.minByOrNull { kotlin.math.abs(it.points - avg) } ?: C
    }
}

@Serializable
data class KcseSubject(
    val code: String,
    val name: String,
    val group: String,
    val compulsory: Boolean = false,
)

@Serializable
data class KcseSubjectGrade(val subjectCode: String, val grade: KcseGrade)

@Serializable
enum class InstitutionType(val label: String) { PUBLIC("Public university"), PRIVATE("Private university") }

/** Admission data is only shown when verified against an official source. */
@Serializable
data class AdmissionRequirement(
    val courseId: String,
    val universityId: String? = null,
    val description: String,
    val sourceName: String,
    val sourceUrl: String = "",
    val academicYear: String = "",
    val verified: Boolean = false,
)

@Serializable
data class Course(
    val id: String,
    val name: String,
    val field: String,
    val overview: String,
    val tags: List<String> = emptyList(),
    val relevantSubjects: List<String> = emptyList(), // KcseSubject codes, general guidance only
    val skills: List<String> = emptyList(),
    val careerIds: List<String> = emptyList(),
)

@Serializable
data class University(
    val id: String,
    val name: String,
    val county: String,
    val type: InstitutionType,
    val website: String = "",
)

/** A university-course link. Unverified listings are mock data and are labelled as such. */
@Serializable
data class ProgrammeOffering(
    val universityId: String,
    val courseId: String,
    val verified: Boolean = false,
)

@Serializable
data class UniversityDataMeta(
    val title: String,
    val source: String,
    val version: String,
    val verified: Boolean,
    val note: String = "",
)

@Serializable
data class UniversityData(
    val meta: UniversityDataMeta,
    val subjects: List<KcseSubject> = emptyList(),
    val courses: List<Course> = emptyList(),
    val universities: List<University> = emptyList(),
    val offerings: List<ProgrammeOffering> = emptyList(),
    val admissionRequirements: List<AdmissionRequirement> = emptyList(),
) {
    fun course(id: String) = courses.firstOrNull { it.id == id }
    fun university(id: String) = universities.firstOrNull { it.id == id }
    fun subject(code: String) = subjects.firstOrNull { it.code == code }
    fun universitiesFor(courseId: String): List<Pair<University, ProgrammeOffering>> =
        offerings.filter { it.courseId == courseId }.mapNotNull { o -> university(o.universityId)?.let { it to o } }
    fun coursesAt(universityId: String): List<Course> =
        offerings.filter { it.universityId == universityId }.mapNotNull { course(it.courseId) }
    fun verifiedRequirements(courseId: String) =
        admissionRequirements.filter { it.courseId == courseId && it.verified }
    val fields: List<String> get() = courses.map { it.field }.distinct().sorted()
    val counties: List<String> get() = universities.map { it.county }.distinct().sorted()
}
