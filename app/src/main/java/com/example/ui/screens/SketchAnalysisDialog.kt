package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MockUiLayout
import com.example.data.remote.GeminiSketchAnalysisService
import com.example.ui.components.MockUiRepresentationView
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppSurface
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextSecondary
import com.example.utils.SketchLayoutParser
import kotlinx.coroutines.launch
import java.io.File

sealed class SketchAnalysisUiState {
    object Idle : SketchAnalysisUiState()
    data class Loading(val message: String = "Mengirim sketsa ke Gemini API...") : SketchAnalysisUiState()
    data class Success(val layout: MockUiLayout, val isSaved: Boolean = false) : SketchAnalysisUiState()
    data class Error(val message: String, val fallbackLayout: MockUiLayout? = null) : SketchAnalysisUiState()
}

@Composable
fun SketchAnalysisDialog(
    photoFile: File?,
    onDismiss: () -> Unit,
    onSaveToGallery: ((MockUiLayout, String, File?) -> Unit)? = null,
    onOpenGallery: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    geminiService: GeminiSketchAnalysisService = remember { GeminiSketchAnalysisService() }
) {
    var uiState by remember { mutableStateOf<SketchAnalysisUiState>(SketchAnalysisUiState.Idle) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun runAnalysis() {
        if (photoFile == null || !photoFile.exists()) {
            // Provide a sample parsed wireframe layout if no photo exists
            val sampleLayout = SketchLayoutParser.parseSketchLayoutDescription(SAMPLE_SKETCH_DESCRIPTION)
            uiState = SketchAnalysisUiState.Success(sampleLayout)
            onSaveToGallery?.invoke(sampleLayout, sampleLayout.rawDescription, photoFile)
            return
        }

        uiState = SketchAnalysisUiState.Loading("Menganalisis sketsa antarmuka dengan Gemini 3.5 Flash...")
        scope.launch {
            val result = geminiService.analyzeAndParseSketch(photoFile)
            result.onSuccess { layout ->
                uiState = SketchAnalysisUiState.Success(layout)
                onSaveToGallery?.invoke(layout, layout.rawDescription, photoFile)
            }.onFailure { error ->
                val fallback = SketchLayoutParser.parseSketchLayoutDescription(SAMPLE_SKETCH_DESCRIPTION)
                uiState = SketchAnalysisUiState.Error(
                    message = error.message ?: "Gagal menganalisis sketsa.",
                    fallbackLayout = fallback
                )
            }
        }
    }

    LaunchedEffect(photoFile) {
        runAnalysis()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
            color = AppBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Gemini UI Sketch Analyzer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (onOpenGallery != null) {
                            IconButton(
                                onClick = {
                                    onDismiss()
                                    onOpenGallery()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CollectionsBookmark,
                                    contentDescription = "Galeri Desain",
                                    tint = TechCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Content View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = uiState,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "analysis_state_anim"
                    ) { state ->
                        when (state) {
                            is SketchAnalysisUiState.Loading -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = TechCyan,
                                        modifier = Modifier.size(44.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Mengekstraksi hierarki layout, container, dan kontrol interaktif...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            is SketchAnalysisUiState.Success -> {
                                MockUiRepresentationView(
                                    mockLayout = state.layout,
                                    onBackClick = onDismiss,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            is SketchAnalysisUiState.Error -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Analisis Sketch Membutuhkan API Key",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { runAnalysis() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.size(4.dp))
                                            Text("Coba Lagi", fontSize = 12.sp)
                                        }

                                        if (state.fallbackLayout != null) {
                                            Button(
                                                onClick = {
                                                    uiState = SketchAnalysisUiState.Success(state.fallbackLayout)
                                                    onSaveToGallery?.invoke(state.fallbackLayout, state.fallbackLayout.rawDescription, photoFile)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = TechCyan,
                                                    contentColor = Color(0xFF071926)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Lihat Preview Mock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

private const val SAMPLE_SKETCH_DESCRIPTION = """
1. **Screen Type & Objective**: Real-time Occupancy & Monitoring Dashboard
2. **Top Navigation & App Bar**: Header title "Smart Facility Monitor", action icons for Search, Alert Notifications, and Settings.
3. **Primary Layout Containers**:
   - Hero KPI Metrics Container: Active Capacity (84%), Total Footfall (1,420 entries), Safety Rating (99.2%).
   - Facility Zone Grid: Zone A Lobby, Zone B Hallway, Zone C Auditorium.
4. **Interactive Elements & Controls**:
   - Filter Chips: All Zones, High Density, Alert Triggers.
   - Real-time Detection Toggle: Enable AI Auto Tracking.
   - Action Buttons: Generate Instant PDF Report, Export Analytics CSV.
5. **Suggested Compose Layout Hierarchy**: Scaffold -> TopAppBar -> Column (verticalScroll) -> Row(Hero Cards) -> Zone Grid -> Actions Row.
"""
