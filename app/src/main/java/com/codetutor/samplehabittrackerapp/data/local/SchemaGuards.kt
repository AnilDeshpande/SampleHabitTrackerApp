package com.codetutor.samplehabittrackerapp.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/** SQLite 3.9-compatible guards; no undeclared indexes or modern UPSERT syntax. */
object SchemaGuards {
    private val predicates = mapOf(
        "occurrences" to """
            NOT (
                (NEW.status = 'OPEN' AND NEW.result IS NULL AND NEW.resolvedAtEpochMillis IS NULL AND NEW.pointsEffect = 0)
                OR (NEW.status = 'FINALIZED' AND NEW.result IS NOT NULL AND NEW.result IN ('SUCCESS', 'MISS') AND NEW.resolvedAtEpochMillis IS NOT NULL)
                OR (NEW.status = 'CANCELLED' AND NEW.result IS 'CANCELLED' AND NEW.resolvedAtEpochMillis IS NOT NULL AND NEW.pointsEffect = 0)
            )
            OR (NEW.missAcknowledgedAtEpochMillis IS NOT NULL AND NEW.result IS NOT 'MISS')
            OR NEW.startInclusiveEpochMillis >= NEW.endExclusiveEpochMillis
        """.trimIndent(),
        "habits" to """
            NOT (
                (NEW.status = 'ACTIVE' AND NEW.retirementDeadlineEpochMillis IS NULL AND NEW.retiredAtEpochMillis IS NULL)
                OR (NEW.status = 'PENDING_RETIREMENT' AND NEW.retirementDeadlineEpochMillis IS NOT NULL AND NEW.retiredAtEpochMillis IS NULL)
                OR (NEW.status = 'RETIRED' AND NEW.retirementDeadlineEpochMillis IS NOT NULL AND NEW.retiredAtEpochMillis IS NOT NULL)
            )
            OR (NEW.frequency = 'CUSTOM' AND NEW.selectedWeekdays NOT BETWEEN 1 AND 127)
            OR (NEW.frequency != 'CUSTOM' AND NEW.selectedWeekdays != 0)
            OR (NEW.status != 'RETIRED' AND EXISTS (
                SELECT 1 FROM habits WHERE nameKey = NEW.nameKey AND status != 'RETIRED' AND habitId != NEW.habitId
            ))
        """.trimIndent(),
        "app_state" to "NEW.appStateId != 1 OR NEW.pointsBalance < 0",
        "reconciliation_lock" to "NEW.lockId != 1",
    )

    val statements: Map<String, String> = buildMap {
        predicates.forEach { (table, predicate) ->
            listOf("INSERT", "UPDATE").forEach { operation ->
                val name = "guard_${table}_${operation.lowercase(java.util.Locale.ROOT)}"
                put(name, """
                    CREATE TRIGGER IF NOT EXISTS $name BEFORE $operation ON $table
                    WHEN $predicate
                    BEGIN SELECT RAISE(ABORT, 'Invalid $table state'); END
                """.trimIndent())
            }
        }
        listOf("app_state", "reconciliation_lock").forEach { table ->
            val name = "guard_${table}_delete"
            put(name, "CREATE TRIGGER IF NOT EXISTS $name BEFORE DELETE ON $table BEGIN SELECT RAISE(ABORT, 'Required singleton'); END")
        }
    }

    fun install(database: SupportSQLiteDatabase) {
        statements.values.forEach(database::execSQL)
    }
}
