package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.OccurrenceEntity

/** Everything the overview needs, read inside one transaction so it is a consistent snapshot. */
data class OverviewRows(
    val pointsBalance: Long,
    val habits: List<HabitEntity>,
    val occurrences: List<OccurrenceEntity>,
    val habitIdsWithBadge: Set<Long>,
)

@Dao
abstract class OverviewDao {
    @Query("SELECT pointsBalance FROM app_state WHERE appStateId = 1")
    abstract suspend fun balance(): Long?

    @Query("SELECT * FROM habits WHERE status != 'RETIRED' ORDER BY habitId")
    abstract suspend fun nonRetiredHabits(): List<HabitEntity>

    @Query(
        "SELECT o.* FROM occurrences o JOIN habits h ON h.habitId = o.habitId WHERE h.status != 'RETIRED' " +
            "ORDER BY o.habitId, o.startInclusiveEpochMillis, o.occurrenceId",
    )
    abstract suspend fun occurrencesOfNonRetiredHabits(): List<OccurrenceEntity>

    @Query("SELECT habitId FROM badges")
    abstract suspend fun badgeHabitIds(): List<Long>

    @Transaction
    open suspend fun read(): OverviewRows = OverviewRows(
        pointsBalance = balance() ?: 0L,
        habits = nonRetiredHabits(),
        occurrences = occurrencesOfNonRetiredHabits(),
        habitIdsWithBadge = badgeHabitIds().toSet(),
    )
}
