package com.codetutor.samplehabittrackerapp

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class DatabaseTest {
    @Test fun productionSchemaEnforcesContract() {
        val queries = Executors.newSingleThreadExecutor()
        val transactions = Executors.newSingleThreadExecutor()
        val database = HabitTrackerDatabase.builder(ApplicationProvider.getApplicationContext(), null)
            .setQueryExecutor(queries).setTransactionExecutor(transactions).build()
        try {
            queries.submit { DatabaseContract.verify(database.openHelper.writableDatabase) }.get(30, TimeUnit.SECONDS)
        } finally {
            database.close()
            queries.shutdownNow()
            transactions.shutdownNow()
            queries.awaitTermination(5, TimeUnit.SECONDS)
            transactions.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}
