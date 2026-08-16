package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Room entity representing a previously processed UI sketch
 * and its generated MockUiLayout.
 */
@Entity(tableName = "processed_sketches")
@JsonClass(generateAdapter = true)
data class ProcessedSketchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val screenType: String = "Dashboard",
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val displayDate: String = "",
    val imagePath: String? = null,
    val layoutJson: String,
    val rawDescription: String = "",
    val elementCount: Int = 0,
    val sectionCount: Int = 0,
    val isFavorite: Boolean = false,
    val tags: String = "Mobile, AI",
    val notes: String = ""
)
