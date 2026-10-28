package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "notification_events",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT), ForeignKey(entity = OccurrenceEntity::class, parentColumns = ["occurrenceId"], childColumns = ["occurrenceId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["occurrenceId"], unique = true), Index(value = ["deliveryState", "habitId"]), Index(value = ["habitId", "deliveryState"])])
data class NotificationEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val occurrenceId: Long,
    val habitId: Long,
    val deliveryState: DeliveryState = DeliveryState.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptedAtEpochMillis: Long? = null,
)
