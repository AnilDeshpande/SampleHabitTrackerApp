package com.codetutor.samplehabittrackerapp.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database-level invariants that Room cannot declare as CHECK constraints. Each guard is an
 * idempotent `BEFORE INSERT` / `BEFORE UPDATE` trigger that aborts the statement, created in
 * `onOpen` so it exists after creation and after every migration. Room's post-migration schema
 * validation ignores triggers, so a test asserts that [triggerNames] all exist.
 *
 * Every condition is written so a NULL column cannot make it evaluate to NULL (which would let the
 * row through): nullable columns are compared with IS NULL / IS NOT NULL or wrapped in IFNULL.
 */
object SchemaGuards {

    private class Guard(val table: String, val condition: String, val message: String)

    private val occurrenceShape = """
        NOT (
          (NEW.status = 'OPEN' AND NEW.result IS NULL AND NEW.resolvedAtEpochMillis IS NULL AND NEW.pointsEffect = 0)
          OR (NEW.status = 'FINALIZED' AND IFNULL(NEW.result, '') IN ('SUCCESS', 'MISS')
              AND NEW.resolvedAtEpochMillis IS NOT NULL)
          OR (NEW.status = 'CANCELLED' AND IFNULL(NEW.result, '') = 'CANCELLED'
              AND NEW.resolvedAtEpochMillis IS NOT NULL AND NEW.pointsEffect = 0)
        )
    """.trimIndent()

    private val habitRetirementFields = """
        NOT (
          (NEW.status = 'ACTIVE' AND NEW.retirementDeadlineEpochMillis IS NULL AND NEW.retiredAtEpochMillis IS NULL)
          OR (NEW.status = 'PENDING_RETIREMENT' AND NEW.retirementDeadlineEpochMillis IS NOT NULL
              AND NEW.retiredAtEpochMillis IS NULL)
          OR (NEW.status = 'RETIRED' AND NEW.retirementDeadlineEpochMillis IS NOT NULL
              AND NEW.retiredAtEpochMillis IS NOT NULL)
        )
    """.trimIndent()

    private val habitWeekdays = """
        (NEW.frequency = 'CUSTOM' AND (NEW.selectedWeekdays < 1 OR NEW.selectedWeekdays > 127))
        OR (NEW.frequency != 'CUSTOM' AND NEW.selectedWeekdays != 0)
    """.trimIndent()

    private val guards: List<Pair<String, Guard>> = listOf(
        "occurrence_shape" to Guard("occurrences", occurrenceShape, "invalid occurrence status/result combination"),
        "occurrence_ack" to Guard(
            "occurrences",
            "NEW.missAcknowledgedAtEpochMillis IS NOT NULL AND IFNULL(NEW.result, '') != 'MISS'",
            "only a missed occurrence can be acknowledged",
        ),
        "occurrence_bounds" to Guard(
            "occurrences",
            "NEW.startInclusiveEpochMillis >= NEW.endExclusiveEpochMillis",
            "occurrence start must be before its end",
        ),
        "habit_retirement_fields" to Guard("habits", habitRetirementFields, "retirement fields do not match status"),
        "habit_weekdays" to Guard("habits", habitWeekdays, "weekday selection does not match frequency"),
        "balance_not_negative" to Guard("app_state", "NEW.pointsBalance < 0", "points balance cannot be negative"),
    )

    private const val NAME_CLASH_INSERT =
        "NEW.status != 'RETIRED' AND EXISTS (SELECT 1 FROM habits WHERE nameKey = NEW.nameKey AND status != 'RETIRED')"
    private const val NAME_CLASH_UPDATE =
        "NEW.status != 'RETIRED' AND EXISTS (SELECT 1 FROM habits WHERE nameKey = NEW.nameKey " +
            "AND status != 'RETIRED' AND habitId != NEW.habitId)"

    private fun trigger(name: String, event: String, table: String, condition: String, message: String) =
        "CREATE TRIGGER IF NOT EXISTS $name BEFORE $event ON $table WHEN $condition " +
            "BEGIN SELECT RAISE(ABORT, '$message'); END"

    private val definitions: List<Pair<String, String>> = buildList {
        for ((key, guard) in guards) {
            for (event in listOf("INSERT", "UPDATE")) {
                val name = "guard_${key}_${event.lowercase()}"
                add(name to trigger(name, event, guard.table, guard.condition, guard.message))
            }
        }
        add("guard_habit_name_insert" to trigger("guard_habit_name_insert", "INSERT", "habits", NAME_CLASH_INSERT, "an active habit already has this name"))
        add("guard_habit_name_update" to trigger("guard_habit_name_update", "UPDATE", "habits", NAME_CLASH_UPDATE, "an active habit already has this name"))
    }

    val triggerNames: List<String> = definitions.map { it.first }

    fun install(db: SupportSQLiteDatabase) {
        for ((_, sql) in definitions) db.execSQL(sql)
    }
}
