package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuminousSerenityColorScheme = lightColorScheme(
    primary = SerenityCoralPrimary,
    onPrimary = SerenityOnPrimaryCoral,
    primaryContainer = SerenityPrimaryContainerCoral,
    onPrimaryContainer = SerenityOnPrimaryContainerCoral,
    secondary = SerenitySageAccent,
    onSecondary = SerenityOnPrimaryCoral,
    secondaryContainer = SerenityMintContainer,
    onSecondaryContainer = SerenityDarkSageText,
    tertiary = SerenityAmberTertiary,
    onTertiary = SerenityDeepEspresso,
    tertiaryContainer = SerenityAmberContainer,
    onTertiaryContainer = SerenityOnAmberContainer,
    background = SerenityBaseCanvas,
    onBackground = SerenityDeepEspresso,
    surface = SerenityBaseCanvas,
    onSurface = SerenityDeepEspresso,
    surfaceVariant = SerenitySubtleContainer,
    onSurfaceVariant = SerenityWarmSlateCharcoal,
    outline = SerenityWarmSlateCharcoal,
    outlineVariant = SerenityWarmBorder,
    error = SerenityError,
    onError = SerenityOnError,
    errorContainer = SerenityErrorContainer
)

private val MetaHorizonColorScheme = lightColorScheme(
    primary = MetaPrimaryBlue,
    onPrimary = MetaOnPrimary,
    primaryContainer = MetaPrimaryContainer,
    onPrimaryContainer = MetaOnPrimaryContainer,
    secondary = MetaCyanSecondary,
    onSecondary = MetaOnSecondary,
    secondaryContainer = MetaCyanContainer,
    onSecondaryContainer = MetaDarkCyanText,
    tertiary = MetaHorizonViolet,
    onTertiary = Color.White,
    tertiaryContainer = MetaAmberContainer,
    onTertiaryContainer = MetaOnAmberContainer,
    background = MetaBaseCanvas,
    onBackground = MetaDeepCharcoal,
    surface = MetaBaseCanvas,
    onSurface = MetaDeepCharcoal,
    surfaceVariant = MetaSubtleContainer,
    onSurfaceVariant = MetaSlateText,
    outline = MetaSlateText,
    outlineVariant = MetaBorder,
    error = SerenityError,
    onError = SerenityOnError,
    errorContainer = SerenityErrorContainer
)

@Composable
fun SerenityTheme(
    themeMode: AppThemeMode = ThemeState.currentTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ThemeState.currentTheme = themeMode
    val colorScheme = when (themeMode) {
        AppThemeMode.LUMINOUS_SERENITY -> LuminousSerenityColorScheme
        AppThemeMode.META_HORIZON -> MetaHorizonColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SerenityTypography,
        content = content
    )
}
