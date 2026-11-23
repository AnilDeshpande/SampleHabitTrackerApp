package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import java.time.LocalDate

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun get(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE status = 'ACTIVE' ORDER BY habitId")
    suspend fun active(): List<HabitEntity>

    @Query(
        "SELECT * FROM habits WHERE status = 'PENDING_RETIREMENT' " +
            "AND retirementDeadlineEpochMillis <= :nowEpochMillis ORDER BY habitId",
    )
    suspend fun pendingWithExpiredDeadline(nowEpochMillis: Long): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits WHERE nameKey = :nameKey AND status != 'RETIRED'")
    suspend fun countNonRetiredWithNameKey(nameKey: String): Int

    @Query("UPDATE habits SET plannedThroughLocalDate = :watermark WHERE habitId = :habitId")
    suspend fun setWatermark(habitId: Long, watermark: LocalDate?)

    @Query(
        "UPDATE habits SET status = 'RETIRED', retiredAtEpochMillis = :retiredAtEpochMillis " +
            "WHERE habitId = :habitId AND status = 'PENDING_RETIREMENT'",
    )
    suspend fun markRetired(habitId: Long, retiredAtEpochMillis: Long): Int

    @Query("SELECT MIN(retirementDeadlineEpochMillis) FROM habits WHERE status = 'PENDING_RETIREMENT'")
    suspend fun earliestRetirementDeadline(): Long?
}
