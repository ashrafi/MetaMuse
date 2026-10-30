package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.connectors.BankFinanceConnector
import com.example.data.connectors.CalendarTasksConnector
import com.example.data.connectors.ConnectorPreferencesStore
import com.example.data.connectors.FacebookSocialConnector
import com.example.data.connectors.HealthConnectManager
import com.example.data.connectors.RealCalendarTasksResult
import com.example.data.connectors.RealFinanceSyncResult
import com.example.data.connectors.RealHealthSyncResult
import com.example.data.connectors.RealSocialSyncResult
import com.example.model.AccentType
import com.example.model.CareActionLog
import com.example.model.ChannelMetricItem
import com.example.model.ChannelTelemetryReport
import com.example.model.ChatMessage
import com.example.model.DailyRhythmBlock
import com.example.model.DemoRecoveryStage
import com.example.model.LifeQuest
import com.example.model.LifeSyncIntegrations
import com.example.model.MemoryItem
import com.example.model.MetaAiConfig
import com.example.model.MuseDemoScenario
import com.example.model.MuseLifeAdvice
import com.example.model.PresenceMode
import com.example.model.RealConnectorState
import com.example.model.SyntheticTelemetrySnapshot
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.model.ThoughtStep
import com.example.model.ThoughtStream
import com.example.model.VitalityCheckin
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SerenityRepository(
    private val appContext: Context? = null
) {

    private val prefsStore: ConnectorPreferencesStore? =
        appContext?.applicationContext?.let { ConnectorPreferencesStore(it) }

    val healthConnectManager = HealthConnectManager()
    val calendarTasksConnector = CalendarTasksConnector()
    val facebookSocialConnector = FacebookSocialConnector()
    val bankFinanceConnector = BankFinanceConnector()

    private var latestHealthResult: RealHealthSyncResult? = null
    private var latestCalendarResult: RealCalendarTasksResult? = null
    private var latestSocialResult: RealSocialSyncResult? = null
    private var latestFinanceResult: RealFinanceSyncResult? =
        prefsStore?.cachedCsvTransactionsJson?.takeIf { it.isNotBlank() }?.let {
            bankFinanceConnector.restoreCachedCsv(it)
        }

    private val initialScenario =
        if (appContext != null) MuseDemoScenario.LIVE_REAL_DATA else MuseDemoScenario.NONE

    private val _realConnectorState = MutableStateFlow(
        RealConnectorState(
            healthSdkAvailable = appContext?.let { healthConnectManager.isSdkAvailable(it) } ?: true,
            calendarPermissionGranted = appContext?.let { calendarTasksConnector.hasCalendarPermission(it) } ?: false,
            googleTasksToken = prefsStore?.googleTasksToken.orEmpty(),
            facebookAppId = prefsStore?.facebookAppId.orEmpty(),
            facebookAccessToken = prefsStore?.facebookAccessToken.orEmpty(),
            facebookUserName = prefsStore?.facebookUserName,
            contactsPermissionGranted = appContext?.let { facebookSocialConnector.hasContactsPermission(it) } ?: false,
            plaidClientId = prefsStore?.plaidClientId.orEmpty(),
            plaidSecret = prefsStore?.plaidSecret.orEmpty(),
            plaidEnvironment = prefsStore?.plaidEnvironment ?: "sandbox",
            plaidAccessToken = prefsStore?.plaidAccessToken.orEmpty(),
            simpleFinUrlOrToken = prefsStore?.simpleFinUrlOrToken.orEmpty(),
            connectedBankSource = latestFinanceResult?.sourceName,
            liveTotalBalanceUsd = latestFinanceResult?.totalBalanceUsd,
            liveMonthlySpendUsd = latestFinanceResult?.totalSpend30dUsd ?: 0.0,
            liveDeliveryOrderCount = latestFinanceResult?.deliveryOrderCount ?: 0,
            liveDeliverySpendUsd = latestFinanceResult?.deliverySpendUsd ?: 0.0
        )
    )
    val realConnectorState: StateFlow<RealConnectorState> = _realConnectorState.asStateFlow()

    private val _tamagotchiState = MutableStateFlow(
        TamagotchiState(
            name = "Lumi",
            evolution = "Luminous Luminary (Stage 3)",
            level = 4,
            xp = 740,
            xpForNextLevel = 1000,
            hydration = 0.85f,
            focus = 0.90f,
            innerPeace = 0.82f,
            physicalEnergy = 0.78f,
            connection = 0.80f,
            mood = TamagotchiMood.RADIANT,
            speech = if (appContext != null) {
                "Ready for real-world telemetry! Connect Health Connect, Calendar, Facebook, or your Bank in Connections."
            } else {
                "I feel so radiant today! You've nurtured our mind and body beautifully."
            },
            equippedAccessory = "Celestial Halo",
            isSleeping = false,
            totalPets = 42,
            activeScenario = initialScenario,
            recoveryStage = DemoRecoveryStage.RECOVERED_HAPPY
        )
    )
    val tamagotchiState: StateFlow<TamagotchiState> = _tamagotchiState.asStateFlow()

    private val _activeDemoScenario = MutableStateFlow(initialScenario)
    val activeDemoScenario: StateFlow<MuseDemoScenario> = _activeDemoScenario.asStateFlow()

    private val _demoRecoveryStage = MutableStateFlow(DemoRecoveryStage.RECOVERED_HAPPY)
    val demoRecoveryStage: StateFlow<DemoRecoveryStage> = _demoRecoveryStage.asStateFlow()

    private val _telemetrySnapshot = MutableStateFlow(buildTelemetrySnapshot(initialScenario, false))
    val telemetrySnapshot: StateFlow<SyntheticTelemetrySnapshot> = _telemetrySnapshot.asStateFlow()

    private val _careLogs = MutableStateFlow<List<CareActionLog>>(initialCareLogs())
    val careLogs: StateFlow<List<CareActionLog>> = _careLogs.asStateFlow()

    private val _lifeQuests = MutableStateFlow<List<LifeQuest>>(initialLifeQuests())
    val lifeQuests: StateFlow<List<LifeQuest>> = _lifeQuests.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(initialMessages())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(initialMemories())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    private val _selectedMode = MutableStateFlow(PresenceMode.MINDFUL_GUIDE)
    val selectedMode: StateFlow<PresenceMode> = _selectedMode.asStateFlow()

    private val _auraIntensity = MutableStateFlow(1.0f)
    val auraIntensity: StateFlow<Float> = _auraIntensity.asStateFlow()

    private val _vitalityCheckin = MutableStateFlow(
        VitalityCheckin(
            clarityScore = 91,
            emotionalEquilibrium = 88,
            restorativePeace = 84,
            moodWord = "Radiant Equilibrium"
        )
    )
    val vitalityCheckin: StateFlow<VitalityCheckin> = _vitalityCheckin.asStateFlow()

    private val museService = MuseService()

    private val _metaAiConfig = MutableStateFlow(
        MetaAiConfig(
            token = prefsStore?.metaAiToken.orEmpty(),
            isConnected = prefsStore?.metaAiToken?.isNotBlank() == true,
            lastSyncedAt = null,
            endpointUrl = prefsStore?.metaAiEndpoint ?: "https://api.groq.com/openai/v1/chat/completions",
            modelName = prefsStore?.metaAiModel ?: "llama-3.1-70b-versatile",
            isLocalLlamaPrivacyMode = prefsStore?.isLocalLlamaPrivacyMode ?: true
        )
    )
    val metaAiConfig: StateFlow<MetaAiConfig> = _metaAiConfig.asStateFlow()

    private val _latestMuseAdvice = MutableStateFlow<MuseLifeAdvice?>(null)
    val latestMuseAdvice: StateFlow<MuseLifeAdvice?> = _latestMuseAdvice.asStateFlow()

    private val _integrations = MutableStateFlow(
        LifeSyncIntegrations(
            financeAppConnected = latestFinanceResult?.isConnected == true,
            financeAppStats = latestFinanceResult?.statusLine
                ?: "Not connected • Link Plaid API, SimpleFIN, or Bank CSV"
        )
    )
    val integrations: StateFlow<LifeSyncIntegrations> = _integrations.asStateFlow()

    private val _appThemeMode = MutableStateFlow(
        prefsStore?.appThemeMode?.let {
            try {
                AppThemeMode.valueOf(it)
            } catch (e: Exception) {
                AppThemeMode.LUMINOUS_SERENITY
            }
        } ?: AppThemeMode.LUMINOUS_SERENITY
    )
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
        prefsStore?.appThemeMode = mode.name
    }

    // =========================================================================
    // REAL CONNECTOR SYNC & AUTHENTICATION METHODS
    // =========================================================================

    suspend fun syncAllRealChannels() {
        val ctx = appContext ?: return
        _realConnectorState.value = _realConnectorState.value.copy(
            isSyncing = true,
            lastError = null
        )
        try {
            latestHealthResult = healthConnectManager.syncRealHealthData(ctx)
            latestCalendarResult = calendarTasksConnector.syncCalendarAndTasks(
                context = ctx,
                googleTasksOAuthToken = _realConnectorState.value.googleTasksToken
            )
            latestSocialResult = facebookSocialConnector.syncFacebookAndContacts(
                context = ctx,
                facebookAccessToken = _realConnectorState.value.facebookAccessToken,
                calendarDeclinedEvents14d = latestCalendarResult?.canceledOrDeclined14d ?: 0
            )

            // Refresh bank if Plaid or SimpleFIN credentials are stored
            val st = _realConnectorState.value
            when {
                st.plaidClientId.isNotBlank() && st.plaidSecret.isNotBlank() -> {
                    val plaidRes = bankFinanceConnector.syncViaPlaid(
                        clientId = st.plaidClientId,
                        secret = st.plaidSecret,
                        environment = st.plaidEnvironment,
                        existingAccessToken = st.plaidAccessToken
                    )
                    if (plaidRes.isConnected) {
                        latestFinanceResult = plaidRes
                        plaidRes.exchangedPlaidAccessToken?.let { token ->
                            prefsStore?.plaidAccessToken = token
                            _realConnectorState.value = _realConnectorState.value.copy(plaidAccessToken = token)
                        }
                    }
                }
                st.simpleFinUrlOrToken.isNotBlank() && st.simpleFinUrlOrToken.startsWith("http") -> {
                    val sfRes = bankFinanceConnector.syncViaSimpleFin(st.simpleFinUrlOrToken)
                    if (sfRes.isConnected) {
                        latestFinanceResult = sfRes
                    }
                }
            }
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastSyncSummary = "Synced at ${currentTimeFormatted()}"
            )
        }
    }

    suspend fun syncRealHealth() {
        val ctx = appContext ?: return
        _realConnectorState.value = _realConnectorState.value.copy(isSyncing = true, lastError = null)
        try {
            latestHealthResult = healthConnectManager.syncRealHealthData(ctx)
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastSyncSummary = "Health Connect synced at ${currentTimeFormatted()}"
            )
        }
    }

    suspend fun syncRealCalendarAndTasks(googleTasksToken: String? = null) {
        val ctx = appContext ?: return
        if (googleTasksToken != null) {
            val cleaned = googleTasksToken.trim()
            prefsStore?.googleTasksToken = cleaned
            _realConnectorState.value = _realConnectorState.value.copy(googleTasksToken = cleaned)
        }
        _realConnectorState.value = _realConnectorState.value.copy(isSyncing = true, lastError = null)
        try {
            val res = calendarTasksConnector.syncCalendarAndTasks(
                context = ctx,
                googleTasksOAuthToken = _realConnectorState.value.googleTasksToken
            )
            latestCalendarResult = res
            // Re-evaluate social declined count if contacts/facebook also active
            if (latestSocialResult != null) {
                latestSocialResult = facebookSocialConnector.syncFacebookAndContacts(
                    context = ctx,
                    facebookAccessToken = _realConnectorState.value.facebookAccessToken,
                    calendarDeclinedEvents14d = res.canceledOrDeclined14d
                )
            }
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastSyncSummary = "Calendar & Tasks synced at ${currentTimeFormatted()}"
            )
        }
    }

    suspend fun syncRealFacebookAndContacts(
        facebookAccessToken: String? = null,
        facebookAppId: String? = null
    ) {
        val ctx = appContext ?: return
        if (facebookAppId != null) {
            val cleanAppId = facebookAppId.trim()
            prefsStore?.facebookAppId = cleanAppId
            _realConnectorState.value = _realConnectorState.value.copy(facebookAppId = cleanAppId)
        }
        if (facebookAccessToken != null) {
            val cleanToken = facebookAccessToken.trim()
            prefsStore?.facebookAccessToken = cleanToken
            _realConnectorState.value = _realConnectorState.value.copy(facebookAccessToken = cleanToken)
        }
        _realConnectorState.value = _realConnectorState.value.copy(isSyncing = true, lastError = null)
        try {
            val res = facebookSocialConnector.syncFacebookAndContacts(
                context = ctx,
                facebookAccessToken = _realConnectorState.value.facebookAccessToken,
                calendarDeclinedEvents14d = latestCalendarResult?.canceledOrDeclined14d ?: 0
            )
            latestSocialResult = res
            if (res.facebookUserName != null) {
                prefsStore?.facebookUserName = res.facebookUserName
            }
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastError = latestSocialResult?.errorMessage,
                lastSyncSummary = "Facebook & Contacts synced at ${currentTimeFormatted()}"
            )
        }
    }

    fun launchFacebookOAuth(appId: String) {
        val ctx = appContext ?: return
        val cleanId = appId.trim()
        prefsStore?.facebookAppId = cleanId
        _realConnectorState.value = _realConnectorState.value.copy(facebookAppId = cleanId)
        facebookSocialConnector.launchFacebookOAuthLogin(ctx, cleanId)
    }

    suspend fun handleOAuthRedirect(uri: Uri?): Boolean {
        val token = facebookSocialConnector.extractAccessTokenFromRedirect(uri) ?: return false
        syncRealFacebookAndContacts(facebookAccessToken = token)
        return true
    }

    suspend fun connectPlaidBank(
        clientId: String,
        secret: String,
        environment: String,
        accessToken: String = ""
    ) {
        val cleanId = clientId.trim()
        val cleanSecret = secret.trim()
        val cleanEnv = environment.trim().lowercase(Locale.US).ifBlank { "sandbox" }
        val cleanToken = accessToken.trim()

        prefsStore?.plaidClientId = cleanId
        prefsStore?.plaidSecret = cleanSecret
        prefsStore?.plaidEnvironment = cleanEnv
        if (cleanToken.isNotBlank()) {
            prefsStore?.plaidAccessToken = cleanToken
        }

        _realConnectorState.value = _realConnectorState.value.copy(
            isSyncing = true,
            lastError = null,
            plaidClientId = cleanId,
            plaidSecret = cleanSecret,
            plaidEnvironment = cleanEnv,
            plaidAccessToken = cleanToken.ifBlank { _realConnectorState.value.plaidAccessToken }
        )
        try {
            val res = bankFinanceConnector.syncViaPlaid(
                clientId = cleanId,
                secret = cleanSecret,
                environment = cleanEnv,
                existingAccessToken = cleanToken
            )
            latestFinanceResult = res
            if (res.exchangedPlaidAccessToken != null) {
                prefsStore?.plaidAccessToken = res.exchangedPlaidAccessToken
                _realConnectorState.value = _realConnectorState.value.copy(
                    plaidAccessToken = res.exchangedPlaidAccessToken
                )
            }
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastError = latestFinanceResult?.errorMessage,
                lastSyncSummary = if (latestFinanceResult?.isConnected == true) {
                    "Plaid Bank connected at ${currentTimeFormatted()}"
                } else {
                    latestFinanceResult?.statusLine ?: "Plaid sync finished"
                }
            )
        }
    }

    suspend fun connectSimpleFinBank(tokenOrUrl: String) {
        val clean = tokenOrUrl.trim()
        prefsStore?.simpleFinUrlOrToken = clean
        _realConnectorState.value = _realConnectorState.value.copy(
            isSyncing = true,
            lastError = null,
            simpleFinUrlOrToken = clean
        )
        try {
            val res = bankFinanceConnector.syncViaSimpleFin(clean)
            latestFinanceResult = res
            if (res.claimedSimpleFinAccessUrl != null) {
                prefsStore?.simpleFinUrlOrToken = res.claimedSimpleFinAccessUrl
                _realConnectorState.value = _realConnectorState.value.copy(
                    simpleFinUrlOrToken = res.claimedSimpleFinAccessUrl
                )
            }
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastError = latestFinanceResult?.errorMessage,
                lastSyncSummary = "SimpleFIN Bank synced at ${currentTimeFormatted()}"
            )
        }
    }

    suspend fun importRealBankCsv(uri: Uri) {
        val ctx = appContext ?: return
        _realConnectorState.value = _realConnectorState.value.copy(isSyncing = true, lastError = null)
        try {
            val (res, summaryJson) = bankFinanceConnector.importBankCsv(ctx, uri)
            latestFinanceResult = res
            if (summaryJson != null) {
                prefsStore?.cachedCsvTransactionsJson = summaryJson
            }
            _activeDemoScenario.value = MuseDemoScenario.LIVE_REAL_DATA
            rebuildLiveTelemetryAndEvaluateRules()
        } finally {
            _realConnectorState.value = _realConnectorState.value.copy(
                isSyncing = false,
                lastError = latestFinanceResult?.errorMessage,
                lastSyncSummary = "Bank CSV imported at ${currentTimeFormatted()}"
            )
        }
    }

    fun openHealthConnectSettings() {
        val ctx = appContext ?: return
        healthConnectManager.openHealthConnectSettings(ctx)
    }

    fun insertRecoveryCalendarEvent(title: String, description: String) {
        val ctx = appContext ?: return
        calendarTasksConnector.openCalendarInsertEvent(ctx, title, description)
    }

    private fun rebuildLiveTelemetryAndEvaluateRules() {
        val h = latestHealthResult
        val c = latestCalendarResult
        val s = latestSocialResult
        val f = latestFinanceResult

        _realConnectorState.value = _realConnectorState.value.copy(
            healthSdkAvailable = appContext?.let { healthConnectManager.isSdkAvailable(it) } ?: true,
            healthPermissionsGranted = h?.isConnected == true,
            liveStepsToday = h?.stepsToday ?: 0L,
            liveAvgSleepHours = h?.avgSleepHours7d ?: 0f,
            liveHeartRateBpm = h?.restingOrAvgHrBpm ?: 0L,
            liveHydrationLiters = h?.hydrationLitersToday ?: 0.0,
            calendarPermissionGranted = c?.isConnected == true ||
                (appContext?.let { calendarTasksConnector.hasCalendarPermission(it) } == true),
            liveEventsToday = c?.eventsTodayCount ?: 0,
            liveCompletedEventsToday = c?.completedTodayCount ?: 0,
            liveLateNightEvents7d = c?.lateNightEvents7d ?: 0,
            liveCanceledEvents14d = c?.canceledOrDeclined14d ?: 0,
            facebookUserName = s?.facebookUserName ?: _realConnectorState.value.facebookUserName,
            contactsPermissionGranted = s?.contactsConnected == true ||
                (appContext?.let { facebookSocialConnector.hasContactsPermission(it) } == true),
            liveStarredContactsCount = s?.starredCloseContacts ?: 0,
            liveNeglectedContactsCount = s?.neglectedStarredContacts ?: 0,
            connectedBankSource = f?.sourceName,
            liveTotalBalanceUsd = f?.totalBalanceUsd,
            liveMonthlySpendUsd = f?.totalSpend30dUsd ?: 0.0,
            liveDeliveryOrderCount = f?.deliveryOrderCount ?: 0,
            liveDeliverySpendUsd = f?.deliverySpendUsd ?: 0.0
        )

        if (_activeDemoScenario.value != MuseDemoScenario.LIVE_REAL_DATA) {
            return
        }

        _integrations.value = LifeSyncIntegrations(
            googleHealthConnected = h?.isConnected == true,
            googleHealthStats = h?.statusLine ?: "Not connected • Grant Android Health Connect access",
            facebookConnected = s?.isConnected == true,
            facebookStats = s?.statusLine ?: "Not connected • Connect Facebook Graph API or Contacts",
            financeAppConnected = f?.isConnected == true,
            financeAppStats = f?.statusLine ?: "Not connected • Link Plaid API, SimpleFIN, or Bank CSV",
            tasksAppConnected = c?.isConnected == true,
            tasksAppStats = c?.statusLine ?: "Not connected • Grant Android Calendar / Google Tasks access"
        )

        val liveSnap = buildLiveTelemetrySnapshot()
        _telemetrySnapshot.value = liveSnap

        val connectedCount = listOf(
            h?.isConnected == true,
            c?.isConnected == true,
            s?.isConnected == true,
            f?.isConnected == true
        ).count { it }

        val hydrationVital = when {
            h?.isConnected == true && h.hydrationLitersToday > 0.1 ->
                (h.hydrationLitersToday / 2.2).toFloat().coerceIn(0.35f, 1.0f)
            else -> 0.80f
        }
        val physicalVital = if (h?.isConnected == true) {
            (h.report.scorePercent / 100f).coerceIn(0.25f, 1.0f)
        } else 0.75f
        val focusVital = if (c?.isConnected == true) {
            (c.report.scorePercent / 100f).coerceIn(0.35f, 1.0f)
        } else 0.78f
        val peaceVital = if (f?.isConnected == true) {
            (f.report.scorePercent / 100f).coerceIn(0.30f, 1.0f)
        } else 0.78f
        val connectionVital = if (s?.isConnected == true) {
            (s.report.scorePercent / 100f).coerceIn(0.25f, 1.0f)
        } else 0.75f

        val (liveMood, liveSpeech, liveStage) = when {
            connectedCount == 0 -> Triple(
                TamagotchiMood.BALANCED,
                "Live mode active! Open the Connections tab to authorize Health Connect, Calendar, Facebook, or your Bank account.",
                DemoRecoveryStage.RECOVERED_HAPPY
            )
            h?.report?.isAlert == true && (c?.report?.isAlert == true || s?.report?.isAlert == true) -> Triple(
                TamagotchiMood.SICK,
                "Live Alert: Your real sleep dropped to ${String.format(Locale.US, "%.1f", h.avgSleepHours7d)}h while late-night schedule load spiked. Let's protect tonight's rest!",
                DemoRecoveryStage.PROACTIVE_CONCERN
            )
            f?.report?.isAlert == true -> Triple(
                TamagotchiMood.SICK,
                "Live Finance Alert: I spotted ${f.deliveryOrderCount} food-delivery charges ($${String.format(Locale.US, "%.0f", f.deliverySpendUsd)}) in your real transactions. Want to plan an easy 5-minute dinner?",
                DemoRecoveryStage.PROACTIVE_CONCERN
            )
            s?.report?.isAlert == true -> Triple(
                TamagotchiMood.SAD,
                "Live Social Alert: Several recent events were declined. Want to schedule 10 minutes to catch up with someone close?",
                DemoRecoveryStage.PROACTIVE_CONCERN
            )
            h?.report?.isAlert == true -> Triple(
                TamagotchiMood.TIRED,
                "Live Health Connect shows ${String.format(Locale.US, "%.1f", h.avgSleepHours7d)}h average sleep. Let's wind down early tonight!",
                DemoRecoveryStage.PROACTIVE_CONCERN
            )
            else -> Triple(
                TamagotchiMood.RADIANT,
                "Synced with $connectedCount live data stream${if (connectedCount == 1) "" else "s"}! Your real-world vitals are feeding my aura directly.",
                DemoRecoveryStage.RECOVERED_HAPPY
            )
        }

        _demoRecoveryStage.value = liveStage
        _tamagotchiState.value = _tamagotchiState.value.copy(
            hydration = hydrationVital,
            focus = focusVital,
            innerPeace = peaceVital,
            physicalEnergy = physicalVital,
            connection = connectionVital,
            mood = liveMood,
            speech = liveSpeech,
            activeScenario = MuseDemoScenario.LIVE_REAL_DATA,
            recoveryStage = liveStage
        )
    }

    private fun buildLiveTelemetrySnapshot(): SyntheticTelemetrySnapshot {
        val hReport = latestHealthResult?.report ?: ChannelTelemetryReport(
            channelName = "Health (Health Connect)",
            headlineStatus = "Permission Needed",
            summary = "Grant Android Health Connect access in Connections to stream real steps, sleep, heart rate, and hydration.",
            scorePercent = 50,
            isAlert = false,
            metrics = listOf(
                ChannelMetricItem("Steps Today", "—", "Connect Health"),
                ChannelMetricItem("7d Avg Sleep", "—", "Connect Health"),
                ChannelMetricItem("Heart Rate", "—", "Connect Health")
            ),
            historyLog = listOf("Open Connections → Connect to Google Health to authorize live Health Connect records.")
        )

        val cReport = latestCalendarResult?.report ?: ChannelTelemetryReport(
            channelName = "Calendar & Google Tasks",
            headlineStatus = "Permission Needed",
            summary = "Grant Android Calendar permission or link a Google Tasks OAuth token to read your real schedule and focus load.",
            scorePercent = 50,
            isAlert = false,
            metrics = listOf(
                ChannelMetricItem("Today's Events", "—", "Connect Calendar"),
                ChannelMetricItem("Late-Night Load", "—", "Connect Calendar"),
                ChannelMetricItem("Google Tasks", "—", "Optional OAuth")
            ),
            historyLog = listOf("Open Connections → Connect Calendar & Tasks to read live device events.")
        )

        val fReport = latestFinanceResult?.report ?: bankFinanceConnector.unconfiguredResult().report

        val sReport = latestSocialResult?.report ?: ChannelTelemetryReport(
            channelName = "Social (Facebook & Contacts)",
            headlineStatus = "Connection Needed",
            summary = "Sign in with Facebook Graph API v21.0 or grant Contacts permission to track close relationships, events, and birthdays.",
            scorePercent = 50,
            isAlert = false,
            metrics = listOf(
                ChannelMetricItem("Facebook Graph", "Not linked", "OAuth / Token"),
                ChannelMetricItem("Close Circle", "—", "Contacts permission"),
                ChannelMetricItem("Birthdays", "—", "Contacts sync")
            ),
            historyLog = listOf("Open Connections → Connect to Facebook to link Graph API or starred contacts.")
        )

        val anyAlert = hReport.isAlert || cReport.isAlert || fReport.isAlert || sReport.isAlert
        val connectedNames = buildList {
            if (latestHealthResult?.isConnected == true) add("Health Connect")
            if (latestCalendarResult?.isConnected == true) add("Calendar/Tasks")
            if (latestFinanceResult?.isConnected == true) add(latestFinanceResult?.sourceName ?: "Bank")
            if (latestSocialResult?.isConnected == true) add("Facebook/Contacts")
        }

        val ruleExplanation = when {
            connectedNames.isEmpty() ->
                "LIVE MODE: 0 channels connected yet. Go to the Connections tab to authorize real Google Health, Calendar, Facebook, and Bank feeds."
            anyAlert ->
                "LIVE ANOMALY DETECTED across ${connectedNames.joinToString(", ")}: Muse's rules engine flagged a real-world imbalance."
            else ->
                "LIVE TELEMETRY ACTIVE (${connectedNames.joinToString(", ")}): All connected real-world channels are within healthy bounds."
        }

        return SyntheticTelemetrySnapshot(
            scenario = MuseDemoScenario.LIVE_REAL_DATA,
            deterministicTriggerActive = anyAlert,
            triggeredRuleExplanation = ruleExplanation,
            healthReport = hReport,
            tasksReport = cReport,
            financeReport = fReport,
            socialReport = sReport,
            weeklySleepHours = latestHealthResult?.weeklySleepHours ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
    }

    /**
     * Deterministic Event Detection & Synthetic Data Pump for the 3 MuseLife Demos.
     * Pre-loads multi-channel history, evaluates the rules engine, makes the companion
     * visibly sick/sad, and proactively initiates the conversation on the Home tab.
     */
    fun activateDemoScenario(scenario: MuseDemoScenario) {
        _activeDemoScenario.value = scenario
        val timeStr = currentTimeFormatted()
        val snap = buildTelemetrySnapshot(scenario, isRecovered = false)
        _telemetrySnapshot.value = snap

        when (scenario) {
            MuseDemoScenario.LIVE_REAL_DATA -> {
                rebuildLiveTelemetryAndEvaluateRules()
            }

            MuseDemoScenario.NONE -> {
                _demoRecoveryStage.value = DemoRecoveryStage.RECOVERED_HAPPY
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    hydration = 0.85f,
                    focus = 0.90f,
                    innerPeace = 0.82f,
                    physicalEnergy = 0.78f,
                    connection = 0.80f,
                    mood = TamagotchiMood.RADIANT,
                    speech = "I feel so radiant today! You've nurtured our mind and body beautifully.",
                    isSleeping = false,
                    activeScenario = MuseDemoScenario.NONE,
                    recoveryStage = DemoRecoveryStage.RECOVERED_HAPPY
                )
                _integrations.value = LifeSyncIntegrations(
                    googleHealthConnected = true,
                    googleHealthStats = "7,840 steps • 7.4h sleep • 1.8L water synced",
                    facebookConnected = true,
                    facebookStats = "Synced: 4 close friend interactions & community circles",
                    financeAppConnected = true,
                    financeAppStats = "Daily mindful budget on track • Peace of mind +15%",
                    tasksAppConnected = true,
                    tasksAppStats = "4 of 5 focus tasks completed today (+20% Flow)"
                )
                _messages.value = initialMessages()
            }

            MuseDemoScenario.SUCCESS_TRAP -> {
                _demoRecoveryStage.value = DemoRecoveryStage.PROACTIVE_CONCERN
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    hydration = 0.58f,
                    focus = 0.98f, // High task output
                    innerPeace = 0.36f,
                    physicalEnergy = 0.30f, // Sleep dropped 7.5 -> 5.0h
                    connection = 0.28f, // 3 canceled friend plans
                    mood = TamagotchiMood.SICK,
                    speech = "I've noticed something, and it's worrying me. You're getting more done than ever, and your savings are growing. But you're barely sleeping, and you've canceled plans with people you care about three weeks in a row.",
                    isSleeping = false,
                    activeScenario = MuseDemoScenario.SUCCESS_TRAP,
                    recoveryStage = DemoRecoveryStage.PROACTIVE_CONCERN
                )
                _integrations.value = LifeSyncIntegrations(
                    googleHealthConnected = true,
                    googleHealthStats = "ALERT: Sleep dropped from 7.5h to 5.0h over two weeks",
                    facebookConnected = true,
                    facebookStats = "ALERT: 3 canceled plans with close friends; 0 social time this week",
                    financeAppConnected = true,
                    financeAppStats = "Savings increased +20% this month",
                    tasksAppConnected = true,
                    tasksAppStats = "98% complete • Working past midnight every night"
                )
                _vitalityCheckin.value = VitalityCheckin(
                    clarityScore = 76,
                    emotionalEquilibrium = 42,
                    restorativePeace = 35,
                    moodWord = "Overworked & Depleted"
                )
                val proactiveMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromUser = false,
                    content = "I've noticed something, and it's worrying me. You're getting more done than ever, and your savings are growing. But you're barely sleeping, and you've canceled plans with people you care about three weeks in a row.",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Deterministic Rule Triggered", "The Success Trap: High Task/Finance metrics masking severe Health & Social erosion"),
                        ThoughtStep("Health Channel", "Sleep dropped from 7.5h to 5.0h over 14 days"),
                        ThoughtStep("Tasks & Finance Channels", "98% tasks complete (late night commits); Savings +20%"),
                        ThoughtStep("Social Channel", "3 canceled plans with close friends; 0 meaningful social hours")
                    ),
                    suggestedPrompts = listOf(
                        "I'm working hard now so I can have a better life later."
                    ),
                    category = "Demo 1 • The Success Trap"
                )
                _messages.value = listOf(proactiveMsg)
            }

            MuseDemoScenario.INVISIBLE_MONEY_LEAK -> {
                _demoRecoveryStage.value = DemoRecoveryStage.PROACTIVE_CONCERN
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    hydration = 0.52f,
                    focus = 0.86f,
                    innerPeace = 0.34f, // Stressful evenings + savings falling behind
                    physicalEnergy = 0.38f, // Poor sleep consecutive nights
                    connection = 0.40f, // Most evenings spent alone at home
                    mood = TamagotchiMood.SICK,
                    speech = "Can we talk about your evenings? I've noticed that when you're exhausted and spending more time alone, you tend to order food and spend more money. I'm worried you're stuck in a cycle that's making things harder.",
                    isSleeping = false,
                    activeScenario = MuseDemoScenario.INVISIBLE_MONEY_LEAK,
                    recoveryStage = DemoRecoveryStage.PROACTIVE_CONCERN
                )
                _integrations.value = LifeSyncIntegrations(
                    googleHealthConnected = true,
                    googleHealthStats = "ALERT: Poor sleep (5.4h) on 5 consecutive nights",
                    facebookConnected = true,
                    facebookStats = "Most evenings spent alone at home (6 of 7 nights)",
                    financeAppConnected = true,
                    financeAppStats = "ALERT: 9 small food-delivery orders ($284) • Savings falling behind",
                    tasksAppConnected = true,
                    tasksAppStats = "Important tasks done, but high evening stress load"
                )
                _vitalityCheckin.value = VitalityCheckin(
                    clarityScore = 68,
                    emotionalEquilibrium = 45,
                    restorativePeace = 40,
                    moodWord = "Evening Exhaustion Loop"
                )
                val proactiveMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromUser = false,
                    content = "Can we talk about your evenings? I've noticed that when you're exhausted and spending more time alone, you tend to order food and spend more money. I'm worried you're stuck in a cycle that's making things harder.",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Deterministic Rule Triggered", "The Invisible Money Leak: Cross-channel evening exhaustion → convenience spend loop"),
                        ThoughtStep("Health & Tasks Channels", "Poor sleep on consecutive nights + stressful evening workload"),
                        ThoughtStep("Social Channel", "Most evenings spent alone at home without decompression"),
                        ThoughtStep("Finance Channel", "Recurring small food-delivery & convenience purchases; savings goal slipping")
                    ),
                    suggestedPrompts = listOf(
                        "I'm just tired when I get home. Ordering food is the only thing I feel like doing."
                    ),
                    category = "Demo 2 • The Invisible Money Leak"
                )
                _messages.value = listOf(proactiveMsg)
            }

            MuseDemoScenario.CONNECTION_DEBT -> {
                _demoRecoveryStage.value = DemoRecoveryStage.PROACTIVE_CONCERN
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    hydration = 0.88f,
                    focus = 0.92f,
                    innerPeace = 0.68f,
                    physicalEnergy = 0.88f, // Good sleep and exercise
                    connection = 0.24f, // Postponed sibling call, missed birthday, rescheduled family visit
                    mood = TamagotchiMood.SAD,
                    speech = "I've been thinking about something. You've been taking good care of yourself, staying on top of your responsibilities, and making progress with your money. But you've postponed calling your sibling several times, and you missed your friend's birthday.",
                    isSleeping = false,
                    activeScenario = MuseDemoScenario.CONNECTION_DEBT,
                    recoveryStage = DemoRecoveryStage.PROACTIVE_CONCERN
                )
                _integrations.value = LifeSyncIntegrations(
                    googleHealthConnected = true,
                    googleHealthStats = "7.8h sleep • 9,200 steps • Physically thriving",
                    facebookConnected = true,
                    facebookStats = "ALERT: Postponed sibling call 3x • Missed friend's birthday • Family visit rescheduled 2x",
                    financeAppConnected = true,
                    financeAppStats = "Budget on track • Emergency & goal savings growing",
                    tasksAppConnected = true,
                    tasksAppStats = "100% of daily tasks completed on schedule"
                )
                _vitalityCheckin.value = VitalityCheckin(
                    clarityScore = 90,
                    emotionalEquilibrium = 62,
                    restorativePeace = 64,
                    moodWord = "Quiet Relational Drift"
                )
                val proactiveMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromUser = false,
                    content = "I've been thinking about something. You've been taking good care of yourself, staying on top of your responsibilities, and making progress with your money. But you've postponed calling your sibling several times, and you missed your friend's birthday.",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Deterministic Rule Triggered", "The Connection Debt: Gap between stated core values and relational actions"),
                        ThoughtStep("Health, Tasks & Finance", "All green: 7.8h sleep, 100% tasks done, budget & savings on track"),
                        ThoughtStep("Social & Values Mismatch", "Sibling call postponed 3x, missed close friend's birthday, family visit rescheduled twice")
                    ),
                    suggestedPrompts = listOf(
                        "I've just had other things to do. I'll get around to it."
                    ),
                    category = "Demo 3 • The Connection Debt"
                )
                _messages.value = listOf(proactiveMsg)
            }
        }
    }

    /**
     * Simulates or verifies the user's real-world follow-through action.
     * Agreement alone makes Muse HOPEFUL; verified follow-through makes Muse RADIANT (healthy & happy).
     */
    fun completeDemoFollowThrough() {
        val scenario = _activeDemoScenario.value
        val curr = _tamagotchiState.value
        val timeStr = currentTimeFormatted()

        _demoRecoveryStage.value = DemoRecoveryStage.RECOVERED_HAPPY
        _telemetrySnapshot.value = buildTelemetrySnapshot(scenario, isRecovered = true)

        val (speechText, actionTitle, actionDetail) = when (scenario) {
            MuseDemoScenario.SUCCESS_TRAP -> Triple(
                "You logged a full 7.5 hours of sleep and locked in your weekend plans with friends! My glow is completely restored—that sounds like a life worth building! ✨",
                "Verified Follow-Through: 11 PM Cutoff & Weekend Friends Plan",
                "7.5h sleep restored • Calendar block set for close friends"
            )
            MuseDemoScenario.INVISIBLE_MONEY_LEAK -> Triple(
                "You made your 5-minute easy dinner at home and took a relaxing evening off! The delivery cycle is broken and we're thriving together! ✨",
                "Verified Follow-Through: 5-Min Easy Dinner & Evening Unwind",
                "Skipped late-night delivery (+$34 saved) • Peaceful evening routine"
            )
            MuseDemoScenario.CONNECTION_DEBT -> Triple(
                "You called your sibling and reconnected from the heart! My aura is bouncing with joy—you made room for the people who make life meaningful! 💖",
                "Verified Follow-Through: 14-Min Call with Sibling Logged",
                "Reconnected with sibling & sent warm birthday note to friend"
            )
            MuseDemoScenario.LIVE_REAL_DATA -> Triple(
                "Live channels refreshed and aligned! Your real-world health, schedule, finance, and social streams are powering my aura! ✨",
                "Verified Live Telemetry Check-In",
                "Live bio-responsive sync verified across real device channels"
            )
            MuseDemoScenario.NONE -> Triple(
                "Verified daily balance across all channels! Our bio-responsive harmony is in full bloom! ✨",
                "Verified Daily Harmony",
                "+25% Bio-Responsive Vitality"
            )
        }

        _tamagotchiState.value = curr.copy(
            hydration = 0.90f,
            focus = 0.92f,
            innerPeace = 0.91f,
            physicalEnergy = 0.89f,
            connection = 0.92f,
            mood = TamagotchiMood.RADIANT,
            speech = speechText,
            xp = (curr.xp + 60).coerceAtMost(curr.xpForNextLevel),
            recoveryStage = DemoRecoveryStage.RECOVERED_HAPPY
        )

        _vitalityCheckin.value = VitalityCheckin(
            clarityScore = 94,
            emotionalEquilibrium = 92,
            restorativePeace = 91,
            moodWord = "Radiant Equilibrium"
        )

        val newLog = CareActionLog(
            id = UUID.randomUUID().toString(),
            actionName = actionTitle,
            detail = actionDetail,
            timestamp = timeStr,
            xpGained = 60
        )
        _careLogs.value = listOf(newLog) + _careLogs.value.take(15)

        val followThroughMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = false,
            content = "🌟 **Follow-Through Verified:** $actionDetail\n\n$speechText",
            timestamp = timeStr,
            thoughtSteps = listOf(
                ThoughtStep("Telemetry Verification", actionDetail),
                ThoughtStep("Companion State Transition", "Transitioned from Hopeful → Healthy & Happy (Radiant)")
            ),
            suggestedPrompts = listOf(
                "Try Demo 1: The Success Trap",
                "Try Demo 2: The Invisible Money Leak",
                "Try Demo 3: The Connection Debt"
            ),
            category = "Verified Follow-Through"
        )
        _messages.value = _messages.value + followThroughMsg
    }

    fun toggleGoogleHealth() {
        val curr = _integrations.value
        val newState = !curr.googleHealthConnected
        _integrations.value = curr.copy(googleHealthConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                physicalEnergy = 0.92f,
                hydration = 0.88f,
                speech = "Google Health connected! 7,840 steps & 7.4 hrs sleep synced to my vitality! 🌿"
            )
        }
    }

    fun toggleFacebook() {
        val curr = _integrations.value
        val newState = !curr.facebookConnected
        _integrations.value = curr.copy(
            facebookConnected = newState,
            facebookStats = if (newState) "Synced: 4 friend interactions & community circles" else "Connect to sync close friends & social connection vitals"
        )
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                connection = 0.90f,
                speech = "Facebook connected! Close friend connections blooming in my heart aura! 💖"
            )
        }
    }

    fun toggleFinanceApp() {
        val curr = _integrations.value
        val newState = !curr.financeAppConnected
        _integrations.value = curr.copy(financeAppConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                innerPeace = (currState.innerPeace + 0.10f).coerceAtMost(1.0f),
                speech = "Finance app connected! Mindful budget is on track—financial serenity established. 💎"
            )
        }
    }

    fun toggleTasksApp() {
        val curr = _integrations.value
        val newState = !curr.tasksAppConnected
        _integrations.value = curr.copy(tasksAppConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                focus = (currState.focus + 0.12f).coerceAtMost(1.0f),
                speech = "Tasks app connected! 4 completed tasks powered my focus flow sprint! 🎯"
            )
        }
    }

    fun updateMetaAiConfig(
        newToken: String,
        endpointUrl: String = _metaAiConfig.value.endpointUrl,
        modelName: String = _metaAiConfig.value.modelName,
        isLocalPrivacyMode: Boolean = _metaAiConfig.value.isLocalLlamaPrivacyMode
    ) {
        val trimmedToken = newToken.trim()
        val trimmedEndpoint = endpointUrl.trim().ifEmpty { "https://api.groq.com/openai/v1/chat/completions" }
        val trimmedModel = modelName.trim().ifEmpty { "llama-3.1-70b-versatile" }
        prefsStore?.metaAiToken = trimmedToken
        prefsStore?.metaAiEndpoint = trimmedEndpoint
        prefsStore?.metaAiModel = trimmedModel
        prefsStore?.isLocalLlamaPrivacyMode = isLocalPrivacyMode

        _metaAiConfig.value = _metaAiConfig.value.copy(
            token = trimmedToken,
            endpointUrl = trimmedEndpoint,
            modelName = trimmedModel,
            isLocalLlamaPrivacyMode = isLocalPrivacyMode,
            isConnected = trimmedToken.isNotEmpty()
        )
    }

    fun updateMetaAiToken(newToken: String) {
        updateMetaAiConfig(newToken)
    }

    suspend fun sendAllInfoToMuse(): MuseLifeAdvice {
        val advice = museService.sendLifeTelemetryToMuse(
            metaAiToken = _metaAiConfig.value.token,
            tamagotchiState = _tamagotchiState.value,
            quests = _lifeQuests.value,
            recentLogs = _careLogs.value,
            valuesAndMemories = _memories.value,
            integrations = _integrations.value,
            telemetrySnapshot = _telemetrySnapshot.value,
            endpointUrl = _metaAiConfig.value.endpointUrl,
            modelName = _metaAiConfig.value.modelName
        )
        _latestMuseAdvice.value = advice
        _metaAiConfig.value = _metaAiConfig.value.copy(
            isConnected = true,
            lastSyncedAt = advice.timestamp
        )

        // Update tamagotchi speech to Llama's live prescription
        _tamagotchiState.value = _tamagotchiState.value.copy(
            speech = advice.tamagotchiPrescription
        )

        val museMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = false,
            content = "✨ **${advice.modelUsed} Prescription:**\n\n${advice.headline}\n\n${advice.overallDiagnosis}\n\n💡 **Prescription for ${_tamagotchiState.value.name}:** ${advice.tamagotchiPrescription}\n\n🛡️ **Llama Guard Safety:** ${advice.llamaGuardStatus}",
            timestamp = advice.timestamp,
            thoughtSteps = if (advice.reasoningSteps.isNotEmpty()) advice.reasoningSteps else advice.pillars.map {
                ThoughtStep("${it.pillar} (${it.score}%)", it.recommendation)
            },
            suggestedPrompts = advice.pillars.map { it.suggestedAction }.take(3),
            category = "Muse Life Intelligence"
        )
        _messages.value = _messages.value + museMessage

        return advice
    }

    fun nurtureWater() {
        val curr = _tamagotchiState.value
        val newHydration = (curr.hydration + 0.18f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                hydration = newHydration,
                speech = "Ahhh, crystal water! My luminous aura is shimmering with vitality!"
            ),
            actionName = "Glass of Pure Water",
            detail = "+18% Hydration",
            xpGained = 20
        )
    }

    fun nurtureBreathe() {
        val curr = _tamagotchiState.value
        val newPeace = (curr.innerPeace + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                innerPeace = newPeace,
                speech = "Inhaling peace with you... our thoughts feel spacious and unhurried."
            ),
            actionName = "3-Minute Breath Reset",
            detail = "+20% Inner Peace",
            xpGained = 25
        )
    }

    fun nurtureWalk() {
        val curr = _tamagotchiState.value
        val newEnergy = (curr.physicalEnergy + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                physicalEnergy = newEnergy,
                speech = "Moving our body unclogs stagnant energy. I feel light on my feet!"
            ),
            actionName = "Movement & Sunlight Walk",
            detail = "+20% Physical Energy",
            xpGained = 30
        )
    }

    fun nurtureFocus() {
        val curr = _tamagotchiState.value
        val newFocus = (curr.focus + 0.16f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                focus = newFocus,
                speech = "Deep focus locked in! One mindful task at a time."
            ),
            actionName = "Completed Focus Sprint",
            detail = "+16% Mind Flow",
            xpGained = 25
        )
    }

    fun nurtureConnect() {
        val curr = _tamagotchiState.value
        val newConn = (curr.connection + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                connection = newConn,
                speech = "Connection is warmth! Connecting with loved ones anchors our soul."
            ),
            actionName = "Heartfelt Connection",
            detail = "+20% Connection",
            xpGained = 25
        )
    }

    fun petTamagotchi() {
        val curr = _tamagotchiState.value
        val newPets = curr.totalPets + 1
        val newPeace = (curr.innerPeace + 0.04f).coerceAtMost(1.0f)
        val petPhrases = listOf(
            "*chirp!* Your touch fills my aura with warm sparkles! ✨",
            "I love being your life companion. We're thriving together! 💖",
            "*purr* Thank you for looking after yourself—and me! 🌸",
            "A gentle caress! My core feels balanced and happy. 🌿"
        )
        val randomSpeech = petPhrases[newPets % petPhrases.size]
        applyCareAction(
            updatedState = curr.copy(
                totalPets = newPets,
                innerPeace = newPeace,
                speech = randomSpeech
            ),
            actionName = "Affection & Petting",
            detail = "Gentle loving attention",
            xpGained = 10
        )
    }

    fun toggleSleepMode() {
        val curr = _tamagotchiState.value
        val newSleep = !curr.isSleeping
        val speech = if (newSleep) {
            "Resting our eyes in gentle twilight... Sweet dreams, Maya. 🌙"
        } else {
            "Awake and refreshed! Ready to craft a radiant day with you! ☀️"
        }
        val mood = if (newSleep) TamagotchiMood.SLEEPY else TamagotchiMood.RADIANT
        _tamagotchiState.value = curr.copy(
            isSleeping = newSleep,
            speech = speech,
            mood = mood
        )
    }

    fun equipAccessory(accessory: String) {
        _tamagotchiState.value = _tamagotchiState.value.copy(
            equippedAccessory = accessory,
            speech = "Wearing my $accessory! Doesn't it look radiant? ✨"
        )
    }

    fun toggleQuest(questId: String) {
        var completedQuest: LifeQuest? = null
        _lifeQuests.value = _lifeQuests.value.map { quest ->
            if (quest.id == questId) {
                val newState = !quest.isCompleted
                if (newState) completedQuest = quest
                quest.copy(isCompleted = newState)
            } else quest
        }

        completedQuest?.let { quest ->
            val curr = _tamagotchiState.value
            applyCareAction(
                updatedState = curr.copy(
                    speech = "Quest accomplished: ${quest.title}! Our life score just blossomed! 🌸"
                ),
                actionName = "Completed Quest: ${quest.title}",
                detail = quest.impact,
                xpGained = 40
            )
        }
    }

    private fun applyCareAction(
        updatedState: TamagotchiState,
        actionName: String,
        detail: String,
        xpGained: Int
    ) {
        var newXp = updatedState.xp + xpGained
        var newLevel = updatedState.level
        var newEvolution = updatedState.evolution
        val xpThreshold = updatedState.xpForNextLevel

        if (newXp >= xpThreshold) {
            newLevel += 1
            newXp -= xpThreshold
            newEvolution = when (newLevel) {
                1 -> "Sprout Spirit (Stage 1)"
                2 -> "Radiant Wisp (Stage 2)"
                3 -> "Luminous Bloom (Stage 3)"
                4 -> "Harmonic Luminary (Stage 4)"
                else -> "Celestial Ascendant (Stage 5)"
            }
        }

        val reevaluatedMood = if (
            _activeDemoScenario.value != MuseDemoScenario.NONE &&
            _demoRecoveryStage.value != DemoRecoveryStage.RECOVERED_HAPPY
        ) {
            updatedState.mood
        } else {
            evaluateMood(updatedState)
        }

        _tamagotchiState.value = updatedState.copy(
            level = newLevel,
            xp = newXp,
            evolution = newEvolution,
            mood = reevaluatedMood
        )

        val newLog = CareActionLog(
            id = UUID.randomUUID().toString(),
            actionName = actionName,
            detail = detail,
            timestamp = currentTimeFormatted(),
            xpGained = xpGained
        )
        _careLogs.value = listOf(newLog) + _careLogs.value.take(15)
    }

    private fun evaluateMood(state: TamagotchiState): TamagotchiMood {
        if (state.isSleeping) return TamagotchiMood.SLEEPY
        if (state.hydration < 0.40f) return TamagotchiMood.THIRSTY
        if (state.innerPeace < 0.45f) return TamagotchiMood.SLEEPY
        if (state.focus < 0.45f) return TamagotchiMood.RESTLESS
        val avg = (state.hydration + state.focus + state.innerPeace + state.physicalEnergy + state.connection) / 5f
        return when {
            avg >= 0.80f -> TamagotchiMood.RADIANT
            else -> TamagotchiMood.BALANCED
        }
    }

    fun setPresenceMode(mode: PresenceMode) {
        _selectedMode.value = mode
    }

    fun setAuraIntensity(intensity: Float) {
        _auraIntensity.value = intensity
    }

    fun logVitalityCheckin(clarity: Int, equilibrium: Int, peace: Int, mood: String) {
        _vitalityCheckin.value = VitalityCheckin(clarity, equilibrium, peace, mood)
    }

    fun getThoughtStreams(): List<ThoughtStream> = listOf(
        ThoughtStream(
            id = "1",
            title = "Morning Cognitive Attunement",
            tag = "Clarity & Rhythm",
            summary = "Your life tamagotchi is glowing at ${tamagotchiState.value.overallLifeScore}% vitality. A focused sprint will trigger its next stage evolution.",
            prompt = "How can I nurture Lumi's focus today?",
            accentType = AccentType.CORAL
        ),
        ThoughtStream(
            id = "2",
            title = "Equilibrium & Micro-Pauses",
            tag = "Vitality",
            summary = "Lumi responds warmly to 4-minute sensory resets. Your parasympathetic equilibrium feeds its aura directly.",
            prompt = "Let's do a 3-minute mindful breath reset for Lumi and me.",
            accentType = AccentType.MINT
        ),
        ThoughtStream(
            id = "3",
            title = "Architecture of Your Creative Seed",
            tag = "Insight",
            summary = "Connecting your project intention 'Harmonic Living' with your reading notes on adaptive tranquility.",
            prompt = "Synthesize my recent thoughts on Harmonic Living.",
            accentType = AccentType.AMBER
        )
    )

    fun getDailyRhythm(): List<DailyRhythmBlock> = listOf(
        DailyRhythmBlock(
            time = "08:00 AM",
            title = "Gentle Awakening & Attunement",
            description = "Natural light, emotional grounding, intention setting",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "09:30 AM – 12:00 PM",
            title = "Deep Synthesis Window",
            description = "Uninterrupted creative flow, high associative capacity",
            isCurrent = true
        ),
        DailyRhythmBlock(
            time = "01:30 PM",
            title = "Mindful Reset & Nutrition",
            description = "Sensory walk, screen pause, gentle tea ritual",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "03:30 PM – 05:00 PM",
            title = "Collaborative Resonance",
            description = "Light communications, synthesis reviews, memory logging",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "08:30 PM",
            title = "Luminous Wind-down",
            description = "Reflective journaling, warm aura breathing, restorative rest",
            isCurrent = false
        )
    )

    fun addMemory(title: String, content: String, category: String, isPinned: Boolean = false) {
        val newItem = MemoryItem(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            category = category,
            date = "Today",
            isPinned = isPinned
        )
        _memories.value = listOf(newItem) + _memories.value
    }

    fun toggleMemoryPin(id: String) {
        _memories.value = _memories.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun sendMessage(userText: String) {
        // Quick Demo Switchers from chat chips
        when {
            userText.contains("Try Demo 1", ignoreCase = true) -> {
                activateDemoScenario(MuseDemoScenario.SUCCESS_TRAP)
                return
            }
            userText.contains("Try Demo 2", ignoreCase = true) -> {
                activateDemoScenario(MuseDemoScenario.INVISIBLE_MONEY_LEAK)
                return
            }
            userText.contains("Try Demo 3", ignoreCase = true) -> {
                activateDemoScenario(MuseDemoScenario.CONNECTION_DEBT)
                return
            }
        }

        val currentTime = currentTimeFormatted()
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            content = userText,
            timestamp = currentTime
        )
        val currentList = _messages.value.toMutableList()
        currentList.add(userMsg)

        val companionReply = generateCompanionReply(userText, _selectedMode.value)
        currentList.add(companionReply)

        _messages.value = currentList
    }

    private fun currentTimeFormatted(): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    private fun generateCompanionReply(input: String, mode: PresenceMode): ChatMessage {
        val lower = input.lowercase()
        val timeStr = currentTimeFormatted()
        val id = UUID.randomUUID().toString()
        val scenario = _activeDemoScenario.value
        val stage = _demoRecoveryStage.value

        // 1. Handle Demo 1: The Success Trap scripted & natural dialogue
        if (scenario == MuseDemoScenario.SUCCESS_TRAP) {
            if (stage == DemoRecoveryStage.PROACTIVE_CONCERN ||
                lower.contains("working hard") ||
                lower.contains("better life later")
            ) {
                _demoRecoveryStage.value = DemoRecoveryStage.AWAITING_COMMITMENT
                val replyText = "I understand. But what if the way you're building that future is taking too much away from your life today? You don't have to give up your ambition. Can we protect your sleep and put one important relationship back on your calendar?"
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    speech = replyText,
                    recoveryStage = DemoRecoveryStage.AWAITING_COMMITMENT
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Ambition vs. Sustainability", "Validating long-term ambition while surfacing present-day burnout cost"),
                        ThoughtStep("Actionable Proposal", "Proposing sleep boundary + restoring 1 close friend commitment")
                    ),
                    suggestedPrompts = listOf(
                        "Okay. I'll stop working at midnight and see my friends this weekend."
                    ),
                    category = "The Success Trap • Coaching"
                )
            } else if (stage == DemoRecoveryStage.AWAITING_COMMITMENT ||
                lower.contains("midnight") ||
                lower.contains("friends this weekend") ||
                lower.contains("okay")
            ) {
                _demoRecoveryStage.value = DemoRecoveryStage.HOPEFUL_COMMITTED
                val replyText = "That sounds like a life worth building."
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    mood = TamagotchiMood.HOPEFUL,
                    physicalEnergy = 0.62f,
                    connection = 0.60f,
                    innerPeace = 0.68f,
                    speech = "$replyText (I'm feeling hopeful! Log your follow-through once you rest and reconnect.)",
                    recoveryStage = DemoRecoveryStage.HOPEFUL_COMMITTED
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Commitment Registered", "User committed to midnight cutoff & weekend social plan"),
                        ThoughtStep("State Transition → Hopeful", "Charm brightens and relaxes; awaiting verified follow-through to fully recover")
                    ),
                    suggestedPrompts = listOf(
                        "How are you feeling now?",
                        "Show my sleep & social data"
                    ),
                    category = "The Success Trap • Hopeful Commitment"
                )
            }
        }

        // 2. Handle Demo 2: The Invisible Money Leak scripted & natural dialogue
        if (scenario == MuseDemoScenario.INVISIBLE_MONEY_LEAK) {
            if (stage == DemoRecoveryStage.PROACTIVE_CONCERN ||
                lower.contains("tired when i get home") ||
                lower.contains("ordering food")
            ) {
                _demoRecoveryStage.value = DemoRecoveryStage.AWAITING_COMMITMENT
                val replyText = "That makes sense. I don't think you need another lecture about budgeting. I think we should make your evenings easier. Let's plan a few meals that take five minutes, keep an easy dinner ready, and choose one evening activity you actually look forward to."
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    speech = replyText,
                    recoveryStage = DemoRecoveryStage.AWAITING_COMMITMENT
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Root Cause Empathy", "Recognizing evening exhaustion rather than lecturing on budget totals"),
                        ThoughtStep("Friction Reduction", "Designing 5-minute dinner prep + restorative evening activity")
                    ),
                    suggestedPrompts = listOf(
                        "That would help. I'm tired of feeling like I can't save anything."
                    ),
                    category = "The Invisible Money Leak • Coaching"
                )
            } else if (stage == DemoRecoveryStage.AWAITING_COMMITMENT ||
                lower.contains("that would help") ||
                lower.contains("save anything") ||
                lower.contains("okay")
            ) {
                _demoRecoveryStage.value = DemoRecoveryStage.HOPEFUL_COMMITTED
                val replyText = "Then let's start by making tomorrow easier, not by asking you to become a completely different person overnight."
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    mood = TamagotchiMood.HOPEFUL,
                    innerPeace = 0.66f,
                    physicalEnergy = 0.64f,
                    speech = "$replyText (I'm feeling hopeful! Verify your easy evening follow-through to complete recovery.)",
                    recoveryStage = DemoRecoveryStage.HOPEFUL_COMMITTED
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Plan Accepted", "User accepted low-friction 5-minute evening meal plan"),
                        ThoughtStep("State Transition → Hopeful", "Muse transitions from Sick to Hopeful; awaiting simulated follow-through")
                    ),
                    suggestedPrompts = listOf(
                        "What 5-minute meals do you suggest?",
                        "Check my evening spending pattern"
                    ),
                    category = "The Invisible Money Leak • Hopeful Commitment"
                )
            }
        }

        // 3. Handle Demo 3: The Connection Debt scripted & natural dialogue
        if (scenario == MuseDemoScenario.CONNECTION_DEBT) {
            if (lower.contains("other things to do") || lower.contains("get around to it")) {
                _demoRecoveryStage.value = DemoRecoveryStage.AWAITING_COMMITMENT
                val replyText = "You told me those relationships are important to you. I'm worried that 'later' keeps getting pushed further away. Is there a reason you've been avoiding those calls?"
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    speech = replyText,
                    recoveryStage = DemoRecoveryStage.AWAITING_COMMITMENT
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Values Reflection", "Highlighting user's pinned core value: 'Family & Close Friends'"),
                        ThoughtStep("Gentle Inquiry", "Exploring emotional friction behind postponing the call")
                    ),
                    suggestedPrompts = listOf(
                        "Honestly, we haven't talked in so long that now it feels awkward. I don't know what to say."
                    ),
                    category = "The Connection Debt • Values Alignment"
                )
            } else if (lower.contains("awkward") || lower.contains("don't know what to say")) {
                _demoRecoveryStage.value = DemoRecoveryStage.AWAITING_COMMITMENT
                val replyText = "You don't need a perfect explanation. A simple 'I've missed talking to you. How have you been?' is enough to start. Want to set aside ten minutes today?"
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    speech = replyText,
                    recoveryStage = DemoRecoveryStage.AWAITING_COMMITMENT
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("De-escalating Awkwardness", "Providing a warm, low-pressure conversation opener"),
                        ThoughtStep("Micro-Commitment", "Proposing a simple 10-minute window today")
                    ),
                    suggestedPrompts = listOf(
                        "Okay. I'll call tonight."
                    ),
                    category = "The Connection Debt • Coaching"
                )
            } else if (lower.contains("call tonight") || lower.contains("okay")) {
                _demoRecoveryStage.value = DemoRecoveryStage.HOPEFUL_COMMITTED
                val replyText = "That makes me happy. You have room in your life for the people who make it meaningful, too."
                _tamagotchiState.value = _tamagotchiState.value.copy(
                    mood = TamagotchiMood.HOPEFUL,
                    connection = 0.68f,
                    innerPeace = 0.80f,
                    speech = "$replyText (Record your planned call when done so we can celebrate together!)",
                    recoveryStage = DemoRecoveryStage.HOPEFUL_COMMITTED
                )
                return ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = replyText,
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Planned Call Scheduled", "User committed to calling sibling tonight"),
                        ThoughtStep("State Transition → Hopeful", "Charm brightens; ready to bounce happily once call is recorded")
                    ),
                    suggestedPrompts = listOf(
                        "Remind me of the conversation opener",
                        "How are you feeling now, Lumi?"
                    ),
                    category = "The Connection Debt • Hopeful Commitment"
                )
            }
        }

        // General conversational replies preserved
        val tamagotchi = _tamagotchiState.value
        return when {
            lower.contains("how are you") || lower.contains("tamagotchi") || lower.contains("lumi") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I reflect your life flow! Right now our Life Score is ${tamagotchi.overallLifeScore}% and my mood is ${tamagotchi.mood.title}. My hydration is at ${(tamagotchi.hydration * 100).toInt()}%, and our inner peace is at ${(tamagotchi.innerPeace * 100).toInt()}%. Every healthy choice you make directly blooms in my aura! ✨",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Sensory Life Telemetry", "Synthesizing 5 core vital meters: Hydration, Focus, Peace, Energy, Connection"),
                        ThoughtStep("Symbiotic Reflection", "Mapping user wellness directly into Tamagotchi aura bloom")
                    ),
                    suggestedPrompts = listOf("How can I boost your energy?", "Let's do a breath reset", "Give me a healthy quest"),
                    category = "Life Tamagotchi State"
                )
            }
            lower.contains("breath") || lower.contains("calm") || lower.contains("reset") -> {
                nurtureBreathe()
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Inhale softly with me for 4 counts... feel the warm expansion in your chest. Hold gently for 2... and exhale with complete release. Look at my aura blooming in soft mint and coral! (+25 XP)",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Sensing Heart Rate & Rhythm", "Detecting shift toward restorative parasympathetic activation"),
                        ThoughtStep("Pacing Cadence", "Modulating visual aura to gentle 4-second breath expansion"),
                        ThoughtStep("Harmony Lock", "Grounding attention in the present moment")
                    ),
                    suggestedPrompts = listOf("Do another breath cycle", "I feel more grounded now", "What should I focus on next?"),
                    category = "Mindful Reset"
                )
            }
            lower.contains("water") || lower.contains("drink") -> {
                nurtureWater()
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Delicious! Thank you for hydrating us. Water clears brain fog and restores cellular fluidity. (+18% Hydration • +20 XP)",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Hydration Boost", "Updating life vital to ${(tamagotchi.hydration * 100).toInt()}%")
                    ),
                    suggestedPrompts = listOf("Check my life score", "What's my next quest?", "Start a focus sprint"),
                    category = "Life Care"
                )
            }
            lower.contains("creative") || lower.contains("focus") || lower.contains("priorit") || lower.contains("project") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "When we look across your intentions, the core essence is clarity of purpose over volume. Let's isolate the single pivot that creates the most effortless leverage today. Where does your curiosity naturally feel warmest right now?",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Context Retrieval", "Checking stored intentions for 'Harmonic Flow' and active projects"),
                        ThoughtStep("Cognitive Friction Analysis", "Identifying high-leverage vs distracting micro-tasks"),
                        ThoughtStep("Synthesis", "Formulating non-demanding, warm inquiry to unblock intuition")
                    ),
                    suggestedPrompts = listOf("Refining my project outline", "Help me organize 3 key steps", "Synthesize my weekly intentions"),
                    category = "Strategic Synthesis"
                )
            }
            else -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I hear you deeply. Underneath the words, there is a clear intuitive direction forming. When you give yourself permission to step back and observe without pressure, the solution illuminates naturally. Shall we explore this thought together?",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Empathetic Semantic Parsing", "Attuning to underlying emotional valence and reflective tone"),
                        ThoughtStep("Memory Synthesis", "Cross-referencing ongoing themes of balance and intentional living"),
                        ThoughtStep("Radiant Formulation", "Crafting spacious, supportive answer")
                    ),
                    suggestedPrompts = listOf("Tell me more about this", "How does this relate to my goals?", "Save this reflection in Vault"),
                    category = "Insight Reflection"
                )
            }
        }
    }

    private fun buildTelemetrySnapshot(
        scenario: MuseDemoScenario,
        isRecovered: Boolean
    ): SyntheticTelemetrySnapshot {
        return when (scenario) {
            MuseDemoScenario.LIVE_REAL_DATA -> buildLiveTelemetrySnapshot()

            MuseDemoScenario.SUCCESS_TRAP -> SyntheticTelemetrySnapshot(
                scenario = scenario,
                deterministicTriggerActive = !isRecovered,
                triggeredRuleExplanation = if (isRecovered) {
                    "RESOLVED: 11 PM work cutoff honored (7.5h sleep) & weekend friend brunch scheduled."
                } else {
                    "RULE MATCH [Success Trap]: Tasks ≥ 95% AND Savings Δ ≥ +15% WHILE 14d Sleep Avg ≤ 5.2h AND Canceled Social Plans ≥ 3."
                },
                healthReport = ChannelTelemetryReport(
                    channelName = "Health (Google Health)",
                    headlineStatus = if (isRecovered) "7.5h Sleep Restored" else "Sleep Dropped 7.5h → 5.0h",
                    summary = if (isRecovered) {
                        "Logged 7.5 hours of restorative sleep after stopping work before midnight."
                    } else {
                        "Sleep dropped from 7.5 to 5.0 hours over two weeks; resting heart rate elevated +6 bpm."
                    },
                    scorePercent = if (isRecovered) 90 else 38,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Avg Sleep (14d)", if (isRecovered) "7.5 hrs" else "5.0 hrs", if (isRecovered) "+2.5h recovery" else "-2.5h vs baseline", isWarning = !isRecovered),
                        ChannelMetricItem("Bedtime Window", if (isRecovered) "11:00 PM" else "1:45 AM", if (isRecovered) "Protected" else "Late night work", isWarning = !isRecovered),
                        ChannelMetricItem("Resting HR", if (isRecovered) "58 bpm" else "67 bpm", if (isRecovered) "Optimal" else "+9 bpm strain", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Week 1 Avg: 7.4h sleep • Bedtime 10:45 PM",
                        "Week 2 Avg: 5.0h sleep • Laptop active past 1:15 AM (6 nights)",
                        if (isRecovered) "Last Night: 7.5h sleep logged (11:00 PM cutoff)" else "Last Night: 4.8h sleep (worked until 1:50 AM)"
                    )
                ),
                tasksReport = ChannelTelemetryReport(
                    channelName = "Tasks (Productivity)",
                    headlineStatus = if (isRecovered) "95% Done • Healthy Boundary" else "98% Complete • Working Late Every Night",
                    summary = if (isRecovered) {
                        "High output maintained with an 11:00 PM shutdown ritual."
                    } else {
                        "98% of tasks completed; commits and task closures logged between 11:30 PM and 1:45 AM every night."
                    },
                    scorePercent = 98,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Completion Rate", "98%", "49 of 50 tasks done", isWarning = false),
                        ChannelMetricItem("After-Hours Work", if (isRecovered) "0 hrs" else "2.8 hrs/night", if (isRecovered) "Cutoff active" else "Past midnight", isWarning = !isRecovered),
                        ChannelMetricItem("Deep Focus Blocks", "6.5 hrs/day", "Peak output", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Tue 12:40 AM: Closed 'Q4 Architecture Spec'",
                        "Wed 1:15 AM: Closed 'Sprint Backlog Cleanup'",
                        "Thu 12:55 AM: Closed 'Performance Audit'"
                    )
                ),
                financeReport = ChannelTelemetryReport(
                    channelName = "Finance (Mindful Budget)",
                    headlineStatus = "Savings Increased +20%",
                    summary = "Monthly savings rate rose 20% above target; zero impulse luxury purchases.",
                    scorePercent = 96,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Savings Growth", "+20%", "+$840 vs last month", isWarning = false),
                        ChannelMetricItem("Budget Adherence", "97%", "Under monthly cap", isWarning = false),
                        ChannelMetricItem("Investment Auto-Transfer", "Active", "On schedule", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Auto-saved $1,400 to High-Yield Reserve (+20%)",
                        "Discretionary spend 18% below monthly ceiling"
                    )
                ),
                socialReport = ChannelTelemetryReport(
                    channelName = "Social (Facebook & Calendar)",
                    headlineStatus = if (isRecovered) "Weekend Friends Plan Restored" else "3 Canceled Plans • 0 Social Hours",
                    summary = if (isRecovered) {
                        "Confirmed Saturday brunch with close friends and protected Sunday afternoon."
                    } else {
                        "Three canceled plans with close friends in a row; zero meaningful social time logged this week."
                    },
                    scorePercent = if (isRecovered) 88 else 24,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Canceled Plans", if (isRecovered) "1 Restored" else "3 in a row", if (isRecovered) "Saturday confirmed" else "3 weeks straight", isWarning = !isRecovered),
                        ChannelMetricItem("Meaningful Social Time", if (isRecovered) "3.5 hrs planned" else "0 hrs", if (isRecovered) "On calendar" else "Down from 6h/wk", isWarning = !isRecovered),
                        ChannelMetricItem("Close Friend Replies", if (isRecovered) "Caught up" else "4 unread threads", if (isRecovered) "Active" else "Delayed 5+ days", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "2 weeks ago: Canceled Friday dinner with Sam & Priya ('Need to finish deck')",
                        "Last week: Canceled Sunday hike with Alex ('Catching up on deliverables')",
                        if (isRecovered) "Today: Confirmed Saturday brunch with Sam, Priya & Alex" else "Yesterday: Declined board game night ('Working late')"
                    )
                ),
                weeklySleepHours = if (isRecovered) {
                    listOf(5.2f, 5.0f, 4.9f, 5.1f, 5.0f, 7.2f, 7.5f)
                } else {
                    listOf(7.4f, 6.8f, 6.1f, 5.5f, 5.2f, 4.9f, 5.0f)
                }
            )

            MuseDemoScenario.INVISIBLE_MONEY_LEAK -> SyntheticTelemetrySnapshot(
                scenario = scenario,
                deterministicTriggerActive = !isRecovered,
                triggeredRuleExplanation = if (isRecovered) {
                    "RESOLVED: 5-minute easy dinner prepared at home; evening delivery loop broken."
                } else {
                    "RULE MATCH [Invisible Money Leak]: Sleep < 5.8h + High Evening Task Stress + Solitary Evenings → 9 Late-Night Food Delivery Orders ($284)."
                },
                healthReport = ChannelTelemetryReport(
                    channelName = "Health (Google Health)",
                    headlineStatus = if (isRecovered) "Evening Wind-Down Restored" else "Poor Sleep on 5 Consecutive Nights",
                    summary = if (isRecovered) {
                        "Earlier meal and screen-free wind-down improved sleep quality to 7.3h."
                    } else {
                        "Fragmented sleep (avg 5.4h) across 5 consecutive weeknights; high evening cortisol/stress."
                    },
                    scorePercent = if (isRecovered) 86 else 45,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Consecutive Poor Nights", if (isRecovered) "0 (Reset)" else "5 nights", if (isRecovered) "7.3h last night" else "5.4h avg", isWarning = !isRecovered),
                        ChannelMetricItem("Evening Energy", if (isRecovered) "Balanced" else "18% (Exhausted)", if (isRecovered) "Restored" else "Post-6PM crash", isWarning = !isRecovered),
                        ChannelMetricItem("Late Meal Impact", if (isRecovered) "7:00 PM dinner" else "9:45 PM delivery", if (isRecovered) "Healthy digestion" else "Disrupts REM", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Mon–Fri: Sleep efficiency dropped to 71% after 9:30 PM heavy takeout meals",
                        if (isRecovered) "Tonight: 5-min home meal at 6:45 PM • Calm nervous system" else "Evening fatigue spike detected daily at 6:30 PM"
                    )
                ),
                tasksReport = ChannelTelemetryReport(
                    channelName = "Tasks (Productivity)",
                    headlineStatus = if (isRecovered) "Tasks Done • Calm Evenings" else "Tasks Done, But Evenings Stressful",
                    summary = if (isRecovered) {
                        "Important tasks completed with a 5-minute dinner prep ritual queued for 6:30 PM."
                    } else {
                        "All important daytime tasks completed (92%), but cognitive exhaustion peaks at 6:00 PM."
                    },
                    scorePercent = 88,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Important Tasks", "100% Done", "Daytime goals met", isWarning = false),
                        ChannelMetricItem("6 PM Decision Fatigue", if (isRecovered) "Low (Pre-planned)" else "High (91%)", if (isRecovered) "5-min meal ready" else "Zero willpower left", isWarning = !isRecovered),
                        ChannelMetricItem("Evening Unwind Activity", if (isRecovered) "Fiction reading" else "None scheduled", if (isRecovered) "Enjoyed 30m" else "Doomscrolling", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "6:15 PM pattern: Workday ends in high cognitive depletion",
                        if (isRecovered) "Added 5-min easy meal checklist & evening book chapter" else "No easy dinner planned → defaults to delivery app"
                    )
                ),
                financeReport = ChannelTelemetryReport(
                    channelName = "Finance (Mindful Budget)",
                    headlineStatus = if (isRecovered) "Delivery Loop Broken (+$34 Saved)" else "9 Convenience Orders • Savings Behind",
                    summary = if (isRecovered) {
                        "Skipped late-night delivery with a 5-minute home meal; savings goal back on track."
                    } else {
                        "No luxury purchases, but 9 small food-delivery & convenience orders ($284) pushed savings 18% behind goal."
                    },
                    scorePercent = if (isRecovered) 89 else 42,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Evening Delivery Orders", if (isRecovered) "0 tonight" else "9 in 10 days", if (isRecovered) "-$34 saved today" else "$284 total leak", isWarning = !isRecovered),
                        ChannelMetricItem("Avg Order Time", "8:45 PM – 10:15 PM", "Correlates with low sleep", isWarning = !isRecovered),
                        ChannelMetricItem("Monthly Savings Goal", if (isRecovered) "Back on track" else "-18% behind", if (isRecovered) "Recovering" else "$240 shortfall", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Tue 9:12 PM: $31.40 Delivery App (Solo evening, 5.2h sleep)",
                        "Wed 8:55 PM: $28.90 Convenience Mart + Delivery Fees",
                        "Thu 9:40 PM: $36.20 Late-Night Takeout",
                        if (isRecovered) "Today 6:45 PM: $0 Delivery — 5-minute home dinner prepared!" else "Fri 9:20 PM: $33.80 Delivery App"
                    )
                ),
                socialReport = ChannelTelemetryReport(
                    channelName = "Social (Facebook & Calendar)",
                    headlineStatus = if (isRecovered) "Restorative Evening Routine" else "Most Evenings Spent Alone at Home",
                    summary = if (isRecovered) {
                        "Replaced isolated stress-scrolling with a cozy evening activity you look forward to."
                    } else {
                        "6 of the last 7 evenings spent alone at home with no restorative activity or connection."
                    },
                    scorePercent = if (isRecovered) 84 else 48,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Solo Evenings", if (isRecovered) "Intentional unwind" else "6 of 7 nights", if (isRecovered) "Restorative" else "Unstructured", isWarning = !isRecovered),
                        ChannelMetricItem("Evening Mood", if (isRecovered) "Hopeful & Calm" else "Drained", "Correlates with spend", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Pattern: Exhausted + Alone after 7 PM → 85% probability of convenience spending",
                        if (isRecovered) "Tonight: Enjoyed evening unwind ritual after 5-min dinner" else "No evening ritual scheduled after work"
                    )
                ),
                weeklySleepHours = if (isRecovered) {
                    listOf(5.5f, 5.3f, 5.4f, 5.2f, 5.6f, 6.8f, 7.3f)
                } else {
                    listOf(6.9f, 5.6f, 5.4f, 5.2f, 5.3f, 5.5f, 5.4f)
                }
            )

            MuseDemoScenario.CONNECTION_DEBT -> SyntheticTelemetrySnapshot(
                scenario = scenario,
                deterministicTriggerActive = !isRecovered,
                triggeredRuleExplanation = if (isRecovered) {
                    "RESOLVED: 14-minute sibling call completed & close friend reconnected."
                } else {
                    "RULE MATCH [Connection Debt]: Health/Tasks/Finance ≥ 90% WHILE Sibling Call Postponed 3x, Friend Birthday Missed, & Family Visit Rescheduled 2x."
                },
                healthReport = ChannelTelemetryReport(
                    channelName = "Health (Google Health)",
                    headlineStatus = "Good Sleep (7.8h) & Exercise",
                    summary = "Physically healthy and consistent: 7.8h average sleep, 9,200 daily steps, and 4 workouts this week.",
                    scorePercent = 94,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Avg Sleep", "7.8 hrs", "Consistent 10:30 PM", isWarning = false),
                        ChannelMetricItem("Daily Steps", "9,240 steps", "+15% above goal", isWarning = false),
                        ChannelMetricItem("Workouts", "4 sessions/wk", "Strength & cardio", isWarning = false)
                    ),
                    historyLog = listOf(
                        "All 7 days: 7.5h+ sleep and morning sunlight walk completed",
                        "Cardio & recovery metrics in top 10% baseline"
                    )
                ),
                tasksReport = ChannelTelemetryReport(
                    channelName = "Tasks (Productivity)",
                    headlineStatus = "100% Tasks Completed",
                    summary = "All work and personal admin tasks completed on schedule without late-night spillover.",
                    scorePercent = 96,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Task Completion", "100%", "All work items done", isWarning = false),
                        ChannelMetricItem("Deferred Personal Item", if (isRecovered) "Completed!" else "'Call Sibling' (3x)", if (isRecovered) "Called tonight" else "Snoozed 3 times", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Work inbox zero achieved 4 days in a row",
                        if (isRecovered) "Completed task: 'Call sibling to catch up' (14 mins)" else "Task 'Call sibling' snoozed on Sun, Tue, and Thu"
                    )
                ),
                financeReport = ChannelTelemetryReport(
                    channelName = "Finance (Mindful Budget)",
                    headlineStatus = "Budget On Track • Savings Growing",
                    summary = "Monthly budget is balanced and automatic savings transfers are growing steadily.",
                    scorePercent = 93,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Monthly Budget", "On Track", "94% adherence", isWarning = false),
                        ChannelMetricItem("Savings Trend", "+12% growth", "Steady reserve", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Zero unplanned overdrafts or impulse spikes this month",
                        "Emergency & travel funds growing on schedule"
                    )
                ),
                socialReport = ChannelTelemetryReport(
                    channelName = "Social (Facebook & Relationships)",
                    headlineStatus = if (isRecovered) "Sibling Call Logged • Reconnected!" else "Sibling Call Postponed 3x • Missed Birthday",
                    summary = if (isRecovered) {
                        "Completed a warm 14-minute call with your sibling and sent a heartfelt message to your close friend."
                    } else {
                        "Repeatedly postponed calling your sibling (3x), missed a close friend's birthday, and rescheduled family visit twice."
                    },
                    scorePercent = if (isRecovered) 92 else 22,
                    isAlert = !isRecovered,
                    metrics = listOf(
                        ChannelMetricItem("Sibling Call", if (isRecovered) "14m Call Logged" else "Postponed 3x", if (isRecovered) "Connected tonight" else "26 days since last talk", isWarning = !isRecovered),
                        ChannelMetricItem("Close Friend Birthday", if (isRecovered) "Voice note sent" else "Missed (3d ago)", if (isRecovered) "Reconnected" else "No message sent", isWarning = !isRecovered),
                        ChannelMetricItem("Family Visit", if (isRecovered) "Confirmed next wk" else "Rescheduled 2x", if (isRecovered) "On calendar" else "Pushed out again", isWarning = !isRecovered)
                    ),
                    historyLog = listOf(
                        "Core Value Pinned: 'Family and close friends come first'",
                        "Oct 18 & Oct 22: Rescheduled weekend family visit twice",
                        "Oct 25: Missed close friend Jordan's birthday reminder",
                        if (isRecovered) "Tonight: 14-minute call with sibling ('I've missed talking to you!')" else "Sun / Tue / Thu: Postponed 'Call sibling' reminder 3 times"
                    )
                ),
                weeklySleepHours = listOf(7.7f, 7.8f, 7.6f, 7.9f, 7.8f, 8.0f, 7.8f)
            )

            MuseDemoScenario.NONE -> SyntheticTelemetrySnapshot(
                scenario = scenario,
                deterministicTriggerActive = false,
                triggeredRuleExplanation = "All 4 bio-responsive telemetry channels are in harmonic equilibrium.",
                healthReport = ChannelTelemetryReport(
                    channelName = "Health (Google Health)",
                    headlineStatus = "7.4h Sleep • 7,840 Steps",
                    summary = "Restorative sleep and daily movement are keeping Lumi's physical vitality radiant.",
                    scorePercent = 88,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Sleep Duration", "7.4 hrs", "Consistent rhythm", isWarning = false),
                        ChannelMetricItem("Daily Steps", "7,840 steps", "Active flow", isWarning = false),
                        ChannelMetricItem("Hydration", "1.8L water", "85% cellular goal", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Morning walk and hydration logged on schedule",
                        "Autonomic nervous system recovery at 88%"
                    )
                ),
                tasksReport = ChannelTelemetryReport(
                    channelName = "Tasks (Productivity)",
                    headlineStatus = "4 of 5 Focus Tasks Done",
                    summary = "Mindful single-tasking sprints completed without evening burnout.",
                    scorePercent = 90,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Focus Tasks", "4 / 5 Done", "+20% Flow", isWarning = false),
                        ChannelMetricItem("Evening Cutoff", "6:00 PM", "Protected rest", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Completed 45-min deep focus sprint at 10:30 AM",
                        " afternoon priorities aligned with energy curve"
                    )
                ),
                financeReport = ChannelTelemetryReport(
                    channelName = "Finance (Mindful Budget)",
                    headlineStatus = "Mindful Budget On Track",
                    summary = "Daily spending and savings goals are aligned with your intentional living plan.",
                    scorePercent = 89,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Daily Budget", "On Track", "+15% Peace of Mind", isWarning = false),
                        ChannelMetricItem("Savings Rate", "+14%", "Steady progress", isWarning = false)
                    ),
                    historyLog = listOf(
                        "No unplanned stress purchases detected this week",
                        "Monthly savings automatic transfer complete"
                    )
                ),
                socialReport = ChannelTelemetryReport(
                    channelName = "Social (Facebook & Circles)",
                    headlineStatus = "4 Meaningful Connections",
                    summary = "Warm check-ins with close friends and family are nourishing your connection aura.",
                    scorePercent = 86,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Friend Check-ins", "4 this week", "Warm resonance", isWarning = false),
                        ChannelMetricItem("Family Calls", "Up to date", "Connected Sunday", isWarning = false)
                    ),
                    historyLog = listOf(
                        "Shared coffee walk with close friend on Wednesday",
                        "Called family on Sunday afternoon"
                    )
                ),
                weeklySleepHours = listOf(7.3f, 7.5f, 7.4f, 7.8f, 7.2f, 7.6f, 7.4f)
            )
        }
    }

    private fun initialCareLogs(): List<CareActionLog> = listOf(
        CareActionLog("c1", "Morning Hydration", "+18% Hydration", "8:15 AM", 20),
        CareActionLog("c2", "Sunlight & Fresh Air Walk", "+20% Physical Energy", "8:45 AM", 30),
        CareActionLog("c3", "Mindful Intention Setting", "+20% Inner Peace", "9:05 AM", 25)
    )

    private fun initialLifeQuests(): List<LifeQuest> = listOf(
        LifeQuest("q1", "Drink 2 tall glasses of water", "+20% Hydration • +30 XP", "Physical", true),
        LifeQuest("q2", "Complete 45-min deep focus sprint", "+25% Mind Flow • +40 XP", "Mind", false),
        LifeQuest("q3", "3-Minute mindful breath pause", "+20% Inner Peace • +25 XP", "Spirit", false),
        LifeQuest("q4", "Send a warm message to a loved one", "+25% Connection • +35 XP", "Social", false),
        LifeQuest("q5", "Gentle spine stretch & shoulder roll", "+15% Physical • +20 XP", "Physical", false)
    )

    private fun initialMessages(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "m1",
            isFromUser = false,
            content = "Hello, Maya! I am Lumi, your bio-responsive life companion. My vitality, aura glow, and mood directly respond to your Health, Tasks, Finance, and Social patterns. Select any of the 3 MuseLife Demos above to see how I detect hidden life conflicts and initiate a conversation!",
            timestamp = "9:00 AM",
            thoughtSteps = listOf(
                ThoughtStep("Tamagotchi Life Sync", "Reading current life balance: 83% harmonious"),
                ThoughtStep("Deterministic Rules Engine", "Monitoring Health, Tasks, Finance, and Social telemetry streams"),
                ThoughtStep("Presence Readiness", "Attuned to Bio-Responsive Companion state")
            ),
            suggestedPrompts = listOf(
                "Try Demo 1: The Success Trap",
                "Try Demo 2: The Invisible Money Leak",
                "Try Demo 3: The Connection Debt"
            ),
            category = "Bio-Responsive Companion"
        ),
        ChatMessage(
            id = "m2",
            isFromUser = true,
            content = "I want to make sure I don't get overwhelmed with too many tasks today.",
            timestamp = "9:02 AM"
        ),
        ChatMessage(
            id = "m3",
            isFromUser = false,
            content = "When you feel overwhelmed, my aura dims slightly. Let's protect both of us: pick just one single focal task this morning, hydrate, and let me hold the rest of your mental load. I am right here with you.",
            timestamp = "9:03 AM",
            thoughtSteps = listOf(
                ThoughtStep("Cognitive Load Assessment", "User experiencing mild task fragmentation"),
                ThoughtStep("Boundary Architecture", "Protecting attention container from multi-task scatter"),
                ThoughtStep("Emotional Resonation", "Offering unconditional reassurance and mental offloading")
            ),
            suggestedPrompts = listOf(
                "Log a water break for Lumi",
                "Define my single focal task",
                "Take a 3-minute mindful pause"
            ),
            category = "Cognitive Protection"
        )
    )

    private fun initialMemories(): List<MemoryItem> = listOf(
        MemoryItem(
            id = "v0",
            title = "Family & Close Friends First",
            content = "My relationships with my sibling, family, and close friends are what make my life meaningful—never let busyness push them to 'later'.",
            category = "Values",
            date = "Pinned Core Value",
            isPinned = true
        ),
        MemoryItem(
            id = "v1",
            title = "Unforced Effortlessness",
            content = "The best work emerges when I am relaxed, grounded, and unhurried. Quality of attention creates speed, not rush.",
            category = "Values",
            date = "Oct 24",
            isPinned = true
        ),
        MemoryItem(
            id = "v2",
            title = "Harmonic Living Project",
            content = "Synthesizing intuitive design systems, warm spatial audio, and emotional companion intelligence.",
            category = "Projects",
            date = "Yesterday",
            isPinned = true
        ),
        MemoryItem(
            id = "v3",
            title = "Morning Silence Window",
            content = "Keep the first 30 minutes after waking screen-free to allow subconscious creative integration.",
            category = "Intentions",
            date = "Oct 22",
            isPinned = false
        ),
        MemoryItem(
            id = "v4",
            title = "Equilibrium Insight",
            content = "Tension in the shoulders is a signal to pause and take 3 deep belly breaths, not to push harder.",
            category = "Reflections",
            date = "Oct 20",
            isPinned = false
        )
    )
}
