package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AlertNotification
import com.example.data.model.AppSettings
import com.example.data.model.DailySummary
import com.example.data.model.HourlyRecord
import com.example.data.model.ProcessedSketchEntity
import com.example.data.model.TrackingEvent
import com.example.utils.SampleDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrackingEvent::class,
        DailySummary::class,
        HourlyRecord::class,
        AlertNotification::class,
        AppSettings::class,
        ProcessedSketchEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PeopleCountDatabase : RoomDatabase() {

    abstract fun peopleCountDao(): PeopleCountDao
    abstract fun processedSketchDao(): ProcessedSketchDao

    companion object {
        @Volatile
        private var INSTANCE: PeopleCountDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PeopleCountDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeopleCountDatabase::class.java,
                    "people_count_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database.peopleCountDao(), database.processedSketchDao())
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(dao: PeopleCountDao, sketchDao: ProcessedSketchDao? = null) {
            dao.insertSettings(SampleDataProvider.getDefaultSettings())
            dao.insertDailySummary(SampleDataProvider.getInitialDailySummary())
            dao.insertHourlyRecords(SampleDataProvider.getInitialHourlyRecords())
            SampleDataProvider.getInitialAlerts().forEach { dao.insertAlert(it) }
            SampleDataProvider.getInitialEvents().forEach { dao.insertEvent(it) }
            if (sketchDao != null && sketchDao.getSketchCount() == 0) {
                sketchDao.insertSketches(SampleDataProvider.getInitialProcessedSketches())
            }
        }
    }
}
