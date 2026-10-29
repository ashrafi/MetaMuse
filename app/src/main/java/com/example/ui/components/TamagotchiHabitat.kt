package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TamagotchiState
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CoralActive
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

@Composable
fun TamagotchiHabitat(
    state: TamagotchiState,
    onPet: () -> Unit,
    onSleepToggle: () -> Unit,
    onWater: () -> Unit,
    onBreathe: () -> Unit,
    onWalk: () -> Unit,
    onFocus: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val podShape = RoundedCornerShape(36.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = podShape,
                ambientColor = CoralPrimary.copy(alpha = 0.14f),
                spotColor = DeepEspresso.copy(alpha = 0.08f)
            )
            .clip(podShape)
            .background(FloatingSurface)
            .border(1.5.dp, WarmBorder, podShape)
            .testTag("tamagotchi_habitat_pod")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Habitat Top Bar: Evolution & Level + Sleep Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CoralPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "LVL ${state.level}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CoralPrimary,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.evolution,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                }

                IconButton(
                    onClick = onSleepToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.isSleeping) SageAccent else SubtleContainer)
                        .testTag("tamagotchi_sleep_button")
                ) {
                    Icon(
                        imageVector = if (state.isSleeping) Icons.Default.WbSunny else Icons.Default.Bedtime,
                        contentDescription = "Toggle sleep mode",
                        tint = if (state.isSleeping) Color.White else WarmSlateCharcoal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // XP Progress Bar to next evolution
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Life Score: ${state.overallLifeScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CoralPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${state.xp} / ${state.xpForNextLevel} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftMutedText
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { state.xp.toFloat() / state.xpForNextLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = CoralPrimary,
                    trackColor = SubtleContainer
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Creature Speech Bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(SubtleContainer)
                    .border(1.dp, WarmBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("tamagotchi_speech_bubble")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = state.mood.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "\"${state.speech}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DeepEspresso,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            // 3. The Animated Interactive Creature
            Spacer(modifier = Modifier.height(6.dp))
            TamagotchiCreature(
                state = state,
                onPet = onPet,
                size = 180.dp
            )

            Text(
                text = "✨ Tap ${state.name} to pet (${state.totalPets} hugs shared)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CoralPrimary,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Life Vitals Meters (How well you are doing in your life)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(RecessedContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Life Vitals Synchronization",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DeepEspresso,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                LifeVitalBar(
                    label = "Hydration",
                    percent = state.hydration,
                    color = MintSecondary,
                    icon = Icons.Default.LocalDrink
                )
                LifeVitalBar(
                    label = "Mind & Focus",
                    percent = state.focus,
                    color = CoralPrimary,
                    icon = Icons.Default.Psychology
                )
                LifeVitalBar(
                    label = "Inner Peace",
                    percent = state.innerPeace,
                    color = SageAccent,
                    icon = Icons.Default.SelfImprovement
                )
                LifeVitalBar(
                    label = "Physical Energy",
                    percent = state.physicalEnergy,
                    color = AmberTertiary,
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk
                )
                LifeVitalBar(
                    label = "Connection",
                    percent = state.connection,
                    color = CoralActive,
                    icon = Icons.Default.Favorite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Quick Caretaker Action Pills (Nurture your life & creature!)
            Text(
                text = "Nurture Your Life • Feed ${state.name}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CaretakerActionButton(
                    label = "+ Water",
                    icon = Icons.Default.LocalDrink,
                    color = MintSecondary,
                    onClick = onWater,
                    testTag = "care_action_water"
                )
                CaretakerActionButton(
                    label = "+ Breathe",
                    icon = Icons.Default.SelfImprovement,
                    color = SageAccent,
                    onClick = onBreathe,
                    testTag = "care_action_breathe"
                )
                CaretakerActionButton(
                    label = "+ Walk",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = AmberTertiary,
                    onClick = onWalk,
                    testTag = "care_action_walk"
                )
                CaretakerActionButton(
                    label = "+ Focus",
                    icon = Icons.Default.Psychology,
                    color = CoralPrimary,
                    onClick = onFocus,
                    testTag = "care_action_focus"
                )
                CaretakerActionButton(
                    label = "+ Connect",
                    icon = Icons.Default.Favorite,
                    color = CoralActive,
                    onClick = onConnect,
                    testTag = "care_action_connect"
                )
            }
        }
    }
}

@Composable
private fun LifeVitalBar(
    label: String,
    percent: Float,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = DeepEspresso,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(96.dp)
        )
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(percent * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun CaretakerActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = color.copy(alpha = 0.25f),
                    spotColor = color.copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeepEspresso
            )
        )
    }
}
