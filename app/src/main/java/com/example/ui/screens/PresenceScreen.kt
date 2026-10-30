package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.LifeSyncIntegrations
import com.example.model.MuseLifeAdvice
import com.example.model.RealConnectorState
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPillChip
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenitySecondaryButton
import com.example.ui.components.SerenityStatusChip
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MetaCyanSecondary
import com.example.ui.theme.MetaHorizonViolet
import com.example.ui.theme.MetaPrimaryBlue
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RecessedContainer
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityAmberTertiary
import com.example.ui.theme.SerenityCoralPrimary
import com.example.ui.theme.SerenityError
import com.example.ui.theme.SerenityErrorContainer
import com.example.ui.theme.SerenityMintSecondary
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun PresenceScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showFullConnections by viewModel.showFullConnections.collectAsState()
    val metaAiConfig by viewModel.metaAiConfig.collectAsState()
    val latestMuseAdvice by viewModel.latestMuseAdvice.collectAsState()
    val isSyncingWithMuse by viewModel.isSyncingWithMuse.collectAsState()
    val integrations by viewModel.integrations.collectAsState()
    val realState by viewModel.realConnectorState.collectAsState()
    val currentTheme by viewModel.appThemeMode.collectAsState()

    var tokenInput by remember(metaAiConfig.token) { mutableStateOf(metaAiConfig.token) }
    var endpointInput by remember(metaAiConfig.endpointUrl) { mutableStateOf(metaAiConfig.endpointUrl) }
    var modelInput by remember(metaAiConfig.modelName) { mutableStateOf(metaAiConfig.modelName) }
    var isLocalPrivacyMode by remember(metaAiConfig.isLocalLlamaPrivacyMode) { mutableStateOf(metaAiConfig.isLocalLlamaPrivacyMode) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var showTokenSavedMessage by remember { mutableStateOf(false) }

    var googleTasksTokenInput by remember(realState.googleTasksToken) {
        mutableStateOf(realState.googleTasksToken)
    }
    var fbAppIdInput by remember(realState.facebookAppId) {
        mutableStateOf(realState.facebookAppId)
    }
    var fbTokenInput by remember(realState.facebookAccessToken) {
        mutableStateOf(realState.facebookAccessToken)
    }
    var bankTab by remember { mutableStateOf("Plaid API") }
    var plaidClientIdInput by remember(realState.plaidClientId) {
        mutableStateOf(realState.plaidClientId)
    }
    var plaidSecretInput by remember(realState.plaidSecret) {
        mutableStateOf(realState.plaidSecret)
    }
    var plaidEnvInput by remember(realState.plaidEnvironment) {
        mutableStateOf(realState.plaidEnvironment)
    }
    var plaidAccessTokenInput by remember(realState.plaidAccessToken) {
        mutableStateOf(realState.plaidAccessToken)
    }
    var simpleFinInput by remember(realState.simpleFinUrlOrToken) {
        mutableStateOf(realState.simpleFinUrlOrToken)
    }

    // 1. Real Android Health Connect + Activity Recognition Launchers
    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.syncRealHealth()
    }

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = remember { viewModel.createHealthPermissionContract() }
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
        ) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.syncRealHealth()
        }
    }

    val requestRealHealthConnect: () -> Unit = {
        if (realState.healthSdkAvailable) {
            try {
                healthPermissionLauncher.launch(viewModel.healthConnectPermissions)
            } catch (_: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                } else {
                    viewModel.openHealthConnectSettings()
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.openHealthConnectSettings()
        }
    }

    // 2. Real Android Calendar Permission Launcher
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.syncRealCalendarAndTasks()
    }

    val requestRealCalendar: () -> Unit = {
        calendarPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
        )
    }

    // 3. Real Android Contacts Permission Launcher (Close Circle & Birthdays)
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.syncRealFacebookAndContacts()
    }

    val requestRealContacts: () -> Unit = {
        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    // 4. Real Bank .CSV Statement Picker Launcher
    val bankCsvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importRealBankCsv(uri)
        }
    }

    if (showFullConnections) {
        BackHandler {
            viewModel.setShowFullConnections(false)
        }
    }

    AnimatedContent(
        targetState = showFullConnections,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ConnectionsViewTransition"
    ) { isFullScreen ->
        if (!isFullScreen) {
            SimpleConnectionsGraphicView(
                integrations = integrations,
                isMetaKeyConnected = metaAiConfig.token.isNotBlank(),
                isSyncingWithMuse = isSyncingWithMuse || realState.isSyncing,
                currentTheme = currentTheme,
                onSelectTheme = { viewModel.setAppThemeMode(it) },
                contentPadding = contentPadding,
                onConnectGoogleHealth = requestRealHealthConnect,
                onConnectCalendarTasks = requestRealCalendar,
                onConnectFinance = { viewModel.setShowFullConnections(true) },
                onConnectFacebook = {
                    if (!realState.contactsPermissionGranted && realState.facebookAccessToken.isBlank()) {
                        viewModel.setShowFullConnections(true)
                    } else {
                        viewModel.syncRealFacebookAndContacts()
                    }
                },
                onSyncToMuse = { viewModel.sendAllInfoToMuse() },
                onOpenFullConnections = { viewModel.setShowFullConnections(true) },
                modifier = modifier
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(BaseCanvas)
                    .testTag("presence_screen"),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 18.dp,
                    end = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Back Button + Re-Sync All Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SubtleContainer,
                            modifier = Modifier
                                .border(1.dp, WarmBorder, CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.setShowFullConnections(false) }
                                .testTag("back_to_connections_graphic_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Connections Graphic",
                                    tint = DeepEspresso,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Back to Constellation",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = DeepEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MintContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { viewModel.syncAllRealChannels() }
                                .testTag("resync_all_real_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync All",
                                    tint = DarkSageText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (realState.isSyncing) "Syncing..." else "Sync All Live",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DarkSageText,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Settings Header + Live Sync Status Banner
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SerenityStatusChip(
                            text = realState.lastSyncSummary,
                            pulseColor = if (realState.isSyncing) AmberTertiary else MintSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Real Data Connections",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Live Android Health Connect, Device Calendar & Google Tasks API, Facebook Graph v21.0, and Plaid / SimpleFIN / Bank CSV.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WarmSlateCharcoal
                            )
                        )

                        realState.lastError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SerenityErrorContainer)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SerenityError,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // =============================================================
                // 0. COMPANION THEME & APPEARANCE (Meta Colors vs Luminous Serenity)
                // =============================================================
                item {
                    ThemeAppearanceCard(
                        currentTheme = currentTheme,
                        onSelectTheme = { viewModel.setAppThemeMode(it) }
                    )
                }

                // =============================================================
                // 1. REAL GOOGLE HEALTH CONNECT + STEP SENSOR CARD
                // =============================================================
                item {
                    RealGoogleHealthCard(
                        isConnected = integrations.googleHealthConnected,
                        statsText = integrations.googleHealthStats,
                        realState = realState,
                        onGrantAndSync = requestRealHealthConnect,
                        onOpenHealthConnect = { viewModel.openHealthConnectSettings() }
                    )
                }

                // =============================================================
                // 2. REAL CALENDAR & GOOGLE TASKS CARD
                // =============================================================
                item {
                    RealCalendarAndTasksCard(
                        isConnected = integrations.tasksAppConnected,
                        statsText = integrations.tasksAppStats,
                        calendarGranted = realState.calendarPermissionGranted,
                        googleTasksToken = googleTasksTokenInput,
                        onTasksTokenChange = { googleTasksTokenInput = it },
                        onGrantCalendar = requestRealCalendar,
                        onSyncGoogleTasks = {
                            viewModel.syncRealCalendarAndTasks(googleTasksTokenInput)
                        },
                        onInsertRecoveryBlock = {
                            viewModel.insertRecoveryCalendarEvent(
                                title = "Muse Restorative Wind-Down",
                                description = "Protected evening recovery window created from MetaMuse"
                            )
                        }
                    )
                }

                // =============================================================
                // 3. REAL FACEBOOK GRAPH API + CLOSE CONTACTS CARD
                // =============================================================
                item {
                    RealFacebookSocialCard(
                        isConnected = integrations.facebookConnected,
                        statsText = integrations.facebookStats,
                        contactsGranted = realState.contactsPermissionGranted,
                        facebookAppId = fbAppIdInput,
                        onAppIdChange = { fbAppIdInput = it },
                        facebookToken = fbTokenInput,
                        onTokenChange = { fbTokenInput = it },
                        onLaunchOAuth = { viewModel.launchFacebookOAuth(fbAppIdInput) },
                        onVerifyGraphToken = {
                            viewModel.syncRealFacebookAndContacts(
                                facebookAccessToken = fbTokenInput,
                                facebookAppId = fbAppIdInput
                            )
                        },
                        onGrantContacts = requestRealContacts
                    )
                }

                // =============================================================
                // 4. REAL BANK / FINANCE CONNECTOR CARD (Plaid + SimpleFIN + CSV)
                // =============================================================
                item {
                    RealBankFinanceCard(
                        isConnected = integrations.financeAppConnected,
                        statsText = integrations.financeAppStats,
                        selectedTab = bankTab,
                        onSelectTab = { bankTab = it },
                        plaidClientId = plaidClientIdInput,
                        onPlaidClientIdChange = { plaidClientIdInput = it },
                        plaidSecret = plaidSecretInput,
                        onPlaidSecretChange = { plaidSecretInput = it },
                        plaidEnv = plaidEnvInput,
                        onPlaidEnvChange = { plaidEnvInput = it },
                        plaidAccessToken = plaidAccessTokenInput,
                        onPlaidAccessTokenChange = { plaidAccessTokenInput = it },
                        onConnectPlaid = {
                            viewModel.connectPlaidBank(
                                clientId = plaidClientIdInput,
                                secret = plaidSecretInput,
                                environment = plaidEnvInput,
                                accessToken = plaidAccessTokenInput
                            )
                        },
                        simpleFinInput = simpleFinInput,
                        onSimpleFinChange = { simpleFinInput = it },
                        onConnectSimpleFin = {
                            viewModel.connectSimpleFinBank(simpleFinInput)
                        },
                        onPickBankCsv = {
                            bankCsvPickerLauncher.launch("*/*")
                        }
                    )
                }

                // =============================================================
                // 5. META AI KEY & MUSE SUPERINTELLIGENCE CARD
                // =============================================================
                item {
                    SerenityCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("section_meta_key")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(CoralPrimary.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = CoralPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Meta Llama Brain",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepEspresso
                                            )
                                        )
                                        Text(
                                            text = "Cross-channel reasoning & Llama Guard safety",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = WarmSlateCharcoal
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isLocalPrivacyMode) MintContainer else if (tokenInput.isNotBlank()) MintContainer else SubtleContainer
                                ) {
                                    Text(
                                        text = if (isLocalPrivacyMode) "On-Device (Private)" else if (tokenInput.isNotBlank()) "Cloud Llama 3.1" else "Key Needed",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DarkSageText,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mode toggle chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isLocalPrivacyMode) CoralPrimary else SubtleContainer,
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (isLocalPrivacyMode) CoralPrimary else WarmBorder, CircleShape)
                                        .clip(CircleShape)
                                        .clickable { isLocalPrivacyMode = true }
                                ) {
                                    Text(
                                        text = "🔒 On-Device (Zero-Cloud)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isLocalPrivacyMode) Color.White else DeepEspresso,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (!isLocalPrivacyMode) CoralPrimary else SubtleContainer,
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (!isLocalPrivacyMode) CoralPrimary else WarmBorder, CircleShape)
                                        .clip(CircleShape)
                                        .clickable { isLocalPrivacyMode = false }
                                ) {
                                    Text(
                                        text = "☁️ Meta Llama 3.1 API",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (!isLocalPrivacyMode) Color.White else DeepEspresso,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isLocalPrivacyMode) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MintContainer)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "🛡️ 100% On-Device Privacy: Your bank transactions, Health Connect biometrics, and calendars never leave your phone. Evaluated via local Llama 3 weights.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DarkSageText,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = endpointInput,
                                    onValueChange = { endpointInput = it },
                                    label = { Text("Llama Inference Endpoint") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = connectorTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = modelInput,
                                    onValueChange = { modelInput = it },
                                    label = { Text("Model Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = connectorTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = tokenInput,
                                    onValueChange = { tokenInput = it },
                                    label = { Text("Meta Llama / Provider API Key") },
                                    placeholder = { Text("Bearer token") },
                                    singleLine = true,
                                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                            Icon(
                                                imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = WarmSlateCharcoal
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("meta_key_input"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = connectorTextFieldColors()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SerenitySecondaryButton(
                                    text = "Save Config",
                                    icon = Icons.Default.Check,
                                    onClick = {
                                        viewModel.updateMetaAiConfig(
                                            token = tokenInput,
                                            endpoint = endpointInput,
                                            model = modelInput,
                                            isLocalPrivacyMode = isLocalPrivacyMode
                                        )
                                        showTokenSavedMessage = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "save_meta_key_button"
                                )

                                SerenityPrimaryButton(
                                    text = if (isSyncingWithMuse) "Reasoning..." else "Run Llama Brain",
                                    icon = Icons.Default.Sync,
                                    onClick = { viewModel.sendAllInfoToMuse() },
                                    modifier = Modifier.weight(1.3f),
                                    testTag = "send_to_muse_button"
                                )
                            }

                            if (showTokenSavedMessage) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintContainer)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DarkSageText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Llama brain configuration saved securely.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DarkSageText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (metaAiConfig.lastSyncedAt != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Last synced with Muse: ${metaAiConfig.lastSyncedAt}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SoftMutedText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Latest Muse Life Advice Card (When present)
                latestMuseAdvice?.let { advice ->
                    item {
                        MuseAdviceSnippet(advice = advice)
                    }
                }
            }
        }
    }
}

@Composable
private fun RealGoogleHealthCard(
    isConnected: Boolean,
    statsText: String,
    realState: RealConnectorState,
    onGrantAndSync: () -> Unit,
    onOpenHealthConnect: () -> Unit
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_google_health")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ConnectorHeaderRow(
                title = "Google Health Data",
                category = "Android Health Connect + Step Sensor",
                isConnected = isConnected,
                icon = Icons.Default.Favorite,
                accentColor = MintSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Reads real daily steps, 7-day sleep sessions, resting/average heart rate, and hydration records directly from Android Health Connect & hardware step sensors.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            ConnectorLiveStatusBox(isConnected = isConnected, statsText = statsText, accentColor = MintSecondary)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SerenityPrimaryButton(
                    text = if (isConnected) "Re-Sync Health" else "Connect Health",
                    icon = Icons.Default.Favorite,
                    onClick = onGrantAndSync,
                    modifier = Modifier.weight(1.25f),
                    testTag = "connect_google_health_button"
                )
                SerenitySecondaryButton(
                    text = if (realState.healthSdkAvailable) "Manage" else "Install App",
                    icon = Icons.Default.OpenInNew,
                    onClick = onOpenHealthConnect,
                    modifier = Modifier.weight(0.95f)
                )
            }
        }
    }
}

@Composable
private fun RealCalendarAndTasksCard(
    isConnected: Boolean,
    statsText: String,
    calendarGranted: Boolean,
    googleTasksToken: String,
    onTasksTokenChange: (String) -> Unit,
    onGrantCalendar: () -> Unit,
    onSyncGoogleTasks: () -> Unit,
    onInsertRecoveryBlock: () -> Unit
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_tasks_app")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ConnectorHeaderRow(
                title = "Calendar & Tasks List",
                category = "Android CalendarContract + Google Tasks API v1",
                isConnected = isConnected,
                icon = Icons.Default.TaskAlt,
                accentColor = SageAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Queries real device calendar instances (completed today, upcoming 7-day events, late-night work past 9:30 PM, and declined RSVPs) plus optional Google Tasks REST API.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            ConnectorLiveStatusBox(isConnected = isConnected, statsText = statsText, accentColor = SageAccent)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SerenityPrimaryButton(
                    text = if (calendarGranted) "Re-Sync Calendar" else "Connect Calendar",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onGrantCalendar,
                    modifier = Modifier.weight(1.25f),
                    testTag = "connect_calendar_button"
                )
                SerenitySecondaryButton(
                    text = "+ Rest Event",
                    onClick = onInsertRecoveryBlock,
                    modifier = Modifier.weight(0.95f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = googleTasksToken,
                onValueChange = onTasksTokenChange,
                label = { Text("Google Tasks OAuth Token (Optional)") },
                placeholder = { Text("ya29.a0... (tasks.readonly scope)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = connectorTextFieldColors()
            )

            if (googleTasksToken.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                SerenitySecondaryButton(
                    text = "Sync Google Tasks REST API",
                    icon = Icons.Default.Sync,
                    onClick = onSyncGoogleTasks,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RealFacebookSocialCard(
    isConnected: Boolean,
    statsText: String,
    contactsGranted: Boolean,
    facebookAppId: String,
    onAppIdChange: (String) -> Unit,
    facebookToken: String,
    onTokenChange: (String) -> Unit,
    onLaunchOAuth: () -> Unit,
    onVerifyGraphToken: () -> Unit,
    onGrantContacts: () -> Unit
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_facebook")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ConnectorHeaderRow(
                title = "Facebook & Close Circle",
                category = "Facebook Graph API v21.0 + Android Contacts",
                isConnected = isConnected,
                icon = Icons.Default.Groups,
                accentColor = CoralPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Connect via Facebook OAuth / Graph API v21.0 (profile, friends count, attending & declined events) and/or sync starred close-circle contacts & birthdays from your phone.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            ConnectorLiveStatusBox(isConnected = isConnected, statsText = statsText, accentColor = CoralPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            SerenitySecondaryButton(
                text = if (contactsGranted) "Re-Sync Starred Contacts & Birthdays" else "Sync Device Starred Contacts & Birthdays",
                icon = Icons.Default.Contacts,
                onClick = onGrantContacts,
                modifier = Modifier.fillMaxWidth(),
                testTag = "connect_contacts_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = facebookToken,
                onValueChange = onTokenChange,
                label = { Text("Facebook Graph User Access Token") },
                placeholder = { Text("EAABwzLixnjYBO... (v21.0 Graph API)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = connectorTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = facebookAppId,
                onValueChange = onAppIdChange,
                label = { Text("Facebook App ID (for Browser OAuth)") },
                placeholder = { Text("e.g. 123456789012345") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = connectorTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SerenityPrimaryButton(
                    text = "Verify Graph Token",
                    icon = Icons.Default.Check,
                    onClick = onVerifyGraphToken,
                    modifier = Modifier.weight(1.1f),
                    testTag = "verify_facebook_token_button"
                )
                if (facebookAppId.isNotBlank()) {
                    SerenitySecondaryButton(
                        text = "OAuth Login",
                        icon = Icons.Default.OpenInNew,
                        onClick = onLaunchOAuth,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RealBankFinanceCard(
    isConnected: Boolean,
    statsText: String,
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    plaidClientId: String,
    onPlaidClientIdChange: (String) -> Unit,
    plaidSecret: String,
    onPlaidSecretChange: (String) -> Unit,
    plaidEnv: String,
    onPlaidEnvChange: (String) -> Unit,
    plaidAccessToken: String,
    onPlaidAccessTokenChange: (String) -> Unit,
    onConnectPlaid: () -> Unit,
    simpleFinInput: String,
    onSimpleFinChange: (String) -> Unit,
    onConnectSimpleFin: () -> Unit,
    onPickBankCsv: () -> Unit
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_finance_app")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ConnectorHeaderRow(
                title = "Bank & Finance App",
                category = "Plaid API • SimpleFIN Open Banking • CSV",
                isConnected = isConnected,
                icon = Icons.Default.AccountBalance,
                accentColor = AmberTertiary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Pull real bank account balances, 30-day spending, and late-night food delivery convenience patterns via Plaid REST API, SimpleFIN Bridge, or any exported Bank .CSV statement.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            ConnectorLiveStatusBox(isConnected = isConnected, statsText = statsText, accentColor = AmberTertiary)
            Spacer(modifier = Modifier.height(12.dp))

            // Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Plaid API", "SimpleFIN", "Bank CSV").forEach { tab ->
                    SerenityPillChip(
                        text = tab,
                        isSelected = selectedTab == tab,
                        onClick = { onSelectTab(tab) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                "Plaid API" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Environment:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WarmSlateCharcoal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        listOf("sandbox", "production").forEach { env ->
                            SerenityPillChip(
                                text = env.replaceFirstChar { it.uppercase() },
                                isSelected = plaidEnv.lowercase() == env,
                                onClick = { onPlaidEnvChange(env) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = plaidClientId,
                        onValueChange = onPlaidClientIdChange,
                        label = { Text("Plaid Client ID") },
                        placeholder = { Text("Enter Plaid client_id") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = connectorTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = plaidSecret,
                        onValueChange = onPlaidSecretChange,
                        label = { Text("Plaid Secret") },
                        placeholder = { Text("Enter Plaid sandbox or production secret") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = connectorTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = plaidAccessToken,
                        onValueChange = onPlaidAccessTokenChange,
                        label = { Text("Item Access Token (Auto-created in Sandbox)") },
                        placeholder = { Text("Leave blank in Sandbox to auto-link First Platypus Bank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = connectorTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SerenityPrimaryButton(
                        text = "Connect Plaid & Fetch Transactions",
                        icon = Icons.Default.AccountBalance,
                        onClick = onConnectPlaid,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "connect_plaid_button"
                    )
                }

                "SimpleFIN" -> {
                    Text(
                        text = "Paste a Base64 Setup Token or Access URL from SimpleFIN Bridge (beta-bridge.simplefin.org) to connect 10,000+ real US/CA banks directly.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmSlateCharcoal,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = simpleFinInput,
                        onValueChange = onSimpleFinChange,
                        label = { Text("SimpleFIN Setup Token or Access URL") },
                        placeholder = { Text("aHR0cHM6Ly9iZXRhLWJyaWRnZS5zaW1wbGVmaW4ub3Jn...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = connectorTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SerenityPrimaryButton(
                        text = "Connect SimpleFIN Open Banking",
                        icon = Icons.Default.Sync,
                        onClick = onConnectSimpleFin,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "connect_simplefin_button"
                    )
                }

                else -> {
                    Text(
                        text = "Import any real .CSV statement exported from Chase, Bank of America, Apple Card, Amex, Wells Fargo, or Capital One. Parsed 100% locally on your phone.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmSlateCharcoal,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SerenityPrimaryButton(
                        text = "Import Real Bank .CSV Statement",
                        icon = Icons.Default.UploadFile,
                        onClick = onPickBankCsv,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "import_bank_csv_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectorHeaderRow(
    title: String,
    category: String,
    isConnected: Boolean,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WarmSlateCharcoal
                    )
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = if (isConnected) MintContainer else SubtleContainer
        ) {
            Text(
                text = if (isConnected) "LIVE" else "Not Linked",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isConnected) DarkSageText else WarmSlateCharcoal,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ConnectorLiveStatusBox(
    isConnected: Boolean,
    statsText: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isConnected) SubtleContainer else RecessedContainer)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) accentColor else SoftMutedText.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statsText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isConnected) DeepEspresso else SoftMutedText,
                    fontWeight = if (isConnected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun connectorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CoralHoverGlow,
    unfocusedBorderColor = WarmBorder,
    focusedContainerColor = FloatingSurface,
    unfocusedContainerColor = SubtleContainer
)

@Composable
private fun SimpleConnectionsGraphicView(
    integrations: LifeSyncIntegrations,
    isMetaKeyConnected: Boolean,
    isSyncingWithMuse: Boolean,
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    contentPadding: PaddingValues,
    onConnectGoogleHealth: () -> Unit,
    onConnectCalendarTasks: () -> Unit,
    onConnectFinance: () -> Unit,
    onConnectFacebook: () -> Unit,
    onSyncToMuse: () -> Unit,
    onOpenFullConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCount = listOf(
        integrations.googleHealthConnected,
        integrations.tasksAppConnected,
        integrations.financeAppConnected,
        integrations.facebookConnected,
        isMetaKeyConnected
    ).count { it }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BaseCanvas)
            .padding(
                top = contentPadding.calculateTopPadding() + 10.dp,
                bottom = contentPadding.calculateBottomPadding() + 12.dp,
                start = 20.dp,
                end = 20.dp
            )
            .testTag("simple_connections_graphic_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Status Chip & Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SerenityStatusChip(
                    text = "$activeCount of 5 Streams Active",
                    pulseColor = if (activeCount >= 3) MintSecondary else AmberTertiary
                )
                Surface(
                    shape = CircleShape,
                    color = if (currentTheme == AppThemeMode.META_HORIZON) MetaPrimaryBlue.copy(alpha = 0.14f) else CoralPrimary.copy(alpha = 0.14f),
                    modifier = Modifier
                        .border(1.dp, WarmBorder, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val next = if (currentTheme == AppThemeMode.META_HORIZON) AppThemeMode.LUMINOUS_SERENITY else AppThemeMode.META_HORIZON
                            onSelectTheme(next)
                        }
                        .testTag("quick_theme_toggle_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (currentTheme == AppThemeMode.META_HORIZON) MetaPrimaryBlue else CoralPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentTheme == AppThemeMode.META_HORIZON) "Meta Theme" else "Serenity Theme",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Life-Sync Constellation",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
            Text(
                text = "Live Health Connect, Calendar, Facebook Graph & Bank telemetry.",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = WarmSlateCharcoal
                )
            )
        }

        // 2. Centerpiece: Animated Constellation Hub + Interactive 2x2 Live Connect Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                ConnectionsHubGraphic(
                    integrations = integrations,
                    isMetaKeyConnected = isMetaKeyConnected,
                    modifier = Modifier
                        .size(205.dp)
                        .clickable(onClick = onOpenFullConnections)
                )
            }

            // Interactive 2x2 Real Integration Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactIntegrationToggleTile(
                        title = "Google Health",
                        subtitle = integrations.googleHealthStats,
                        isConnected = integrations.googleHealthConnected,
                        icon = Icons.Default.Favorite,
                        accentColor = MintSecondary,
                        onClick = onConnectGoogleHealth,
                        modifier = Modifier.weight(1f)
                    )
                    CompactIntegrationToggleTile(
                        title = "Calendar & Tasks",
                        subtitle = integrations.tasksAppStats,
                        isConnected = integrations.tasksAppConnected,
                        icon = Icons.Default.TaskAlt,
                        accentColor = SageAccent,
                        onClick = onConnectCalendarTasks,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactIntegrationToggleTile(
                        title = "Bank / Finance",
                        subtitle = integrations.financeAppStats,
                        isConnected = integrations.financeAppConnected,
                        icon = Icons.Default.AccountBalance,
                        accentColor = AmberTertiary,
                        onClick = onConnectFinance,
                        modifier = Modifier.weight(1f)
                    )
                    CompactIntegrationToggleTile(
                        title = "Facebook",
                        subtitle = integrations.facebookStats,
                        isConnected = integrations.facebookConnected,
                        icon = Icons.Default.Groups,
                        accentColor = CoralPrimary,
                        onClick = onConnectFacebook,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Meta AI Key & Instant Muse Sync Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(FloatingSurface)
                    .border(
                        1.dp,
                        if (isMetaKeyConnected) MintSecondary.copy(alpha = 0.6f) else WarmBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onOpenFullConnections)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CoralPrimary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Meta Llama Brain & Privacy",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepEspresso
                                )
                            )
                            Text(
                                text = if (isMetaKeyConnected) "Llama Inference Active • Ready" else "On-Device Zero-Cloud Privacy • Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkSageText,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = CoralPrimary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onSyncToMuse)
                    ) {
                        Text(
                            text = if (isSyncingWithMuse) "Reasoning..." else "Run Llama",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CoralPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Navigation Button to Full Connections Screen
        SerenityPrimaryButton(
            text = "Setup Connections",
            icon = Icons.Default.Hub,
            onClick = onOpenFullConnections,
            modifier = Modifier.fillMaxWidth(),
            testTag = "open_full_connections_button"
        )
    }
}

@Composable
private fun CompactIntegrationToggleTile(
    title: String,
    subtitle: String,
    isConnected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isConnected) accentColor.copy(alpha = 0.55f) else WarmBorder
    val badgeBg = if (isConnected) accentColor.copy(alpha = 0.15f) else SubtleContainer

    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(if (isConnected) FloatingSurface else RecessedContainer)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isConnected) accentColor else SoftMutedText,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) DeepEspresso else SoftMutedText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) accentColor else SoftMutedText.copy(alpha = 0.4f))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isConnected) subtitle else "Tap to connect live",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isConnected) WarmSlateCharcoal else SoftMutedText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ConnectionsHubGraphic(
    integrations: LifeSyncIntegrations,
    isMetaKeyConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ConnectionsGraphicIdle")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HubPulse"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HubFloatY"
    )
    val photonProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhotonStream"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = (h / 2f) + floatOffset

        val auraRadius = (w * 0.46f) * pulseScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CoralPrimary.copy(alpha = 0.22f),
                    MintSecondary.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(cx, cy)
        )

        val orbitRadius = w * 0.33f
        drawCircle(
            color = WarmBorder.copy(alpha = 0.65f),
            radius = orbitRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = CoralPrimary.copy(alpha = 0.14f),
            radius = orbitRadius * 0.62f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )

        val diag = orbitRadius * 0.707f
        val nodes = listOf(
            Triple(Offset(cx - diag, cy - diag), MintSecondary, integrations.googleHealthConnected),
            Triple(Offset(cx + diag, cy - diag), SageAccent, integrations.tasksAppConnected),
            Triple(Offset(cx - diag, cy + diag), AmberTertiary, integrations.financeAppConnected),
            Triple(Offset(cx + diag, cy + diag), CoralPrimary, integrations.facebookConnected),
            Triple(Offset(cx, cy - orbitRadius * 1.12f), CoralHoverGlow, isMetaKeyConnected)
        )

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

        nodes.forEachIndexed { idx, (nodePos, color, connected) ->
            drawLine(
                color = if (connected) color.copy(alpha = 0.70f) else SoftMutedText.copy(alpha = 0.30f),
                start = Offset(cx, cy),
                end = nodePos,
                strokeWidth = if (connected) 3.2.dp.toPx() else 1.8.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = if (connected) null else dashEffect
            )

            if (connected) {
                val staggeredT = (photonProgress + idx * 0.2f) % 1f
                val px = nodePos.x + (cx - nodePos.x) * staggeredT
                val py = nodePos.y + (cy - nodePos.y) * staggeredT
                drawCircle(
                    color = color.copy(alpha = 0.35f),
                    radius = 6.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        nodes.forEach { (nodePos, color, connected) ->
            val nodeR = 16.dp.toPx() * (if (connected) pulseScale else 0.92f)
            if (connected) {
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = nodeR * 1.45f,
                    center = nodePos
                )
            }
            drawCircle(
                color = if (connected) color else SubtleContainer,
                radius = nodeR,
                center = nodePos
            )
            drawCircle(
                color = if (connected) Color.White else WarmBorder,
                radius = if (connected) 5.5.dp.toPx() else nodeR,
                center = nodePos,
                style = if (connected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 2.dp.toPx())
            )
        }

        val hubRadius = (w * 0.14f) * pulseScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFFFEDE8), CoralHoverGlow),
                center = Offset(cx - 4.dp.toPx(), cy - 4.dp.toPx()),
                radius = hubRadius * 1.2f
            ),
            radius = hubRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = CoralPrimary.copy(alpha = 0.45f),
            radius = hubRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5.dp.toPx())
        )
        drawCircle(
            color = CoralPrimary,
            radius = 7.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun MuseAdviceSnippet(advice: MuseLifeAdvice) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("muse_advice_snippet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Muse Life Superintelligence",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                }

                Text(
                    text = advice.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SoftMutedText
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = advice.headline,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = advice.overallDiagnosis,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SubtleContainer)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = advice.tamagotchiPrescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = DeepEspresso
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeAppearanceCard(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_theme_settings")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CoralPrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Companion Appearance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Text(
                            text = "Design System & Color Tokens",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DarkSageText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MintContainer
                ) {
                    Text(
                        text = if (currentTheme == AppThemeMode.META_HORIZON) "Meta Active" else "Serenity Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkSageText,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Switch between the original Luminous Serenity palette and the official Meta Horizon brand colors. This updates the creature's crystal heart, orbital motes, bio-radar charts, buttons, and navigation aura.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Meta Horizon Theme Option (NEW)
                ThemeOptionTile(
                    title = "Meta Horizon",
                    subtitle = "Meta Blue (#0866FF), Cyan & AI Violet",
                    isSelected = currentTheme == AppThemeMode.META_HORIZON,
                    accentColor = MetaPrimaryBlue,
                    swatches = listOf(MetaPrimaryBlue, MetaCyanSecondary, MetaHorizonViolet),
                    onClick = { onSelectTheme(AppThemeMode.META_HORIZON) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_option_meta"
                )

                // 2. Luminous Serenity Theme Option (ORIGINAL)
                ThemeOptionTile(
                    title = "Serenity",
                    subtitle = "Warm Coral, Mint & Sunny Amber",
                    isSelected = currentTheme == AppThemeMode.LUMINOUS_SERENITY,
                    accentColor = SerenityCoralPrimary,
                    swatches = listOf(SerenityCoralPrimary, SerenityMintSecondary, SerenityAmberTertiary),
                    onClick = { onSelectTheme(AppThemeMode.LUMINOUS_SERENITY) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_option_serenity"
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionTile(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    swatches: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "theme_option_tile"
) {
    val borderColor = if (isSelected) accentColor else WarmBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.08f) else SubtleContainer

    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    swatches.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = WarmSlateCharcoal,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}
