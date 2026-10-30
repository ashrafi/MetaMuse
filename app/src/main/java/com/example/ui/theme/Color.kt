package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val subtitle: String) {
    LUMINOUS_SERENITY(
        title = "Luminous Serenity",
        subtitle = "Warm coral, organic mint & radiant amber"
    ),
    META_HORIZON(
        title = "Meta Horizon",
        subtitle = "Meta Blue (#0866FF), Electric Cyan & Meta AI Violet"
    )
}

// =========================================================================
// 1. LUMINOUS SERENITY PALETTE (Original)
// =========================================================================
val SerenityCoralPrimary = Color(0xFFFF6F61)
val SerenityCoralHoverGlow = Color(0xFFFF8A7A)
val SerenityCoralActive = Color(0xFFFF7B6B)
val SerenityDeepCoral = Color(0xFFAC332A)
val SerenityOnPrimaryCoral = Color(0xFFFFFFFF)
val SerenityPrimaryContainerCoral = Color(0xFFFFDAD5)
val SerenityOnPrimaryContainerCoral = Color(0xFF410001)

val SerenityMintSecondary = Color(0xFF4ECDC4)
val SerenitySageAccent = Color(0xFF3EB489)
val SerenityMintContainer = Color(0xFF79F3EA)
val SerenityOnMintSecondary = Color(0xFF003734)
val SerenityDarkSageText = Color(0xFF00504C)
val SerenityMintTranslucentFill = Color(0x1F4ECDC4)

val SerenityAmberTertiary = Color(0xFFFBBF24)
val SerenityAmberDeep = Color(0xFFC79500)
val SerenityAmberContainer = Color(0xFFFFDF9F)
val SerenityOnAmberContainer = Color(0xFF453100)

val SerenityDeepEspresso = Color(0xFF2C2023)
val SerenityWarmSlateCharcoal = Color(0xFF4A3E42)
val SerenitySoftMutedText = Color(0xFF756569)

val SerenityBaseCanvas = Color(0xFFFFF8F6)
val SerenitySubtleContainer = Color(0xFFFFF0EC)
val SerenityRecessedContainer = Color(0xFFFDEDE8)
val SerenityFloatingSurface = Color(0xFFFFFFFF)
val SerenityWarmBorder = Color(0x14FF6F61)
val SerenityWarmBorderStrong = Color(0x33FF6F61)

// =========================================================================
// 2. META HORIZON PALETTE (Meta Official Brand Colors)
// Meta Blue: #0866FF (Primary brand blue)
// Meta Horizon Cyan: #00B2FF / #00C2FF (Vibrant secondary)
// Meta AI Violet: #7B61FF (Signature Meta AI gradient accent)
// Canvas: Clean modern high-tech slate/white canvas
// =========================================================================
val MetaPrimaryBlue = Color(0xFF0866FF)
val MetaBlueHoverGlow = Color(0xFF2E87FF)
val MetaBlueActive = Color(0xFF0053D6)
val MetaDeepNavy = Color(0xFF003882)
val MetaOnPrimary = Color(0xFFFFFFFF)
val MetaPrimaryContainer = Color(0xFFE7F0FF)
val MetaOnPrimaryContainer = Color(0xFF001D4D)

val MetaCyanSecondary = Color(0xFF00B2FF)
val MetaHorizonViolet = Color(0xFF7B61FF)
val MetaCyanContainer = Color(0xFFD8F3FF)
val MetaOnSecondary = Color(0xFF003147)
val MetaDarkCyanText = Color(0xFF004B6E)
val MetaBlueTranslucentFill = Color(0x1A0866FF)

val MetaAmberTertiary = Color(0xFFFFAB00)
val MetaAmberDeep = Color(0xFFD97706)
val MetaAmberContainer = Color(0xFFFFEFD2)
val MetaOnAmberContainer = Color(0xFF4D2800)

val MetaDeepCharcoal = Color(0xFF0F172A)
val MetaSlateText = Color(0xFF334155)
val MetaMutedText = Color(0xFF64748B)

val MetaBaseCanvas = Color(0xFFF3F6FA)
val MetaSubtleContainer = Color(0xFFE7EEF7)
val MetaRecessedContainer = Color(0xFFDCE5F1)
val MetaFloatingSurface = Color(0xFFFFFFFF)
val MetaBorder = Color(0x240866FF)
val MetaBorderStrong = Color(0x400866FF)

// Error & Safety tokens
val SerenityError = Color(0xFFBA1A1A)
val SerenityOnError = Color(0xFFFFFFFF)
val SerenityErrorContainer = Color(0xFFFFDAD6)

// =========================================================================
// 3. REACTIVE THEME STATE & DYNAMIC COLOR RESOLUTION
// =========================================================================
object ThemeState {
    var currentTheme by mutableStateOf(AppThemeMode.LUMINOUS_SERENITY)
}

val CoralPrimary: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaPrimaryBlue else SerenityCoralPrimary

val CoralHoverGlow: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBlueHoverGlow else SerenityCoralHoverGlow

val CoralActive: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBlueActive else SerenityCoralActive

val DeepCoral: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaDeepNavy else SerenityDeepCoral

val OnPrimaryCoral: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaOnPrimary else SerenityOnPrimaryCoral

val PrimaryContainerCoral: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaPrimaryContainer else SerenityPrimaryContainerCoral

val OnPrimaryContainerCoral: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaOnPrimaryContainer else SerenityOnPrimaryContainerCoral

val MintSecondary: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaCyanSecondary else SerenityMintSecondary

val SageAccent: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaHorizonViolet else SerenitySageAccent

val MintContainer: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaCyanContainer else SerenityMintContainer

val OnMintSecondary: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaOnSecondary else SerenityOnMintSecondary

val DarkSageText: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaDarkCyanText else SerenityDarkSageText

val MintTranslucentFill: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBlueTranslucentFill else SerenityMintTranslucentFill

val AmberTertiary: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaAmberTertiary else SerenityAmberTertiary

val AmberDeep: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaAmberDeep else SerenityAmberDeep

val AmberContainer: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaAmberContainer else SerenityAmberContainer

val OnAmberContainer: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaOnAmberContainer else SerenityOnAmberContainer

val DeepEspresso: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaDeepCharcoal else SerenityDeepEspresso

val WarmSlateCharcoal: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaSlateText else SerenityWarmSlateCharcoal

val SoftMutedText: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaMutedText else SerenitySoftMutedText

val BaseCanvas: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBaseCanvas else SerenityBaseCanvas

val SubtleContainer: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaSubtleContainer else SerenitySubtleContainer

val RecessedContainer: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaRecessedContainer else SerenityRecessedContainer

val FloatingSurface: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaFloatingSurface else SerenityFloatingSurface

val WarmBorder: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBorder else SerenityWarmBorder

val WarmBorderStrong: Color
    get() = if (ThemeState.currentTheme == AppThemeMode.META_HORIZON) MetaBorderStrong else SerenityWarmBorderStrong
