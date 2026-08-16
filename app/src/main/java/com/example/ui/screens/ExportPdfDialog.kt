package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.PdfExportConfig
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PeopleCountViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPdfScreen(
    viewModel: PeopleCountViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("25 Mei 2024") }
    var selectedReportType by remember { mutableStateOf("Ringkasan Harian") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var includeSummary by remember { mutableStateOf(true) }
    var includeTrend by remember { mutableStateOf(true) }
    var includePeakHour by remember { mutableStateOf(true) }
    var includeHourlyData by remember { mutableStateOf(true) }

    val reportTypes = listOf("Ringkasan Harian", "Laporan Detail Lengkap", "Laporan Mingguan", "Laporan Bulanan")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("export_pdf_screen")
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("export_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Export PDF",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // --- Pilih Tanggal ---
            Text(
                text = "Pilih Tanggal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppCardBg)
                    .border(1.dp, AppCardBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
                                selectedDate = "$dayOfMonth ${monthNames[month]} $year"
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pilih Tanggal",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- Jenis Laporan ---
            Text(
                text = "Jenis Laporan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppCardBg)
                        .border(1.dp, AppCardBorder, RoundedCornerShape(10.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedReportType,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih Jenis",
                        tint = TextSecondary
                    )
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFF131D31))
                        .border(1.dp, AppCardBorder, RoundedCornerShape(8.dp))
                ) {
                    reportTypes.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = type,
                                    color = if (type == selectedReportType) BrandAccent else Color.White
                                )
                            },
                            onClick = {
                                selectedReportType = type
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // --- Informasi yang disertakan ---
            Text(
                text = "Informasi yang disertakan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Checkboxes
            ExportOptionCheckbox(
                label = "Ringkasan",
                checked = includeSummary,
                onCheckedChange = { includeSummary = it },
                tag = "cb_ringkasan"
            )

            ExportOptionCheckbox(
                label = "Tren Harian",
                checked = includeTrend,
                onCheckedChange = { includeTrend = it },
                tag = "cb_tren_harian"
            )

            ExportOptionCheckbox(
                label = "Peak Hour",
                checked = includePeakHour,
                onCheckedChange = { includePeakHour = it },
                tag = "cb_peak_hour"
            )

            ExportOptionCheckbox(
                label = "Data per Jam",
                checked = includeHourlyData,
                onCheckedChange = { includeHourlyData = it },
                tag = "cb_data_per_jam"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary "Buat PDF" Green Button
            Button(
                onClick = {
                    val config = PdfExportConfig(
                        date = selectedDate,
                        reportType = selectedReportType,
                        includeSummary = includeSummary,
                        includeTrend = includeTrend,
                        includePeakHour = includePeakHour,
                        includeHourlyData = includeHourlyData
                    )
                    viewModel.exportPdf(context, config)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("button_submit_export_pdf"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandAccent,
                    contentColor = Color(0xFF062619)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat PDF",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ExportOptionCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BrandAccent,
                uncheckedColor = Color(0xFF475569),
                checkmarkColor = Color(0xFF0F172A)
            ),
            modifier = Modifier.testTag(tag)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontSize = 14.sp
            )
        )
    }
}
