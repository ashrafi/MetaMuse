package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
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
import com.example.ui.screens.DialogueScreen
import com.example.ui.screens.PresenceScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.VitalityScreen
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.SerenityTheme
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.viewmodel.SerenityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SerenityTheme {
                SerenityMainApp()
            }
        }
    }
}

@Composable
fun SerenityMainApp(viewModel: SerenityViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle Android system back press to return to Attune (Home)
    if (currentScreen != ScreenNav.ATTUNE) {
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
                ScreenNav.ATTUNE -> AttuneScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.DIALOGUE -> DialogueScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.VITALITY -> VitalityScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.VAULT -> VaultScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
                ScreenNav.PRESENCE -> PresenceScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
            }
        }
    }
}

/**
 * Floating Pill Luminous Navigation Bar with soft tactility & frosted appearance
 */
@Composable
fun SerenityBottomNavBar(
    currentScreen: ScreenNav,
    onScreenSelected: (ScreenNav) -> Unit
) {
    val navShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("serenity_bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = "Tamagotchi",
                    icon = Icons.Default.Spa,
                    isSelected = currentScreen == ScreenNav.ATTUNE,
                    onClick = { onScreenSelected(ScreenNav.ATTUNE) },
                    testTag = "nav_tamagotchi"
                )
                NavBarItem(
                    label = "Dialogue",
                    icon = Icons.Default.GraphicEq,
                    isSelected = currentScreen == ScreenNav.DIALOGUE,
                    onClick = { onScreenSelected(ScreenNav.DIALOGUE) },
                    testTag = "nav_dialogue"
                )
                NavBarItem(
                    label = "Vitality",
                    icon = Icons.Default.SelfImprovement,
                    isSelected = currentScreen == ScreenNav.VITALITY,
                    onClick = { onScreenSelected(ScreenNav.VITALITY) },
                    testTag = "nav_vitality"
                )
                NavBarItem(
                    label = "Vault",
                    icon = Icons.Default.Bookmark,
                    isSelected = currentScreen == ScreenNav.VAULT,
                    onClick = { onScreenSelected(ScreenNav.VAULT) },
                    testTag = "nav_vault"
                )
                NavBarItem(
                    label = "Settings",
                    icon = Icons.Default.Tune,
                    isSelected = currentScreen == ScreenNav.PRESENCE,
                    onClick = { onScreenSelected(ScreenNav.PRESENCE) },
                    testTag = "nav_settings"
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
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
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}
