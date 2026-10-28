package com.codetutor.samplehabittrackerapp

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import com.codetutor.samplehabittrackerapp.data.local.SchemaGuards
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseDeviceTest {
    @Test fun platformSqliteContractAndReopen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "foundation-contract-test.db"
        context.deleteDatabase(name)
        val queries = Executors.newSingleThreadExecutor()
        val transactions = Executors.newSingleThreadExecutor()
        fun open() = HabitTrackerDatabase.builder(context, name)
            .setQueryExecutor(queries).setTransactionExecutor(transactions).build()
        var database = open()
        try {
            val sqlite = database.openHelper.writableDatabase
            sqlite.query("SELECT sqlite_version()").use {
                assertTrue(it.moveToFirst())
                Log.i("FoundationSqlite", "API ${Build.VERSION.SDK_INT}, SQLite ${it.getString(0)}")
            }
            sqlite.query("PRAGMA journal_mode").use {
                assertTrue(it.moveToFirst())
                assertEquals("wal", it.getString(0))
            }
            DatabaseContract.verify(sqlite)
            // Simulate a missing trigger; the shared onOpen callback must recreate it.
            sqlite.execSQL("DROP TRIGGER ${SchemaGuards.statements.keys.first()}")
            database.close()
            database = open()
            val reopened = database.openHelper.writableDatabase
            assertEquals(2L, DatabaseContract.scalar(reopened, "SELECT COUNT(*) FROM habits"))
            assertEquals(SchemaGuards.statements.size.toLong(), DatabaseContract.scalar(reopened, "SELECT COUNT(*) FROM sqlite_master WHERE type = 'trigger'"))
        } finally {
            database.close()
            queries.shutdownNow()
            transactions.shutdownNow()
            queries.awaitTermination(5, TimeUnit.SECONDS)
            transactions.awaitTermination(5, TimeUnit.SECONDS)
            context.deleteDatabase(name)
        }
    }
}
