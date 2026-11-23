package com.codetutor.samplehabittrackerapp.data.repository

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import com.codetutor.samplehabittrackerapp.data.local.entity.BadgeEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.NotificationEventEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.OccurrenceEntity
import com.codetutor.samplehabittrackerapp.domain.model.BadgeType
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.NotificationDeliveryState
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import com.codetutor.samplehabittrackerapp.domain.rules.BadgeEvaluator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitSchedule
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.PlanningState
import com.codetutor.samplehabittrackerapp.domain.rules.ScoringEngine
import java.math.BigDecimal
import java.time.LocalDate

/**
 * `ReconcileTracking` (ADR 0002). Runs inside a transaction that already holds the database gate and
 * captured [TimeSnapshot], so every period decision in the transaction uses one time reading.
 *
 * Order: finalize expired retirements, plan started periods for ACTIVE habits, then finalize elapsed
 * OPEN rows of ACTIVE habits in (end, habit, occurrence) order so the points balance is identical
 * whichever command triggered the catch-up. Idempotent: the OPEN-conditional update means a repeat
 * applies nothing twice.
 */
internal class TrackingEngine(
    private val db: HabitTrackerDatabase,
    private val planner: OccurrencePlanner,
    private val scoring: ScoringEngine,
    private val badges: BadgeEvaluator,
) {
    private val habitDao = db.habitDao()
    private val occurrenceDao = db.occurrenceDao()
    private val appStateDao = db.appStateDao()
    private val badgeDao = db.badgeDao()
    private val notificationDao = db.notificationEventDao()

    suspend fun reconcile(snapshot: TimeSnapshot): ReconcileSummary {
        val now = snapshot.epochMillis

        var retired = 0
        for (habit in habitDao.pendingWithExpiredDeadline(now)) {
            occurrenceDao.cancelOpen(habit.habitId, now)
            if (habitDao.markRetired(habit.habitId, habit.retirementDeadlineEpochMillis!!) == 1) retired++
        }

        val activeHabits = habitDao.active()
        var planned = 0
        for (habit in activeHabits) planned += planHabit(habit, snapshot)

        val elapsed = occurrenceDao.elapsedOpenOfActiveHabits(now)
        var finalized = 0
        var misses = 0
        if (elapsed.isNotEmpty()) {
            val habitsById = activeHabits.associateBy { it.habitId }
            val successDates = HashMap<Long, MutableSet<LocalDate>>()
            val awarded = HashMap<Long, Boolean>()
            val startBalance = appStateDao.balance() ?: 0L
            var balance = startBalance

            for (occurrence in elapsed) {
                val habit = habitsById.getValue(occurrence.habitId)
                val result = scoring.resultFor(occurrence.progress, occurrence.effectiveTarget)
                val score = scoring.score(result, balance)
                val updated = occurrenceDao.finalizeOpen(occurrence.occurrenceId, result, score.pointsEffect, now)
                if (updated != 1) continue // another invocation already finalized it
                finalized++
                balance = score.balanceAfter

                if (result == OccurrenceResult.MISS) {
                    misses++
                    notificationDao.insertIfAbsent(
                        NotificationEventEntity(
                            occurrenceId = occurrence.occurrenceId,
                            habitId = habit.habitId,
                            deliveryState = NotificationDeliveryState.PENDING,
                            attemptCount = 0,
                        ),
                    )
                }
                if (habit.frequency == Frequency.DAILY) {
                    awardBadgeIfEarned(habit, occurrence, result, successDates, awarded, now)
                }
            }
            if (balance != startBalance) appStateDao.setBalance(balance)
        }
        return ReconcileSummary(planned, finalized, misses, retired)
    }

    /** Creates every started period of [habit] that is not yet decided. Returns how many rows it inserted. */
    suspend fun planHabit(habit: HabitEntity, snapshot: TimeSnapshot): Int {
        val schedule = HabitSchedule(habit.frequency, habit.selectedWeekdays, habit.creationLocalDate)
        val state = PlanningState(habit.plannedThroughLocalDate, occurrenceDao.latestEndExclusive(habit.habitId))
        val plan = planner.plan(schedule, state, snapshot)
        for (row in plan.occurrences) {
            occurrenceDao.insert(
                OccurrenceEntity(
                    habitId = habit.habitId,
                    startLocalDate = row.startLocalDate,
                    endLocalDate = row.endLocalDate,
                    boundaryZoneId = row.boundaryZoneId,
                    startInclusiveEpochMillis = row.startInclusiveEpochMillis,
                    endExclusiveEpochMillis = row.endExclusiveEpochMillis,
                    effectiveTarget = habit.currentTarget,
                    progress = BigDecimal.ZERO,
                    status = OccurrenceStatus.OPEN,
                    pointsEffect = 0,
                ),
            )
        }
        if (plan.plannedThroughLocalDate != habit.plannedThroughLocalDate) {
            habitDao.setWatermark(habit.habitId, plan.plannedThroughLocalDate)
        }
        return plan.occurrences.size
    }

    private suspend fun awardBadgeIfEarned(
        habit: HabitEntity,
        occurrence: OccurrenceEntity,
        result: OccurrenceResult,
        successDates: MutableMap<Long, MutableSet<LocalDate>>,
        awarded: MutableMap<Long, Boolean>,
        now: Long,
    ) {
        val dates = successDates.getOrPut(habit.habitId) {
            // Loaded before this occurrence was finalized, so it is added below when it succeeded.
            occurrenceDao.forHabit(habit.habitId)
                .filter { it.status == OccurrenceStatus.FINALIZED && it.result == OccurrenceResult.SUCCESS }
                .mapTo(HashSet()) { it.startLocalDate }
        }
        if (result == OccurrenceResult.SUCCESS) dates += occurrence.startLocalDate
        val already = awarded.getOrPut(habit.habitId) { badgeDao.exists(habit.habitId, BadgeType.CONSISTENCY_21) }
        if (already || !badges.qualifies(dates, occurrence.startLocalDate)) return
        badgeDao.insertIfAbsent(
            BadgeEntity(
                habitId = habit.habitId,
                type = BadgeType.CONSISTENCY_21,
                awardedForLocalDate = occurrence.startLocalDate,
                awardedAtEpochMillis = now,
            ),
        )
        awarded[habit.habitId] = true
    }
}
