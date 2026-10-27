package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sync
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenitySecondaryButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentType
import com.example.model.CareActionLog
import com.example.model.LifeQuest
import com.example.model.ScreenNav
import com.example.model.ThoughtStream
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityStatusChip
import com.example.ui.components.TamagotchiHabitat
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun AttuneScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val tamagotchi by viewModel.tamagotchiState.collectAsState()
    val careLogs by viewModel.careLogs.collectAsState()
    val lifeQuests by viewModel.lifeQuests.collectAsState()
    val isBreathingActive by viewModel.isBreathingActive.collectAsState()
    val breathPhase by viewModel.breathPhase.collectAsState()
    val metaAiConfig by viewModel.metaAiConfig.collectAsState()
    val latestMuseAdvice by viewModel.latestMuseAdvice.collectAsState()
    val isSyncingWithMuse by viewModel.isSyncingWithMuse.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BaseCanvas)
            .testTag("attune_screen"),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 18.dp,
            end = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header & Life Companion Status
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SerenityStatusChip(
                    text = "${tamagotchi.name} is ${tamagotchi.mood.title}",
                    pulseColor = CoralPrimary,
                    modifier = Modifier.testTag("tamagotchi_status_chip")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Life Companion Tamagotchi",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Your life choices directly shape ${tamagotchi.name}'s vitality and happiness.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmSlateCharcoal
                    )
                )
            }
        }

        // 2. The Tamagotchi Habitat Pod (Living Creature, Life Vitals & Caretaker Actions)
        item {
            TamagotchiHabitat(
                state = tamagotchi,
                onPet = { viewModel.petTamagotchi() },
                onSleepToggle = { viewModel.toggleSleepMode() },
                onWater = { viewModel.feedWater() },
                onBreathe = { viewModel.doBreathReset() },
                onWalk = { viewModel.logMovementWalk() },
                onFocus = { viewModel.logFocusSprint() },
                onConnect = { viewModel.logConnection() }
            )
        }

        // 2a. Muse Superintelligence Life Telemetry Sync
        item {
            SerenityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("muse_sync_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CoralPrimary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Send Info to Muse",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepEspresso
                                    )
                                )
                                Text(
                                    text = "Life Telemetry → AI Guidance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarmSlateCharcoal
                                    )
                                )
                            }
                        }

                        if (metaAiConfig.token.isNotBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = MintContainer
                            ) {
                                Text(
                                    text = "Token Connected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DarkSageText,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = SubtleContainer,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.navigateTo(ScreenNav.PRESENCE) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Token",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CoralPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Packages ${tamagotchi.name}'s 5 vitals, habit quests, and care history to Muse to generate an adaptive life prescription.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SerenityPrimaryButton(
                            text = if (isSyncingWithMuse) "Transmitting..." else "Consult Muse for Advice",
                            icon = Icons.Default.Sync,
                            onClick = { viewModel.sendAllInfoToMuse() },
                            modifier = Modifier.weight(1f),
                            testTag = "home_send_to_muse_button"
                        )
                    }

                    // Display latest Muse life advice snippet if available
                    latestMuseAdvice?.let { advice ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SubtleContainer)
                                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = advice.headline,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CoralPrimary
                                        )
                                    )
                                    Text(
                                        text = advice.timestamp,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SoftMutedText,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = advice.tamagotchiPrescription,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepEspresso
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2b. Mindful Breath Active Banner (if user clicked breathe)
        item {
            AnimatedVisibility(
                visible = isBreathingActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = MintSecondary.copy(alpha = 0.25f),
                            spotColor = CoralPrimary.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(FloatingSurface)
                        .border(1.5.dp, MintSecondary.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = DarkSageText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mindful Breath Sync",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SageAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = breathPhase,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DeepEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.toggleBreathingExercise() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close breath reset",
                                tint = WarmSlateCharcoal
                            )
                        }
                    }
                }
            }
        }

        // 3. Today's Life Quests (Boost your life & your Tamagotchi)
        item {
            SerenityCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Life Quests",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${lifeQuests.count { it.isCompleted }} / ${lifeQuests.size} Done",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CoralPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lifeQuests.forEach { quest ->
                            LifeQuestRow(
                                quest = quest,
                                onToggle = { viewModel.toggleQuest(quest.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Conversations with your Tamagotchi
        item {
            Column {
                Text(
                    text = "Talk with ${tamagotchi.name}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val quickPrompts = listOf(
                        "✨ How are you feeling right now, Lumi?",
                        "💧 I just drank water!",
                        "🧘 Guide me through a 3-min reset",
                        "🎯 Help me plan my focus sprint",
                        "💖 What makes you happiest?"
                    )
                    items(quickPrompts) { prompt ->
                        Surface(
                            shape = CircleShape,
                            color = FloatingSurface,
                            modifier = Modifier
                                .shadow(2.dp, CircleShape)
                                .border(1.dp, WarmBorder, CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.sendQuickPrompt(prompt) }
                                .testTag("quick_tamagotchi_chat_pill")
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = DeepEspresso,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Care Logs
        item {
            SerenityCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Recent Life Care Nurturing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    careLogs.take(4).forEach { log ->
                        CareLogRow(log = log)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 6. Life Attunement Thought Streams
        item {
            Text(
                text = "Cognitive Synthesis for ${tamagotchi.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
        }

        items(viewModel.thoughtStreams) { stream ->
            ThoughtStreamCard(
                stream = stream,
                onPromptClick = { viewModel.sendQuickPrompt(it) }
            )
        }
    }
}

@Composable
private fun LifeQuestRow(
    quest: LifeQuest,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (quest.isCompleted) SubtleContainer else FloatingSurface)
            .border(1.dp, if (quest.isCompleted) WarmBorder else WarmBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (quest.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (quest.isCompleted) "Completed" else "Incomplete",
            tint = if (quest.isCompleted) CoralPrimary else SoftMutedText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (quest.isCompleted) FontWeight.SemiBold else FontWeight.Medium,
                    color = DeepEspresso
                )
            )
            Text(
                text = quest.impact,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CoralPrimary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun CareLogRow(log: CareActionLog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = log.actionName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepEspresso
                    )
                )
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WarmSlateCharcoal,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+${log.xpGained} XP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SoftMutedText,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun ThoughtStreamCard(
    stream: ThoughtStream,
    onPromptClick: (String) -> Unit
) {
    val accentColor = when (stream.accentType) {
        AccentType.CORAL -> CoralPrimary
        AccentType.MINT -> SageAccent
        AccentType.AMBER -> AmberTertiary
    }

    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("thought_stream_card"),
        onClick = { onPromptClick(stream.prompt) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stream.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Open stream",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stream.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stream.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "Explore this stream",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "→",
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
