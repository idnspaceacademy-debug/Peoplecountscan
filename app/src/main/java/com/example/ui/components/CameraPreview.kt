package com.example.ui.components

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GridGuideMode {
    RULE_OF_THIRDS,
    FRAME_ONLY,
    DISABLED
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    showGridGuide: Boolean = true,
    showCaptureButton: Boolean = true,
    onPhotoCaptured: ((File) -> Unit)? = null,
    onCameraReady: (() -> Unit)? = null
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var gridMode by remember {
        mutableStateOf(if (showGridGuide) GridGuideMode.RULE_OF_THIRDS else GridGuideMode.DISABLED)
    }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(5f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }
    var capturedSuccessMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Observe real camera zoom state when camera is bound
    DisposableEffect(cameraRef, lifecycleOwner) {
        val camera = cameraRef ?: return@DisposableEffect onDispose {}
        val zoomLiveData = camera.cameraInfo.zoomState
        val observer = Observer<ZoomState> { zoomState ->
            if (zoomState != null) {
                currentZoomRatio = zoomState.zoomRatio
                minZoomRatio = zoomState.minZoomRatio
                maxZoomRatio = zoomState.maxZoomRatio.coerceAtMost(8f)
            }
        }
        zoomLiveData.observe(lifecycleOwner, observer)
        onDispose {
            zoomLiveData.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF0B1322))
            .testTag("camera_preview_container"),
        contentAlignment = Alignment.Center
    ) {
        when {
            cameraPermissionState.status.isGranted -> {
                // Pinch-to-zoom gesture modifier on the live viewfinder feed
                LiveCameraXFeed(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(cameraRef, minZoomRatio, maxZoomRatio) {
                            detectTransformGestures { _, _, zoomChange, _ ->
                                if (zoomChange != 1f && cameraRef != null) {
                                    val newZoom = (currentZoomRatio * zoomChange).coerceIn(minZoomRatio, maxZoomRatio)
                                    currentZoomRatio = newZoom
                                    cameraRef?.cameraControl?.setZoomRatio(newZoom)
                                }
                            }
                        },
                    cameraSelector = cameraSelector,
                    onImageCaptureBound = { capture ->
                        imageCaptureRef = capture
                    },
                    onCameraBound = { camera ->
                        cameraRef = camera
                    },
                    onCameraReady = onCameraReady
                )

                // Alignment Grid Guide Overlay for UI Sketches
                AnimatedVisibility(
                    visible = gridMode != GridGuideMode.DISABLED,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    CameraGridGuideOverlay(
                        mode = gridMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Grid Guide Mode Selector & Alignment Banner
                CameraGridControls(
                    currentMode = gridMode,
                    onToggleMode = {
                        gridMode = when (gridMode) {
                            GridGuideMode.RULE_OF_THIRDS -> GridGuideMode.FRAME_ONLY
                            GridGuideMode.FRAME_ONLY -> GridGuideMode.DISABLED
                            GridGuideMode.DISABLED -> GridGuideMode.RULE_OF_THIRDS
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                )

                // Zoom Level Controls & Quick Preset Chips (1x, 2x, 3x)
                CameraZoomControls(
                    currentZoomRatio = currentZoomRatio,
                    minZoomRatio = minZoomRatio,
                    maxZoomRatio = maxZoomRatio,
                    onSetZoomRatio = { targetRatio ->
                        val safeZoom = targetRatio.coerceIn(minZoomRatio, maxZoomRatio)
                        currentZoomRatio = safeZoom
                        cameraRef?.cameraControl?.setZoomRatio(safeZoom)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showCaptureButton) 78.dp else 16.dp)
                )

                // Bottom Capture Controls Overlay
                if (showCaptureButton) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Capture Shutter Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .border(2.dp, TechCyan, CircleShape)
                                .clickable(enabled = !isCapturing && imageCaptureRef != null) {
                                    val imageCapture = imageCaptureRef ?: return@clickable
                                    isCapturing = true
                                    captureSketchPhoto(
                                        context = context,
                                        imageCapture = imageCapture,
                                        onSuccess = { savedFile ->
                                            isCapturing = false
                                            capturedSuccessMessage = savedFile.name
                                            Toast.makeText(
                                                context,
                                                "Sketsa berhasil diambil: ${savedFile.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onPhotoCaptured?.invoke(savedFile)
                                        },
                                        onError = { exception ->
                                            isCapturing = false
                                            Toast.makeText(
                                                context,
                                                "Gagal mengambil gambar: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                                .testTag("capture_photo_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(
                                    color = BrandAccent,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(TechCyan)
                                        .testTag("shutter_inner_circle"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Capture Sketch",
                                        tint = Color(0xFF0B1322),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            cameraPermissionState.status.shouldShowRationale -> {
                PermissionRationaleView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
            else -> {
                PermissionRequestView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

/**
 * Captures a photo using ImageCapture and saves it into the internal storage cache/sketches directory.
 */
fun captureSketchPhoto(
    context: Context,
    imageCapture: ImageCapture,
    onSuccess: (File) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val sketchesDir = File(context.filesDir, "sketches").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val photoFile = File(sketchesDir, "SKETCH_${timeStamp}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    val executor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

@Composable
fun CameraGridGuideOverlay(
    mode: GridGuideMode,
    modifier: Modifier = Modifier,
    gridColor: Color = TechCyan.copy(alpha = 0.5f),
    cornerColor: Color = BrandAccent,
    crosshairColor: Color = TechCyan.copy(alpha = 0.8f)
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("camera_grid_guide_overlay")
    ) {
        val w = size.width
        val h = size.height

        // Frame bounds for sketch document alignment (margin padding)
        val marginX = w * 0.08f
        val marginY = h * 0.08f
        val frameLeft = marginX
        val frameTop = marginY
        val frameRight = w - marginX
        val frameBottom = h - marginY
        val frameWidth = frameRight - frameLeft
        val frameHeight = frameBottom - frameTop

        val cornerLength = 28f
        val cornerStroke = 4f
        val gridStroke = 1.5f
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

        if (mode == GridGuideMode.RULE_OF_THIRDS) {
            // Rule of thirds lines (vertical)
            val v1 = frameLeft + frameWidth / 3f
            val v2 = frameLeft + (frameWidth * 2f) / 3f
            drawLine(
                color = gridColor,
                start = Offset(v1, frameTop),
                end = Offset(v1, frameBottom),
                strokeWidth = gridStroke,
                pathEffect = dashedEffect
            )
            drawLine(
                color = gridColor,
                start = Offset(v2, frameTop),
                end = Offset(v2, frameBottom),
                strokeWidth = gridStroke,
                pathEffect = dashedEffect
            )

            // Rule of thirds lines (horizontal)
            val h1 = frameTop + frameHeight / 3f
            val h2 = frameTop + (frameHeight * 2f) / 3f
            drawLine(
                color = gridColor,
                start = Offset(frameLeft, h1),
                end = Offset(frameRight, h1),
                strokeWidth = gridStroke,
                pathEffect = dashedEffect
            )
            drawLine(
                color = gridColor,
                start = Offset(frameLeft, h2),
                end = Offset(frameRight, h2),
                strokeWidth = gridStroke,
                pathEffect = dashedEffect
            )

            // Center Crosshair Marker
            val cx = w / 2f
            val cy = h / 2f
            val crossSize = 14f

            drawLine(
                color = crosshairColor,
                start = Offset(cx - crossSize, cy),
                end = Offset(cx + crossSize, cy),
                strokeWidth = 2f
            )
            drawLine(
                color = crosshairColor,
                start = Offset(cx, cy - crossSize),
                end = Offset(cx, cy + crossSize),
                strokeWidth = 2f
            )
            drawCircle(
                color = crosshairColor,
                radius = 4f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // 4-Corner Alignment Frame Brackets (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
        // Top-Left Corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft, frameTop + cornerLength),
            strokeWidth = cornerStroke
        )

        // Top-Right Corner
        drawLine(
            color = cornerColor,
            start = Offset(frameRight - cornerLength, frameTop),
            end = Offset(frameRight, frameTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameRight, frameTop),
            end = Offset(frameRight, frameTop + cornerLength),
            strokeWidth = cornerStroke
        )

        // Bottom-Left Corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameBottom),
            end = Offset(frameLeft + cornerLength, frameBottom),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameBottom - cornerLength),
            end = Offset(frameLeft, frameBottom),
            strokeWidth = cornerStroke
        )

        // Bottom-Right Corner
        drawLine(
            color = cornerColor,
            start = Offset(frameRight - cornerLength, frameBottom),
            end = Offset(frameRight, frameBottom),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameRight, frameBottom - cornerLength),
            end = Offset(frameRight, frameBottom),
            strokeWidth = cornerStroke
        )
    }
}

@Composable
private fun CameraGridControls(
    currentMode: GridGuideMode,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC0B1426))
            .border(1.dp, Color(0xFF1E2F4F), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (currentMode != GridGuideMode.DISABLED) TechCyan else Color.Gray)
            )
            Text(
                text = when (currentMode) {
                    GridGuideMode.RULE_OF_THIRDS -> "PANDUAN GRID 3X3 • SEJAJARKAN SKETSA"
                    GridGuideMode.FRAME_ONLY -> "BINGKAI DOKUMEN • SEJAJARKAN TEPI"
                    GridGuideMode.DISABLED -> "GRID NONAKTIF"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x4438BDF8))
                .clickable(onClick = onToggleMode)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("toggle_grid_guide_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when (currentMode) {
                    GridGuideMode.RULE_OF_THIRDS -> Icons.Default.Grid3x3
                    GridGuideMode.FRAME_ONLY -> Icons.Default.GridOn
                    GridGuideMode.DISABLED -> Icons.Default.GridOff
                },
                contentDescription = "Ganti Mode Grid",
                tint = TechCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = when (currentMode) {
                    GridGuideMode.RULE_OF_THIRDS -> "Grid 3x3"
                    GridGuideMode.FRAME_ONLY -> "Frame"
                    GridGuideMode.DISABLED -> "Off"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TechCyan
                )
            )
        }
    }
}

@Composable
private fun LiveCameraXFeed(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onImageCaptureBound: ((ImageCapture) -> Unit)? = null,
    onCameraBound: ((Camera) -> Unit)? = null,
    onCameraReady: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraBound by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(lifecycleOwner, cameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        val listener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                val previewView = previewViewRef
                if (previewView != null) {
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    cameraProvider.unbindAll()
                    val camera = if (cameraProvider.hasCamera(cameraSelector)) {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } else null

                    onImageCaptureBound?.invoke(imageCapture)
                    if (camera != null) {
                        onCameraBound?.invoke(camera)
                    }
                    isCameraBound = true
                    onCameraReady?.invoke()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isCameraBound = true // avoid infinite progress indicator on error
            }
        }

        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef = this
                }
            },
            update = { view ->
                previewViewRef = view
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("camerax_preview_view")
        )

        if (!isCameraBound) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestView(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Camera Permission",
            tint = BrandAccent,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Akses Kamera Diperlukan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Berikan izin kamera untuk mengaktifkan live feed deteksi kamera perangkat.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.5.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandAccent,
                contentColor = Color(0xFF08261A)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("request_camera_permission_button")
        ) {
            Text("Izinkan Kamera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PermissionRationaleView(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Camera Permission Rationale",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Izin Kamera Dibutuhkan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Aplikasi membutuhkan kamera fisik untuk mendeteksi orang dan memindai secara real-time.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.5.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandAccent,
                contentColor = Color(0xFF08261A)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("grant_camera_permission_rationale_button")
        ) {
            Text("Coba Lagi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CameraZoomControls(
    currentZoomRatio: Float,
    minZoomRatio: Float,
    maxZoomRatio: Float,
    onSetZoomRatio: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember(minZoomRatio, maxZoomRatio) {
        listOf(1.0f, 2.0f, 3.0f, 5.0f).filter { it in minZoomRatio..maxZoomRatio }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xBB08111E))
                .border(1.dp, Color(0x3322D3EE), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Live Zoom Ratio Indicator Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x3322D3EE))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom Indicator",
                    tint = TechCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", currentZoomRatio),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                )
            }

            // Quick Preset Buttons (1x, 2x, 3x...)
            presets.forEach { preset ->
                val isSelected = kotlin.math.abs(currentZoomRatio - preset) < 0.25f
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) TechCyan else Color.Transparent
                        )
                        .clickable { onSetZoomRatio(preset) }
                        .testTag("zoom_preset_${preset.toInt()}x"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${preset.toInt()}x",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color(0xFF09101D) else Color(0xFFCBD5E1),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}


