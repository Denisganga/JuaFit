package com.pathwayfit.app.core.model

import kotlinx.serialization.Serializable

/** The two learner experiences. They never share an assessment, engine or dashboard. */
@Serializable
enum class EducationSystem(val label: String, val shortLabel: String) {
    CBE("CBE Pathway", "CBE"),
    EIGHT_FOUR_FOUR("8-4-4 / KUCCPS", "8-4-4"),
}

/**
 * Access-control role. Only LEARNER is implemented in v1; the others exist so the
 * navigation and permission checks are ready for counsellor, school and admin tools.
 */
@Serializable
enum class UserRole { LEARNER, COUNSELLOR, SCHOOL, ADMIN }

@Serializable
enum class SubscriptionStatus { FREE, PREMIUM, TRIAL, EXPIRED }

@Serializable
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.LEARNER,
    val educationSystem: EducationSystem? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val createdAt: Long = System.currentTimeMillis(),
    val dataConsentVersion: String? = null,
    val dataConsentAcceptedAt: Long? = null,
) {
    val firstName: String get() = name.trim().substringBefore(' ').ifBlank { name }
}

/** Shared, system-neutral learner details. */
@Serializable
data class LearnerProfile(
    val userId: String,
    val name: String,
    val school: String = "",
    val county: String = "",
)

/** Profile for a Competency-Based Education learner. Contains no KCSE data. */
@Serializable
data class CbeProfile(
    val grade: String = "",
    val school: String = "",
    val county: String = "",
    val interests: List<String> = emptyList(),          // tag ids, see TagCatalog
    val strengths: List<String> = emptyList(),          // tag ids
    val favouriteActivities: List<String> = emptyList(), // tag ids
    val learningAreas: List<String> = emptyList(),       // CbeSubject ids
    val careerInterests: List<String> = emptyList(),     // Career ids
    val otherNotes: String = "",
) {
    fun completion(): Float {
        val checks = listOf(
            grade.isNotBlank(), school.isNotBlank(), county.isNotBlank(),
            interests.isNotEmpty(), strengths.isNotEmpty(), favouriteActivities.isNotEmpty(),
            learningAreas.isNotEmpty(), careerInterests.isNotEmpty(),
        )
        return checks.count { it } / checks.size.toFloat()
    }
}

/** Profile for an 8-4-4 / KCSE learner heading toward university. Contains no CBE data. */
@Serializable
data class UniversityProfile(
    val school: String = "",
    val county: String = "",
    val kcseYear: Int? = null,
    val meanGrade: KcseGrade? = null,
    val subjectGrades: List<KcseSubjectGrade> = emptyList(),
    val preferredFields: List<String> = emptyList(),      // CourseField names
    val careerInterests: List<String> = emptyList(),      // Career ids
    val institutionTypes: List<InstitutionType> = emptyList(),
    val preferredLocations: List<String> = emptyList(),   // county names
    val otherPreferences: String = "",
) {
    fun completion(): Float {
        val checks = listOf(
            school.isNotBlank(), kcseYear != null, meanGrade != null,
            subjectGrades.size >= 7, preferredFields.isNotEmpty(), careerInterests.isNotEmpty(),
            preferredLocations.isNotEmpty() || institutionTypes.isNotEmpty(),
        )
        return checks.count { it } / checks.size.toFloat()
    }
}

@Serializable
data class StoredAccount(val user: User, val passwordHash: String, val salt: String)
