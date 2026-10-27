package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CoralActive
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import kotlinx.coroutines.delay

@Composable
fun TamagotchiCreature(
    state: TamagotchiState,
    onPet: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TamagotchiIdle")

    // Gentle vertical floating breathing motion
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    // Gentle aura breathing scale
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    // Continuous soft halo shimmer rotation
    val haloShimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HaloShimmer"
    )

    // Periodic natural blinking
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..5000).random().toLong())
            isBlinking = true
            delay(160)
            isBlinking = false
        }
    }

    // Touch petting bounce reaction
    var petBounceTrigger by remember { mutableFloatStateOf(1f) }
    val animatedBounceScale by animateFloatAsState(
        targetValue = petBounceTrigger,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        finishedListener = { petBounceTrigger = 1f },
        label = "PetBounce"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("tamagotchi_creature")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    petBounceTrigger = 1.22f
                    onPet()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = (canvasH / 2f) + floatOffset

            // 1. Aura Halo Bloom (Reflecting life wellness score)
            val lifeScore = state.overallLifeScore
            val auraColor = when {
                state.isSleeping -> Color(0xFFCCC2DC)
                lifeScore >= 80 -> CoralPrimary
                lifeScore >= 60 -> MintSecondary
                else -> AmberTertiary
            }

            val auraRadius = (canvasW * 0.44f) * auraScale * animatedBounceScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = if (state.isSleeping) 0.15f else 0.32f),
                        auraColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(centerX, centerY)
            )

            // Secondary Mint/Amber inner ring
            val innerAuraRadius = auraRadius * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MintSecondary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerAuraRadius
                ),
                radius = innerAuraRadius,
                center = Offset(centerX, centerY)
            )

            // 2. Celestial Halo / Crown (Accessory)
            if (state.equippedAccessory.contains("Halo") || state.equippedAccessory.contains("Crown")) {
                val haloY = centerY - (canvasH * 0.28f) * animatedBounceScale
                drawOval(
                    brush = Brush.horizontalGradient(
                        listOf(AmberTertiary, CoralHoverGlow, AmberTertiary)
                    ),
                    topLeft = Offset(centerX - 36.dp.toPx(), haloY - 8.dp.toPx()),
                    size = Size(72.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.5.dp.toPx())
                )
                // Small celestial gem at top
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX, haloY - 8.dp.toPx())
                )
            }

            // 3. Cute Wisps / Ears on sides
            val earSpread = 38.dp.toPx() * animatedBounceScale
            val earY = centerY - 16.dp.toPx()
            // Left Ear Wisp
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(CoralHoverGlow, CoralPrimary),
                    center = Offset(centerX - earSpread, earY),
                    radius = 18.dp.toPx()
                ),
                radius = 16.dp.toPx() * animatedBounceScale,
                center = Offset(centerX - earSpread, earY)
            )
            // Right Ear Wisp
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(CoralHoverGlow, CoralPrimary),
                    center = Offset(centerX + earSpread, earY),
                    radius = 18.dp.toPx()
                ),
                radius = 16.dp.toPx() * animatedBounceScale,
                center = Offset(centerX + earSpread, earY)
            )

            // 4. Main Creature Body (Soft luminous pearlescent orb/egg shape)
            val bodyRadius = (canvasW * 0.26f) * animatedBounceScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFFFF0EC),
                        Color(0xFFFFDAD5)
                    ),
                    center = Offset(centerX - 8.dp.toPx(), centerY - 10.dp.toPx()),
                    radius = bodyRadius
                ),
                radius = bodyRadius,
                center = Offset(centerX, centerY)
            )

            // Subtle body outline for soft tactile definition
            drawCircle(
                color = CoralPrimary.copy(alpha = 0.25f),
                radius = bodyRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Rosy Cheeks (Coral blush bloom)
            val cheekOffset = 18.dp.toPx() * animatedBounceScale
            val cheekY = centerY + 8.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CoralPrimary.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(centerX - cheekOffset, cheekY),
                    radius = 10.dp.toPx()
                ),
                radius = 9.dp.toPx(),
                center = Offset(centerX - cheekOffset, cheekY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CoralPrimary.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(centerX + cheekOffset, cheekY),
                    radius = 10.dp.toPx()
                ),
                radius = 9.dp.toPx(),
                center = Offset(centerX + cheekOffset, cheekY)
            )

            // 6. Expressive Eyes
            val eyeSpacing = 14.dp.toPx() * animatedBounceScale
            val eyeY = centerY - 2.dp.toPx()

            if (state.isSleeping) {
                // Sleeping closed eyes: gentle curved arcs `u u`
                val leftEyePath = Path().apply {
                    moveTo(centerX - eyeSpacing - 7.dp.toPx(), eyeY)
                    quadraticTo(centerX - eyeSpacing, eyeY + 6.dp.toPx(), centerX - eyeSpacing + 7.dp.toPx(), eyeY)
                }
                val rightEyePath = Path().apply {
                    moveTo(centerX + eyeSpacing - 7.dp.toPx(), eyeY)
                    quadraticTo(centerX + eyeSpacing, eyeY + 6.dp.toPx(), centerX + eyeSpacing + 7.dp.toPx(), eyeY)
                }
                drawPath(leftEyePath, DeepEspresso, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightEyePath, DeepEspresso, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                // Little floating zZz symbol
                val zPath = Path().apply {
                    moveTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 14.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 14.dp.toPx())
                }
                drawPath(zPath, SageAccent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            } else if (isBlinking || petBounceTrigger > 1.1f) {
                // Happy curved eyes `^ ^`
                val leftHappy = Path().apply {
                    moveTo(centerX - eyeSpacing - 7.dp.toPx(), eyeY + 2.dp.toPx())
                    quadraticTo(centerX - eyeSpacing, eyeY - 6.dp.toPx(), centerX - eyeSpacing + 7.dp.toPx(), eyeY + 2.dp.toPx())
                }
                val rightHappy = Path().apply {
                    moveTo(centerX + eyeSpacing - 7.dp.toPx(), eyeY + 2.dp.toPx())
                    quadraticTo(centerX + eyeSpacing, eyeY - 6.dp.toPx(), centerX + eyeSpacing + 7.dp.toPx(), eyeY + 2.dp.toPx())
                }
                drawPath(leftHappy, DeepEspresso, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightHappy, DeepEspresso, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            } else {
                // Wide, twinkling sparkling eyes
                val eyeRadius = 5.5.dp.toPx()
                // Left eye
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(centerX - eyeSpacing - 1.5.dp.toPx(), eyeY - 1.5.dp.toPx()))
                // Right eye
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(centerX + eyeSpacing - 1.5.dp.toPx(), eyeY - 1.5.dp.toPx()))
            }

            // 7. Cheerful Mouth
            val mouthY = centerY + 8.dp.toPx()
            if (state.isSleeping) {
                // Sleeping small gentle mouth `.`
                drawCircle(color = DeepEspresso, radius = 1.8.dp.toPx(), center = Offset(centerX, mouthY))
            } else if (lifeScore >= 75 || petBounceTrigger > 1.1f) {
                // Big joyful open smile :D
                val mouthPath = Path().apply {
                    moveTo(centerX - 5.dp.toPx(), mouthY)
                    quadraticTo(centerX, mouthY + 7.dp.toPx(), centerX + 5.dp.toPx(), mouthY)
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            } else {
                // Gentle content smile :)
                val mouthPath = Path().apply {
                    moveTo(centerX - 4.dp.toPx(), mouthY)
                    quadraticTo(centerX, mouthY + 4.dp.toPx(), centerX + 4.dp.toPx(), mouthY)
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }

            // 8. Petting Heart Particles (when petted)
            if (petBounceTrigger > 1.05f) {
                drawCircle(
                    color = CoralPrimary.copy(alpha = 0.85f),
                    radius = 4.dp.toPx(),
                    center = Offset(centerX - 28.dp.toPx(), centerY - 28.dp.toPx())
                )
                drawCircle(
                    color = AmberTertiary.copy(alpha = 0.9f),
                    radius = 3.dp.toPx(),
                    center = Offset(centerX + 30.dp.toPx(), centerY - 24.dp.toPx())
                )
            }
        }
    }
}
