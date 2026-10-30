package com.pathwayfit.app.ui.viewmodel

import kotlinx.serialization.json.*

import kotlinx.serialization.json.encodeToJsonElement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pathwayfit.app.AppContainer
import com.pathwayfit.app.ai.AiExplanation
import com.pathwayfit.app.ai.CbeAiContext
import com.pathwayfit.app.ai.UniversityAiContext
import com.pathwayfit.app.assessment.CbeAssessmentBank
import com.pathwayfit.app.assessment.EightFourFourAssessmentBank
import com.pathwayfit.app.core.model.Assessment
import com.pathwayfit.app.core.model.AssessmentAnswer
import com.pathwayfit.app.core.model.AssessmentRecord
import com.pathwayfit.app.core.model.CareerData
import com.pathwayfit.app.core.model.CbeProfile
import com.pathwayfit.app.core.model.CbeReference
import com.pathwayfit.app.core.model.EducationSystem
import com.pathwayfit.app.core.model.SavedRecommendation
import com.pathwayfit.app.core.model.UniversityData
import com.pathwayfit.app.core.model.UniversityProfile
import com.pathwayfit.app.core.model.User
import com.pathwayfit.app.engine.CbeEngineInput
import com.pathwayfit.app.engine.CbePathwayMatch
import com.pathwayfit.app.engine.CbeRecommendationSet
import com.pathwayfit.app.engine.CourseMatch
import com.pathwayfit.app.engine.CourseRecommendationSet
import com.pathwayfit.app.engine.EightFourFourEngineInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModelFactory(private val c: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SessionViewModel::class.java) -> SessionViewModel(c)
        modelClass.isAssignableFrom(CbeViewModel::class.java) -> CbeViewModel(c)
        modelClass.isAssignableFrom(EightFourFourViewModel::class.java) -> EightFourFourViewModel(c)
        modelClass.isAssignableFrom(PremiumViewModel::class.java) -> PremiumViewModel(c)
        else -> error("Unknown ViewModel ${modelClass.name}")
    } as T
}

private val WhileSubscribed = SharingStarted.WhileSubscribed(5_000)

// ============================ SESSION ============================

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class NeedsSystem(val user: User) : SessionState
    data class Active(val user: User, val system: EducationSystem) : SessionState
}

class SessionViewModel(private val c: AppContainer) : ViewModel() {

    val state: StateFlow<SessionState> = c.auth.currentUser.map { u ->
        val system = u?.educationSystem
        when {
            u == null -> SessionState.SignedOut
            system == null -> SessionState.NeedsSystem(u)
            else -> SessionState.Active(u, system)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    val user: StateFlow<User?> = c.auth.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun register(name: String, email: String, password: String, consentAccepted: Boolean, onResult: (String?) -> Unit) = viewModelScope.launch {
        onResult(c.auth.register(name, email, password, consentAccepted).exceptionOrNull()?.message)
    }

    fun login(email: String, password: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        onResult(c.auth.login(email, password).exceptionOrNull()?.message)
    }

    fun chooseSystem(system: EducationSystem, onDone: () -> Unit) = viewModelScope.launch {
        c.auth.setEducationSystem(system)
        onDone()
    }

    /** Switching clears the previous system's assessment results so workflows never mix. */
    fun switchSystem(onDone: () -> Unit) = viewModelScope.launch {
        val u = user.value ?: return@launch
        u.educationSystem?.let { c.learnerData.clearSystemResults(u.userId, it) }
        onDone()
    }

    fun updateName(name: String) = viewModelScope.launch { c.auth.updateName(name) }

    /** Local notification preference (study reminders). Delivery is added with the backend. */
    val reminders: StateFlow<Boolean> = c.store.boolFlow("reminders_enabled", true)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    fun setReminders(on: Boolean) = viewModelScope.launch { c.store.putBool("reminders_enabled", on) }

    val history: StateFlow<List<AssessmentRecord>> = c.auth.currentUser.flatMapLatestCompat { u ->
        if (u == null) kotlinx.coroutines.flow.flowOf(emptyList<AssessmentRecord>()) else c.learnerData.history(u.userId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun logout(onDone: () -> Unit) = viewModelScope.launch { c.auth.logout(); onDone() }

}

@OptIn(ExperimentalCoroutinesApi::class)
private fun <T, R> Flow<T>.flatMapLatestCompat(transform: suspend (T) -> Flow<R>): Flow<R> = flatMapLatest(transform)

// ============================ ASSESSMENT DRAFT (shared mechanics) ============================

class AssessmentDraft(val assessment: Assessment) {
    private val _answers = MutableStateFlow<Map<String, AssessmentAnswer>>(emptyMap())
    val answers = _answers.asStateFlow()
    private val _index = MutableStateFlow(0)
    val index = _index.asStateFlow()

    fun reset(from: List<AssessmentAnswer>) {
        _answers.value = from.associateBy { it.questionId }
        _index.value = 0
    }
    fun set(answer: AssessmentAnswer) = _answers.update { it + (answer.questionId to answer) }
    fun next() = _index.update { (it + 1).coerceAtMost(assessment.questions.lastIndex) }
    fun back() = _index.update { (it - 1).coerceAtLeast(0) }
    fun answered(): List<AssessmentAnswer> = _answers.value.values.filter { it.isAnswered }
}

// ============================ CBE ============================

@OptIn(ExperimentalCoroutinesApi::class)
class CbeViewModel(private val c: AppContainer) : ViewModel() {

    private val system = EducationSystem.CBE
    val assessment = CbeAssessmentBank.assessment
    val draft = AssessmentDraft(assessment)

    private val userFlow: Flow<User> = c.auth.currentUser.filterNotNull()
        .filter { it.educationSystem == system }
        .distinctUntilChanged { a, b -> a.userId == b.userId && a.name == b.name }

    val user: StateFlow<User?> = userFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _reference = MutableStateFlow<CbeReference?>(null)
    val reference: StateFlow<CbeReference?> = _reference
    private val _careers = MutableStateFlow<CareerData?>(null)
    val careers: StateFlow<CareerData?> = _careers
    /** Only used to show *future* university directions in the Premium map. */
    private val _futureCourses = MutableStateFlow<UniversityData?>(null)
    val futureCourses: StateFlow<UniversityData?> = _futureCourses

    init {
        viewModelScope.launch {
            _reference.value = c.reference.cbeReference()
            _careers.value = c.reference.careerData()
            _futureCourses.value = c.reference.universityData()
        }
    }

    val profile: StateFlow<CbeProfile> = userFlow.flatMapLatest { c.learnerData.cbeProfile(it.userId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CbeProfile())
    private val savedAnswers = userFlow.flatMapLatest { c.learnerData.answers(it.userId, system) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val completed: StateFlow<Boolean> = userFlow.flatMapLatest { c.learnerData.completed(it.userId, system) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val saved: StateFlow<List<SavedRecommendation>> = userFlow.flatMapLatest { c.learnerData.saved(it.userId) }
        .map { list -> list.filter { it.system == system } }
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    val results: StateFlow<CbeRecommendationSet?> =
        combine(profile, savedAnswers, completed, _reference, _careers) { p, a, done, ref, car ->
            if (!done || ref == null || car == null) null
            else c.cbeEngine.recommend(CbeEngineInput(p, assessment.questions, a, ref, car))
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun startAssessment(resume: Boolean) = draft.reset(if (resume) savedAnswers.value else emptyList())

    fun saveProfile(p: CbeProfile, onDone: () -> Unit = {}) = viewModelScope.launch {
        user.value?.let { c.learnerData.saveCbeProfile(it.userId, p) }
        onDone()
    }

    fun submitAssessment(onDone: () -> Unit) = viewModelScope.launch {
        val u = user.value ?: return@launch
        val answers = draft.answered()
        c.learnerData.saveAnswers(u.userId, system, answers)
        c.learnerData.setCompleted(u.userId, system, true)
        val ref = _reference.value ?: c.reference.cbeReference()
        val car = _careers.value ?: c.reference.careerData()
        val set = c.cbeEngine.recommend(CbeEngineInput(profile.value, assessment.questions, answers, ref, car))
        set.top?.let { c.learnerData.addHistory(u.userId, AssessmentRecord(system, System.currentTimeMillis(), it.pathway.name, it.percent)) }
        onDone()
    }

    fun toggleSave(match: CbePathwayMatch) = viewModelScope.launch {
        val u = user.value ?: return@launch
        c.learnerData.toggleSaved(u.userId, SavedRecommendation(system, match.pathway.id, match.pathway.name, match.percent, System.currentTimeMillis()))
    }

    suspend fun explain(match: CbePathwayMatch): AiExplanation {
        val set = results.value
        return c.ai.explainCbe(
            CbeAiContext(
                firstName = user.value?.firstName ?: "",
                grade = profile.value.grade,
                pathwayName = match.pathway.name,
                trackName = match.bestTrack?.item?.name,
                areaName = match.bestArea?.item?.name,
                percent = match.percent,
                factors = match.factors,
                strengths = set?.strengthProfile ?: emptyList(),
                interests = set?.interestHighlights ?: emptyList(),
                careerNames = match.careers.map { it.item.name },
            ),
        )
    }
}

// ============================ 8-4-4 ============================

@OptIn(ExperimentalCoroutinesApi::class)
class EightFourFourViewModel(private val c: AppContainer) : ViewModel() {

    private val system = EducationSystem.EIGHT_FOUR_FOUR
    val assessment = EightFourFourAssessmentBank.assessment
    val draft = AssessmentDraft(assessment)

    private val userFlow: Flow<User> = c.auth.currentUser.filterNotNull()
        .filter { it.educationSystem == system }
        .distinctUntilChanged { a, b -> a.userId == b.userId && a.name == b.name }

    val user: StateFlow<User?> = userFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _data = MutableStateFlow<UniversityData?>(null)
    val data: StateFlow<UniversityData?> = _data
    private val _careers = MutableStateFlow<CareerData?>(null)
    val careers: StateFlow<CareerData?> = _careers

    init {
        viewModelScope.launch {
            _data.value = c.reference.universityData()
            _careers.value = c.reference.careerData()
        }
    }

    val profile: StateFlow<UniversityProfile> = userFlow.flatMapLatest { c.learnerData.universityProfile(it.userId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UniversityProfile())
    private val savedAnswers = userFlow.flatMapLatest { c.learnerData.answers(it.userId, system) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val subjectRanking = savedAnswers.map { EightFourFourAssessmentBank.subjectRanking(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val completed: StateFlow<Boolean> = userFlow.flatMapLatest { c.learnerData.completed(it.userId, system) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val saved: StateFlow<List<SavedRecommendation>> = userFlow.flatMapLatest { c.learnerData.saved(it.userId) }
        .map { list -> list.filter { it.system == system } }
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    val analysisStage = MutableStateFlow(0)
    val analysisError = MutableStateFlow<String?>(null)
    val analysisPreview = MutableStateFlow("")
    val analysisCourseNames = MutableStateFlow<List<String>>(emptyList())
    val geminiResult = MutableStateFlow<com.pathwayfit.app.ai.GeminiAssessmentResult?>(null)
    private val analyzedCourses = MutableStateFlow<CourseRecommendationSet?>(null)
    private var analysisRunning = false
    init {
        viewModelScope.launch {
            var owner: String? = null
            user.collect { current ->
                if (current?.userId != owner) {
                    geminiResult.value = null; analyzedCourses.value = null
                    analysisStage.value = 0; analysisError.value = null
                    owner = current?.userId
                }
            }
        }
    }


    fun analyzeAnswers() = viewModelScope.launch {
        if (analysisRunning || geminiResult.value != null) return@launch
        analysisRunning = true
        analysisPreview.value = ""; analysisCourseNames.value = emptyList()
        analysisError.value = null
        try {
            analysisStage.value = 1
            val u = user.value ?: error("Please sign in again.")
            val a = c.learnerData.answers(u.userId, system).first()
            check(a.isNotEmpty()) { "Complete your assessment first." }
            if (com.pathwayfit.app.ai.JuaGuideDemo.enabled) kotlinx.coroutines.delay(650)
            analysisStage.value = 2
            val d = _data.value ?: c.reference.universityData()
            val car = _careers.value ?: c.reference.careerData()
            val candidates = c.eightFourFourEngine.recommend(EightFourFourEngineInput(EightFourFourAssessmentBank.withPerformance(profile.value, a), assessment.questions, a, d, car))
            if (com.pathwayfit.app.ai.JuaGuideDemo.enabled) kotlinx.coroutines.delay(650)
            analysisStage.value = 3
            val result = com.pathwayfit.app.ai.GeminiAssessmentService().analyze(EightFourFourAssessmentBank.withPerformance(profile.value, a), assessment.questions, a, candidates) { event ->
                when (event["type"]?.jsonPrimitive?.content) {
                    "text" -> analysisPreview.value = event["text"]?.jsonPrimitive?.content.orEmpty()
                    "course" -> {
                        val id = event["course"]?.jsonObject?.get("courseId")?.jsonPrimitive?.content
                        candidates.courses.firstOrNull { it.course.id == id }?.let { match ->
                            analysisCourseNames.value = (analysisCourseNames.value + match.course.name).distinct()
                        }
                    }
                }
            }
            val selected = result.courseIds.map { id -> candidates.courses.first { it.course.id == id } }
            analyzedCourses.value = candidates.copy(courses = selected + candidates.courses.filter { it.course.id !in result.courseIds })
            geminiResult.value = result
            c.store.putObject("gemini_analysis_${u.userId}", result)
            analysisStage.value = 4
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            analysisError.value = error.message ?: "Analysis could not finish. Please retry."
        } finally { analysisRunning = false }
    }

    /** Course recommendations need the assessment; grades sharpen them but are optional. */
    private val localResults: StateFlow<CourseRecommendationSet?> =
        combine(profile, savedAnswers, completed, _data, _careers) { p, a, done, d, car ->
            if (!done || d == null || car == null) null
            else c.eightFourFourEngine.recommend(EightFourFourEngineInput(EightFourFourAssessmentBank.withPerformance(p, a), assessment.questions, a, d, car))
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val results: StateFlow<CourseRecommendationSet?> = combine(localResults, analyzedCourses) { local, ai -> ai ?: local }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Selected pair for the course comparison screen. */
    val comparison = MutableStateFlow<Pair<String, String>?>(null)

    fun startAssessment(resume: Boolean) {
        draft.reset(if (resume) savedAnswers.value else emptyList())
        geminiResult.value = null; analyzedCourses.value = null; analysisStage.value = 0; analysisError.value = null
    }

    fun saveProfile(p: UniversityProfile, onDone: () -> Unit = {}) = viewModelScope.launch {
        user.value?.let { c.learnerData.saveUniversityProfile(it.userId, p) }
        onDone()
    }

    fun submitAssessment(onDone: () -> Unit) = viewModelScope.launch {
        val u = user.value ?: return@launch
        val answers = draft.answered()
        c.learnerData.saveAnswers(u.userId, system, answers)
        c.learnerData.setCompleted(u.userId, system, true)
        val d = _data.value ?: c.reference.universityData()
        val car = _careers.value ?: c.reference.careerData()
        val set = c.eightFourFourEngine.recommend(EightFourFourEngineInput(EightFourFourAssessmentBank.withPerformance(profile.value, answers), assessment.questions, answers, d, car))
        set.top?.let { c.learnerData.addHistory(u.userId, AssessmentRecord(system, System.currentTimeMillis(), it.course.name, it.percent)) }
        onDone()
    }

    val guideTrialUsed = user.flatMapLatest { learner ->
        if (learner == null) kotlinx.coroutines.flow.flowOf(false)
        else c.store.boolFlow("guide_trial_${learner.userId}")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val guidePremium = c.premium.access

    suspend fun askJuaGuide(messages: List<Pair<String, String>>, attachment: kotlinx.serialization.json.JsonObject? = null, onText: (String) -> Unit = {}): com.pathwayfit.app.ai.GuideReply {
        val learner = user.value ?: error("Please sign in to use JuaGuide.")
        check(com.pathwayfit.app.ai.JuaGuideDemo.enabled || c.premium.access.value.isPremium || !c.store.boolFlow("guide_trial_${learner.userId}").first()) { "Your free JuaGuide question has been used. Upgrade to Premium to continue." }
        val answers = c.learnerData.answers(learner.userId, system).first()
        val context = kotlinx.serialization.json.buildJsonObject {
            attachment?.let { put("attachment", it) }
            put("assessment", kotlinx.serialization.json.Json.encodeToJsonElement(
                assessment.questions.map { q ->
                    val a = answers.firstOrNull { it.questionId == q.id }
                    mapOf("question" to q.prompt, "answer" to (q.options.filter { it.id in (a?.selectedOptionIds ?: emptySet()) }.joinToString { it.label } + " " + (a?.otherText ?: "")))
                }))
            put("catalog", kotlinx.serialization.json.Json.encodeToJsonElement(data.value?.courses?.map { course ->
                mapOf("course" to course.name, "universities" to data.value?.universitiesFor(course.id)?.joinToString { (university, offering) -> "${university.name} (offering verified: ${offering.verified})" }.orEmpty())
            }.orEmpty()))
            put("recommendations", kotlinx.serialization.json.Json.encodeToJsonElement(results.value?.courses?.take(5)?.map { it.course.name }.orEmpty()))
        }
        val reply = com.pathwayfit.app.ai.JuaGuideService.reply(messages, context, onText)
        if (reply.completed && !com.pathwayfit.app.ai.JuaGuideDemo.enabled) c.store.putBool("guide_trial_${learner.userId}", true)
        return reply
    }

    fun toggleSave(match: CourseMatch) = viewModelScope.launch {
        val u = user.value ?: return@launch
        c.learnerData.toggleSaved(u.userId, SavedRecommendation(system, match.course.id, match.course.name, match.percent, System.currentTimeMillis()))
    }

    suspend fun explain(match: CourseMatch): AiExplanation = c.ai.explainCourse(
        UniversityAiContext(
            firstName = user.value?.firstName ?: "",
            courseName = match.course.name,
            field = match.course.field,
            percent = match.percent,
            factors = match.factors,
            academicStrengths = results.value?.academicStrengths ?: emptyList(),
            careerNames = match.careers.map { it.name },
        ),
    )
}

// ============================ PREMIUM ============================

class PremiumViewModel(private val c: AppContainer) : ViewModel() {
    val access = c.premium.access
    val notifyMe: StateFlow<Boolean> = c.premium.notifyMe.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val developerPreview: StateFlow<Boolean> = c.premium.developerPreview.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val devToolsVisible = c.premium.devToolsVisible

    fun setNotifyMe(on: Boolean) = viewModelScope.launch { c.premium.setNotifyMe(on) }
    fun setDeveloperPreview(on: Boolean) = viewModelScope.launch { c.premium.setDeveloperPreview(on) }
}
