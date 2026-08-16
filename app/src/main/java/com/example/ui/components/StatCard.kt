package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TextSecondary

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    highlightValue: Boolean = false,
    valueColor: Color = Color.White,
    tag: String = "stat_card"
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppCardBg)
            .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (highlightValue) BrandAccent else valueColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(vertical = 2.dp),
            maxLines = 1
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}
