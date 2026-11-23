package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codetutor.samplehabittrackerapp.data.local.entity.OccurrenceEntity
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult

@Dao
interface OccurrenceDao {
    @Insert
    suspend fun insert(occurrence: OccurrenceEntity): Long

    @Query("SELECT MAX(endExclusiveEpochMillis) FROM occurrences WHERE habitId = :habitId")
    suspend fun latestEndExclusive(habitId: Long): Long?

    @Query("SELECT * FROM occurrences WHERE habitId = :habitId ORDER BY startInclusiveEpochMillis, occurrenceId")
    suspend fun forHabit(habitId: Long): List<OccurrenceEntity>

    /** Elapsed open rows of ACTIVE habits in the total order that makes the balance deterministic. */
    @Query(
        "SELECT o.* FROM occurrences o JOIN habits h ON h.habitId = o.habitId " +
            "WHERE o.status = 'OPEN' AND h.status = 'ACTIVE' AND o.endExclusiveEpochMillis <= :nowEpochMillis " +
            "ORDER BY o.endExclusiveEpochMillis, o.habitId, o.occurrenceId",
    )
    suspend fun elapsedOpenOfActiveHabits(nowEpochMillis: Long): List<OccurrenceEntity>

    /** Conditional on OPEN, so a second invocation updates zero rows and applies nothing twice. */
    @Query(
        "UPDATE occurrences SET status = 'FINALIZED', result = :result, pointsEffect = :pointsEffect, " +
            "resolvedAtEpochMillis = :resolvedAtEpochMillis WHERE occurrenceId = :occurrenceId AND status = 'OPEN'",
    )
    suspend fun finalizeOpen(
        occurrenceId: Long,
        result: OccurrenceResult,
        pointsEffect: Long,
        resolvedAtEpochMillis: Long,
    ): Int

    /** Final retirement cancels the occurrence frozen at confirmation: zero points, no streak or badge effect. */
    @Query(
        "UPDATE occurrences SET status = 'CANCELLED', result = 'CANCELLED', pointsEffect = 0, " +
            "resolvedAtEpochMillis = :resolvedAtEpochMillis WHERE habitId = :habitId AND status = 'OPEN'",
    )
    suspend fun cancelOpen(habitId: Long, resolvedAtEpochMillis: Long): Int

    @Query(
        "SELECT MIN(o.endExclusiveEpochMillis) FROM occurrences o JOIN habits h ON h.habitId = o.habitId " +
            "WHERE o.status = 'OPEN' AND h.status = 'ACTIVE'",
    )
    suspend fun earliestOpenEndOfActiveHabits(): Long?
}
