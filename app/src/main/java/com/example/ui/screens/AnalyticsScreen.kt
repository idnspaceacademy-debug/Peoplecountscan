package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailySummary
import com.example.data.model.HourlyRecord
import com.example.data.model.PeakHourItem
import com.example.ui.components.StatCard
import com.example.ui.components.TrendLineChart
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.PeopleCountViewModel
import com.example.utils.SampleDataProvider

@Composable
fun AnalyticsScreen(
    viewModel: PeopleCountViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val hourlyRecords by viewModel.hourlyRecords.collectAsStateWithLifecycle()
    val top5PeakHours = SampleDataProvider.getTop5PeakHours()

    val tabs = listOf("Ringkasan", "Tren Harian", "Peak Hour")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("analytics_screen")
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = when (uiState.selectedSubTab) {
                        1 -> "Tren Harian"
                        2 -> "Peak Hour"
                        else -> "Analisis"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }

            // Export PDF Button
            OutlinedButton(
                onClick = { viewModel.toggleExportPdfDialog(true) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0x1810B981),
                    contentColor = BrandAccent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BrandAccent.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Export PDF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Sub Tabs (Ringkasan, Tren Harian, Peak Hour) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF10192A))
                .border(1.dp, AppCardBorder, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = uiState.selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1A2A47) else Color.Transparent)
                        .clickable { viewModel.selectAnalyticsSubTab(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- SubTab Content Body ---
        AnimatedContent(
            targetState = uiState.selectedSubTab,
            label = "analytics_tab_content",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SummaryTabContent(summary = summary, records = hourlyRecords)
                1 -> DailyTrendTabContent(records = hourlyRecords)
                2 -> PeakHourTabContent(top5 = top5PeakHours)
                else -> SummaryTabContent(summary = summary, records = hourlyRecords)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: RINGKASAN (Summary)
// -------------------------------------------------------------
@Composable
private fun SummaryTabContent(
    summary: DailySummary,
    records: List<HourlyRecord>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("summary_tab_content")
    ) {
        // Date Section Title
        Text(
            text = "Ringkasan Hari Ini",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        )
        Text(
            text = summary.displayDate,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Top 3 Metrics Row (Total Masuk, Total Keluar, Total Sekarang)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Total Masuk",
                value = String.format(java.util.Locale.GERMANY, "%,d", summary.totalMasuk),
                modifier = Modifier.weight(1f),
                tag = "stat_total_masuk"
            )

            StatCard(
                title = "Total Keluar",
                value = String.format(java.util.Locale.GERMANY, "%,d", summary.totalKeluar),
                modifier = Modifier.weight(1f),
                tag = "stat_total_keluar"
            )

            StatCard(
                title = "Total Sekarang",
                value = "${summary.totalSekarang}",
                highlightValue = true,
                modifier = Modifier.weight(1f),
                tag = "stat_total_sekarang"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary 3 Metrics Row (Peningkatan, Rata-rata, Peak Hour)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Peningkatan",
                value = "+${summary.peningkatanPercent.toInt()}%",
                subtitle = "Dari kemarin",
                modifier = Modifier.weight(1f),
                tag = "stat_peningkatan"
            )

            StatCard(
                title = "Rata-rata",
                value = "${summary.rataRataPerJam}",
                subtitle = "Orang / Jam",
                modifier = Modifier.weight(1f),
                tag = "stat_rata_rata"
            )

            StatCard(
                title = "Peak Hour",
                value = summary.peakHourTime,
                subtitle = "${summary.peakCount} Orang",
                modifier = Modifier.weight(1f),
                tag = "stat_peak_hour"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grafik Tren Harian Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "Grafik Tren Harian",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrendLineChart(
                records = records,
                peakCalloutHour = 13,
                peakCalloutCount = 128
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// TAB 2: TREN HARIAN (Daily Trend & Hourly Table)
// -------------------------------------------------------------
@Composable
private fun DailyTrendTabContent(
    records: List<HourlyRecord>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("daily_trend_tab_content")
    ) {
        // Date Navigator Row < 25 Mei 2024 >
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Tanggal Sebelumnya",
                    tint = Color.White
                )
            }

            Text(
                text = "25 Mei 2024",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            )

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Tanggal Berikutnya",
                    tint = Color.White
                )
            }
        }

        // Subtitle Stat
        Text(
            text = "Total Masuk: 1.254 orang",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.5.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Trend Chart Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            TrendLineChart(
                records = records,
                peakCalloutHour = 13,
                peakCalloutCount = 128
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Data per Jam Header
        Text(
            text = "Data per Jam",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Table Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Waktu",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Masuk",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Keluar",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Sekarang",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display Table Rows matching screenshot (10:00 - 15:00 & full hours)
            records.filter { it.hour in 10..15 }.forEach { row ->
                val isPeakRow = row.hour == 14
                val rowColor = if (isPeakRow) StatusDanger else Color.White
                val rowWeight = if (isPeakRow) FontWeight.Bold else FontWeight.Normal

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPeakRow) Color(0x20EF4444) else Color.Transparent)
                        .padding(vertical = 9.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = row.timeLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = rowColor,
                            fontWeight = rowWeight,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${row.masuk}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = rowColor,
                            fontWeight = rowWeight,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${row.keluar}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (row.hour == 13) BrandAccent else rowColor,
                            fontWeight = rowWeight,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${row.sekarang}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (row.hour == 13) BrandAccent else rowColor,
                            fontWeight = if (row.hour in 13..14) FontWeight.Bold else rowWeight,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// TAB 3: PEAK HOUR
// -------------------------------------------------------------
@Composable
private fun PeakHourTabContent(
    top5: List<PeakHourItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("peak_hour_tab_content")
    ) {
        // Subtitle
        Text(
            text = "Peak Hour - 25 Mei 2024",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Jam Puncak Header
        Text(
            text = "Jam Puncak",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Highlight Red Card matching screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF38121A)) // Red Tinted Dark BG
                .border(1.dp, StatusDanger.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33EF4444)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = StatusDanger,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "14:00 - 15:00",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "156 Orang (Tertinggi)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFFCA5A5),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Top 5 Jam Tertinggi Header
        Text(
            text = "Top 5 Jam Tertinggi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Top 5 Items List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppCardBg)
                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            top5.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${item.rank}.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (index == 0) StatusDanger else TextSecondary,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = item.timeRange,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == 0) Color.White else Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "${item.count} Orang",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (index == 0) StatusDanger else BrandAccent,
                            fontSize = 13.sp
                        )
                    )
                }

                if (index < top5.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF19253C))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Informasi Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101A2C))
                .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = BrandAccent,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Informasi",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Peak hour membantu Anda mengelola kapasitas dan jadwal operasional dengan lebih efektif.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
