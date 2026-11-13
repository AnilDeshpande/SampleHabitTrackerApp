package com.codetutor.samplehabittrackerapp.domain.repository

import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputErrors
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.Flow

/** What one catch-up changed. */
data class ReconcileSummary(
    val plannedCount: Int,
    val finalizedCount: Int,
    val missCount: Int,
    val retiredCount: Int,
) {
    val changedAnything: Boolean get() = plannedCount + finalizedCount + retiredCount > 0
}

sealed interface CreateHabitResult {
    data class Created(val habitId: Long, val firstStartLocalDate: LocalDate) : CreateHabitResult

    /** Nothing was saved; every invalid field is reported so the form can show them all. */
    data class InvalidInput(val errors: HabitInputErrors) : CreateHabitResult

    /** An active or pending-retirement habit already has this name, ignoring case. */
    data object DuplicateName : CreateHabitResult
}

/**
 * Durable product truth. Every period-sensitive command runs in one transaction that acquires the
 * database gate, captures one time snapshot, and catches up all habits before its own write. An
 * ordinary failure surfaces as an exception and leaves nothing partially saved.
 */
interface HabitRepository {
    /** Re-emits after every committed change. Fails if the local database cannot be read. */
    fun observeOverview(): Flow<OverviewData>

    suspend fun reconcile(): ReconcileSummary

    suspend fun createHabit(raw: RawHabitInput, locale: Locale): CreateHabitResult

    /** When the next catch-up matters: a period end, a retirement deadline, or a period start. */
    suspend fun nextBoundary(): Instant?
}
