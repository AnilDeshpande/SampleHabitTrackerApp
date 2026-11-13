package com.codetutor.samplehabittrackerapp

import android.database.sqlite.SQLiteException
import com.codetutor.samplehabittrackerapp.data.local.SchemaGuards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

/**
 * SchemaGuards and foreign-key cases. These are SQL-facing, so they count as minimum-API evidence
 * only when they pass on API 24 (SQLite 3.9); the host run is a development aid.
 */
object GuardScenarios {
    private const val HABIT_COLUMNS =
        "name, nameKey, unit, frequency, selectedWeekdays, creationLocalDate, currentTarget, status, " +
            "retirementDeadlineEpochMillis, retiredAtEpochMillis"

    private fun habitSql(
        name: String = "Read",
        frequency: String = "DAILY",
        weekdays: Int = 0,
        status: String = "ACTIVE",
        deadline: String = "NULL",
        retiredAt: String = "NULL",
    ) = "INSERT INTO habits ($HABIT_COLUMNS) VALUES ('$name', '${name.lowercase()}', 'pages', '$frequency', $weekdays, " +
        "'2026-10-11', '10', '$status', $deadline, $retiredAt)"

    private fun occurrenceSql(
        date: String,
        status: String = "OPEN",
        result: String = "NULL",
        resolvedAt: String = "NULL",
        points: Int = 0,
        ack: String = "NULL",
        start: Long = 1000,
        end: Long = 2000,
    ) = "INSERT INTO occurrences (habitId, startLocalDate, endLocalDate, boundaryZoneId, startInclusiveEpochMillis, " +
        "endExclusiveEpochMillis, effectiveTarget, progress, status, pointsEffect, result, resolvedAtEpochMillis, " +
        "missAcknowledgedAtEpochMillis) VALUES (1, '$date', '$date', 'UTC', $start, $end, '10', '0', '$status', " +
        "$points, ${if (result == "NULL") "NULL" else "'$result'"}, $resolvedAt, $ack)"

    private suspend fun RepositoryFixture.assertAborts(sql: String, messageFragment: String) {
        try {
            exec(sql)
        } catch (e: SQLiteException) {
            assertTrue("'$sql' failed with '${e.message}', expected '$messageFragment'", e.message.orEmpty().contains(messageFragment))
            return
        }
        fail("Expected the guard to abort: $sql")
    }

    suspend fun everyGuardTriggerExistsAfterOpen(fx: RepositoryFixture) {
        val existing = fx.strings("SELECT name FROM sqlite_master WHERE type = 'trigger'").toSet()
        val missing = SchemaGuards.triggerNames.filterNot { it in existing }
        assertEquals("missing guards: $missing", emptyList<String>(), missing)
    }

    suspend fun habitGuards(fx: RepositoryFixture) {
        fx.exec(habitSql(name = "Read"))
        fx.assertAborts(habitSql(name = "Read"), "already has this name")
        fx.exec(habitSql(name = "Write"))
        fx.assertAborts("UPDATE habits SET name = 'Read', nameKey = 'read' WHERE nameKey = 'write'", "already has this name")

        // Weekdays must match the frequency.
        fx.assertAborts(habitSql(name = "A", frequency = "CUSTOM", weekdays = 0), "weekday selection")
        fx.assertAborts(habitSql(name = "B", frequency = "CUSTOM", weekdays = 128), "weekday selection")
        fx.assertAborts(habitSql(name = "C", frequency = "DAILY", weekdays = 3), "weekday selection")
        fx.exec(habitSql(name = "D", frequency = "CUSTOM", weekdays = 21))

        // Retirement fields must match the status.
        fx.assertAborts(habitSql(name = "E", status = "PENDING_RETIREMENT"), "retirement fields")
        fx.assertAborts(habitSql(name = "F", status = "ACTIVE", deadline = "5"), "retirement fields")
        fx.assertAborts(habitSql(name = "G", status = "RETIRED", deadline = "5"), "retirement fields")
        fx.exec(habitSql(name = "H", status = "PENDING_RETIREMENT", deadline = "5"))
        fx.exec(habitSql(name = "I", status = "RETIRED", deadline = "5", retiredAt = "6"))

        // A retired habit does not reserve its name.
        fx.exec(habitSql(name = "I"))
        fx.assertAborts(habitSql(name = "I"), "already has this name")
    }

    suspend fun occurrenceGuards(fx: RepositoryFixture) {
        fx.exec(habitSql(name = "Read"))
        fx.exec(occurrenceSql("2026-10-01"))
        fx.exec(occurrenceSql("2026-10-02", status = "FINALIZED", result = "SUCCESS", resolvedAt = "5", points = 10))
        fx.exec(occurrenceSql("2026-10-03", status = "FINALIZED", result = "MISS", resolvedAt = "5", ack = "9"))
        fx.exec(occurrenceSql("2026-10-04", status = "CANCELLED", result = "CANCELLED", resolvedAt = "5"))

        fx.assertAborts(occurrenceSql("2026-10-05", status = "OPEN", result = "MISS"), "status/result")
        fx.assertAborts(occurrenceSql("2026-10-06", status = "OPEN", resolvedAt = "5"), "status/result")
        fx.assertAborts(occurrenceSql("2026-10-07", status = "OPEN", points = 3), "status/result")
        // A NULL result on a finalized row must be rejected, not slip through three-valued logic.
        fx.assertAborts(occurrenceSql("2026-10-08", status = "FINALIZED", resolvedAt = "5"), "status/result")
        fx.assertAborts(occurrenceSql("2026-10-09", status = "FINALIZED", result = "CANCELLED", resolvedAt = "5"), "status/result")
        fx.assertAborts(occurrenceSql("2026-10-10", status = "FINALIZED", result = "SUCCESS"), "status/result")
        fx.assertAborts(occurrenceSql("2026-10-11", status = "CANCELLED", result = "CANCELLED", resolvedAt = "5", points = 5), "status/result")

        fx.assertAborts(occurrenceSql("2026-10-12", status = "FINALIZED", result = "SUCCESS", resolvedAt = "5", ack = "9"), "acknowledged")
        fx.assertAborts(occurrenceSql("2026-10-13", start = 2000, end = 2000), "start must be before")

        // The same guards apply to updates.
        fx.assertAborts("UPDATE occurrences SET status = 'FINALIZED' WHERE startLocalDate = '2026-10-01'", "status/result")
        fx.assertAborts("UPDATE occurrences SET missAcknowledgedAtEpochMillis = 7 WHERE startLocalDate = '2026-10-02'", "acknowledged")

        // One row per habit and start date.
        try {
            fx.exec(occurrenceSql("2026-10-01"))
            fail("the unique (habitId, startLocalDate) index should reject a duplicate")
        } catch (expected: SQLiteException) {
            // expected
        }
    }

    suspend fun balanceAndForeignKeyGuards(fx: RepositoryFixture) {
        fx.assertAborts("UPDATE app_state SET pointsBalance = -1", "cannot be negative")
        fx.exec("UPDATE app_state SET pointsBalance = 0")

        fx.exec(habitSql(name = "Read"))
        fx.exec(occurrenceSql("2026-10-01"))
        try {
            fx.exec("DELETE FROM habits WHERE habitId = 1")
            fail("ON DELETE RESTRICT should keep a habit that has occurrences")
        } catch (expected: SQLiteException) {
            // expected
        }
    }
}
