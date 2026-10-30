package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SerenityRepository
import com.example.model.CareActionLog
import com.example.model.ChatMessage
import com.example.model.DailyRhythmBlock
import com.example.model.DemoRecoveryStage
import com.example.model.LifeQuest
import com.example.model.MemoryItem
import com.example.model.MuseDemoScenario
import com.example.model.PresenceMode
import com.example.model.RealConnectorState
import com.example.model.ScreenNav
import com.example.model.SyntheticTelemetrySnapshot
import com.example.model.TamagotchiState
import com.example.model.ThoughtStream
import com.example.model.VitalityCheckin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerenityViewModel(
    private val repository: SerenityRepository = SerenityRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenNav.ATTUNE)
    val currentScreen: StateFlow<ScreenNav> = _currentScreen.asStateFlow()

    private val _showFullHome = MutableStateFlow(false)
    val showFullHome: StateFlow<Boolean> = _showFullHome.asStateFlow()

    fun setShowFullHome(show: Boolean) {
        _showFullHome.value = show
    }

    private val _showFullData = MutableStateFlow(false)
    val showFullData: StateFlow<Boolean> = _showFullData.asStateFlow()

    fun setShowFullData(show: Boolean) {
        _showFullData.value = show
    }

    private val _showFullConnections = MutableStateFlow(false)
    val showFullConnections: StateFlow<Boolean> = _showFullConnections.asStateFlow()

    fun setShowFullConnections(show: Boolean) {
        _showFullConnections.value = show
    }

    val tamagotchiState: StateFlow<TamagotchiState> = repository.tamagotchiState
    val activeDemoScenario: StateFlow<MuseDemoScenario> = repository.activeDemoScenario
    val demoRecoveryStage: StateFlow<DemoRecoveryStage> = repository.demoRecoveryStage
    val telemetrySnapshot: StateFlow<SyntheticTelemetrySnapshot> = repository.telemetrySnapshot
    val realConnectorState: StateFlow<RealConnectorState> = repository.realConnectorState

    val healthConnectPermissions: Set<String> = repository.healthConnectManager.requiredPermissions

    fun createHealthPermissionContract() =
        repository.healthConnectManager.createPermissionRequestContract()

    val careLogs: StateFlow<List<CareActionLog>> = repository.careLogs
    val lifeQuests: StateFlow<List<LifeQuest>> = repository.lifeQuests

    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val memories: StateFlow<List<MemoryItem>> = repository.memories
    val selectedMode: StateFlow<PresenceMode> = repository.selectedMode
    val auraIntensity: StateFlow<Float> = repository.auraIntensity
    val vitalityCheckin: StateFlow<VitalityCheckin> = repository.vitalityCheckin

    val thoughtStreams: List<ThoughtStream> = repository.getThoughtStreams()
    val dailyRhythm: List<DailyRhythmBlock> = repository.getDailyRhythm()

    val metaAiConfig = repository.metaAiConfig
    val latestMuseAdvice = repository.latestMuseAdvice
    val integrations = repository.integrations

    init {
        syncAllRealChannels()
    }

    fun syncAllRealChannels() {
        viewModelScope.launch {
            repository.syncAllRealChannels()
        }
    }

    fun syncRealHealth() {
        viewModelScope.launch {
            repository.syncRealHealth()
        }
    }

    fun syncRealCalendarAndTasks(googleTasksToken: String? = null) {
        viewModelScope.launch {
            repository.syncRealCalendarAndTasks(googleTasksToken)
        }
    }

    fun syncRealFacebookAndContacts(
        facebookAccessToken: String? = null,
        facebookAppId: String? = null
    ) {
        viewModelScope.launch {
            repository.syncRealFacebookAndContacts(facebookAccessToken, facebookAppId)
        }
    }

    fun launchFacebookOAuth(appId: String) {
        repository.launchFacebookOAuth(appId)
    }

    fun handleOAuthRedirect(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val handled = repository.handleOAuthRedirect(uri)
            if (handled) {
                _currentScreen.value = ScreenNav.CONNECTIONS
                _showFullConnections.value = true
            }
        }
    }

    fun connectPlaidBank(
        clientId: String,
        secret: String,
        environment: String,
        accessToken: String = ""
    ) {
        viewModelScope.launch {
            repository.connectPlaidBank(clientId, secret, environment, accessToken)
        }
    }

    fun connectSimpleFinBank(tokenOrUrl: String) {
        viewModelScope.launch {
            repository.connectSimpleFinBank(tokenOrUrl)
        }
    }

    fun importRealBankCsv(uri: Uri) {
        viewModelScope.launch {
            repository.importRealBankCsv(uri)
        }
    }

    fun openHealthConnectSettings() {
        repository.openHealthConnectSettings()
    }

    fun insertRecoveryCalendarEvent(title: String, description: String) {
        repository.insertRecoveryCalendarEvent(title, description)
    }

    fun activateDemoScenario(scenario: MuseDemoScenario) {
        repository.activateDemoScenario(scenario)
        if (scenario == MuseDemoScenario.LIVE_REAL_DATA) {
            syncAllRealChannels()
        }
    }

    fun completeDemoFollowThrough() {
        if (activeDemoScenario.value == MuseDemoScenario.LIVE_REAL_DATA) {
            syncAllRealChannels()
        }
        repository.completeDemoFollowThrough()
    }

    fun toggleGoogleHealth() {
        repository.toggleGoogleHealth()
    }

    fun toggleFacebook() {
        repository.toggleFacebook()
    }

    fun toggleFinanceApp() {
        repository.toggleFinanceApp()
    }

    fun toggleTasksApp() {
        repository.toggleTasksApp()
    }

    private val _isSyncingWithMuse = MutableStateFlow(false)
    val isSyncingWithMuse: StateFlow<Boolean> = _isSyncingWithMuse.asStateFlow()

    fun updateMetaAiToken(token: String) {
        repository.updateMetaAiToken(token)
    }

    fun sendAllInfoToMuse(onSuccess: (com.example.model.MuseLifeAdvice) -> Unit = {}) {
        viewModelScope.launch {
            _isSyncingWithMuse.value = true
            try {
                val advice = repository.sendAllInfoToMuse()
                onSuccess(advice)
            } finally {
                _isSyncingWithMuse.value = false
            }
        }
    }

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedMemoryCategory = MutableStateFlow("All")
    val selectedMemoryCategory: StateFlow<String> = _selectedMemoryCategory.asStateFlow()

    private val _isBreathingActive = MutableStateFlow(false)
    val isBreathingActive: StateFlow<Boolean> = _isBreathingActive.asStateFlow()

    private val _breathPhase = MutableStateFlow("Inhale...")
    val breathPhase: StateFlow<String> = _breathPhase.asStateFlow()

    // Tamagotchi caretaker actions
    fun feedWater() {
        repository.nurtureWater()
    }

    fun doBreathReset() {
        repository.nurtureBreathe()
        toggleBreathingExercise()
    }

    fun logMovementWalk() {
        repository.nurtureWalk()
    }

    fun logFocusSprint() {
        repository.nurtureFocus()
    }

    fun logConnection() {
        repository.nurtureConnect()
    }

    fun petTamagotchi() {
        repository.petTamagotchi()
    }

    fun toggleSleepMode() {
        repository.toggleSleepMode()
    }

    fun equipAccessory(accessory: String) {
        repository.equipAccessory(accessory)
    }

    fun toggleQuest(questId: String) {
        repository.toggleQuest(questId)
    }

    fun navigateTo(screen: ScreenNav) {
        when (screen) {
            ScreenNav.HOME, ScreenNav.ATTUNE, ScreenNav.DIALOGUE -> _showFullHome.value = false
            ScreenNav.DATA, ScreenNav.VITALITY, ScreenNav.VAULT -> _showFullData.value = false
            ScreenNav.CONNECTIONS, ScreenNav.PRESENCE -> _showFullConnections.value = false
        }
        _currentScreen.value = screen
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun sendCurrentMessage() {
        val text = _inputText.value.trim()
        if (text.isNotEmpty()) {
            _inputText.value = ""
            repository.sendMessage(text)
        }
    }

    fun sendQuickPrompt(promptText: String) {
        repository.sendMessage(promptText)
    }

    fun setPresenceMode(mode: PresenceMode) {
        repository.setPresenceMode(mode)
    }

    fun setAuraIntensity(intensity: Float) {
        repository.setAuraIntensity(intensity)
    }

    fun setMemoryCategory(category: String) {
        _selectedMemoryCategory.value = category
    }

    fun addMemory(title: String, content: String, category: String, isPinned: Boolean = false) {
        repository.addMemory(title, content, category, isPinned)
    }

    fun toggleMemoryPin(id: String) {
        repository.toggleMemoryPin(id)
    }

    fun updateVitality(clarity: Int, equilibrium: Int, peace: Int, mood: String) {
        repository.logVitalityCheckin(clarity, equilibrium, peace, mood)
    }

    fun toggleBreathingExercise() {
        if (_isBreathingActive.value) {
            _isBreathingActive.value = false
        } else {
            _isBreathingActive.value = true
            startBreathingCycle()
        }
    }

    private fun startBreathingCycle() {
        viewModelScope.launch {
            while (_isBreathingActive.value) {
                _breathPhase.value = "Inhale gently (4s)..."
                delay(4000)
                if (!_isBreathingActive.value) break
                _breathPhase.value = "Hold in serene calm (2s)..."
                delay(2000)
                if (!_isBreathingActive.value) break
                _breathPhase.value = "Exhale softly (4s)..."
                delay(4000)
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appCtx = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SerenityViewModel(SerenityRepository(appCtx)) as T
                }
            }
        }
    }
}
