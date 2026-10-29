package com.codetutor.samplehabittrackerapp

import android.content.Context
import android.os.Build
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.data.local.DatabaseConfig
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals

/** Platform facts recorded with every SQL-facing test report (LLD: minimum-API evidence). */
data class SqlEvidence(val apiLevel: Int, val sqliteVersion: String, val journalMode: String) {
    override fun toString() = "api=$apiLevel sqlite=$sqliteVersion journal_mode=$journalMode"
}

/** SQL cases shared by the host and instrumented tiers so the assertions stay identical. */
object DatabaseSmoke {
    /** A fresh database is seeded with the singleton row and exposes it as a one-shot read and a Flow. */
    suspend fun verifySeeded(provider: DatabaseProvider) {
        val dao = provider.get().appStateDao()
        assertEquals(0L, dao.balance())
        assertEquals(0L, dao.observeBalance().first())
    }

    /** A file-backed database is WAL, keeps committed rows across close/reopen, and is not reseeded. */
    suspend fun verifyFileBackedReopen(context: Context, fileName: String): SqlEvidence {
        context.deleteDatabase(fileName)
        val dispatchers = AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)
        val config = DatabaseConfig(fileName)
        val first = DatabaseProvider(context, config, dispatchers)
        try {
            val sql = first.get().openHelper.writableDatabase
            val evidence = SqlEvidence(
                apiLevel = Build.VERSION.SDK_INT,
                sqliteVersion = sql.query("SELECT sqlite_version()").use { it.moveToFirst(); it.getString(0) },
                journalMode = sql.query("PRAGMA journal_mode").use { it.moveToFirst(); it.getString(0) },
            )
            assertEquals("wal", evidence.journalMode.lowercase())
            assertEquals(0L, first.get().appStateDao().balance())
            sql.execSQL("UPDATE app_state SET pointsBalance = 42 WHERE appStateId = 1")
            first.close()

            val second = DatabaseProvider(context, config, dispatchers)
            try {
                assertEquals(42L, second.get().appStateDao().balance())
            } finally {
                second.close()
            }
            return evidence
        } finally {
            first.close()
            context.deleteDatabase(fileName)
        }
    }
}
