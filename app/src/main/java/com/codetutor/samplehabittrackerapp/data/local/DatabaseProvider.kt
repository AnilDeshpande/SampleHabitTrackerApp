package com.codetutor.samplehabittrackerapp.data.local

import android.content.Context
import androidx.room.RoomDatabase
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Where the database lives; null means in memory (tests). [customize] lets a test adjust the builder, for example
 * to attach a SQL trace; production passes nothing.
 */
data class DatabaseConfig(
    val fileName: String?,
    val customize: (RoomDatabase.Builder<HabitTrackerDatabase>) -> Unit = {},
)

/**
 * Cheap to construct. The first [get] opens and initializes Room once on the
 * injected io dispatcher; concurrent first callers await the same initialization.
 * Dedicated single-thread query and transaction executors are owned by this
 * provider and released by [close].
 */
@Singleton
class DatabaseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: DatabaseConfig,
    private val dispatchers: AppDispatchers,
) {
    private val mutex = Mutex()
    private val queryExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { Thread(it, "room-query") } }
    private val transactionExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { Thread(it, "room-transaction") } }

    @Volatile
    private var database: HabitTrackerDatabase? = null

    suspend fun get(): HabitTrackerDatabase = database ?: mutex.withLock {
        database ?: withContext(dispatchers.io) {
            HabitTrackerDatabaseFactory.builder(context, config.fileName)
                .setQueryExecutor(queryExecutor)
                .setTransactionExecutor(transactionExecutor)
                .also(config.customize)
                .build()
                .also { it.openHelper.writableDatabase } // force create/open off the main thread
        }.also { database = it }
    }

    fun close() {
        database?.close()
        database = null
        queryExecutor.shutdown()
        transactionExecutor.shutdown()
    }
}
