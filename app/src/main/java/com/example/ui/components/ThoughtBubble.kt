package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RecessedContainer
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtBubble(
    message: ChatMessage,
    onPromptClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (message.isFromUser) {
        UserMessageBubble(message = message, modifier = modifier)
    } else {
        AiThoughtBubble(
            message = message,
            onPromptClick = onPromptClick,
            modifier = modifier
        )
    }
}

/**
 * User Message: Soft coral container with asymmetrical rounded corners
 */
@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp,
        bottomEnd = 6.dp
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = bubbleShape,
                    ambientColor = CoralPrimary.copy(alpha = 0.18f),
                    spotColor = CoralPrimary.copy(alpha = 0.22f)
                )
                .clip(bubbleShape)
                .background(CoralPrimary)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .testTag("user_message_bubble")
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 23.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall.copy(
                color = SoftMutedText,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(end = 6.dp)
        )
    }
}

/**
 * AI Message: Luminous Serenity Conversational AI Thought Bubble
 * - Asymmetrical pill-like speech vessel (28dp, 28dp, 28dp, 8dp)
 * - Luminous blush glass background (rgba(255, 255, 255, 0.94))
 * - Left-edge gradient trace flowing from coral (#FF6F61) into warm amber (#FBBF24)
 * - Expandable "Cognitive Synthesis" steps
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiThoughtBubble(
    message: ChatMessage,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 26.dp,
        topEnd = 26.dp,
        bottomStart = 8.dp,
        bottomEnd = 26.dp
    )

    var showCognitiveSteps by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // AI Header tag with Serenity Star
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Serenity",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
            if (message.category != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•  ${message.category}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CoralPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // The Vessel with Left Gradient Rail
        Box(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = bubbleShape,
                    ambientColor = CoralPrimary.copy(alpha = 0.10f),
                    spotColor = WarmSlateCharcoal.copy(alpha = 0.05f)
                )
                .clip(bubbleShape)
                .background(Color.White.copy(alpha = 0.96f))
                .border(1.dp, WarmBorder, bubbleShape)
                .testTag("ai_thought_bubble")
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Left-edge gradient accent trace
                Box(
                    modifier = Modifier
                        .width(4.5.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                listOf(CoralPrimary, AmberTertiary, MintSecondary)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Optional expandable thought chain summary button
                    if (message.thoughtSteps.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SubtleContainer)
                                .clickable { showCognitiveSteps = !showCognitiveSteps }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cognitive Synthesis (${message.thoughtSteps.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DeepEspresso,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showCognitiveSteps) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = WarmSlateCharcoal,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = showCognitiveSteps,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 8.dp, bottom = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RecessedContainer)
                                    .padding(10.dp)
                            ) {
                                message.thoughtSteps.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = CoralPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = step.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = DeepEspresso,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = step.detail,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = WarmSlateCharcoal,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Main Message Body
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DeepEspresso,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftMutedText,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Suggested Follow-up Prompt Chips
        if (message.suggestedPrompts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.suggestedPrompts.forEach { prompt ->
                    Surface(
                        shape = CircleShape,
                        color = SubtleContainer,
                        modifier = Modifier
                            .border(1.dp, WarmBorder, CircleShape)
                            .clip(CircleShape)
                            .clickable { onPromptClick(prompt) }
                            .testTag("suggested_prompt_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = DeepEspresso,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
