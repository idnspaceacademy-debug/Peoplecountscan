package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CameraAiOverlayView
import com.example.ui.components.RadialCapacityGauge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PeopleCountViewModel

@Composable
fun LiveTrackingScreen(
    viewModel: PeopleCountViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadAlertCount.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("live_tracking_screen")
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleNotificationsDialog(true) },
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isSimulating) BrandAccent else Color.Gray)
                )
                Text(
                    text = "Live Tracking",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sketch Gallery Shortcut Button
                IconButton(
                    onClick = { viewModel.toggleSketchGallery(true) },
                    modifier = Modifier.testTag("gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CollectionsBookmark,
                        contentDescription = "Galeri Desain",
                        tint = TechCyan
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleNotificationsDialog(true) },
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = StatusDanger,
                                    contentColor = Color.White
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = Color.White
                    )
                }
            }
        }

        // --- Live AI Camera Feed Component ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            CameraAiOverlayView(
                isDetecting = uiState.isSimulating,
                onOpenGallery = { viewModel.toggleSketchGallery(true) },
                onSaveSketch = { layout, rawDesc, file ->
                    viewModel.saveProcessedSketch(layout, rawDesc, file)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Main Status Card Container ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Badge (AMAN / HAMPIR PENUH / MELEBIHI BATAS)
            StatusBadge(level = uiState.alertLevel)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Kapasitas: ${uiState.capacityLimit} orang",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radial Capacity Circular Gauge
            RadialCapacityGauge(
                currentCount = uiState.currentCount,
                maxCapacity = uiState.capacityLimit,
                level = uiState.alertLevel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Hari Ini Section Header ---
        Text(
            text = "Hari Ini",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // --- Hari Ini 3-Column Stats Row (Masuk, Keluar, Sekarang) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Masuk",
                value = "${uiState.todayMasuk}",
                modifier = Modifier.weight(1f),
                tag = "stat_masuk"
            )

            StatCard(
                title = "Keluar",
                value = "${uiState.todayKeluar}",
                modifier = Modifier.weight(1f),
                tag = "stat_keluar"
            )

            StatCard(
                title = "Sekarang",
                value = "${uiState.currentCount}",
                highlightValue = true,
                modifier = Modifier.weight(1f),
                tag = "stat_sekarang"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Interactive Controller / Quick Simulation Panel ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manual +1 In Button
            FilledTonalButton(
                onClick = { viewModel.manualPersonIn() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("button_manual_in"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF162A48),
                    contentColor = BrandAccent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Manual -1 Out Button
            FilledTonalButton(
                onClick = { viewModel.manualPersonOut() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("button_manual_out"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF162A48),
                    contentColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Pause/Play AI Simulation Stream
            OutlinedButton(
                onClick = { viewModel.toggleSimulation() },
                modifier = Modifier
                    .height(44.dp)
                    .testTag("button_toggle_sim"),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AppCardBorder)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (uiState.isSimulating) BrandAccent else TextSecondary
                )
            ) {
                Icon(
                    imageVector = if (uiState.isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Toggle Simulasi",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Reset counter
            OutlinedButton(
                onClick = { viewModel.resetLiveCount() },
                modifier = Modifier
                    .height(44.dp)
                    .testTag("button_reset_count"),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AppCardBorder)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Count",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
