package com.codetutor.samplehabittrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codetutor.samplehabittrackerapp.data.local.entity.BadgeEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.NotificationEventEntity
import com.codetutor.samplehabittrackerapp.domain.model.BadgeType

@Dao
interface ReconciliationLockDao {
    /** The gate: must be the first statement of every period-sensitive transaction. */
    @Query("UPDATE reconciliation_lock SET version = version + 1 WHERE lockId = 1")
    suspend fun acquire(): Int

    @Query("SELECT version FROM reconciliation_lock WHERE lockId = 1")
    suspend fun version(): Long?
}

@Dao
interface BadgeDao {
    /** The unique (habitId, type) index makes a second award a no-op; returns -1 when ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(badge: BadgeEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM badges WHERE habitId = :habitId AND type = :type)")
    suspend fun exists(habitId: Long, type: BadgeType): Boolean
}

@Dao
interface NotificationEventDao {
    /** One event per missed occurrence; the unique occurrence index makes a repeat a no-op. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(event: NotificationEventEntity): Long

    @Query("SELECT * FROM notification_events ORDER BY eventId")
    suspend fun all(): List<NotificationEventEntity>
}
