package com.codetutor.samplehabittrackerapp.data.local

import android.content.Context
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Process-owned database. No synchronous caller can accidentally open it on main. */
@Singleton
class DatabaseProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) {
    private val initialization = Mutex()
    private var database: HabitTrackerDatabase? = null
    private var executors: List<ExecutorService> = emptyList()

    suspend fun get(): HabitTrackerDatabase = withContext(dispatchers.io) {
        initialization.withLock {
            database ?: run {
                val queries = Executors.newSingleThreadExecutor()
                val transactions = Executors.newSingleThreadExecutor()
                val candidate = HabitTrackerDatabase.builder(context)
                    .setQueryExecutor(queries)
                    .setTransactionExecutor(transactions)
                    .build()
                try {
                    candidate.openHelper.writableDatabase // Complete guards before publishing.
                    candidate.also {
                        database = it
                        executors = listOf(queries, transactions)
                    }
                } catch (failure: Throwable) {
                    candidate.close()
                    queries.shutdownNow()
                    transactions.shutdownNow()
                    throw failure
                }
            }
        }
    }

    /** Owner teardown after all consumers stop; production lifetime is the process. */
    suspend fun close() = withContext(dispatchers.io) {
        initialization.withLock {
            database?.close()
            database = null
            executors.forEach { it.shutdownNow() }
            executors.forEach { it.awaitTermination(5, TimeUnit.SECONDS) }
            executors = emptyList()
        }
    }
}
