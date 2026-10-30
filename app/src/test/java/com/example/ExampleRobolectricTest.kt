package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.SerenityRepository
import com.example.data.connectors.BankFinanceConnector
import com.example.data.connectors.FacebookSocialConnector
import com.example.model.DemoRecoveryStage
import com.example.model.MuseDemoScenario
import com.example.model.PresenceMode
import com.example.model.ScreenNav
import com.example.model.TamagotchiMood
import com.example.viewmodel.SerenityViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

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
        assertEquals(false, viewModel.showFullHome.value)

        viewModel.setShowFullHome(true)
        assertEquals(true, viewModel.showFullHome.value)

        viewModel.navigateTo(ScreenNav.DATA)
        assertEquals(ScreenNav.DATA, viewModel.currentScreen.value)
        assertEquals(false, viewModel.showFullData.value)

        viewModel.setShowFullData(true)
        assertEquals(true, viewModel.showFullData.value)

        viewModel.navigateTo(ScreenNav.ATTUNE)
        assertEquals(ScreenNav.ATTUNE, viewModel.currentScreen.value)
        assertEquals(false, viewModel.showFullHome.value)

        viewModel.navigateTo(ScreenNav.CONNECTIONS)
        assertEquals(ScreenNav.CONNECTIONS, viewModel.currentScreen.value)
        assertEquals(false, viewModel.showFullConnections.value)

        viewModel.setShowFullConnections(true)
        assertEquals(true, viewModel.showFullConnections.value)

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
    fun `demo 1 success trap transitions from sick to hopeful on agreement and radiant on follow through`() {
        val repo = SerenityRepository()
        repo.activateDemoScenario(MuseDemoScenario.SUCCESS_TRAP)

        assertEquals(TamagotchiMood.SICK, repo.tamagotchiState.value.mood)
        assertTrue(repo.telemetrySnapshot.value.deterministicTriggerActive)
        assertTrue(repo.messages.value.last().content.contains("barely sleeping"))

        // User step 1
        repo.sendMessage("I'm working hard now so I can have a better life later.")
        assertTrue(repo.messages.value.last().content.contains("protect your sleep"))
        assertEquals(TamagotchiMood.SICK, repo.tamagotchiState.value.mood)

        // User step 2 (agreement makes Muse HOPEFUL, not immediately healthy)
        repo.sendMessage("Okay. I'll stop working at midnight and see my friends this weekend.")
        assertEquals("That sounds like a life worth building.", repo.messages.value.last().content)
        assertEquals(TamagotchiMood.HOPEFUL, repo.tamagotchiState.value.mood)
        assertEquals(DemoRecoveryStage.HOPEFUL_COMMITTED, repo.demoRecoveryStage.value)

        // Simulated/verified follow-through makes Muse RADIANT (healthy and happy)
        repo.completeDemoFollowThrough()
        assertEquals(TamagotchiMood.RADIANT, repo.tamagotchiState.value.mood)
        assertEquals(DemoRecoveryStage.RECOVERED_HAPPY, repo.demoRecoveryStage.value)
    }

    @Test
    fun `demo 2 invisible money leak coaching and recovery flow`() {
        val repo = SerenityRepository()
        repo.activateDemoScenario(MuseDemoScenario.INVISIBLE_MONEY_LEAK)

        assertEquals(TamagotchiMood.SICK, repo.tamagotchiState.value.mood)
        assertTrue(repo.messages.value.last().content.contains("Can we talk about your evenings?"))

        repo.sendMessage("I'm just tired when I get home. Ordering food is the only thing I feel like doing.")
        assertTrue(repo.messages.value.last().content.contains("meals that take five minutes"))

        repo.sendMessage("That would help. I'm tired of feeling like I can't save anything.")
        assertTrue(repo.messages.value.last().content.contains("making tomorrow easier"))
        assertEquals(TamagotchiMood.HOPEFUL, repo.tamagotchiState.value.mood)

        repo.completeDemoFollowThrough()
        assertEquals(TamagotchiMood.RADIANT, repo.tamagotchiState.value.mood)
    }

    @Test
    fun `demo 3 connection debt values alignment and recovery flow`() {
        val repo = SerenityRepository()
        repo.activateDemoScenario(MuseDemoScenario.CONNECTION_DEBT)

        assertEquals(TamagotchiMood.SAD, repo.tamagotchiState.value.mood)
        assertTrue(repo.messages.value.last().content.contains("postponed calling your sibling"))

        repo.sendMessage("I've just had other things to do. I'll get around to it.")
        assertTrue(repo.messages.value.last().content.contains("avoiding those calls"))

        repo.sendMessage("Honestly, we haven't talked in so long that now it feels awkward. I don't know what to say.")
        assertTrue(repo.messages.value.last().content.contains("I've missed talking to you"))

        repo.sendMessage("Okay. I'll call tonight.")
        assertTrue(repo.messages.value.last().content.contains("That makes me happy"))
        assertEquals(TamagotchiMood.HOPEFUL, repo.tamagotchiState.value.mood)

        repo.completeDemoFollowThrough()
        assertEquals(TamagotchiMood.RADIANT, repo.tamagotchiState.value.mood)
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

    @Test
    fun `facebook oauth redirect extracts real access token`() {
        val connector = FacebookSocialConnector()
        val uri = Uri.parse("metamuse://oauth/facebook#access_token=EAAB_REAL_TOKEN_999&expires_in=5184000")
        val extracted = connector.extractAccessTokenFromRedirect(uri)
        assertEquals("EAAB_REAL_TOKEN_999", extracted)
    }

    @Test
    fun `bank csv importer parses real statement and flags delivery leak in live mode`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val csvFile = File(context.cacheDir, "real_statement_test.csv")
        csvFile.writeText(
            """
            Date,Description,Amount,Balance
            2026-10-28,Payroll Direct Deposit,3200.00,5410.50
            2026-10-27,DoorDash*ThaiKitchen,-36.40,2210.50
            2026-10-26,Uber Eats Order,-42.10,2246.90
            2026-10-25,Grubhub*BurgerJoint,-31.50,2289.00
            2026-10-24,Instacart Convenience,-48.00,2320.50
            2026-10-23,Postmates Late Night,-29.90,2368.50
            """.trimIndent()
        )

        val repo = SerenityRepository(context)
        assertEquals(MuseDemoScenario.LIVE_REAL_DATA, repo.activeDemoScenario.value)

        repo.importRealBankCsv(Uri.fromFile(csvFile))

        assertTrue(repo.integrations.value.financeAppConnected)
        assertEquals(5, repo.realConnectorState.value.liveDeliveryOrderCount)
        assertEquals(5410.50, repo.realConnectorState.value.liveTotalBalanceUsd ?: 0.0, 0.01)
        assertTrue(repo.telemetrySnapshot.value.financeReport.isAlert)
        assertEquals(TamagotchiMood.SICK, repo.tamagotchiState.value.mood)

        val bankConnector = BankFinanceConnector()
        val restored = bankConnector.restoreCachedCsv(
            context.getSharedPreferences("metamuse_real_connectors_prefs", Context.MODE_PRIVATE)
                .getString("cached_csv_transactions_json", "") ?: ""
        )
        assertNotNull(restored)
        assertEquals(5, restored?.deliveryOrderCount)
    }
}
