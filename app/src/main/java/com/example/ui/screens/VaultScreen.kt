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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemoryItem
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPillChip
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenitySecondaryButton
import com.example.ui.components.SerenityStatusChip
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun VaultScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    val selectedCategory by viewModel.selectedMemoryCategory.collectAsState()

    var showAddModal by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Intentions") }

    val filteredMemories = if (selectedCategory == "All") {
        memories
    } else {
        memories.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BaseCanvas)
            .testTag("vault_screen"),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SerenityStatusChip(
                        text = "Encrypted Mind Vault",
                        pulseColor = CoralPrimary
                    )

                    IconButton(
                        onClick = { showAddModal = !showAddModal },
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(CoralPrimary)
                            .testTag("add_memory_button")
                    ) {
                        Icon(
                            imageVector = if (showAddModal) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add intention or memory",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Memory & Intentions",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Text(
                    text = "Serenity stores your core values, creative seeds, and insights for continuous cognitive attunement.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmSlateCharcoal
                    )
                )
            }
        }

        // Add New Memory Expandable Card
        item {
            AnimatedVisibility(visible = showAddModal) {
                SerenityCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_memory_form_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Log Intention or Value",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Category selection
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Intentions", "Values", "Projects", "Reflections")) { cat ->
                                SerenityPillChip(
                                    text = cat,
                                    isSelected = newCategory == cat,
                                    onClick = { newCategory = cat }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title / Core Essence") },
                            placeholder = { Text("e.g. Unforced Effortlessness") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_title_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralHoverGlow,
                                unfocusedBorderColor = WarmBorder,
                                focusedContainerColor = FloatingSurface,
                                unfocusedContainerColor = SubtleContainer
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            label = { Text("Reflection or Intention Detail") },
                            placeholder = { Text("Describe the thought or intention...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_content_input"),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralHoverGlow,
                                unfocusedBorderColor = WarmBorder,
                                focusedContainerColor = FloatingSurface,
                                unfocusedContainerColor = SubtleContainer
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SerenityPrimaryButton(
                                text = "Preserve in Vault",
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.addMemory(
                                            title = newTitle.trim(),
                                            content = newContent.trim().ifEmpty { newTitle.trim() },
                                            category = newCategory
                                        )
                                        newTitle = ""
                                        newContent = ""
                                        showAddModal = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "save_memory_button"
                            )
                            SerenitySecondaryButton(
                                text = "Cancel",
                                onClick = { showAddModal = false },
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Values", "Projects", "Intentions", "Reflections")) { category ->
                    SerenityPillChip(
                        text = category,
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.setMemoryCategory(category) }
                    )
                }
            }
        }

        // Memory List
        items(filteredMemories, key = { it.id }) { item ->
            MemoryCard(
                memory = item,
                onTogglePin = { viewModel.toggleMemoryPin(item.id) },
                onReflect = {
                    viewModel.sendQuickPrompt("Reflect with me on my stored memory: '${item.title}' - ${item.content}")
                }
            )
        }
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryItem,
    onTogglePin: () -> Unit,
    onReflect: () -> Unit
) {
    val categoryColor = when (memory.category) {
        "Values" -> CoralPrimary
        "Projects" -> SageAccent
        "Intentions" -> AmberTertiary
        else -> DeepEspresso
    }

    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_item_card")
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
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = memory.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memory.date,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmSlateCharcoal
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (memory.isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (memory.isPinned) "Unpin" else "Pin",
                            tint = if (memory.isPinned) CoralPrimary else WarmSlateCharcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = memory.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onReflect)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reflect on this with Serenity",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
