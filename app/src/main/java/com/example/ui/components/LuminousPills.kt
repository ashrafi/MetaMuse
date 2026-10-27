package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.MintTranslucentFill
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal

/**
 * Primary Pill Button: Vibrant warm coral (#FF6F61), full pill radius, coral ambient glow shadow
 */
@Composable
fun SerenityPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "serenity_primary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 48.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = CoralPrimary.copy(alpha = 0.35f),
                spotColor = CoralPrimary.copy(alpha = 0.45f)
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = CoralPrimary,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}

/**
 * Secondary Pill Button: Translucent mint-tinted fill (rgba(78, 205, 196, 0.12)), text in dark sage
 */
@Composable
fun SerenitySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "serenity_secondary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MintTranslucentFill,
            contentColor = SageAccent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SageAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = DarkSageText
            )
        )
    }
}

/**
 * Status Chip with pulsing dot: e.g. "Harmonic Presence • Attuned"
 */
@Composable
fun SerenityStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    pulseColor: Color = MintSecondary,
    backgroundColor: Color = SubtleContainer,
    textColor: Color = DeepEspresso
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusPulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = modifier
            .border(1.dp, WarmBorder, CircleShape)
            .padding(horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(pulseColor.copy(alpha = alphaAnim))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    letterSpacing = 0.02.sp
                )
            )
        }
    }
}

/**
 * Filter / Category Pill Chip
 */
@Composable
fun SerenityPillChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "serenity_pill_chip"
) {
    val background = if (isSelected) CoralPrimary else SubtleContainer
    val contentColor = if (isSelected) Color.White else WarmSlateCharcoal
    val borderModifier = if (isSelected) {
        Modifier
    } else {
        Modifier.border(1.dp, WarmBorder, CircleShape)
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 36.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        )
    }
}

/**
 * Serenity Card: Crisp White against canvas, 32dp corner radius, soft warm boundary
 */
@Composable
fun SerenityCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val cardModifier = if (onClick != null) {
        modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = CoralPrimary.copy(alpha = 0.08f),
                spotColor = WarmSlateCharcoal.copy(alpha = 0.05f)
            )
            .clip(shape)
            .background(FloatingSurface)
            .border(1.dp, WarmBorder, shape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = CoralPrimary.copy(alpha = 0.08f),
                spotColor = WarmSlateCharcoal.copy(alpha = 0.05f)
            )
            .clip(shape)
            .background(FloatingSurface)
            .border(1.dp, WarmBorder, shape)
    }

    Box(modifier = cardModifier) {
        content()
    }
}
