package com.codetutor.samplehabittrackerapp.di

import com.codetutor.samplehabittrackerapp.data.repository.RoomHabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.rules.BadgeEvaluator
import com.codetutor.samplehabittrackerapp.domain.rules.ContinuityCalculator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.OverviewAssembler
import com.codetutor.samplehabittrackerapp.domain.rules.ScoringEngine
import com.codetutor.samplehabittrackerapp.domain.rules.TargetPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The pure rule services (real in every test graph) and the repository binding. */
@Module
@InstallIn(SingletonComponent::class)
object TrackingModule {
    @Provides @Singleton fun validator() = HabitInputValidator()
    @Provides @Singleton fun planner() = OccurrencePlanner()
    @Provides @Singleton fun targetPolicy() = TargetPolicy()
    @Provides @Singleton fun scoring() = ScoringEngine()
    @Provides @Singleton fun continuity() = ContinuityCalculator()
    @Provides @Singleton fun badges() = BadgeEvaluator()

    @Provides
    @Singleton
    fun overviewAssembler(planner: OccurrencePlanner, continuity: ContinuityCalculator) =
        OverviewAssembler(planner, continuity)

    @Provides
    @Singleton
    fun habitRepository(repository: RoomHabitRepository): HabitRepository = repository
}
