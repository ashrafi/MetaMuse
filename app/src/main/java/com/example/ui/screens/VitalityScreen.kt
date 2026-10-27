package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenityStatusChip
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RecessedContainer
import com.example.ui.theme.SageAccent
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

    var claritySlider by remember { mutableFloatStateOf(checkin.clarityScore.toFloat()) }
    var emotionalSlider by remember { mutableFloatStateOf(checkin.emotionalEquilibrium.toFloat()) }
    var selectedMood by remember { mutableStateOf(checkin.moodWord) }
    var showCheckinSuccess by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SerenityStatusChip(
                    text = "Cognitive Synthesis & Balance",
                    pulseColor = MintSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Equilibrium & Vitality",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEspresso
                    )
                )
                Text(
                    text = "Longitudinal cognitive synthesis and restorative patterns.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmSlateCharcoal
                    )
                )
            }
        }

        // Metrics Overview: 3 Pillar Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VitalityMetricCard(
                    title = "Clarity",
                    value = "${checkin.clarityScore}%",
                    status = "Peak Focus",
                    icon = Icons.Default.Psychology,
                    accentColor = CoralPrimary,
                    modifier = Modifier.weight(1f)
                )
                VitalityMetricCard(
                    title = "Equilibrium",
                    value = "${checkin.emotionalEquilibrium}%",
                    status = "Harmonious",
                    icon = Icons.Default.SelfImprovement,
                    accentColor = SageAccent,
                    modifier = Modifier.weight(1f)
                )
                VitalityMetricCard(
                    title = "Peace",
                    value = "${checkin.restorativePeace}%",
                    status = "Attuned",
                    icon = Icons.Default.WbSunny,
                    accentColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Equilibrium Wave (Custom Canvas)
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
                                text = "Weekly Resonance Wave",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepEspresso
                                )
                            )
                            Text(
                                text = "Consistency: +14% compared to last cycle",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SageAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Canvas Wave
                    EquilibriumWaveCanvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (day == "Thu") CoralPrimary else WarmSlateCharcoal,
                                    fontWeight = if (day == "Thu") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Daily Mindful Check-in Module
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
                        text = "Serenity adjusts response cadence and tone based on your state.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clarity Slider
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
                        valueRange = 50f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CoralPrimary,
                            activeTrackColor = CoralPrimary,
                            inactiveTrackColor = SubtleContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Emotional Equilibrium Slider
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
                        valueRange = 50f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = SageAccent,
                            activeTrackColor = SageAccent,
                            inactiveTrackColor = SubtleContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Felt Resonance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = DeepEspresso,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(18.dp))

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
                                text = "Resonance attuned. Serenity calibrated to $selectedMood.",
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

        // Synthesized Patterns
        item {
            Text(
                text = "Synthesized Behavioral Insights",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
        }

        item {
            PatternCard(
                title = "Circadian Synthesis Alignment",
                description = "Your cognitive synthesis speed is highest during morning natural light exposure (9:00 AM – 11:30 AM).",
                pillText = "Deep Focus Window",
                accentColor = CoralPrimary
            )
        }

        item {
            PatternCard(
                title = "Restorative Breath Impact",
                description = "Taking 3-minute mindful resets reduces afternoon cognitive fragmentation by 32%.",
                pillText = "Vitality Spark",
                accentColor = SageAccent
            )
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
private fun EquilibriumWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Data points representing weekly scores (Mon-Sun)
        val points = listOf(
            Offset(0f, height * 0.65f),
            Offset(width * 0.16f, height * 0.45f),
            Offset(width * 0.33f, height * 0.55f),
            Offset(width * 0.50f, height * 0.25f), // Thu peak
            Offset(width * 0.66f, height * 0.40f),
            Offset(width * 0.83f, height * 0.30f),
            Offset(width, height * 0.35f)
        )

        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2
            path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        // Fill under path with subtle coral/mint gradient
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

        // Draw the glowing line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(CoralPrimary, SageAccent, AmberTertiary)
            ),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points with glowing halo
        points.forEachIndexed { index, point ->
            val isPeak = index == 3
            drawCircle(
                color = if (isPeak) CoralPrimary.copy(alpha = 0.35f) else MintSecondary.copy(alpha = 0.2f),
                radius = if (isPeak) 10.dp.toPx() else 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (isPeak) CoralPrimary else FloatingSurface,
                radius = if (isPeak) 5.dp.toPx() else 3.5.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun PatternCard(
    title: String,
    description: String,
    pillText: String,
    accentColor: Color
) {
    SerenityCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 22.sp
                )
            )
        }
    }
}
