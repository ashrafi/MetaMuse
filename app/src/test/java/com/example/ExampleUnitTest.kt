package com.example

import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MetaCyanSecondary
import com.example.ui.theme.MetaHorizonViolet
import com.example.ui.theme.MetaPrimaryBlue
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityCoralPrimary
import com.example.ui.theme.SerenityMintSecondary
import com.example.ui.theme.ThemeState
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testThemeModeSwitching() {
    ThemeState.currentTheme = AppThemeMode.LUMINOUS_SERENITY
    assertEquals(SerenityCoralPrimary, CoralPrimary)
    assertEquals(SerenityMintSecondary, MintSecondary)

    ThemeState.currentTheme = AppThemeMode.META_HORIZON
    assertEquals(MetaPrimaryBlue, CoralPrimary)
    assertEquals(MetaCyanSecondary, MintSecondary)
    assertEquals(MetaHorizonViolet, SageAccent)

    ThemeState.currentTheme = AppThemeMode.LUMINOUS_SERENITY
    assertEquals(SerenityCoralPrimary, CoralPrimary)
  }
}
