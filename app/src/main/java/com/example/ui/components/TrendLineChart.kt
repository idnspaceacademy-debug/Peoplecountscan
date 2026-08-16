package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyRecord
import com.example.ui.theme.ChartFill
import com.example.ui.theme.ChartLine
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun TrendLineChart(
    records: List<HourlyRecord>,
    modifier: Modifier = Modifier,
    peakCalloutHour: Int = 13,
    peakCalloutCount: Int = 128
) {
    var selectedHour by remember { mutableStateOf<HourlyRecord?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(records) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val displayRecords = remember(records) {
        if (records.isNotEmpty()) records else (0..23).map {
            HourlyRecord(id = it.toLong(), date = "2024-05-25", hour = it, timeLabel = String.format("%02d:00", it), masuk = 0, keluar = 0, sekarang = 0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_line_chart")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerInput(displayRecords) {
                    detectTapGestures { offset ->
                        val leftPadding = 32.dp.toPx()
                        val rightPadding = 16.dp.toPx()
                        val chartWidth = size.width - leftPadding - rightPadding
                        val ratio = ((offset.x - leftPadding) / chartWidth).coerceIn(0f, 1f)
                        val index = (ratio * (displayRecords.size - 1)).roundToInt()
                        selectedHour = displayRecords.getOrNull(index)
                    }
                }
        ) {
            val leftPadding = 34.dp.toPx()
            val rightPadding = 16.dp.toPx()
            val topPadding = 28.dp.toPx()
            val bottomPadding = 24.dp.toPx()

            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val maxY = 200f // Scale up to 200

            // 1. Draw horizontal grid lines & Y labels (200, 100, 50, 0)
            val ySteps = listOf(200 to 0f, 100 to 0.5f, 50 to 0.75f, 0 to 1f)
            val gridPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 9.sp.toPx()
                isAntiAlias = true
            }

            ySteps.forEach { (valLabel, ratio) ->
                val y = topPadding + (chartHeight * ratio)
                drawLine(
                    color = Color(0xFF1E2D4A).copy(alpha = 0.6f),
                    start = Offset(leftPadding, y),
                    end = Offset(size.width - rightPadding, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    valLabel.toString(),
                    6.dp.toPx(),
                    y + 3.dp.toPx(),
                    gridPaint
                )
            }

            // 2. Draw X-axis time marks (00:00, 04:00, 08:00, 12:00, 16:00, 20:00, 24:00)
            val xHours = listOf(0, 4, 8, 12, 16, 20, 24)
            xHours.forEach { hour ->
                val ratio = hour / 24f
                val x = leftPadding + (chartWidth * ratio)

                val label = String.format("%02d:00", hour)
                val textWidth = gridPaint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    (x - textWidth / 2f).coerceIn(leftPadding, size.width - rightPadding - textWidth),
                    size.height - 4.dp.toPx(),
                    gridPaint
                )
            }

            // 3. Compute Coordinates for line and area
            val points = displayRecords.mapIndexed { idx, record ->
                val x = leftPadding + (chartWidth * (idx.toFloat() / (displayRecords.size - 1).coerceAtLeast(1)))
                val normalizedY = (record.sekarang.toFloat() / maxY).coerceIn(0f, 1.2f)
                val y = topPadding + chartHeight - (normalizedY * chartHeight * animProgress.value)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val linePath = Path()
                linePath.moveTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx = (prev.x + curr.x) / 2f
                    linePath.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                }

                // Area Fill Gradient Path
                val fillPath = Path()
                fillPath.addPath(linePath)
                fillPath.lineTo(points.last().x, topPadding + chartHeight)
                fillPath.lineTo(points.first().x, topPadding + chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ChartFill.copy(alpha = 0.28f * animProgress.value),
                            ChartFill.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Stroke Line
                drawPath(
                    path = linePath,
                    color = ChartLine,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Draw Callout Marker
                // Use selected hour if user tapped, or default to 13:00 / 128 Orang (or 14:00)
                val targetHourIndex = if (selectedHour != null) {
                    displayRecords.indexOfFirst { it.hour == selectedHour?.hour }.coerceAtLeast(0)
                } else {
                    displayRecords.indexOfFirst { it.hour == peakCalloutHour }.takeIf { it >= 0 } ?: 13.coerceAtMost(displayRecords.size - 1)
                }

                val targetPoint = points.getOrNull(targetHourIndex) ?: points.last()
                val targetRec = displayRecords.getOrNull(targetHourIndex)

                val displayTime = targetRec?.timeLabel ?: "13:00"
                val displayCount = targetRec?.sekarang ?: peakCalloutCount

                // Marker Circle Outer Glow & Center
                drawCircle(
                    color = ChartLine.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = targetPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = targetPoint
                )
                drawCircle(
                    color = ChartLine,
                    radius = 3.dp.toPx(),
                    center = targetPoint
                )

                // Callout Badge Box
                val badgeWidth = 68.dp.toPx()
                val badgeHeight = 28.dp.toPx()
                val badgeX = (targetPoint.x - badgeWidth / 2f).coerceIn(leftPadding, size.width - rightPadding - badgeWidth)
                val badgeY = (targetPoint.y - badgeHeight - 8.dp.toPx()).coerceAtLeast(4.dp.toPx())

                // Draw Callout Background Pill
                drawRoundRect(
                    color = Color(0xFF0F1B30),
                    topLeft = Offset(badgeX, badgeY),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF283B60),
                    topLeft = Offset(badgeX, badgeY),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Callout Texts
                val titlePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 8.5.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val countPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#10B981")
                    textSize = 8.5.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val tTime = displayTime
                val tCount = "$displayCount Orang"
                val tw1 = titlePaint.measureText(tTime)
                val tw2 = countPaint.measureText(tCount)

                drawContext.canvas.nativeCanvas.drawText(
                    tTime,
                    badgeX + (badgeWidth - tw1) / 2f,
                    badgeY + 11.dp.toPx(),
                    titlePaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    tCount,
                    badgeX + (badgeWidth - tw2) / 2f,
                    badgeY + 22.dp.toPx(),
                    countPaint
                )
            }
        }
    }
}
