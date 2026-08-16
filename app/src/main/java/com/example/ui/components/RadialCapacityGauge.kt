package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextSecondary

@Composable
fun RadialCapacityGauge(
    currentCount: Int,
    maxCapacity: Int,
    level: AlertLevel,
    modifier: Modifier = Modifier
) {
    val progress = (currentCount.toFloat() / maxCapacity.coerceAtLeast(1).toFloat()).coerceIn(0f, 1.5f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    val primaryColor = when (level) {
        AlertLevel.SAFE -> StatusSafe
        AlertLevel.WARNING -> StatusWarning
        AlertLevel.DANGER -> StatusDanger
    }

    val glowColor = primaryColor.copy(alpha = 0.35f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radial_capacity_gauge"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(175.dp)
        ) {
            Canvas(modifier = Modifier.size(165.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background Track Circle
                drawCircle(
                    color = Color(0xFF16233B),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Soft Outer Glow Ring
                drawCircle(
                    color = glowColor.copy(alpha = 0.12f),
                    radius = radius + (strokeWidth * 0.4f),
                    center = center,
                    style = Stroke(width = strokeWidth * 0.7f)
                )

                // Animated Progress Arc
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor.copy(alpha = 0.8f),
                        0.5f to primaryColor,
                        1.0f to primaryColor
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central Counter Number and Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currentCount",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 42.sp,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.testTag("gauge_count_text")
                )
                Text(
                    text = "Orang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Linear Progress Bar & Ratio labels
        val percent = if (maxCapacity > 0) ((currentCount.toDouble() / maxCapacity) * 100).toInt() else 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentCount / $maxCapacity",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )

            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(6.dp),
            color = primaryColor,
            trackColor = Color(0xFF1E2D4A),
            strokeCap = StrokeCap.Round
        )
    }
}
