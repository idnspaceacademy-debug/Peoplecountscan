package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventType {
    IN, OUT, RESET, ADJUST
}

enum class AlertLevel {
    SAFE,       // Green
    WARNING,    // Amber (Hampir Penuh)
    DANGER      // Red (Kapasitas Melebihi Batas)
}

@Entity(tableName = "tracking_events")
data class TrackingEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: EventType,
    val countChange: Int,
    val currentTotal: Int,
    val source: String = "AI Camera 01 (Hallway)"
)

@Entity(tableName = "hourly_records")
data class HourlyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,          // "2024-05-25"
    val hour: Int,             // 0..23
    val timeLabel: String,     // "10:00"
    val masuk: Int,
    val keluar: Int,
    val sekarang: Int,
    val isPeak: Boolean = false
)

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey
    val date: String,          // "2024-05-25"
    val displayDate: String,   // "25 Mei 2024"
    val totalMasuk: Int,
    val totalKeluar: Int,
    val totalSekarang: Int,
    val peningkatanPercent: Double, // +12.0
    val rataRataPerJam: Int,        // 104
    val peakHourTime: String,       // "14:00"
    val peakHourRange: String,      // "14:00 - 15:00"
    val peakCount: Int              // 156
)

@Entity(tableName = "alert_notifications")
data class AlertNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String,     // "10:35"
    val level: AlertLevel,
    val title: String,             // "KAPASITAS MELEBIHI BATAS!"
    val count: Int,                // 124
    val percent: Int,              // 124
    val capacityLimit: Int = 100,
    val isRead: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val maxCapacity: Int = 100,
    val warningThresholdPercent: Int = 80,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoSimulationEnabled: Boolean = true,
    val simulationIntervalMs: Long = 2500L,
    val activeDetectionMode: String = "SIMULATION" // "SIMULATION", "CAMERA", "MANUAL"
)

data class PeakHourItem(
    val rank: Int,
    val timeRange: String,
    val count: Int,
    val isHighest: Boolean = false
)

data class AiBoundingBox(
    val id: Int,
    val label: String,
    val confidence: Float,
    val normX: Float,
    val normY: Float,
    val normWidth: Float,
    val normHeight: Float,
    val direction: String = "IN", // "IN", "OUT", "STATIONARY"
    val isEntering: Boolean = true
)

data class PdfExportConfig(
    val date: String = "25 Mei 2024",
    val reportType: String = "Ringkasan Harian",
    val includeSummary: Boolean = true,
    val includeTrend: Boolean = true,
    val includePeakHour: Boolean = true,
    val includeHourlyData: Boolean = true
)
