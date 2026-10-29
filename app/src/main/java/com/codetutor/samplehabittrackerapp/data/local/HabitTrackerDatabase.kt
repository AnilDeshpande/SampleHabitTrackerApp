package com.codetutor.samplehabittrackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codetutor.samplehabittrackerapp.data.local.dao.AppStateDao
import com.codetutor.samplehabittrackerapp.data.local.entity.AppStateEntity

@Database(entities = [AppStateEntity::class], version = 1, exportSchema = true)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun appStateDao(): AppStateDao
}

/**
 * The one builder configuration shared by production and tests, so both use the
 * same callback (and later converters, migrations and SchemaGuards). A null
 * [fileName] builds an in-memory database. There is no destructive-migration fallback.
 */
object HabitTrackerDatabaseFactory {
    fun builder(context: Context, fileName: String?): RoomDatabase.Builder<HabitTrackerDatabase> {
        val builder = if (fileName == null) {
            Room.inMemoryDatabaseBuilder(context, HabitTrackerDatabase::class.java)
        } else {
            Room.databaseBuilder(context, HabitTrackerDatabase::class.java, fileName)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        }
        return builder.addCallback(SeedCallback)
    }

    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO app_state (appStateId, pointsBalance) " +
                    "VALUES (${AppStateEntity.SINGLETON_ID}, 0)",
            )
        }
    }
}
