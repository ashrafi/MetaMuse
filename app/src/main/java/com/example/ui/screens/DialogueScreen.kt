package com.example.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SerenityStatusChip
import com.example.ui.components.ThoughtBubble
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun DialogueScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()

    var isInputFocused by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BaseCanvas)
            .testTag("dialogue_screen")
    ) {
        // Dialogue Header with Companion Persona & Status
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding()),
            color = BaseCanvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Serenity Dialogue",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Text(
                            text = "Personal Superintelligence Stream",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WarmSlateCharcoal
                            )
                        )
                    }

                    SerenityStatusChip(
                        text = selectedMode.title,
                        pulseColor = Color(selectedMode.auraGlowColorHex)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Context topic chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .border(1.dp, CoralPrimary.copy(alpha = 0.4f), CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.sendAllInfoToMuse() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ Consult Muse AI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CoralPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    val topics = listOf(
                        "Mindful Clarity",
                        "Strategic Synthesis",
                        "Creative Flow",
                        "Calm Reflection"
                    )
                    items(topics) { topic ->
                        Surface(
                            shape = CircleShape,
                            color = SubtleContainer,
                            modifier = Modifier
                                .border(1.dp, WarmBorder, CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.sendQuickPrompt(topic) }
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WarmSlateCharcoal,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Messages LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ThoughtBubble(
                    message = message,
                    onPromptClick = { viewModel.sendQuickPrompt(it) }
                )
            }
        }

        // Input Field Area with Design System Specs:
        // Enclosed in 24dp pill, pure white, luminous ring on focus, coral bloom send button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 10.dp,
                    top = 6.dp
                ),
            color = Color.Transparent
        ) {
            val containerShape = RoundedCornerShape(24.dp)
            val borderModifier = if (isInputFocused) {
                Modifier.border(2.dp, CoralHoverGlow, containerShape)
            } else {
                Modifier.border(1.dp, WarmBorder, containerShape)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isInputFocused) 10.dp else 4.dp,
                        shape = containerShape,
                        ambientColor = CoralPrimary.copy(alpha = if (isInputFocused) 0.20f else 0.08f),
                        spotColor = CoralPrimary.copy(alpha = 0.15f)
                    )
                    .clip(containerShape)
                    .background(FloatingSurface)
                    .then(borderModifier)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.sendQuickPrompt("Take a 3-minute mindful breath reset.")
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice attunement",
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask, reflect, or explore an intention...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = WarmSlateCharcoal.copy(alpha = 0.60f)
                                )
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = DeepEspresso
                            ),
                            cursorBrush = SolidColor(CoralPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { viewModel.sendCurrentMessage() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isInputFocused = it.isFocused }
                                .testTag("dialogue_input_field")
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Send Button with Warm Coral Glow
                    IconButton(
                        onClick = { viewModel.sendCurrentMessage() },
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                ambientColor = CoralPrimary.copy(alpha = 0.35f),
                                spotColor = CoralPrimary.copy(alpha = 0.45f)
                            )
                            .clip(CircleShape)
                            .background(CoralPrimary)
                            .testTag("dialogue_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
