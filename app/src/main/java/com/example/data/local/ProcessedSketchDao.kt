package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProcessedSketchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedSketchDao {

    @Query("SELECT * FROM processed_sketches ORDER BY createdAt DESC")
    fun getAllSketches(): Flow<List<ProcessedSketchEntity>>

    @Query("SELECT * FROM processed_sketches WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSketches(): Flow<List<ProcessedSketchEntity>>

    @Query("SELECT * FROM processed_sketches WHERE id = :id LIMIT 1")
    fun getSketchById(id: Long): Flow<ProcessedSketchEntity?>

    @Query("SELECT * FROM processed_sketches WHERE id = :id LIMIT 1")
    suspend fun getSketchByIdDirect(id: Long): ProcessedSketchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSketch(sketch: ProcessedSketchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSketches(sketches: List<ProcessedSketchEntity>)

    @Update
    suspend fun updateSketch(sketch: ProcessedSketchEntity)

    @Query("UPDATE processed_sketches SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE processed_sketches SET title = :newTitle, tags = :newTags, notes = :newNotes WHERE id = :id")
    suspend fun updateMetadata(id: Long, newTitle: String, newTags: String, newNotes: String)

    @Delete
    suspend fun deleteSketch(sketch: ProcessedSketchEntity)

    @Query("DELETE FROM processed_sketches WHERE id = :id")
    suspend fun deleteSketchById(id: Long)

    @Query("DELETE FROM processed_sketches")
    suspend fun clearAllSketches()

    @Query("SELECT COUNT(*) FROM processed_sketches")
    suspend fun getSketchCount(): Int
}
