package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MuseLifeAdvice
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenitySecondaryButton
import com.example.ui.components.SerenityStatusChip
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RecessedContainer
import com.example.ui.theme.SageAccent
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
    val metaAiConfig by viewModel.metaAiConfig.collectAsState()
    val latestMuseAdvice by viewModel.latestMuseAdvice.collectAsState()
    val isSyncingWithMuse by viewModel.isSyncingWithMuse.collectAsState()
    val integrations by viewModel.integrations.collectAsState()

    var tokenInput by remember(metaAiConfig.token) { mutableStateOf(metaAiConfig.token) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var showTokenSavedMessage by remember { mutableStateOf(false) }

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
        // Settings Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SerenityStatusChip(
                    text = "Life Telemetry & Integrations",
                    pulseColor = CoralPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Setup Connections",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Connect Google Health, Tasks, Finance App, Facebook, and your Meta AI Key to feed Muse.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmSlateCharcoal
                    )
                )
            }
        }

        // Section 1: Meta Key
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
                                    text = "Meta Key",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepEspresso
                                    )
                                )
                                Text(
                                    text = "Meta AI & Muse Intelligence",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarmSlateCharcoal
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (metaAiConfig.token.isNotBlank()) MintContainer else SubtleContainer
                        ) {
                            Text(
                                text = if (metaAiConfig.token.isNotBlank()) "Key Connected" else "Key Needed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (metaAiConfig.token.isNotBlank()) DarkSageText else WarmSlateCharcoal,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enter your Meta AI Token. All your Tamagotchi vitals, habits, and life telemetry will be sent to Muse to synthesize personalized life advice.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Meta AI Token") },
                        placeholder = { Text("Paste your Meta AI Bearer Token") },
                        singleLine = true,
                        visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                Icon(
                                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isTokenVisible) "Hide token" else "Show token",
                                    tint = WarmSlateCharcoal
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meta_key_input"),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralHoverGlow,
                            unfocusedBorderColor = WarmBorder,
                            focusedContainerColor = FloatingSurface,
                            unfocusedContainerColor = SubtleContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SerenitySecondaryButton(
                            text = "Save Key",
                            icon = Icons.Default.Check,
                            onClick = {
                                viewModel.updateMetaAiToken(tokenInput)
                                showTokenSavedMessage = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "save_meta_key_button"
                        )

                        SerenityPrimaryButton(
                            text = if (isSyncingWithMuse) "Transmitting..." else "Send to Muse",
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
                                text = "Meta Key saved securely.",
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

        // Section 2: Connect to Google Health
        item {
            IntegrationSettingCard(
                title = "Connect to Google Health",
                category = "Health & Physical Vitality",
                description = "Synchronizes your daily step count, sleep duration, and hydration metrics directly into Lumi's physical energy and hydration vitals.",
                statsText = integrations.googleHealthStats,
                isConnected = integrations.googleHealthConnected,
                icon = Icons.Default.Favorite,
                accentColor = MintSecondary,
                onToggle = { viewModel.toggleGoogleHealth() },
                testTag = "section_google_health"
            )
        }

        // Section 3: Connect to Facebook
        item {
            IntegrationSettingCard(
                title = "Connect to Facebook",
                category = "Social Connection & Circles",
                description = "Bridges close friend milestones, community check-ins, and shared moments to nurture Lumi's social connection vital.",
                statsText = integrations.facebookStats,
                isConnected = integrations.facebookConnected,
                icon = Icons.Default.Groups,
                accentColor = CoralPrimary,
                onToggle = { viewModel.toggleFacebook() },
                testTag = "section_facebook"
            )
        }

        // Section 4: Connect to Finance App
        item {
            IntegrationSettingCard(
                title = "Connect to Finance App",
                category = "Financial Peace & Clarity",
                description = "Monitors daily budget health and peace of mind metrics, removing financial anxiety and feeding your inner peace vital.",
                statsText = integrations.financeAppStats,
                isConnected = integrations.financeAppConnected,
                icon = Icons.Default.AccountBalance,
                accentColor = AmberTertiary,
                onToggle = { viewModel.toggleFinanceApp() },
                testTag = "section_finance_app"
            )
        }

        // Section 5: Connect to Tasks App
        item {
            IntegrationSettingCard(
                title = "Connect to Tasks App",
                category = "Focus Flow & Productivity",
                description = "Connects Google Tasks or Todoist. Completed tasks directly advance your daily Life Quests and elevate Lumi's Mind & Focus flow.",
                statsText = integrations.tasksAppStats,
                isConnected = integrations.tasksAppConnected,
                icon = Icons.Default.TaskAlt,
                accentColor = SageAccent,
                onToggle = { viewModel.toggleTasksApp() },
                testTag = "section_tasks_app"
            )
        }

        // Latest Muse Life Advice Card (When present)
        latestMuseAdvice?.let { advice ->
            item {
                MuseAdviceSnippet(advice = advice)
            }
        }
    }
}

@Composable
private fun IntegrationSettingCard(
    title: String,
    category: String,
    description: String,
    statsText: String,
    isConnected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onToggle: () -> Unit,
    testTag: String
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
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

                Switch(
                    checked = isConnected,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = WarmSlateCharcoal,
                        uncheckedTrackColor = SubtleContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

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
