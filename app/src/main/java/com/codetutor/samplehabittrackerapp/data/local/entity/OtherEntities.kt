package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.codetutor.samplehabittrackerapp.domain.model.BadgeType
import com.codetutor.samplehabittrackerapp.domain.model.NotificationDeliveryState
import java.math.BigDecimal
import java.time.LocalDate

/** Every saved target change is kept; a later change with the same effective start supersedes an earlier one. */
@Entity(
    tableName = "target_changes",
    foreignKeys = [
        ForeignKey(HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["habitId", "effectiveStartLocalDate", "changeId"])],
)
data class TargetChangeEntity(
    @PrimaryKey(autoGenerate = true) val changeId: Long = 0,
    val habitId: Long,
    val requestedTarget: BigDecimal,
    val effectiveStartLocalDate: LocalDate,
    val changedAtEpochMillis: Long,
)

/** [awardedForLocalDate] is the last date of the qualifying window. */
@Entity(
    tableName = "badges",
    foreignKeys = [
        ForeignKey(HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [
        Index(value = ["habitId", "type"], unique = true),
        Index(value = ["habitId", "awardedForLocalDate"]),
    ],
)
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val badgeId: Long = 0,
    val habitId: Long,
    val type: BadgeType,
    val awardedForLocalDate: LocalDate,
    val awardedAtEpochMillis: Long,
)

/** One durable outbox event per missed occurrence; notifications are aggregated per habit at dispatch. */
@Entity(
    tableName = "notification_events",
    foreignKeys = [
        ForeignKey(OccurrenceEntity::class, parentColumns = ["occurrenceId"], childColumns = ["occurrenceId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [
        Index(value = ["occurrenceId"], unique = true),
        Index(value = ["deliveryState", "habitId"]),
        Index(value = ["habitId", "deliveryState"]),
    ],
)
data class NotificationEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val occurrenceId: Long,
    val habitId: Long,
    val deliveryState: NotificationDeliveryState,
    val attemptCount: Int,
    val lastAttemptedAtEpochMillis: Long? = null,
)

/** Singleton row (always id 1). Updating it is the first statement of every gated transaction. */
@Entity(tableName = "reconciliation_lock")
data class ReconciliationLockEntity(
    @PrimaryKey val lockId: Long = SINGLETON_ID,
    val version: Long,
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}
