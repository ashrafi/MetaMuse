package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LuminousSerenityColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = OnPrimaryCoral,
    primaryContainer = PrimaryContainerCoral,
    onPrimaryContainer = OnPrimaryContainerCoral,
    secondary = SageAccent,
    onSecondary = OnPrimaryCoral,
    secondaryContainer = MintContainer,
    onSecondaryContainer = DarkSageText,
    tertiary = AmberTertiary,
    onTertiary = DeepEspresso,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = BaseCanvas,
    onBackground = DeepEspresso,
    surface = BaseCanvas,
    onSurface = DeepEspresso,
    surfaceVariant = SubtleContainer,
    onSurfaceVariant = WarmSlateCharcoal,
    outline = WarmSlateCharcoal,
    outlineVariant = WarmBorder,
    error = SerenityError,
    onError = SerenityOnError,
    errorContainer = SerenityErrorContainer
)

@Composable
fun SerenityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Luminous Serenity is intentionally warm, radiant, and sunny-blush by design
    MaterialTheme(
        colorScheme = LuminousSerenityColorScheme,
        typography = SerenityTypography,
        content = content
    )
}
