package com.codetutor.samplehabittrackerapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Never migrate by discarding user history. Each migration is exercised by an instrumented test. */
object Migrations {
    /**
     * Version 2 adds the habit, occurrence, target-change, badge, outbox and gate tables. The DDL
     * is copied from the exported `schemas/.../2.json` so Room's post-migration validation matches;
     * `app_state` is untouched, so the points balance survives. SchemaGuards are installed by the
     * `onOpen` callback after this runs.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `habits` (`habitId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `nameKey` TEXT NOT NULL, `unit` TEXT NOT NULL, `frequency` TEXT NOT NULL, `selectedWeekdays` INTEGER NOT NULL, `creationLocalDate` TEXT NOT NULL, `currentTarget` TEXT NOT NULL, `status` TEXT NOT NULL, `plannedThroughLocalDate` TEXT, `retirementDeadlineEpochMillis` INTEGER, `retiredAtEpochMillis` INTEGER)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_habits_status_habitId` ON `habits` (`status`, `habitId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_habits_nameKey_status` ON `habits` (`nameKey`, `status`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `occurrences` (`occurrenceId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `startLocalDate` TEXT NOT NULL, `endLocalDate` TEXT NOT NULL, `boundaryZoneId` TEXT NOT NULL, `startInclusiveEpochMillis` INTEGER NOT NULL, `endExclusiveEpochMillis` INTEGER NOT NULL, `effectiveTarget` TEXT NOT NULL, `progress` TEXT NOT NULL, `status` TEXT NOT NULL, `pointsEffect` INTEGER NOT NULL, `result` TEXT, `resolvedAtEpochMillis` INTEGER, `missAcknowledgedAtEpochMillis` INTEGER, FOREIGN KEY(`habitId`) REFERENCES `habits`(`habitId`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_occurrences_habitId_startLocalDate` ON `occurrences` (`habitId`, `startLocalDate`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_occurrences_status_endExclusiveEpochMillis_habitId_occurrenceId` ON `occurrences` (`status`, `endExclusiveEpochMillis`, `habitId`, `occurrenceId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_occurrences_habitId_result_missAcknowledgedAtEpochMillis` ON `occurrences` (`habitId`, `result`, `missAcknowledgedAtEpochMillis`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `target_changes` (`changeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `requestedTarget` TEXT NOT NULL, `effectiveStartLocalDate` TEXT NOT NULL, `changedAtEpochMillis` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habits`(`habitId`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_target_changes_habitId_effectiveStartLocalDate_changeId` ON `target_changes` (`habitId`, `effectiveStartLocalDate`, `changeId`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `badges` (`badgeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `type` TEXT NOT NULL, `awardedForLocalDate` TEXT NOT NULL, `awardedAtEpochMillis` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habits`(`habitId`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_badges_habitId_type` ON `badges` (`habitId`, `type`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_badges_habitId_awardedForLocalDate` ON `badges` (`habitId`, `awardedForLocalDate`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `notification_events` (`eventId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `occurrenceId` INTEGER NOT NULL, `habitId` INTEGER NOT NULL, `deliveryState` TEXT NOT NULL, `attemptCount` INTEGER NOT NULL, `lastAttemptedAtEpochMillis` INTEGER, FOREIGN KEY(`occurrenceId`) REFERENCES `occurrences`(`occurrenceId`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`habitId`) REFERENCES `habits`(`habitId`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_notification_events_occurrenceId` ON `notification_events` (`occurrenceId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_notification_events_deliveryState_habitId` ON `notification_events` (`deliveryState`, `habitId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_notification_events_habitId_deliveryState` ON `notification_events` (`habitId`, `deliveryState`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `reconciliation_lock` (`lockId` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`lockId`))",
            )
            db.execSQL(
                "INSERT INTO reconciliation_lock (lockId, version) VALUES (1, 0)",
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
