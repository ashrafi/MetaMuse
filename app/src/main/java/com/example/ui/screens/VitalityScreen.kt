package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.example.model.ChannelTelemetryReport
import com.example.model.MemoryItem
import com.example.model.MuseDemoScenario
import com.example.model.ScreenNav
import com.example.model.SyntheticTelemetrySnapshot
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPillChip
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
import com.example.ui.theme.SerenityError
import com.example.ui.theme.SerenityErrorContainer
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun VitalityScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val checkin by viewModel.vitalityCheckin.collectAsState()
    val showFullData by viewModel.showFullData.collectAsState()
    val activeScenario by viewModel.activeDemoScenario.collectAsState()
    val telemetry by viewModel.telemetrySnapshot.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val selectedCategory by viewModel.selectedMemoryCategory.collectAsState()

    var claritySlider by remember(checkin.clarityScore) { mutableFloatStateOf(checkin.clarityScore.toFloat()) }
    var emotionalSlider by remember(checkin.emotionalEquilibrium) { mutableFloatStateOf(checkin.emotionalEquilibrium.toFloat()) }
    var selectedMood by remember(checkin.moodWord) { mutableStateOf(checkin.moodWord) }
    var showCheckinSuccess by remember { mutableStateOf(false) }

    var showAddModal by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Values") }

    val filteredMemories = if (selectedCategory == "All") {
        memories
    } else {
        memories.filter { it.category == selectedCategory }
    }

    if (showFullData) {
        BackHandler {
            viewModel.setShowFullData(false)
        }
    }

    AnimatedContent(
        targetState = showFullData,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "DataViewTransition"
    ) { isFullScreen ->
        if (!isFullScreen) {
            SimpleDataGraphicView(
                telemetry = telemetry,
                activeScenario = activeScenario,
                contentPadding = contentPadding,
                onSelectScenario = { viewModel.activateDemoScenario(it) },
                onOpenFullData = { viewModel.setShowFullData(true) },
                modifier = modifier
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(BaseCanvas)
                    .testTag("vitality_screen"),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Back Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SubtleContainer,
                            modifier = Modifier
                                .border(1.dp, WarmBorder, CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.setShowFullData(false) }
                                .testTag("back_to_data_graphic_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Data Graphic",
                                    tint = DeepEspresso,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Back to Data Overview",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = DeepEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // 1. Header
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SerenityStatusChip(
                            text = if (telemetry.deterministicTriggerActive) {
                                "Anomaly Detected • ${activeScenario.title}"
                            } else {
                                "4-Channel Bio-Responsive Telemetry"
                            },
                            pulseColor = if (telemetry.deterministicTriggerActive) SerenityError else MintSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Show Data",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Text(
                            text = "Synthetic & live telemetry across Health, Tasks, Finance, Social, and Core Values.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WarmSlateCharcoal
                            )
                        )
                    }
                }

        // 2. Pump Synthetic Data Switcher (Switch between the 3 MuseLife Demos right on Data tab)
        item {
            SerenityCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pump Synthetic Telemetry Dataset",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val scenarios = listOf(
                            MuseDemoScenario.LIVE_REAL_DATA,
                            MuseDemoScenario.SUCCESS_TRAP,
                            MuseDemoScenario.INVISIBLE_MONEY_LEAK,
                            MuseDemoScenario.CONNECTION_DEBT,
                            MuseDemoScenario.NONE
                        )
                        items(scenarios) { scenario ->
                            val isSelected = activeScenario == scenario
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CoralPrimary else SubtleContainer,
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) CoralPrimary else WarmBorder, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { viewModel.activateDemoScenario(scenario) }
                            ) {
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else DeepEspresso,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (telemetry.deterministicTriggerActive) SerenityErrorContainer else MintContainer)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (telemetry.deterministicTriggerActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (telemetry.deterministicTriggerActive) SerenityError else DarkSageText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = telemetry.triggeredRuleExplanation,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (telemetry.deterministicTriggerActive) SerenityError else DarkSageText,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. The 4 Multi-Channel Data Cards (Health, Tasks, Finance, Social)
        item {
            ChannelDataCard(
                report = telemetry.healthReport,
                icon = Icons.Default.Favorite,
                accentColor = MintSecondary,
                testTag = "data_channel_health"
            )
        }

        item {
            ChannelDataCard(
                report = telemetry.tasksReport,
                icon = Icons.Default.TaskAlt,
                accentColor = SageAccent,
                testTag = "data_channel_tasks"
            )
        }

        item {
            ChannelDataCard(
                report = telemetry.financeReport,
                icon = Icons.Default.AccountBalance,
                accentColor = AmberTertiary,
                testTag = "data_channel_finance"
            )
        }

        item {
            ChannelDataCard(
                report = telemetry.socialReport,
                icon = Icons.Default.Groups,
                accentColor = CoralPrimary,
                testTag = "data_channel_social"
            )
        }

        // 4. Metrics Overview: 3 Pillar Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VitalityMetricCard(
                    title = "Clarity",
                    value = "${checkin.clarityScore}%",
                    status = "Cognitive",
                    icon = Icons.Default.Psychology,
                    accentColor = CoralPrimary,
                    modifier = Modifier.weight(1f)
                )
                VitalityMetricCard(
                    title = "Equilibrium",
                    value = "${checkin.emotionalEquilibrium}%",
                    status = "Balance",
                    icon = Icons.Default.SelfImprovement,
                    accentColor = SageAccent,
                    modifier = Modifier.weight(1f)
                )
                VitalityMetricCard(
                    title = "Peace",
                    value = "${checkin.restorativePeace}%",
                    status = "Restorative",
                    icon = Icons.Default.WbSunny,
                    accentColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Weekly Sleep & Resonance Wave (Custom Canvas reflecting active scenario)
        item {
            SerenityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equilibrium_wave_card")
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
                        Column {
                            Text(
                                text = "Weekly Sleep & Resonance Trend",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepEspresso
                                )
                            )
                            Text(
                                text = "Hours of sleep across last 7 days (${telemetry.weeklySleepHours.last()}h latest)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SageAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    EquilibriumWaveCanvas(
                        sleepHours = telemetry.weeklySleepHours,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEachIndexed { idx, day ->
                            val hrs = telemetry.weeklySleepHours.getOrElse(idx) { 7.0f }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarmSlateCharcoal,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "${hrs}h",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (hrs < 6.0f) SerenityError else CoralPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Daily Mindful Check-in Module
        item {
            SerenityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mindful_checkin_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Attune Today's Resonance",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Text(
                        text = "Manual check-in updates cognitive clarity and emotional equilibrium.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cognitive Clarity",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DeepEspresso,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${claritySlider.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = CoralPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Slider(
                        value = claritySlider,
                        onValueChange = { claritySlider = it },
                        valueRange = 30f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CoralPrimary,
                            activeTrackColor = CoralPrimary,
                            inactiveTrackColor = SubtleContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Inner Calm & Space",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DeepEspresso,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${emotionalSlider.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SageAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Slider(
                        value = emotionalSlider,
                        onValueChange = { emotionalSlider = it },
                        valueRange = 30f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = SageAccent,
                            activeTrackColor = SageAccent,
                            inactiveTrackColor = SubtleContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val moods = listOf(
                        "Radiant Equilibrium",
                        "Grounded Peace",
                        "Creative Spark",
                        "Reflective Rest"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moods) { mood ->
                            val isSelected = selectedMood == mood
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CoralPrimary else SubtleContainer,
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) CoralPrimary else WarmBorder, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { selectedMood = mood }
                            ) {
                                Text(
                                    text = mood,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else DeepEspresso,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showCheckinSuccess) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MintContainer)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DarkSageText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resonance attuned. Calibrated to $selectedMood.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = DarkSageText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SerenityPrimaryButton(
                        text = "Harmonize State",
                        onClick = {
                            viewModel.updateVitality(
                                clarity = claritySlider.toInt(),
                                equilibrium = emotionalSlider.toInt(),
                                peace = ((claritySlider + emotionalSlider) / 2f).toInt(),
                                mood = selectedMood
                            )
                            showCheckinSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 7. Stated Values & Memory Vault Section (Used by Demo 3 Connection Debt to detect values-vs-behavior gap)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stated Values & Mind Vault",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Text(
                        text = "Muse compares your pinned values against your real telemetry.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal
                        )
                    )
                }
                IconButton(
                    onClick = { showAddModal = !showAddModal },
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CoralPrimary)
                        .testTag("add_memory_button")
                ) {
                    Icon(
                        imageVector = if (showAddModal) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add intention or value",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

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
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Log Core Value or Intention",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Values", "Intentions", "Projects", "Reflections")) { cat ->
                                SerenityPillChip(
                                    text = cat,
                                    isSelected = newCategory == cat,
                                    onClick = { newCategory = cat }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title / Core Value") },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            label = { Text("Detail") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_content_input"),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 2,
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
                            SerenityPrimaryButton(
                                text = "Preserve Value",
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.addMemory(
                                            title = newTitle.trim(),
                                            content = newContent.trim().ifEmpty { newTitle.trim() },
                                            category = newCategory,
                                            isPinned = true
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
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    }
                }
            }
        }

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

                items(filteredMemories, key = { it.id }) { item ->
                    DataVaultMemoryCard(
                        memory = item,
                        onTogglePin = { viewModel.toggleMemoryPin(item.id) },
                        onReflect = {
                            viewModel.sendQuickPrompt("Reflect with me on my stored value: '${item.title}' - ${item.content}")
                            viewModel.navigateTo(ScreenNav.HOME)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleDataGraphicView(
    telemetry: SyntheticTelemetrySnapshot,
    activeScenario: MuseDemoScenario,
    contentPadding: PaddingValues,
    onSelectScenario: (MuseDemoScenario) -> Unit,
    onOpenFullData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compositeScore = (
        telemetry.healthReport.scorePercent +
            telemetry.tasksReport.scorePercent +
            telemetry.financeReport.scorePercent +
            telemetry.socialReport.scorePercent
        ) / 4

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
            .testTag("simple_data_graphic_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Status Chip & Synthetic Dataset Switcher
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SerenityStatusChip(
                text = if (telemetry.deterministicTriggerActive) {
                    "Anomaly Detected • ${activeScenario.title}"
                } else {
                    "4-Channel Bio-Telemetry • $compositeScore% Harmony"
                },
                pulseColor = if (telemetry.deterministicTriggerActive) SerenityError else MintSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val scenarios = listOf(
                MuseDemoScenario.LIVE_REAL_DATA to "🟢 Live Data",
                MuseDemoScenario.NONE to "Healthy",
                MuseDemoScenario.SUCCESS_TRAP to "1. Success Trap",
                MuseDemoScenario.INVISIBLE_MONEY_LEAK to "2. Money Leak",
                MuseDemoScenario.CONNECTION_DEBT to "3. Connection Debt"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(scenarios) { (scenario, shortLabel) ->
                    val isSelected = activeScenario == scenario
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) CoralPrimary else FloatingSurface,
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (isSelected) CoralPrimary else WarmBorder,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { onSelectScenario(scenario) }
                    ) {
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else DeepEspresso,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Centerpiece: 4-Channel Concentric Radar Graphic + 2x2 Live Channel Tiles
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
                DataTelemetryGraphic(
                    telemetry = telemetry,
                    modifier = Modifier
                        .size(195.dp)
                        .clickable(onClick = onOpenFullData)
                )
            }

            // 2x2 Live Telemetry Channel Summary Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactChannelTelemetryTile(
                        report = telemetry.healthReport,
                        icon = Icons.Default.Favorite,
                        accentColor = MintSecondary,
                        onClick = onOpenFullData,
                        modifier = Modifier.weight(1f)
                    )
                    CompactChannelTelemetryTile(
                        report = telemetry.tasksReport,
                        icon = Icons.Default.TaskAlt,
                        accentColor = SageAccent,
                        onClick = onOpenFullData,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactChannelTelemetryTile(
                        report = telemetry.financeReport,
                        icon = Icons.Default.AccountBalance,
                        accentColor = AmberTertiary,
                        onClick = onOpenFullData,
                        modifier = Modifier.weight(1f)
                    )
                    CompactChannelTelemetryTile(
                        report = telemetry.socialReport,
                        icon = Icons.Default.Groups,
                        accentColor = CoralPrimary,
                        onClick = onOpenFullData,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Deterministic Rule Trigger Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (telemetry.deterministicTriggerActive) SerenityErrorContainer else MintContainer)
                    .border(
                        1.dp,
                        if (telemetry.deterministicTriggerActive) SerenityError.copy(alpha = 0.35f) else MintSecondary.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (telemetry.deterministicTriggerActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (telemetry.deterministicTriggerActive) SerenityError else DarkSageText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telemetry.triggeredRuleExplanation,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (telemetry.deterministicTriggerActive) SerenityError else DarkSageText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. Bottom Navigation Button to Full Data Screen
        SerenityPrimaryButton(
            text = "Open Data & Telemetry",
            icon = Icons.Default.Analytics,
            onClick = onOpenFullData,
            modifier = Modifier.fillMaxWidth(),
            testTag = "open_full_data_button"
        )
    }
}

@Composable
private fun CompactChannelTelemetryTile(
    report: ChannelTelemetryReport,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (report.isAlert) SerenityError.copy(alpha = 0.55f) else WarmBorder
    val badgeBg = if (report.isAlert) SerenityErrorContainer else accentColor.copy(alpha = 0.14f)
    val badgeTextCol = if (report.isAlert) SerenityError else DarkSageText

    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(FloatingSurface)
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
                            tint = if (report.isAlert) SerenityError else accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.channelName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = badgeBg
                ) {
                    Text(
                        text = "${report.scorePercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeTextCol,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = report.headlineStatus,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (report.isAlert) SerenityError else WarmSlateCharcoal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DataTelemetryGraphic(
    telemetry: SyntheticTelemetrySnapshot,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DataGraphicIdle")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DataAuraPulse"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DataFloatY"
    )

    val healthSweep by animateFloatAsState(
        targetValue = telemetry.healthReport.scorePercent / 100f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "HealthSweep"
    )
    val tasksSweep by animateFloatAsState(
        targetValue = telemetry.tasksReport.scorePercent / 100f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "TasksSweep"
    )
    val financeSweep by animateFloatAsState(
        targetValue = telemetry.financeReport.scorePercent / 100f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "FinanceSweep"
    )
    val socialSweep by animateFloatAsState(
        targetValue = telemetry.socialReport.scorePercent / 100f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "SocialSweep"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = (h / 2f) + floatOffset
        val isAlert = telemetry.deterministicTriggerActive

        // 1. Soft Ambient Aura
        val auraColor = if (isAlert) SerenityError else MintSecondary
        val auraRadius = (w * 0.48f) * pulseScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.24f),
                    CoralPrimary.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(cx, cy)
        )

        // 2. Four Concentric Bio-Telemetry Rings (Health, Tasks, Finance, Social)
        val channels = listOf(
            Triple(healthSweep, MintSecondary, telemetry.healthReport.isAlert),
            Triple(tasksSweep, SageAccent, telemetry.tasksReport.isAlert),
            Triple(financeSweep, AmberTertiary, telemetry.financeReport.isAlert),
            Triple(socialSweep, CoralPrimary, telemetry.socialReport.isAlert)
        )

        val strokeWidth = 9.dp.toPx()
        val ringGap = 13.dp.toPx()
        val outerRadius = w * 0.40f

        channels.forEachIndexed { idx, (progress, baseColor, channelAlert) ->
            val r = outerRadius - (idx * ringGap)
            val topLeft = Offset(cx - r, cy - r)
            val arcSize = Size(r * 2f, r * 2f)
            val activeColor = if (channelAlert) SerenityError else baseColor
            val sweepDeg = 260f * progress.coerceIn(0.12f, 1f)

            // Subtle track ring
            drawArc(
                color = WarmBorder.copy(alpha = 0.45f),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active telemetry sweep arc
            drawArc(
                color = activeColor,
                startAngle = -220f,
                sweepAngle = sweepDeg,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glowing endpoint dot at the tip of the arc
            val endAngleRad = Math.toRadians((-220f + sweepDeg).toDouble())
            val tipX = cx + (r * cos(endAngleRad)).toFloat()
            val tipY = cy + (r * sin(endAngleRad)).toFloat()
            drawCircle(
                color = Color.White,
                radius = 2.8.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }

        // 3. Central Luminous Pulse Core + Sleep Waveform
        val coreRadius = (w * 0.15f) * pulseScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    if (isAlert) Color(0xFFFFEAE6) else Color(0xFFEFFBF8)
                ),
                center = Offset(cx, cy),
                radius = coreRadius
            ),
            radius = coreRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = (if (isAlert) SerenityError else CoralPrimary).copy(alpha = 0.35f),
            radius = coreRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // Mini sleep resonance wave inside the core
        val waveW = coreRadius * 1.15f
        val waveH = coreRadius * 0.55f
        val hours = telemetry.weeklySleepHours
        if (hours.size >= 2) {
            val wavePath = Path()
            hours.forEachIndexed { idx, hrs ->
                val px = (cx - waveW / 2f) + waveW * (idx.toFloat() / (hours.size - 1))
                val norm = ((hrs - 4.5f) / 4.0f).coerceIn(0f, 1f)
                val py = (cy + waveH / 2f) - (waveH * norm)
                if (idx == 0) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
            }
            drawPath(
                path = wavePath,
                color = if (isAlert) SerenityError else CoralPrimary,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun ChannelDataCard(
    report: ChannelTelemetryReport,
    icon: ImageVector,
    accentColor: Color,
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
                .padding(18.dp)
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
                            .background(
                                if (report.isAlert) SerenityErrorContainer else accentColor.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (report.isAlert) SerenityError else accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = report.channelName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Text(
                            text = report.headlineStatus,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (report.isAlert) SerenityError else DarkSageText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (report.isAlert) SerenityErrorContainer else MintContainer
                ) {
                    Text(
                        text = "${report.scorePercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (report.isAlert) SerenityError else DarkSageText,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                report.metrics.forEach { metric ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (metric.isWarning) SerenityErrorContainer.copy(alpha = 0.55f) else SubtleContainer)
                            .border(
                                1.dp,
                                if (metric.isWarning) SerenityError.copy(alpha = 0.35f) else WarmBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(10.dp)
                        ) {
                        Column {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WarmSlateCharcoal,
                                    fontSize = 10.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (metric.isWarning) SerenityError else DeepEspresso
                                )
                            )
                            Text(
                                text = metric.trend,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (metric.isWarning) SerenityError else SageAccent,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            if (report.historyLog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RecessedContainer)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    report.historyLog.forEach { entry ->
                        Text(
                            text = "• $entry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DeepEspresso,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalityMetricCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.12f),
                spotColor = WarmSlateCharcoal.copy(alpha = 0.05f)
            )
            .clip(shape)
            .background(FloatingSurface)
            .border(1.dp, WarmBorder, shape)
            .padding(14.dp)
            .testTag("vitality_metric_card")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = WarmSlateCharcoal,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun EquilibriumWaveCanvas(
    sleepHours: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val safeHours = if (sleepHours.size >= 2) sleepHours else listOf(7f, 7f, 7f, 7f, 7f, 7f, 7f)

        val points = safeHours.mapIndexed { idx, hrs ->
            val x = width * (idx.toFloat() / (safeHours.size - 1).coerceAtLeast(1))
            val normalized = ((hrs - 4.0f) / 5.0f).coerceIn(0.05f, 0.95f)
            val y = height * (1f - normalized)
            Offset(x, y)
        }

        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2
            path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CoralPrimary.copy(alpha = 0.22f),
                    MintSecondary.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        )

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(CoralPrimary, SageAccent, AmberTertiary)
            ),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEachIndexed { index, point ->
            val hrs = safeHours[index]
            val isLow = hrs < 6.0f
            drawCircle(
                color = if (isLow) SerenityError.copy(alpha = 0.35f) else CoralPrimary.copy(alpha = 0.28f),
                radius = 7.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (isLow) SerenityError else CoralPrimary,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun DataVaultMemoryCard(
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
                .padding(16.dp)
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
                        modifier = Modifier.size(30.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = memory.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reflect on this with Muse",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
