package com.example.ui.components

import androidx.camera.core.CameraSelector
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MockUiLayout
import com.example.ui.screens.SketchAnalysisDialog
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.TechCyan
import java.io.File

data class TrackedPerson(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    val width: Float,
    val height: Float,
    val confidence: Int,
    val isEntering: Boolean,
    val speed: Float
)

@Composable
fun CameraAiOverlayView(
    modifier: Modifier = Modifier,
    isDetecting: Boolean = true,
    onOpenGallery: (() -> Unit)? = null,
    onSaveSketch: ((MockUiLayout, String, File?) -> Unit)? = null
) {
    var useDeviceCamera by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var capturedSketchFile by remember { mutableStateOf<File?>(null) }
    var showSketchAnalysisModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "camera_anim")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Bounding boxes representing people in hallway
    val people = remember {
        listOf(
            TrackedPerson("P-01", 0.08f, 0.35f, 0.16f, 0.45f, 98, isEntering = true, speed = 0.02f),
            TrackedPerson("P-02", 0.28f, 0.28f, 0.17f, 0.50f, 96, isEntering = true, speed = 0.015f),
            TrackedPerson("P-03", 0.52f, 0.30f, 0.16f, 0.48f, 97, isEntering = false, speed = 0.018f),
            TrackedPerson("P-04", 0.74f, 0.32f, 0.18f, 0.52f, 94, isEntering = false, speed = 0.022f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D1629))
            .border(1.dp, Color(0xFF1E2E4E), RoundedCornerShape(14.dp))
            .testTag("camera_ai_overlay")
    ) {
        if (useDeviceCamera) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraSelector = cameraSelector,
                onPhotoCaptured = { file ->
                    capturedSketchFile = file
                    showSketchAnalysisModal = true
                }
            )
        } else {
            // Hallway background photo
            Image(
                painter = painterResource(id = R.drawable.live_hallway_feed),
                contentDescription = "Live Hallway Feed",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark tint gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x70060C18),
                            Color(0x30060C18),
                            Color(0x80060C18)
                        )
                    )
                )
        )

        // Canvas for AI Bounding boxes and reticles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Subtle scanning radar beam line
            val scanY = scanLineY * h
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        BrandAccent.copy(alpha = 0.6f * pulseAlpha),
                        TechCyan.copy(alpha = 0.8f * pulseAlpha),
                        BrandAccent.copy(alpha = 0.6f * pulseAlpha),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(w, scanY),
                strokeWidth = 1.5.dp.toPx()
            )

            // 2. Draw People Bounding Boxes with smart brackets & confidence tags
            people.forEach { person ->
                val boxX = person.initialX * w
                val boxY = person.initialY * h
                val boxW = person.width * w
                val boxH = person.height * h

                val boxColor = if (person.isEntering) BrandAccent else TechCyan

                // Box fill (very soft tint)
                drawRect(
                    color = boxColor.copy(alpha = 0.08f),
                    topLeft = Offset(boxX, boxY),
                    size = Size(boxW, boxH)
                )

                // Corner Brackets for high-tech HUD look
                val cornerLen = 10.dp.toPx()
                val strokeW = 2.dp.toPx()

                // Top-Left Corner
                drawLine(boxColor, Offset(boxX, boxY), Offset(boxX + cornerLen, boxY), strokeW)
                drawLine(boxColor, Offset(boxX, boxY), Offset(boxX, boxY + cornerLen), strokeW)

                // Top-Right Corner
                drawLine(boxColor, Offset(boxX + boxW, boxY), Offset(boxX + boxW - cornerLen, boxY), strokeW)
                drawLine(boxColor, Offset(boxX + boxW, boxY), Offset(boxX + boxW, boxY + cornerLen), strokeW)

                // Bottom-Left Corner
                drawLine(boxColor, Offset(boxX, boxY + boxH), Offset(boxX + cornerLen, boxY + boxH), strokeW)
                drawLine(boxColor, Offset(boxX, boxY + boxH), Offset(boxX, boxY + boxH - cornerLen), strokeW)

                // Bottom-Right Corner
                drawLine(boxColor, Offset(boxX + boxW, boxY + boxH), Offset(boxX + boxW - cornerLen, boxY + boxH), strokeW)
                drawLine(boxColor, Offset(boxX + boxW, boxY + boxH), Offset(boxX + boxW, boxY + boxH - cornerLen), strokeW)

                // Label pill above head
                val tagWidth = 64.dp.toPx()
                val tagHeight = 14.dp.toPx()
                val tagX = boxX
                val tagY = (boxY - tagHeight - 2.dp.toPx()).coerceAtLeast(2.dp.toPx())

                drawRoundRect(
                    color = Color(0xCC0B1324),
                    topLeft = Offset(tagX, tagY),
                    size = Size(tagWidth, tagHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(tagX, tagY),
                    size = Size(tagWidth, tagHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Label text inside tag
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 7.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val dirArrow = if (person.isEntering) "IN ➔" else "OUT ➔"
                val labelText = "${person.id} ${person.confidence}%"
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    tagX + 4.dp.toPx(),
                    tagY + 10.dp.toPx(),
                    labelPaint
                )
            }
        }

        // Top-Left Live Camera Badge & Source Toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC09101D))
                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
                .clickable { useDeviceCamera = !useDeviceCamera }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (useDeviceCamera) TechCyan.copy(alpha = pulseAlpha) else BrandAccent.copy(alpha = pulseAlpha))
            )
            Text(
                text = if (useDeviceCamera) "DEVICE CAM • LIVE FEED" else "CAM-01 • LOBBY ENTRANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Ganti Sumber Kamera",
                tint = if (useDeviceCamera) TechCyan else BrandAccent,
                modifier = Modifier.size(12.dp)
            )
        }

        // Top-Right AI Mode & Camera Switch Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (useDeviceCamera) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC09101D))
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
                        .clickable {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = TechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC09101D))
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = BrandAccent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "AI YOLOv8 • 30 FPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandAccent
                    )
                )
            }

            // Gemini UI Sketch Analyzer trigger button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC09101D))
                    .border(1.dp, TechCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .clickable {
                        showSketchAnalysisModal = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analyze Sketch",
                        tint = TechCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AI Sketch Mock",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    )
                }
            }

            // Quick Gallery Icon Button
            if (onOpenGallery != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC09101D))
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
                        .clickable { onOpenGallery() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = "Galeri",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Galeri",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA78BFA)
                            )
                        )
                    }
                }
            }
        }
    }

    if (showSketchAnalysisModal) {
        SketchAnalysisDialog(
            photoFile = capturedSketchFile,
            onDismiss = { showSketchAnalysisModal = false },
            onSaveToGallery = onSaveSketch,
            onOpenGallery = onOpenGallery
        )
    }
}
