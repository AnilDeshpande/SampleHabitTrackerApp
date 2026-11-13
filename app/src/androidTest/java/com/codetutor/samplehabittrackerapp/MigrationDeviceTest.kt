package com.codetutor.samplehabittrackerapp

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabaseFactory
import com.codetutor.samplehabittrackerapp.data.local.Migrations
import com.codetutor.samplehabittrackerapp.data.local.SchemaGuards
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationDeviceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val name = "migration-1-2.db"

    @get:Rule
    val helper = MigrationTestHelper(instrumentation, HabitTrackerDatabase::class.java)

    @After fun tearDown() {
        instrumentation.targetContext.deleteDatabase(name)
    }

    @Test
    fun version1MigratesToVersion2KeepingTheBalanceAndGainingTablesGateAndGuards() {
        instrumentation.targetContext.deleteDatabase(name)
        helper.createDatabase(name, 1).apply {
            execSQL("INSERT INTO app_state (appStateId, pointsBalance) VALUES (1, 42)")
            close()
        }
        // Room validates the migrated schema against the exported 2.json.
        helper.runMigrationsAndValidate(name, 2, true, Migrations.MIGRATION_1_2).close()

        // Reopen through the production builder: SchemaGuards are installed by onOpen after a migration.
        val db = HabitTrackerDatabaseFactory.builder(instrumentation.targetContext, name).build()
        try {
            runBlocking {
                assertEquals(42L, db.appStateDao().balance())
                assertEquals(0L, db.reconciliationLockDao().version())
                assertEquals(0, db.habitDao().active().size)
            }
            val sql = db.openHelper.writableDatabase
            val triggers = sql.query("SELECT name FROM sqlite_master WHERE type = 'trigger'").use { c ->
                buildSet { while (c.moveToNext()) add(c.getString(0)) }
            }
            assertEquals(emptyList<String>(), SchemaGuards.triggerNames.filterNot { it in triggers })
            sql.query("PRAGMA foreign_key_check").use { assertEquals(0, it.count) }
        } finally {
            db.close()
        }
    }
}
