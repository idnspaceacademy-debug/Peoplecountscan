package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AlertLevel
import com.example.data.model.EventType

class Converters {
    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = try {
        EventType.valueOf(value)
    } catch (e: Exception) {
        EventType.IN
    }

    @TypeConverter
    fun fromAlertLevel(value: AlertLevel): String = value.name

    @TypeConverter
    fun toAlertLevel(value: String): AlertLevel = try {
        AlertLevel.valueOf(value)
    } catch (e: Exception) {
        AlertLevel.SAFE
    }
}
