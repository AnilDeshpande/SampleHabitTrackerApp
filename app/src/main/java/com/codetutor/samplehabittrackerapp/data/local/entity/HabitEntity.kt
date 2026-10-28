package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "habits", indices = [Index(value = ["status", "habitId"]), Index(value = ["nameKey", "status"])])
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val habitId: Long = 0,
    val name: String,
    val nameKey: String,
    val unit: String,
    val frequency: Frequency,
    val selectedWeekdays: Int,
    val creationLocalDate: LocalDate,
    val currentTarget: BigDecimal,
    val status: HabitStatus = HabitStatus.ACTIVE,
    val plannedThroughLocalDate: LocalDate? = null,
    val retirementDeadlineEpochMillis: Long? = null,
    val retiredAtEpochMillis: Long? = null,
)
