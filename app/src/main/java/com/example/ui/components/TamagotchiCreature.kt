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
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityError
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TamagotchiCreature(
    state: TamagotchiState,
    onPet: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TamagotchiIdle")

    val isSickOrSad = state.mood == TamagotchiMood.SICK ||
        state.mood == TamagotchiMood.SAD ||
        state.mood == TamagotchiMood.TIRED

    val isHopeful = state.mood == TamagotchiMood.HOPEFUL

    // Slow, subdued motion when sick/sad; lively floating motion when healthy/hopeful
    val floatAmplitude = when {
        state.mood == TamagotchiMood.SICK -> 2.5f
        state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> 4f
        isHopeful -> 8f
        else -> 11f
    }

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -floatAmplitude,
        targetValue = floatAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    // Gentle aura breathing scale (weak when sick)
    val auraScale by infiniteTransition.animateFloat(
        initialValue = if (isSickOrSad) 0.82f else 0.92f,
        targetValue = if (isSickOrSad) 0.90f else 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    // Orbital bio-resonance angle for living satellite motes
    val orbitAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSickOrSad) 18000 else 9500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
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

            // 1. Aura Halo Bloom (Reflecting bio-responsive state)
            val lifeScore = state.overallLifeScore
            val auraColor = when {
                state.isSleeping -> Color(0xFFCCC2DC)
                state.mood == TamagotchiMood.SICK -> Color(0xFF9E9B95)
                state.mood == TamagotchiMood.SAD -> Color(0xFF8DA3B8)
                state.mood == TamagotchiMood.TIRED -> Color(0xFFB39F93)
                state.mood == TamagotchiMood.HOPEFUL -> MintSecondary
                lifeScore >= 80 -> CoralPrimary
                lifeScore >= 60 -> MintSecondary
                else -> AmberTertiary
            }

            val auraAlpha = when {
                state.isSleeping -> 0.15f
                state.mood == TamagotchiMood.SICK -> 0.12f
                state.mood == TamagotchiMood.SAD -> 0.16f
                state.mood == TamagotchiMood.HOPEFUL -> 0.28f
                else -> 0.34f
            }

            val auraRadius = (canvasW * 0.44f) * auraScale * animatedBounceScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = auraAlpha),
                        auraColor.copy(alpha = auraAlpha * 0.35f),
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
                        if (isSickOrSad) auraColor.copy(alpha = 0.10f) else MintSecondary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerAuraRadius
                ),
                radius = innerAuraRadius,
                center = Offset(centerX, centerY)
            )

            // 1b. Levitation Ground Shadow (scales inversely with floatOffset for 3D depth)
            val shadowY = (canvasH / 2f) + (canvasH * 0.34f)
            val shadowScale = (1f - (floatOffset / 28f)).coerceIn(0.75f, 1.15f)
            drawOval(
                color = DeepEspresso.copy(alpha = 0.08f * shadowScale),
                topLeft = Offset(centerX - (28.dp.toPx() * shadowScale), shadowY - 5.dp.toPx()),
                size = Size(56.dp.toPx() * shadowScale, 10.dp.toPx() * shadowScale)
            )

            // 1c. Orbital Bio-Resonance Motes (4 living satellites representing Health, Tasks, Finance, Social)
            val orbitRadius = canvasW * 0.37f
            val moteColors = if (state.mood == TamagotchiMood.SICK) {
                listOf(SerenityError, AmberTertiary, Color(0xFF9E9B95), SerenityError)
            } else if (state.mood == TamagotchiMood.SAD) {
                listOf(Color(0xFF8DA3B8), MintSecondary.copy(alpha = 0.6f), AmberTertiary.copy(alpha = 0.6f), SerenityError)
            } else {
                listOf(MintSecondary, SageAccent, AmberTertiary, CoralPrimary)
            }
            moteColors.forEachIndexed { idx, moteColor ->
                val rad = Math.toRadians((orbitAngleDeg + idx * 90f).toDouble())
                val mx = centerX + (orbitRadius * cos(rad)).toFloat()
                val my = centerY + (orbitRadius * 0.68f * sin(rad)).toFloat()
                drawCircle(
                    color = moteColor.copy(alpha = if (isSickOrSad) 0.35f else 0.75f),
                    radius = if (idx % 2 == 0) 4.dp.toPx() else 3.dp.toPx(),
                    center = Offset(mx, my)
                )
            }

            // 2. Celestial Halo / Crown (Accessory - dims when sick)
            if (state.equippedAccessory.contains("Halo") || state.equippedAccessory.contains("Crown")) {
                val haloY = centerY - (canvasH * 0.28f) * animatedBounceScale
                val haloAlpha = if (isSickOrSad) 0.35f else 1.0f
                drawOval(
                    brush = Brush.horizontalGradient(
                        listOf(
                            AmberTertiary.copy(alpha = haloAlpha),
                            CoralHoverGlow.copy(alpha = haloAlpha),
                            AmberTertiary.copy(alpha = haloAlpha)
                        )
                    ),
                    topLeft = Offset(centerX - 36.dp.toPx(), haloY - 8.dp.toPx()),
                    size = Size(72.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = haloAlpha),
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX, haloY - 8.dp.toPx())
                )
            }

            // 3. Cute Wisps / Ears on sides (droop lower when sick or sad)
            val earSpread = 38.dp.toPx() * animatedBounceScale
            val earDroop = if (isSickOrSad) 10.dp.toPx() else -16.dp.toPx()
            val earY = centerY + earDroop
            val earColors = if (isSickOrSad) {
                listOf(Color(0xFFD5C8C4), Color(0xFFB5A4A0))
            } else if (isHopeful) {
                listOf(MintSecondary, CoralHoverGlow)
            } else {
                listOf(CoralHoverGlow, CoralPrimary)
            }

            // Left Ear Wisp
            drawCircle(
                brush = Brush.radialGradient(
                    earColors,
                    center = Offset(centerX - earSpread, earY),
                    radius = 18.dp.toPx()
                ),
                radius = 16.dp.toPx() * animatedBounceScale,
                center = Offset(centerX - earSpread, earY)
            )
            // Right Ear Wisp
            drawCircle(
                brush = Brush.radialGradient(
                    earColors,
                    center = Offset(centerX + earSpread, earY),
                    radius = 18.dp.toPx()
                ),
                radius = 16.dp.toPx() * animatedBounceScale,
                center = Offset(centerX + earSpread, earY)
            )

            // 4. Main Creature Body (Pale/wan when sick, soft pearlescent blush when healthy)
            val bodyRadius = (canvasW * 0.26f) * animatedBounceScale
            val bodyGradientColors = when {
                state.mood == TamagotchiMood.SICK -> listOf(
                    Color(0xFFF7F5F2),
                    Color(0xFFE9E4DD),
                    Color(0xFFD8D0C5)
                )
                state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> listOf(
                    Color(0xFFF8F7FA),
                    Color(0xFFE8ECF2),
                    Color(0xFFD7DEE8)
                )
                else -> listOf(
                    Color.White,
                    Color(0xFFFFF0EC),
                    Color(0xFFFFDAD5)
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = bodyGradientColors,
                    center = Offset(centerX - 8.dp.toPx(), centerY - 10.dp.toPx()),
                    radius = bodyRadius
                ),
                radius = bodyRadius,
                center = Offset(centerX, centerY)
            )

            // Subtle body outline for soft tactile definition
            drawCircle(
                color = if (isSickOrSad) Color(0xFF9E9490).copy(alpha = 0.35f) else CoralPrimary.copy(alpha = 0.25f),
                radius = bodyRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Cheeks (Coral blush when healthy/hopeful; faint fever/wan tint when sick)
            val cheekOffset = 18.dp.toPx() * animatedBounceScale
            val cheekY = centerY + 8.dp.toPx()
            val cheekColor = when {
                state.mood == TamagotchiMood.SICK -> AmberTertiary.copy(alpha = 0.28f)
                state.mood == TamagotchiMood.SAD -> Color(0xFF8DA3B8).copy(alpha = 0.25f)
                else -> CoralPrimary.copy(alpha = 0.55f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cheekColor, Color.Transparent),
                    center = Offset(centerX - cheekOffset, cheekY),
                    radius = 10.dp.toPx()
                ),
                radius = 9.dp.toPx(),
                center = Offset(centerX - cheekOffset, cheekY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cheekColor, Color.Transparent),
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

                val zPath = Path().apply {
                    moveTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 14.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 14.dp.toPx())
                }
                drawPath(zPath, SageAccent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            } else if (isSickOrSad) {
                // Worried / tired eyes with angled eyebrows and sweat/tear drop
                val eyeRadius = 4.8.dp.toPx()
                drawCircle(color = DeepEspresso.copy(alpha = 0.85f), radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY + 2.dp.toPx()))
                drawCircle(color = DeepEspresso.copy(alpha = 0.85f), radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY + 2.dp.toPx()))

                // Worried inner-slanted brows
                val leftBrow = Path().apply {
                    moveTo(centerX - eyeSpacing - 7.dp.toPx(), eyeY - 4.dp.toPx())
                    lineTo(centerX - eyeSpacing + 5.dp.toPx(), eyeY - 8.dp.toPx())
                }
                val rightBrow = Path().apply {
                    moveTo(centerX + eyeSpacing - 5.dp.toPx(), eyeY - 8.dp.toPx())
                    lineTo(centerX + eyeSpacing + 7.dp.toPx(), eyeY - 4.dp.toPx())
                }
                drawPath(leftBrow, DeepEspresso.copy(alpha = 0.75f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightBrow, DeepEspresso.copy(alpha = 0.75f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

                // Little sweat / tear droplet on temple
                val dropX = centerX + eyeSpacing + 13.dp.toPx()
                val dropY = eyeY - 2.dp.toPx()
                drawCircle(
                    color = Color(0xFF63B3ED),
                    radius = 3.5.dp.toPx(),
                    center = Offset(dropX, dropY)
                )
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
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(centerX - eyeSpacing - 1.5.dp.toPx(), eyeY - 1.5.dp.toPx()))
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(centerX + eyeSpacing - 1.5.dp.toPx(), eyeY - 1.5.dp.toPx()))
            }

            // 7. Expressive Mouth
            val mouthY = centerY + 10.dp.toPx()
            if (state.isSleeping) {
                drawCircle(color = DeepEspresso, radius = 1.8.dp.toPx(), center = Offset(centerX, mouthY))
            } else if (isSickOrSad) {
                // Downturned worried/sad mouth
                val sadMouth = Path().apply {
                    moveTo(centerX - 6.dp.toPx(), mouthY + 3.dp.toPx())
                    quadraticTo(centerX, mouthY - 4.dp.toPx(), centerX + 6.dp.toPx(), mouthY + 3.dp.toPx())
                }
                drawPath(sadMouth, DeepEspresso, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            } else if (lifeScore >= 75 || isHopeful || petBounceTrigger > 1.1f) {
                // Big joyful open smile :D
                val mouthPath = Path().apply {
                    moveTo(centerX - 5.dp.toPx(), mouthY - 2.dp.toPx())
                    quadraticTo(centerX, mouthY + 6.dp.toPx(), centerX + 5.dp.toPx(), mouthY - 2.dp.toPx())
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            } else {
                val mouthPath = Path().apply {
                    moveTo(centerX - 4.dp.toPx(), mouthY)
                    quadraticTo(centerX, mouthY + 4.dp.toPx(), centerX + 4.dp.toPx(), mouthY)
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }

            // 8. Bio-Resonance Heart Crystal on Chest + Floating Paws
            val crystalY = centerY + (bodyRadius * 0.56f)
            val crystalColor = when (state.mood) {
                TamagotchiMood.SICK -> SerenityError
                TamagotchiMood.SAD, TamagotchiMood.TIRED -> Color(0xFF8DA3B8)
                TamagotchiMood.HOPEFUL -> MintSecondary
                else -> CoralPrimary
            }
            drawCircle(
                color = crystalColor.copy(alpha = 0.25f * auraScale),
                radius = 7.dp.toPx(),
                center = Offset(centerX, crystalY)
            )
            drawCircle(
                color = crystalColor,
                radius = 3.5.dp.toPx(),
                center = Offset(centerX, crystalY)
            )

            // Cute little floating hands/paws on either side of the body
            val pawSpread = bodyRadius * 0.88f
            val pawY = if (isSickOrSad) centerY + (bodyRadius * 0.38f) else centerY + (bodyRadius * 0.16f)
            val pawColor = if (isSickOrSad) Color(0xFFE0D7D0) else Color(0xFFFFE5DF)
            drawCircle(
                color = pawColor,
                radius = 6.5.dp.toPx() * animatedBounceScale,
                center = Offset(centerX - pawSpread, pawY)
            )
            drawCircle(
                color = pawColor,
                radius = 6.5.dp.toPx() * animatedBounceScale,
                center = Offset(centerX + pawSpread, pawY)
            )

            // 9. Petting / State Particles (when petted, hopeful, or radiant)
            if (petBounceTrigger > 1.05f || isHopeful) {
                drawCircle(
                    color = CoralPrimary.copy(alpha = 0.85f),
                    radius = 4.dp.toPx(),
                    center = Offset(centerX - 28.dp.toPx(), centerY - 28.dp.toPx())
                )
                drawCircle(
                    color = MintSecondary.copy(alpha = 0.9f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX + 30.dp.toPx(), centerY - 24.dp.toPx())
                )
            }
        }
    }
}