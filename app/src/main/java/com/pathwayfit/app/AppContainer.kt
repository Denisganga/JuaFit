package com.pathwayfit.app

import android.app.Application
import android.content.Context
import com.pathwayfit.app.ai.AIRecommendationService
import com.pathwayfit.app.ai.LocalExplanationService
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.User
import com.pathwayfit.app.core.model.UserRole
import com.pathwayfit.app.data.local.LocalStore
import com.pathwayfit.app.data.local.pathwayFitDataStore
import com.pathwayfit.app.data.repository.AuthRepository
import com.pathwayfit.app.data.repository.LearnerDataRepository
import com.pathwayfit.app.data.repository.LocalAuthRepository
import com.pathwayfit.app.data.repository.ReferenceDataRepository
import com.pathwayfit.app.engine.CbeRecommendationEngine
import com.pathwayfit.app.engine.CbeScoringWeights
import com.pathwayfit.app.engine.EightFourFourRecommendationEngine
import com.pathwayfit.app.engine.EightFourFourScoringWeights
import com.pathwayfit.app.premium.DisabledPaymentService
import com.pathwayfit.app.premium.LocalSubscriptionRepository
import com.pathwayfit.app.premium.PremiumManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PathwayFitApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency wiring. Swap implementations here when the backend arrives. */
class AppContainer(context: Context) {
    init { com.pathwayfit.app.ai.GeminiConnection.initialize(context.applicationContext) }
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val store = LocalStore(context.pathwayFitDataStore)
    val auth: AuthRepository = LocalAuthRepository(store)
    val learnerData = LearnerDataRepository(store)
    val reference = ReferenceDataRepository(context.applicationContext)

    // Two independent engines with their own configurable weights.
    val cbeEngine = CbeRecommendationEngine(CbeScoringWeights())
    val eightFourFourEngine = EightFourFourRecommendationEngine(EightFourFourScoringWeights())

    val ai: AIRecommendationService = LocalExplanationService()

    val premium = PremiumManager(
        subscriptions = LocalSubscriptionRepository(store),
        payments = DisabledPaymentService(), // paymentEnabled = false
        devToolsAllowed = BuildConfig.DEV_TOOLS,
        scope = appScope,
    )
}

/**
 * Role-based access control. v1 implements learners only; the other roles are
 * recognised so that future counsellor/school/admin areas can be gated here.
 */
object AccessControl {
    fun canUseLearnerWorkflow(user: User?, system: EducationSystem): Boolean =
        user != null && user.role == UserRole.LEARNER && user.educationSystem == system

    fun canUseAdminTools(user: User?): Boolean = user?.role == UserRole.ADMIN
    fun canUseCounsellorTools(user: User?): Boolean = user?.role == UserRole.COUNSELLOR || user?.role == UserRole.ADMIN
}
