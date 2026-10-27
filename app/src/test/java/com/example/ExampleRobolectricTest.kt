package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SerenityRepository
import com.example.model.PresenceMode
import com.example.model.ScreenNav
import com.example.viewmodel.SerenityViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Serenity", appName)
  }

  @Test
  fun `repository initial data and message sending`() {
    val repo = SerenityRepository()
    val initialMsgs = repo.messages.value
    assertTrue(initialMsgs.isNotEmpty())

    repo.sendMessage("I need a calm breath reset")
    val updatedMsgs = repo.messages.value
    assertEquals(initialMsgs.size + 2, updatedMsgs.size)
    val companionReply = updatedMsgs.last()
    assertEquals(false, companionReply.isFromUser)
    assertNotNull(companionReply.content)
  }

  @Test
  fun `viewModel screen navigation and presence mode`() {
    val viewModel = SerenityViewModel()
    assertEquals(ScreenNav.ATTUNE, viewModel.currentScreen.value)

    viewModel.navigateTo(ScreenNav.DIALOGUE)
    assertEquals(ScreenNav.DIALOGUE, viewModel.currentScreen.value)

    viewModel.setPresenceMode(PresenceMode.CREATIVE_MUSE)
    assertEquals(PresenceMode.CREATIVE_MUSE, viewModel.selectedMode.value)
  }

  @Test
  fun `tamagotchi state reflects life care actions`() {
    val repo = SerenityRepository()
    val initialTamagotchi = repo.tamagotchiState.value
    assertEquals("Lumi", initialTamagotchi.name)
    assertTrue(initialTamagotchi.overallLifeScore > 70)

    val initialHydration = initialTamagotchi.hydration
    val initialXp = initialTamagotchi.xp

    repo.nurtureWater()
    val afterWater = repo.tamagotchiState.value
    assertTrue(afterWater.hydration >= initialHydration)
    assertTrue(afterWater.xp > initialXp)

    val initialPets = afterWater.totalPets
    repo.petTamagotchi()
    assertEquals(initialPets + 1, repo.tamagotchiState.value.totalPets)
  }

  @Test
  fun `meta ai token update and muse telemetry transmission`() = runTest {
    val repo = SerenityRepository()
    assertEquals("", repo.metaAiConfig.value.token)
    assertEquals(false, repo.metaAiConfig.value.isConnected)

    repo.updateMetaAiToken("meta_test_token_12345")
    assertEquals("meta_test_token_12345", repo.metaAiConfig.value.token)
    assertEquals(true, repo.metaAiConfig.value.isConnected)

    val advice = repo.sendAllInfoToMuse()
    assertNotNull(advice)
    assertNotNull(advice.headline)
    assertTrue(advice.pillars.isNotEmpty())
    assertNotNull(repo.latestMuseAdvice.value)
  }

  @Test
  fun `integration toggles update life sync state`() {
    val repo = SerenityRepository()
    val initialGoogleHealth = repo.integrations.value.googleHealthConnected
    repo.toggleGoogleHealth()
    assertEquals(!initialGoogleHealth, repo.integrations.value.googleHealthConnected)

    val initialFb = repo.integrations.value.facebookConnected
    repo.toggleFacebook()
    assertEquals(!initialFb, repo.integrations.value.facebookConnected)

    val initialFinance = repo.integrations.value.financeAppConnected
    repo.toggleFinanceApp()
    assertEquals(!initialFinance, repo.integrations.value.financeAppConnected)

    val initialTasks = repo.integrations.value.tasksAppConnected
    repo.toggleTasksApp()
    assertEquals(!initialTasks, repo.integrations.value.tasksAppConnected)
  }
}
