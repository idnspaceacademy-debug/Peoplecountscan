package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ExportPdfScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SketchGalleryScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppSurface
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.PeopleCountTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PeopleCountViewModel

sealed class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Live : NavItem("live", "Live", Icons.Filled.Sensors, Icons.Outlined.Sensors)
    object Riwayat : NavItem("riwayat", "Riwayat", Icons.Filled.History, Icons.Outlined.History)
    object Analisis : NavItem("analisis", "Analisis", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    object Pengaturan : NavItem("pengaturan", "Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PeopleCountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeopleCountTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: PeopleCountViewModel
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem.Live,
        NavItem.Riwayat,
        NavItem.Analisis,
        NavItem.Pengaturan
    )

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        Scaffold(
            containerColor = AppBackground,
            contentWindowInsets = WindowInsets.navigationBars,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0C1322),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF19253C))
                        .testTag("bottom_nav_bar")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandAccent,
                                unselectedIconColor = TextSecondary,
                                selectedTextColor = BrandAccent,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color(0x1810B981)
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedIndex) {
                    0 -> LiveTrackingScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { selectedIndex = 3 }
                    )
                    1 -> HistoryScreen(viewModel = viewModel)
                    2 -> AnalyticsScreen(
                        viewModel = viewModel,
                        onBack = { selectedIndex = 0 }
                    )
                    3 -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { selectedIndex = 0 }
                    )
                }
            }
        }

        // --- Notifications Overlay Sheet/Screen ---
        AnimatedVisibility(
            visible = uiState.showNotificationsDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = { viewModel.toggleNotificationsDialog(false) }
            )
        }

        // --- Export PDF Full Dialog / Screen ---
        AnimatedVisibility(
            visible = uiState.showExportPdfDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ExportPdfScreen(
                viewModel = viewModel,
                onBack = { viewModel.toggleExportPdfDialog(false) }
            )
        }

        // --- AI Sketch Gallery Screen Overlay ---
        AnimatedVisibility(
            visible = uiState.showSketchGalleryDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SketchGalleryScreen(
                viewModel = viewModel,
                onBack = { viewModel.toggleSketchGallery(false) },
                onOpenCaptureSketch = {
                    viewModel.toggleSketchGallery(false)
                    selectedIndex = 0
                }
            )
        }
    }
}
