package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MintSecondary

@Composable
fun LuminousAura(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    intensity: Float = 1.0f,
    accentColor: Color = MintSecondary,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraBreathing")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulseScale"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("luminous_aura_orb")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.minDimension / 2f) * pulseScale * intensity

            // 1. Outermost soft ambient bloom (Warm Coral diffuse halo)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoralPrimary.copy(alpha = 0.22f * intensity),
                        CoralPrimary.copy(alpha = 0.08f * intensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // 2. Mid concentric ring (Crisp Mint & Sage harmony halo)
            val midRadius = maxRadius * 0.76f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.32f * intensity),
                        accentColor.copy(alpha = 0.12f * intensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = midRadius
                ),
                radius = midRadius,
                center = center
            )

            // 3. Inner radiant core ring (Sunny Amber touch)
            val innerRingRadius = maxRadius * 0.52f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmberTertiary.copy(alpha = 0.38f * intensity),
                        AmberTertiary.copy(alpha = 0.14f * intensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = innerRingRadius
                ),
                radius = innerRingRadius,
                center = center
            )

            // 4. Luminous focal core (Pure White core with warm blush aura)
            val coreRadius = maxRadius * 0.32f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        CoralPrimary.copy(alpha = 0.90f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 5. Delicate center pinpoint light
            drawCircle(
                color = Color.White,
                radius = coreRadius * 0.45f,
                center = center
            )
        }
    }
}
