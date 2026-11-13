package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal
import java.time.LocalDate

enum class HabitStatus { ACTIVE, PENDING_RETIREMENT, RETIRED }

/**
 * A habit's mutable current configuration and lifecycle state. Results, progress and targets of
 * past periods live on occurrences, never here.
 *
 * [nameKey] is the trimmed name lower-cased with `Locale.ROOT`. [plannedThroughLocalDate] is the
 * planner watermark: every candidate period starting on or before it has been created or skipped.
 */
data class Habit(
    val habitId: Long,
    val name: String,
    val nameKey: String,
    val unit: String,
    val frequency: Frequency,
    val selectedWeekdays: Int,
    val creationLocalDate: LocalDate,
    val currentTarget: BigDecimal,
    val status: HabitStatus,
    val plannedThroughLocalDate: LocalDate?,
    val retirementDeadlineEpochMillis: Long?,
    val retiredAtEpochMillis: Long?,
)
