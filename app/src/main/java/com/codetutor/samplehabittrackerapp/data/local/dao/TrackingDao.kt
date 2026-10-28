package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Low-level persistence access. Product writes belong to the future gated repository. */
@Dao
interface TrackingDao {
    @Query("SELECT pointsBalance FROM app_state WHERE appStateId = 1")
    fun observePointsBalance(): Flow<Long>

    @Query("SELECT version FROM reconciliation_lock WHERE lockId = 1")
    suspend fun gateVersion(): Long

    /** Must be the first statement inside a period-sensitive Room transaction. */
    @Query("UPDATE reconciliation_lock SET version = version + 1 WHERE lockId = 1")
    suspend fun acquireGate(): Int
}
