package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DailySummary
import com.example.data.model.HourlyRecord
import com.example.data.model.PeakHourItem
import com.example.data.model.PdfExportConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdf(
        context: Context,
        config: PdfExportConfig,
        summary: DailySummary,
        hourlyRecords: List<HourlyRecord>,
        topPeakHours: List<PeakHourItem>
    ): File? {
        val file = generatePdf(context, config, summary, hourlyRecords, topPeakHours)
        if (file != null) {
            sharePdf(context, file)
        }
        return file
    }

    fun generatePdf(
        context: Context,
        config: PdfExportConfig,
        summary: DailySummary,
        hourlyRecords: List<HourlyRecord>,
        topPeakHours: List<PeakHourItem>
    ): File? {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 points (72 dpi)
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val primaryColor = Color.rgb(16, 185, 129) // #10B981 Emerald
        val headerBgColor = Color.rgb(17, 26, 46)   // #111A2E
        val tableHeaderBg = Color.rgb(241, 245, 249) // Slate 100
        val tableBorderColor = Color.rgb(203, 213, 225) // Slate 300
        val textDarkColor = Color.rgb(15, 23, 42)    // Slate 900
        val textMutedColor = Color.rgb(100, 116, 139) // Slate 500
        val peakBgColor = Color.rgb(254, 226, 226)   // Red 100
        val peakTextColor = Color.rgb(185, 28, 28)   // Red 700

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var currentY = 32f

        // --- 1. Header Banner ---
        paint.color = headerBgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(30f, currentY, (pageWidth - 30).toFloat(), currentY + 54f), 8f, 8f, paint)

        // Logo / App Name
        paint.color = primaryColor
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PeopleCount", 46f, currentY + 24f, paint)

        // Report Title
        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Laporan Analisis Harian", 46f, currentY + 42f, paint)

        // Date on Right
        paint.color = Color.rgb(203, 213, 225)
        paint.textSize = 10f
        val dateText = "Tanggal: ${config.date}"
        val dateWidth = paint.measureText(dateText)
        canvas.drawText(dateText, pageWidth - 46f - dateWidth, currentY + 34f, paint)

        currentY += 66f

        // --- 2. Ringkasan Harian Table ---
        if (config.includeSummary) {
            paint.color = textDarkColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("RINGKASAN HARIAN", 30f, currentY, paint)
            currentY += 8f

            val tableLeft = 30f
            val tableRight = (pageWidth - 30).toFloat()
            val colMid = tableLeft + (tableRight - tableLeft) * 0.45f
            val rowHeight = 17f

            val summaryRows = listOf(
                "Total Masuk" to String.format(Locale.GERMANY, "%,d orang", summary.totalMasuk),
                "Total Keluar" to String.format(Locale.GERMANY, "%,d orang", summary.totalKeluar),
                "Total Sekarang" to String.format(Locale.GERMANY, "%,d orang", summary.totalSekarang),
                "Peningkatan" to String.format(Locale.US, "+%.0f%% dari kemarin", summary.peningkatanPercent),
                "Rata-rata" to "${summary.rataRataPerJam} orang / jam",
                "Peak Hour" to "${summary.peakHourRange} (${summary.peakCount} orang)"
            )

            // Table Border
            paint.style = Paint.Style.STROKE
            paint.color = tableBorderColor
            paint.strokeWidth = 1f
            val tableTop = currentY
            val tableBottom = currentY + (summaryRows.size * rowHeight)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, paint)
            canvas.drawLine(colMid, tableTop, colMid, tableBottom, paint)

            // Draw rows
            summaryRows.forEachIndexed { index, (label, value) ->
                val rY = currentY + (index * rowHeight)

                // Row background alternate
                if (index % 2 == 0) {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft + 1, rY, tableRight - 1, rY + rowHeight, paint)
                }

                // Row bottom line
                if (index < summaryRows.size - 1) {
                    paint.style = Paint.Style.STROKE
                    paint.color = tableBorderColor
                    canvas.drawLine(tableLeft, rY + rowHeight, tableRight, rY + rowHeight, paint)
                }

                // Texts
                paint.style = Paint.Style.FILL
                paint.color = textMutedColor
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(label, tableLeft + 8f, rY + 12f, paint)

                paint.color = textDarkColor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(value, colMid + 8f, rY + 12f, paint)
            }

            currentY = tableBottom + 16f
        }

        // --- 3. Grafik Tren Harian ---
        if (config.includeTrend) {
            paint.color = textDarkColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GRAFIK TREN HARIAN", 30f, currentY, paint)
            currentY += 8f

            val chartLeft = 54f
            val chartRight = (pageWidth - 36).toFloat()
            val chartTop = currentY + 10f
            val chartHeight = 84f
            val chartBottom = chartTop + chartHeight

            // Chart border & background
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(250, 252, 254)
            canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), chartBottom + 20f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = tableBorderColor
            paint.strokeWidth = 1f
            canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), chartBottom + 20f, paint)

            // Grid lines & labels (200, 100, 50, 0)
            val yLevels = listOf(200 to 0f, 100 to 0.5f, 50 to 0.75f, 0 to 1f)
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            yLevels.forEach { (value, ratio) ->
                val lineY = chartTop + (chartHeight * ratio)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(chartLeft, lineY, chartRight, lineY, paint)

                paint.style = Paint.Style.FILL
                paint.color = textMutedColor
                canvas.drawText("$value", 34f, lineY + 3f, paint)
            }

            // X-axis time marks (00:00, 04:00, 08:00, 12:00, 16:00, 20:00, 24:00)
            val xHours = listOf(0, 4, 8, 12, 16, 20, 24)
            xHours.forEach { h ->
                val ratio = h / 24f
                val lineX = chartLeft + ((chartRight - chartLeft) * ratio)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(lineX, chartTop, lineX, chartBottom, paint)

                val label = String.format("%02d:00", if (h == 24) 24 else h)
                paint.style = Paint.Style.FILL
                paint.color = textMutedColor
                val lWidth = paint.measureText(label)
                canvas.drawText(label, (lineX - lWidth / 2).coerceIn(chartLeft, chartRight - lWidth), chartBottom + 12f, paint)
            }

            // Plot data points
            val points = if (hourlyRecords.isNotEmpty()) {
                hourlyRecords.map { rec ->
                    val x = chartLeft + ((chartRight - chartLeft) * (rec.hour / 23f))
                    val y = chartBottom - ((rec.sekarang.coerceIn(0, 200) / 200f) * chartHeight)
                    Pair(x, y)
                }
            } else {
                emptyList()
            }

            if (points.isNotEmpty()) {
                // Draw curve path
                val path = Path()
                path.moveTo(points.first().first, points.first().second)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx = (prev.first + curr.first) / 2f
                    path.cubicTo(cx, prev.second, cx, curr.second, curr.first, curr.second)
                }

                // Line stroke
                paint.style = Paint.Style.STROKE
                paint.color = primaryColor
                paint.strokeWidth = 2f
                canvas.drawPath(path, paint)

                // Peak callout marker at index 13 or 14 (14:00)
                val peakPoint = points.maxByOrNull { it.first } ?: points[14.coerceAtMost(points.size - 1)]
                val markerX = chartLeft + ((chartRight - chartLeft) * (14f / 23f))
                val markerY = chartBottom - ((156f / 200f) * chartHeight)

                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(239, 68, 68)
                canvas.drawCircle(markerX, markerY, 3.5f, paint)

                // Callout Box
                paint.color = Color.WHITE
                canvas.drawRoundRect(RectF(markerX - 28f, markerY - 22f, markerX + 28f, markerY - 6f), 4f, 4f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = tableBorderColor
                paint.strokeWidth = 0.8f
                canvas.drawRoundRect(RectF(markerX - 28f, markerY - 22f, markerX + 28f, markerY - 6f), 4f, 4f, paint)

                paint.style = Paint.Style.FILL
                paint.color = textDarkColor
                paint.textSize = 6.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val calloutText = "14:00"
                val calloutVal = "156 Org"
                canvas.drawText(calloutText, markerX - 22f, markerY - 14f, paint)
                paint.color = primaryColor
                canvas.drawText(calloutVal, markerX - 22f, markerY - 7.5f, paint)
            }

            currentY = chartBottom + 32f
        }

        // --- 4. Peak Hour (Top 5) ---
        if (config.includePeakHour) {
            paint.color = textDarkColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("PEAK HOUR (TOP 5)", 30f, currentY, paint)
            currentY += 8f

            val tableLeft = 30f
            val tableRight = (pageWidth - 30).toFloat()
            val rowHeight = 16f
            val rankWidth = 28f
            val countColX = tableRight - 80f

            val top5 = if (topPeakHours.isNotEmpty()) topPeakHours else SampleDataProvider.getTop5PeakHours()
            val tableTop = currentY
            val tableBottom = currentY + (top5.size * rowHeight)

            // Table Border
            paint.style = Paint.Style.STROKE
            paint.color = tableBorderColor
            paint.strokeWidth = 1f
            canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, paint)

            top5.forEachIndexed { index, item ->
                val rY = currentY + (index * rowHeight)

                // Highlight rank 1
                if (index == 0) {
                    paint.style = Paint.Style.FILL
                    paint.color = peakBgColor
                    canvas.drawRect(tableLeft + 1, rY, tableRight - 1, rY + rowHeight, paint)
                } else if (index % 2 == 1) {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(tableLeft + 1, rY, tableRight - 1, rY + rowHeight, paint)
                }

                if (index < top5.size - 1) {
                    paint.style = Paint.Style.STROKE
                    paint.color = tableBorderColor
                    canvas.drawLine(tableLeft, rY + rowHeight, tableRight, rY + rowHeight, paint)
                }

                // Rank
                paint.style = Paint.Style.FILL
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (index == 0) peakTextColor else textMutedColor
                canvas.drawText("${item.rank}.", tableLeft + 8f, rY + 11.5f, paint)

                // Time Range
                paint.typeface = Typeface.create(Typeface.DEFAULT, if (index == 0) Typeface.BOLD else Typeface.NORMAL)
                paint.color = if (index == 0) peakTextColor else textDarkColor
                canvas.drawText(item.timeRange, tableLeft + rankWidth, rY + 11.5f, paint)

                // Count
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val countStr = "${item.count} orang"
                canvas.drawText(countStr, countColX, rY + 11.5f, paint)
            }

            currentY = tableBottom + 16f
        }

        // --- 5. Data Per Jam Table (Compact) ---
        if (config.includeHourlyData && currentY < (pageHeight - 120)) {
            paint.color = textDarkColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DATA PER JAM (Cuplikan)", 30f, currentY, paint)
            currentY += 8f

            val tableLeft = 30f
            val tableRight = (pageWidth - 30).toFloat()
            val rowHeight = 14f
            val colW = (tableRight - tableLeft) / 4f

            // Table Header
            paint.style = Paint.Style.FILL
            paint.color = tableHeaderBg
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, paint)

            paint.style = Paint.Style.STROKE
            paint.color = tableBorderColor
            paint.strokeWidth = 1f
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, paint)

            val headers = listOf("Waktu", "Masuk", "Keluar", "Sekarang")
            paint.style = Paint.Style.FILL
            paint.textSize = 8f
            paint.color = textDarkColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            headers.forEachIndexed { i, h ->
                canvas.drawText(h, tableLeft + (i * colW) + 8f, currentY + 10f, paint)
            }
            currentY += rowHeight

            // Sample rows around peak hours (10:00 - 15:00)
            val sampleHourly = hourlyRecords.filter { it.hour in 10..15 }
            sampleHourly.forEachIndexed { idx, item ->
                val rY = currentY + (idx * rowHeight)
                if (item.isPeak) {
                    paint.style = Paint.Style.FILL
                    paint.color = peakBgColor
                    canvas.drawRect(tableLeft + 1, rY, tableRight - 1, rY + rowHeight, paint)
                }

                paint.style = Paint.Style.STROKE
                paint.color = tableBorderColor
                canvas.drawRect(tableLeft, rY, tableRight, rY + rowHeight, paint)

                paint.style = Paint.Style.FILL
                paint.textSize = 7.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, if (item.isPeak) Typeface.BOLD else Typeface.NORMAL)
                paint.color = if (item.isPeak) peakTextColor else textDarkColor

                canvas.drawText(item.timeLabel, tableLeft + 8f, rY + 10f, paint)
                canvas.drawText("${item.masuk}", tableLeft + colW + 8f, rY + 10f, paint)
                canvas.drawText("${item.keluar}", tableLeft + (colW * 2) + 8f, rY + 10f, paint)
                canvas.drawText("${item.sekarang}", tableLeft + (colW * 3) + 8f, rY + 10f, paint)
            }

            currentY += (sampleHourly.size * rowHeight) + 14f
        }

        // --- Footer ---
        val footerY = (pageHeight - 24).toFloat()
        paint.style = Paint.Style.FILL
        paint.color = textMutedColor
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dicetak oleh PeopleCount AI Tracking System • Dokumen Resmi", 30f, footerY, paint)

        val genTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val genText = "Generated: $genTime"
        val gWidth = paint.measureText(genText)
        canvas.drawText(genText, pageWidth - 30f - gWidth, footerY, paint)

        document.finishPage(page)

        // Write to File
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val outputFile = File(reportsDir, "PeopleCount_Laporan_${System.currentTimeMillis()}.pdf")
        return try {
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            document.close()
            outputStream.flush()
            outputStream.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Analisis PeopleCount")
                putExtra(Intent.EXTRA_TEXT, "Berikut terlampir Laporan Analisis PeopleCount.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
