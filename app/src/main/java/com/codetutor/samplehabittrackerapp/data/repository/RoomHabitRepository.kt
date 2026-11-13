package com.codetutor.samplehabittrackerapp.data.repository

import androidx.room.withTransaction
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.core.time.snapshot
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import com.codetutor.samplehabittrackerapp.data.local.toDomain
import com.codetutor.samplehabittrackerapp.domain.model.HabitStatus
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import com.codetutor.samplehabittrackerapp.domain.rules.BadgeEvaluator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputResult
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitSchedule
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.OverviewAssembler
import com.codetutor.samplehabittrackerapp.domain.rules.PlanningState
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import com.codetutor.samplehabittrackerapp.domain.rules.ScoringEngine
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest

/**
 * Room-backed repository. Each command is one transaction: acquire the gate (the first statement),
 * capture one [com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot], catch up every habit,
 * then make the command's own write. Business rejections return a result and still commit the
 * catch-up; an exception rolls the whole transaction back, including the gate increment.
 */
@Singleton
class RoomHabitRepository @Inject constructor(
    private val provider: DatabaseProvider,
    private val clock: Clock,
    private val validator: HabitInputValidator,
    private val planner: OccurrencePlanner,
    private val scoring: ScoringEngine,
    private val badges: BadgeEvaluator,
    private val assembler: OverviewAssembler,
) : HabitRepository {

    private suspend fun engine() = TrackingEngine(provider.get(), planner, scoring, badges)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOverview(): Flow<OverviewData> = flow {
        val db = provider.get()
        emitAll(
            db.invalidationTracker
                .createFlow("app_state", "habits", "occurrences", "badges")
                .mapLatest {
                    val rows = db.overviewDao().read()
                    assembler.assemble(
                        pointsBalance = rows.pointsBalance,
                        habits = rows.habits.map { it.toDomain() },
                        occurrences = rows.occurrences.map { it.toDomain() },
                        habitIdsWithBadge = rows.habitIdsWithBadge,
                        snapshot = clock.snapshot(),
                    )
                }
                .distinctUntilChanged(),
        )
    }

    override suspend fun reconcile(): ReconcileSummary {
        val db = provider.get()
        val engine = engine()
        return db.withTransaction {
            db.reconciliationLockDao().acquire()
            engine.reconcile(clock.snapshot())
        }
    }

    override suspend fun createHabit(raw: RawHabitInput, locale: Locale): CreateHabitResult {
        val db = provider.get()
        val engine = engine()
        return db.withTransaction {
            db.reconciliationLockDao().acquire()
            val snapshot = clock.snapshot()
            engine.reconcile(snapshot)

            when (val validation = validator.validateHabit(raw, locale)) {
                is HabitInputResult.Invalid -> CreateHabitResult.InvalidInput(validation.errors)
                is HabitInputResult.Valid -> {
                    val input = validation.input
                    if (db.habitDao().countNonRetiredWithNameKey(input.nameKey) > 0) {
                        CreateHabitResult.DuplicateName
                    } else {
                        val inserted = HabitEntity(
                            name = input.name,
                            nameKey = input.nameKey,
                            unit = input.unit,
                            frequency = input.frequency,
                            selectedWeekdays = input.selectedWeekdays,
                            creationLocalDate = snapshot.localDate,
                            currentTarget = input.target,
                            status = HabitStatus.ACTIVE,
                        )
                        val habit = inserted.copy(habitId = db.habitDao().insert(inserted))
                        // Open the first period now if it has started, so the overview shows it at once.
                        engine.planHabit(habit, snapshot)
                        CreateHabitResult.Created(
                            habitId = habit.habitId,
                            firstStartLocalDate = planner.firstStartDate(
                                HabitSchedule(input.frequency, input.selectedWeekdays, snapshot.localDate),
                            ),
                        )
                    }
                }
            }
        }
    }

    override suspend fun nextBoundary(): Instant? {
        val db = provider.get()
        val zone = clock.zone()
        val candidates = mutableListOf<Long>()
        db.occurrenceDao().earliestOpenEndOfActiveHabits()?.let(candidates::add)
        db.habitDao().earliestRetirementDeadline()?.let(candidates::add)
        for (habit in db.habitDao().active()) {
            val state = PlanningState(habit.plannedThroughLocalDate, db.occurrenceDao().latestEndExclusive(habit.habitId))
            planner.nextOccurrence(
                HabitSchedule(habit.frequency, habit.selectedWeekdays, habit.creationLocalDate),
                state,
                zone,
            )?.let { candidates += it.startInstant.toEpochMilli() }
        }
        return candidates.minOrNull()?.let(Instant::ofEpochMilli)
    }
}
