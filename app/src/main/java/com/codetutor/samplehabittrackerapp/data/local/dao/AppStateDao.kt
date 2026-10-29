package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("SELECT pointsBalance FROM app_state WHERE appStateId = 1")
    fun observeBalance(): Flow<Long?>

    @Query("SELECT pointsBalance FROM app_state WHERE appStateId = 1")
    suspend fun balance(): Long?
}
