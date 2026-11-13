package com.codetutor.samplehabittrackerapp

import android.content.Context
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.data.local.DatabaseConfig
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.data.repository.RoomHabitRepository
import com.codetutor.samplehabittrackerapp.domain.rules.BadgeEvaluator
import com.codetutor.samplehabittrackerapp.domain.rules.ContinuityCalculator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.OverviewAssembler
import com.codetutor.samplehabittrackerapp.domain.rules.ScoringEngine
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers

/** A clock the test moves by hand. Thread-safe reads so contention tests can share it. */
class ManualClock(
    start: Instant = Instant.parse("2026-10-11T10:00:00Z"), // a Sunday
    zone: ZoneId = ZoneId.of("UTC"),
) : Clock {
    @Volatile var instant: Instant = start
    @Volatile var currentZone: ZoneId = zone
    override fun now(): Instant = instant
    override fun zone(): ZoneId = currentZone
}

/**
 * The real repository, rules and Room database (in memory, or file-backed when [fileName] is given),
 * with only time controlled. Nothing here is mocked: every assertion reads persisted rows through
 * the production schema, converters and SchemaGuards.
 */
class RepositoryFixture(
    context: Context,
    val fileName: String? = null,
    val clock: ManualClock = ManualClock(),
) {
    private val dispatchers = AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)
    val provider = DatabaseProvider(context, DatabaseConfig(fileName), dispatchers)
    val repository: RoomHabitRepository = buildRepository(provider, clock)

    suspend fun exec(sql: String, vararg args: Any?) {
        provider.get().openHelper.writableDatabase.execSQL(sql, arrayOf(*args))
    }

    suspend fun longs(sql: String): List<Long> =
        provider.get().openHelper.writableDatabase.query(sql).use { c ->
            buildList { while (c.moveToNext()) add(c.getLong(0)) }
        }

    suspend fun strings(sql: String): List<String> =
        provider.get().openHelper.writableDatabase.query(sql).use { c ->
            buildList { while (c.moveToNext()) add(c.getString(0)) }
        }

    suspend fun count(sql: String): Long = longs(sql).single()

    fun close() = provider.close()

    companion object {
        fun buildRepository(provider: DatabaseProvider, clock: Clock): RoomHabitRepository {
            val planner = OccurrencePlanner()
            return RoomHabitRepository(
                provider = provider,
                clock = clock,
                validator = HabitInputValidator(),
                planner = planner,
                scoring = ScoringEngine(),
                badges = BadgeEvaluator(),
                assembler = OverviewAssembler(planner, ContinuityCalculator()),
            )
        }
    }
}
