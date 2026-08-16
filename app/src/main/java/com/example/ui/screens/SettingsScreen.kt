package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PeopleCountViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: PeopleCountViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var capacitySlider by remember(uiState.capacityLimit) {
        mutableFloatStateOf(uiState.capacityLimit.toFloat())
    }

    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
            .testTag("settings_screen")
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            }
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Kapasitas Ruangan Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Batas Kapasitas Maksimum",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }

                Text(
                    text = "${capacitySlider.roundToInt()} Orang",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandAccent,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Slider(
                value = capacitySlider,
                onValueChange = {
                    capacitySlider = it
                    viewModel.updateMaxCapacity(it.roundToInt())
                },
                valueRange = 20f..300f,
                steps = 27,
                colors = SliderDefaults.colors(
                    thumbColor = BrandAccent,
                    activeTrackColor = BrandAccent,
                    inactiveTrackColor = Color(0xFF1E2F4F)
                ),
                modifier = Modifier.testTag("capacity_slider")
            )

            Text(
                text = "Peringatan akan otomatis menyala jika kapasitas mencapai ${ (capacitySlider * 0.8).roundToInt() } orang (80%) dan status bahaya saat mencapai ${capacitySlider.roundToInt()} orang.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Notifikasi & Alarm Setting ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Pemberitahuan & Peringatan",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sound Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Suara Alarm Peringatan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                }

                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vibration Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Getaran Saat Melebihi Batas",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                }

                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandAccent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Demo Data Reset Action ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Pemeliharaan & Data Demo",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            FilledTonalButton(
                onClick = { viewModel.resetToDemoData(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("reset_demo_data_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF162540),
                    contentColor = BrandAccent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Muat Ulang Data Sampel (25 Mei 2024)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Spesifikasi Aplikasi Card (Directly Matching Screenshot) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F1829))
                .border(1.dp, Color(0xFF1D2F50), RoundedCornerShape(14.dp))
                .padding(16.dp)
                .testTag("app_specs_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = BrandAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Spesifikasi Aplikasi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val specs = listOf(
                "Nama Aplikasi" to "PeopleCount",
                "Versi" to "1.0.0",
                "Ukuran APK" to "± 25 MB",
                "Minimal Android" to "Android 7.0 (API 24)",
                "Teknologi" to "TensorFlow Lite (Offline)",
                "Bahasa" to "Indonesia"
            )

            specs.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Fitur Utama",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            val features = listOf(
                "• Real-time People Tracking",
                "• Notifikasi Kapasitas",
                "• Dashboard Analisis",
                "• Peak Hour",
                "• Export PDF"
            )

            features.forEach { feat ->
                Text(
                    text = feat,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    ),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
