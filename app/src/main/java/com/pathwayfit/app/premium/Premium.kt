package com.pathwayfit.app.premium

import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.SubscriptionStatus
import com.pathwayfit.app.data.local.LocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Individually controllable premium features. Each declares which education systems it
 * belongs to, so a CBE learner never sees university-course features as current tools.
 */
enum class PremiumFeature(val title: String, val description: String, val systems: Set<EducationSystem>) {
    CAREER_MAP("Complete Career Map", "Tap through every stage from your strengths to possible careers.", EducationSystem.entries.toSet()),
    CBE_PATHWAY_MAP("Complete CBE Pathway Map", "Grade → pathway → track → area → learning areas → future study → careers.", setOf(EducationSystem.CBE)),
    WHAT_IF("What-If Pathways", "Compare two pathways side by side before you choose.", setOf(EducationSystem.CBE)),
    ALL_DIRECTIONS("All Your Directions", "See every direction we found, not just the top three.", EducationSystem.entries.toSet()),
    DETAILED_WHY_FIT("Detailed Match Analysis", "Every factor scored and explained in plain language.", EducationSystem.entries.toSet()),
    TOP_COURSES("Top 10 Course Matches", "Your ten strongest course matches instead of five.", setOf(EducationSystem.EIGHT_FOUR_FOUR)),
    UNIVERSITY_SHORTLIST("University Shortlist", "A personalised shortlist built from your courses and preferences.", setOf(EducationSystem.EIGHT_FOUR_FOUR)),
    COURSE_COMPARISON("Course Comparison", "Course A vs Course B on fit, subjects, skills and careers.", setOf(EducationSystem.EIGHT_FOUR_FOUR)),
    SUBJECT_GUIDANCE("Subject Improvement Guidance", "Which subjects matter most for your top directions.", EducationSystem.entries.toSet()),
    ACTION_PLAN("My Next 3 Years", "A personalised development roadmap. Guidance only — not an admission plan.", EducationSystem.entries.toSet());

    fun appliesTo(system: EducationSystem?) = system == null || system in systems
}

/** Future subscription models. No prices are defined or shown in this version. */
enum class PlanType(val displayName: String) {
    MONTHLY("JuaFit Premium — Monthly"),
    ANNUAL("JuaFit Premium — Annual"),
    ONE_TIME_CAREER_REPORT("JuaFit Career Report"),
    SCHOOL_PACKAGE("JuaFit for Schools"),
}

data class PremiumAccess(
    val status: SubscriptionStatus = SubscriptionStatus.FREE,
    val developerPreview: Boolean = false,
    val paymentEnabled: Boolean = false,
) {
    val isPremium: Boolean get() = developerPreview || status == SubscriptionStatus.PREMIUM || status == SubscriptionStatus.TRIAL
    fun has(feature: PremiumFeature): Boolean = isPremium
}

sealed interface PaymentResult {
    data object NotAvailable : PaymentResult
    data class Success(val plan: PlanType) : PaymentResult
    data class Failed(val reason: String) : PaymentResult
}

/**
 * Payment abstraction. M-Pesa, PayHero, Stripe or Google Play Billing can each be added
 * later as an implementation of this interface. None are connected in this version.
 */
interface PaymentService {
    val isEnabled: Boolean
    suspend fun availablePlans(): List<PlanType>
    suspend fun purchase(plan: PlanType): PaymentResult
}

class DisabledPaymentService : PaymentService {
    override val isEnabled = false
    override suspend fun availablePlans(): List<PlanType> = emptyList()
    override suspend fun purchase(plan: PlanType): PaymentResult = PaymentResult.NotAvailable
}

interface SubscriptionRepository {
    val status: kotlinx.coroutines.flow.Flow<SubscriptionStatus>
    val notifyMe: kotlinx.coroutines.flow.Flow<Boolean>
    val developerPreview: kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun setNotifyMe(enabled: Boolean)
    suspend fun setDeveloperPreview(enabled: Boolean)
}

class LocalSubscriptionRepository(private val store: LocalStore) : SubscriptionRepository {
    // Every user defaults to FREE. A billing backend will write real status here later.
    override val status = store.stringFlow("subscription_status").map { raw ->
        raw?.let { runCatching { SubscriptionStatus.valueOf(it) }.getOrNull() } ?: SubscriptionStatus.FREE
    }
    override val notifyMe = store.boolFlow("premium_notify_me")
    override val developerPreview = store.boolFlow("dev_premium_preview")
    override suspend fun setNotifyMe(enabled: Boolean) = store.putBool("premium_notify_me", enabled)
    override suspend fun setDeveloperPreview(enabled: Boolean) = store.putBool("dev_premium_preview", enabled)
}

class PremiumManager(
    private val subscriptions: SubscriptionRepository,
    val payments: PaymentService,
    private val devToolsAllowed: Boolean,
    scope: CoroutineScope,
) {
    val access: StateFlow<PremiumAccess> =
        combine(subscriptions.status, subscriptions.developerPreview) { status, dev ->
            PremiumAccess(status = status, developerPreview = dev && devToolsAllowed, paymentEnabled = payments.isEnabled)
        }.stateIn(scope, SharingStarted.Eagerly, PremiumAccess(paymentEnabled = payments.isEnabled))

    val notifyMe = subscriptions.notifyMe
    val developerPreview = subscriptions.developerPreview
    val devToolsVisible: Boolean get() = devToolsAllowed

    fun isUnlocked(feature: PremiumFeature) = access.value.has(feature)
    suspend fun setNotifyMe(enabled: Boolean) = subscriptions.setNotifyMe(enabled)
    suspend fun setDeveloperPreview(enabled: Boolean) {
        if (devToolsAllowed) subscriptions.setDeveloperPreview(enabled)
    }
}
