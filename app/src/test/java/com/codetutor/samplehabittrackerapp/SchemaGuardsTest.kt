package com.codetutor.samplehabittrackerapp

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Host-tier guard run. Guard claims also require the API 24 device tier (SQLite 3.9). */
@RunWith(RobolectricTestRunner::class)
class SchemaGuardsTest {
    private lateinit var fx: RepositoryFixture

    @Before fun setUp() { fx = RepositoryFixture(ApplicationProvider.getApplicationContext()) }
    @After fun tearDown() { fx.close() }

    @Test fun everyGuardTriggerExistsAfterOpen() = runBlocking { GuardScenarios.everyGuardTriggerExistsAfterOpen(fx) }
    @Test fun habitGuards() = runBlocking { GuardScenarios.habitGuards(fx) }
    @Test fun occurrenceGuards() = runBlocking { GuardScenarios.occurrenceGuards(fx) }
    @Test fun balanceAndForeignKeyGuards() = runBlocking { GuardScenarios.balanceAndForeignKeyGuards(fx) }
}
