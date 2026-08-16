package com.example.data.repository

import com.example.data.local.PeopleCountDao
import com.example.data.local.PeopleCountDatabase
import com.example.data.local.ProcessedSketchDao
import com.example.data.model.AlertLevel
import com.example.data.model.AlertNotification
import com.example.data.model.AppSettings
import com.example.data.model.DailySummary
import com.example.data.model.EventType
import com.example.data.model.HourlyRecord
import com.example.data.model.MockUiLayout
import com.example.data.model.PeakHourItem
import com.example.data.model.ProcessedSketchEntity
import com.example.data.model.TrackingEvent
import com.example.utils.SampleDataProvider
import com.example.utils.SketchLayoutParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PeopleCountRepository(
    private val dao: PeopleCountDao,
    private val sketchDao: ProcessedSketchDao
) {

    val recentEvents: Flow<List<TrackingEvent>> = dao.getRecentEvents(50)
    val allAlerts: Flow<List<AlertNotification>> = dao.getAllAlerts()
    val unreadAlertCount: Flow<Int> = dao.getUnreadAlertCount()
    val appSettings: Flow<AppSettings> = dao.getSettings().map { it ?: SampleDataProvider.getDefaultSettings() }

    // --- Processed Sketches & Layout Gallery ---
    val allSketches: Flow<List<ProcessedSketchEntity>> = sketchDao.getAllSketches()
    val favoriteSketches: Flow<List<ProcessedSketchEntity>> = sketchDao.getFavoriteSketches()

    fun getSketchById(id: Long): Flow<ProcessedSketchEntity?> = sketchDao.getSketchById(id)

    suspend fun saveProcessedSketch(
        layout: MockUiLayout,
        rawDescription: String = layout.rawDescription,
        photoFile: File? = null,
        customTitle: String? = null,
        tags: String = "Mobile, AI"
    ): Long {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val now = System.currentTimeMillis()
        val title = customTitle ?: layout.screenTitle.ifBlank { "UI Sketch Design" }

        val entity = ProcessedSketchEntity(
            title = title,
            screenType = layout.screenType.ifBlank { "Dashboard" },
            summary = layout.summary.ifBlank { "Generated layout mockup from sketch analysis" },
            createdAt = now,
            displayDate = dateFormat.format(Date(now)),
            imagePath = photoFile?.absolutePath,
            layoutJson = SketchLayoutParser.layoutToJson(layout),
            rawDescription = rawDescription,
            elementCount = SketchLayoutParser.countElements(layout),
            sectionCount = layout.sections.size,
            isFavorite = false,
            tags = tags,
            notes = ""
        )
        return sketchDao.insertSketch(entity)
    }

    suspend fun toggleSketchFavorite(id: Long, isFavorite: Boolean) {
        sketchDao.toggleFavorite(id, isFavorite)
    }

    suspend fun updateSketchMetadata(id: Long, newTitle: String, newTags: String, newNotes: String) {
        sketchDao.updateMetadata(id, newTitle, newTags, newNotes)
    }

    suspend fun deleteProcessedSketch(id: Long) {
        sketchDao.deleteSketchById(id)
    }

    suspend fun duplicateSketch(id: Long): Long? {
        val existing = sketchDao.getSketchByIdDirect(id) ?: return null
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val now = System.currentTimeMillis()
        val duplicate = existing.copy(
            id = 0,
            title = "${existing.title} (Salinan)",
            createdAt = now,
            displayDate = dateFormat.format(Date(now)),
            isFavorite = false
        )
        return sketchDao.insertSketch(duplicate)
    }

    fun getDailySummary(date: String): Flow<DailySummary> =
        dao.getDailySummary(date).map { it ?: SampleDataProvider.getInitialDailySummary() }
    fun getHourlyRecords(date: String): Flow<List<HourlyRecord>> = dao.getHourlyRecords(date)

    suspend fun recordTrackingEvent(
        type: EventType,
        countChange: Int,
        newTotal: Int,
        source: String = "AI Camera 01"
    ) {
        val event = TrackingEvent(
            type = type,
            countChange = countChange,
            currentTotal = newTotal,
            source = source
        )
        dao.insertEvent(event)
    }

    suspend fun createAlertNotification(
        level: AlertLevel,
        title: String,
        count: Int,
        capacityLimit: Int
    ) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(Date())
        val percent = if (capacityLimit > 0) ((count.toDouble() / capacityLimit) * 100).toInt() else 0

        val alert = AlertNotification(
            formattedTime = formattedTime,
            level = level,
            title = title,
            count = count,
            percent = percent,
            capacityLimit = capacityLimit,
            isRead = false
        )
        dao.insertAlert(alert)
    }

    suspend fun markAlertAsRead(id: Long) = dao.markAlertAsRead(id)
    suspend fun markAllAlertsAsRead() = dao.markAllAlertsAsRead()
    suspend fun clearAllAlerts() = dao.clearAllAlerts()

    suspend fun updateDailySummary(summary: DailySummary) = dao.insertDailySummary(summary)
    suspend fun updateHourlyRecords(records: List<HourlyRecord>) = dao.insertHourlyRecords(records)

    suspend fun updateSettings(settings: AppSettings) = dao.updateSettings(settings)

    suspend fun resetToSampleData() {
        PeopleCountDatabase.populateInitialDatabase(dao, sketchDao)
    }
}
