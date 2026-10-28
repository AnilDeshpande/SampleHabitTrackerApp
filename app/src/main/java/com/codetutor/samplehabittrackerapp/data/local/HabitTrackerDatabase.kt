package com.codetutor.samplehabittrackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codetutor.samplehabittrackerapp.data.local.dao.TrackingDao
import com.codetutor.samplehabittrackerapp.data.local.entity.*

@Database(
    entities = [HabitEntity::class, OccurrenceEntity::class, TargetChangeEntity::class,
        AppStateEntity::class, BadgeEntity::class, NotificationEventEntity::class,
        ReconciliationLockEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StorageConverters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun trackingDao(): TrackingDao

    companion object {
        const val FILE_NAME = "habit-tracker.db"

        /** Shared production/test configuration. Never enable destructive migrations. */
        fun builder(context: Context, name: String? = FILE_NAME): Builder<HabitTrackerDatabase> {
            val builder = if (name == null) {
                Room.inMemoryDatabaseBuilder(context.applicationContext, HabitTrackerDatabase::class.java)
            } else {
                Room.databaseBuilder(context.applicationContext, HabitTrackerDatabase::class.java, name)
            }
            return builder.setJournalMode(JournalMode.WRITE_AHEAD_LOGGING).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("INSERT INTO app_state (appStateId, pointsBalance) VALUES (1, 0)")
                    db.execSQL("INSERT INTO reconciliation_lock (lockId, version) VALUES (1, 0)")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    SchemaGuards.install(db)
                }
            })
        }
    }
}
