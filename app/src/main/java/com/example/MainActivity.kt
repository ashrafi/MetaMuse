package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenNav
import com.example.ui.screens.AttuneScreen
import com.example.ui.screens.PresenceScreen
import com.example.ui.screens.VitalityScreen
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.SerenityTheme
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.WarmBorder
import com.example.viewmodel.SerenityViewModel

class MainActivity : ComponentActivity() {

    private val serenityViewModel: SerenityViewModel by viewModels {
        SerenityViewModel.provideFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.data?.let { serenityViewModel.handleOAuthRedirect(it) }
        setContent {
            val themeMode by serenityViewModel.appThemeMode.collectAsState()
            SerenityTheme(themeMode = themeMode) {
                SerenityMainApp(viewModel = serenityViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { serenityViewModel.handleOAuthRedirect(it) }
    }
}

@Composable
fun SerenityMainApp(viewModel: SerenityViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val isHomeTab = currentScreen == ScreenNav.HOME ||
        currentScreen == ScreenNav.ATTUNE ||
        currentScreen == ScreenNav.DIALOGUE

    // Handle Android system back press to return to Home
    if (!isHomeTab) {
        BackHandler {
            viewModel.navigateTo(ScreenNav.ATTUNE)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BaseCanvas),
        bottomBar = {
            SerenityBottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { target ->
            when (target) {
                ScreenNav.HOME, ScreenNav.ATTUNE, ScreenNav.DIALOGUE -> AttuneScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.DATA, ScreenNav.VITALITY, ScreenNav.VAULT -> VitalityScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.CONNECTIONS, ScreenNav.PRESENCE -> PresenceScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
            }
        }
    }
}

/**
 * 3-Tab Floating Pill Luminous Navigation Bar:
 * 1. Home (Tamagotchi + Proactive Dialogue)
 * 2. Show Data (Health, Tasks, Finance, Social, Values)
 * 3. Connections (Google Health, Tasks, Finance App, Facebook, Meta Key)
 */
@Composable
fun SerenityBottomNavBar(
    currentScreen: ScreenNav,
    onScreenSelected: (ScreenNav) -> Unit
) {
    val navShape = RoundedCornerShape(32.dp)

    val isHomeSelected = currentScreen == ScreenNav.HOME ||
        currentScreen == ScreenNav.ATTUNE ||
        currentScreen == ScreenNav.DIALOGUE
    val isDataSelected = currentScreen == ScreenNav.DATA ||
        currentScreen == ScreenNav.VITALITY ||
        currentScreen == ScreenNav.VAULT
    val isConnectionsSelected = currentScreen == ScreenNav.CONNECTIONS ||
        currentScreen == ScreenNav.PRESENCE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = navShape,
                    ambientColor = CoralPrimary.copy(alpha = 0.16f),
                    spotColor = DeepEspresso.copy(alpha = 0.08f)
                )
                .clip(navShape)
                .background(FloatingSurface.copy(alpha = 0.95f))
                .border(1.dp, WarmBorder, navShape)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("serenity_bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = "Home",
                    icon = Icons.Default.Spa,
                    isSelected = isHomeSelected,
                    onClick = { onScreenSelected(ScreenNav.ATTUNE) },
                    testTag = "nav_home"
                )
                NavBarItem(
                    label = "Show Data",
                    icon = Icons.Default.Analytics,
                    isSelected = isDataSelected,
                    onClick = { onScreenSelected(ScreenNav.DATA) },
                    testTag = "nav_data"
                )
                NavBarItem(
                    label = "Connections",
                    icon = Icons.Default.Hub,
                    isSelected = isConnectionsSelected,
                    onClick = { onScreenSelected(ScreenNav.CONNECTIONS) },
                    testTag = "nav_connections"
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val pillShape = CircleShape
    val background = if (isSelected) CoralPrimary else Color.Transparent
    val contentColor = if (isSelected) Color.White else SoftMutedText

    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(pillShape)
                .background(background)
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}
