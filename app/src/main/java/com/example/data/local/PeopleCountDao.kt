package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlertNotification
import com.example.data.model.AppSettings
import com.example.data.model.DailySummary
import com.example.data.model.HourlyRecord
import com.example.data.model.TrackingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleCountDao {

    // --- Tracking Events ---
    @Query("SELECT * FROM tracking_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 50): Flow<List<TrackingEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TrackingEvent): Long

    @Query("DELETE FROM tracking_events")
    suspend fun clearAllEvents()

    // --- Daily Summaries ---
    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    fun getDailySummary(date: String): Flow<DailySummary?>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC")
    fun getAllDailySummaries(): Flow<List<DailySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailySummary)

    // --- Hourly Records ---
    @Query("SELECT * FROM hourly_records WHERE date = :date ORDER BY hour ASC")
    fun getHourlyRecords(date: String): Flow<List<HourlyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyRecords(records: List<HourlyRecord>)

    @Query("DELETE FROM hourly_records WHERE date = :date")
    suspend fun clearHourlyRecordsForDate(date: String)

    // --- Alert Notifications ---
    @Query("SELECT * FROM alert_notifications ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertNotification>>

    @Query("SELECT COUNT(*) FROM alert_notifications WHERE isRead = 0")
    fun getUnreadAlertCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertNotification): Long

    @Query("UPDATE alert_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Long)

    @Query("UPDATE alert_notifications SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alert_notifications")
    suspend fun clearAllAlerts()

    // --- Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    @Update
    suspend fun updateSettings(settings: AppSettings)
}
