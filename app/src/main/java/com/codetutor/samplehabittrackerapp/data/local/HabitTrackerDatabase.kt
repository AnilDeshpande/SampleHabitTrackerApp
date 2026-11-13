package com.codetutor.samplehabittrackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codetutor.samplehabittrackerapp.data.local.dao.AppStateDao
import com.codetutor.samplehabittrackerapp.data.local.dao.BadgeDao
import com.codetutor.samplehabittrackerapp.data.local.dao.HabitDao
import com.codetutor.samplehabittrackerapp.data.local.dao.NotificationEventDao
import com.codetutor.samplehabittrackerapp.data.local.dao.OccurrenceDao
import com.codetutor.samplehabittrackerapp.data.local.dao.OverviewDao
import com.codetutor.samplehabittrackerapp.data.local.dao.ReconciliationLockDao
import com.codetutor.samplehabittrackerapp.data.local.entity.AppStateEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.BadgeEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.NotificationEventEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.OccurrenceEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.ReconciliationLockEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.TargetChangeEntity

@Database(
    entities = [
        AppStateEntity::class,
        HabitEntity::class,
        OccurrenceEntity::class,
        TargetChangeEntity::class,
        BadgeEntity::class,
        NotificationEventEntity::class,
        ReconciliationLockEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun appStateDao(): AppStateDao
    abstract fun habitDao(): HabitDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun badgeDao(): BadgeDao
    abstract fun notificationEventDao(): NotificationEventDao
    abstract fun reconciliationLockDao(): ReconciliationLockDao
    abstract fun overviewDao(): OverviewDao
}

/**
 * The one builder configuration shared by production and tests, so both use the same callback,
 * converters, migrations and SchemaGuards. A null [fileName] builds an in-memory database. There
 * is no destructive-migration fallback: user history is never discarded.
 */
object HabitTrackerDatabaseFactory {
    fun builder(context: Context, fileName: String?): RoomDatabase.Builder<HabitTrackerDatabase> {
        val builder = if (fileName == null) {
            Room.inMemoryDatabaseBuilder(context, HabitTrackerDatabase::class.java)
        } else {
            Room.databaseBuilder(context, HabitTrackerDatabase::class.java, fileName)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        }
        return builder
            .addMigrations(*Migrations.ALL)
            .addCallback(SeedAndGuardCallback)
    }

    private object SeedAndGuardCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO app_state (appStateId, pointsBalance) VALUES (${AppStateEntity.SINGLETON_ID}, 0)")
            db.execSQL("INSERT INTO reconciliation_lock (lockId, version) VALUES (${ReconciliationLockEntity.SINGLETON_ID}, 0)")
        }

        /** Runs after creation and after every migration, so the guards always exist. */
        override fun onOpen(db: SupportSQLiteDatabase) {
            SchemaGuards.install(db)
        }
    }
}
