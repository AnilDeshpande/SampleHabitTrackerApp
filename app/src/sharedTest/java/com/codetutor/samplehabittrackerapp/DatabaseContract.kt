package com.codetutor.samplehabittrackerapp

import androidx.sqlite.db.SupportSQLiteDatabase
import com.codetutor.samplehabittrackerapp.data.local.SchemaGuards
import org.junit.Assert.*

/** Identical SQL cases run against host SQLite and Android's platform SQLite. */
object DatabaseContract {
    fun scalar(db: SupportSQLiteDatabase, sql: String): Long = db.query(sql).use {
        check(it.moveToFirst())
        it.getLong(0)
    }

    fun verify(db: SupportSQLiteDatabase) {
        assertEquals(0L, scalar(db, "SELECT pointsBalance FROM app_state WHERE appStateId = 1"))
        assertEquals(0L, scalar(db, "SELECT version FROM reconciliation_lock WHERE lockId = 1"))
        val guards = db.query("SELECT name, sql FROM sqlite_master WHERE type = 'trigger'").use { cursor ->
            buildMap { while (cursor.moveToNext()) put(cursor.getString(0), cursor.getString(1)) }
        }
        assertEquals(SchemaGuards.statements.keys, guards.keys)
        fun normalized(sql: String) = sql.replace("IF NOT EXISTS ", "").replace(Regex("\\s+"), " ").trim()
        SchemaGuards.statements.forEach { (name, definition) ->
            assertEquals(normalized(definition), normalized(guards.getValue(name)))
        }

        db.execSQL("""INSERT INTO habits (habitId, name, nameKey, unit, frequency, selectedWeekdays, creationLocalDate, currentTarget, status)
            VALUES (1, 'Walk', 'walk', 'steps', 'DAILY', 0, '2026-10-28', '10', 'ACTIVE')""")
        reject(db, "UPDATE habits SET selectedWeekdays = 1 WHERE habitId = 1")
        reject(db, "UPDATE habits SET frequency = 'CUSTOM' WHERE habitId = 1")
        reject(db, "UPDATE habits SET status = 'PENDING_RETIREMENT' WHERE habitId = 1")
        reject(db, "UPDATE habits SET status = 'RETIRED', retiredAtEpochMillis = 10 WHERE habitId = 1")
        reject(db, "UPDATE habits SET retirementDeadlineEpochMillis = 10 WHERE habitId = 1")
        reject(db, "INSERT INTO habits SELECT 2, name, nameKey, unit, frequency, selectedWeekdays, creationLocalDate, currentTarget, status, plannedThroughLocalDate, retirementDeadlineEpochMillis, retiredAtEpochMillis FROM habits WHERE habitId = 1")

        db.execSQL("""INSERT INTO occurrences (occurrenceId, habitId, startLocalDate, endLocalDate, boundaryZoneId,
            startInclusiveEpochMillis, endExclusiveEpochMillis, effectiveTarget, progress, status, pointsEffect)
            VALUES (1, 1, '2026-10-28', '2026-10-29', 'UTC', 0, 86400000, '10', '0', 'OPEN', 0)""")
        reject(db, "UPDATE occurrences SET pointsEffect = 10 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET status = 'FINALIZED', resolvedAtEpochMillis = 1 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET status = 'CANCELLED', resolvedAtEpochMillis = 1 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET result = 'MISS' WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET endExclusiveEpochMillis = 0 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET missAcknowledgedAtEpochMillis = 1 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET habitId = 99 WHERE occurrenceId = 1")
        reject(db, "DELETE FROM habits WHERE habitId = 1")
        db.execSQL("UPDATE occurrences SET status = 'FINALIZED', result = 'MISS', resolvedAtEpochMillis = 86400000 WHERE occurrenceId = 1")
        db.execSQL("UPDATE occurrences SET missAcknowledgedAtEpochMillis = 86400001 WHERE occurrenceId = 1")
        reject(db, "UPDATE occurrences SET result = 'SUCCESS' WHERE occurrenceId = 1")
        db.execSQL("INSERT INTO notification_events (eventId, occurrenceId, habitId, deliveryState, attemptCount) VALUES (1, 1, 1, 'PENDING', 0)")
        reject(db, "INSERT INTO notification_events (eventId, occurrenceId, habitId, deliveryState, attemptCount) VALUES (2, 1, 1, 'PENDING', 0)")

        reject(db, "UPDATE app_state SET pointsBalance = -1")
        reject(db, "INSERT INTO app_state VALUES (2, 0)")
        reject(db, "DELETE FROM app_state")
        reject(db, "INSERT INTO reconciliation_lock VALUES (2, 0)")
        reject(db, "DELETE FROM reconciliation_lock")
        db.beginTransaction()
        try {
            db.execSQL("UPDATE reconciliation_lock SET version = version + 1 WHERE lockId = 1")
            assertEquals(1L, scalar(db, "SELECT version FROM reconciliation_lock"))
        } finally {
            db.endTransaction() // Roll back the gate along with the transaction.
        }
        assertEquals(0L, scalar(db, "SELECT version FROM reconciliation_lock"))

        db.execSQL("UPDATE habits SET status = 'RETIRED', retirementDeadlineEpochMillis = 1, retiredAtEpochMillis = 2 WHERE habitId = 1")
        db.execSQL("""INSERT INTO habits (habitId, name, nameKey, unit, frequency, selectedWeekdays, creationLocalDate, currentTarget, status)
            VALUES (2, 'Walk', 'walk', 'steps', 'CUSTOM', 127, '2026-10-28', '10', 'ACTIVE')""")
        reject(db, "UPDATE habits SET status = 'ACTIVE', retirementDeadlineEpochMillis = NULL, retiredAtEpochMillis = NULL WHERE habitId = 1")
        assertEquals(2L, scalar(db, "SELECT COUNT(*) FROM habits"))
    }

    private fun reject(db: SupportSQLiteDatabase, sql: String) {
        try {
            db.execSQL(sql)
            fail("Invalid SQL was accepted: $sql")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // Real platform constraint failure, not a mocked DAO outcome.
        }
    }
}
