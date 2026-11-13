package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "occurrences",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["habitId"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["habitId", "startLocalDate"], unique = true),
        Index(value = ["status", "endExclusiveEpochMillis", "habitId", "occurrenceId"]),
        Index(value = ["habitId", "result", "missAcknowledgedAtEpochMillis"]),
    ],
)
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val occurrenceId: Long = 0,
    val habitId: Long,
    val startLocalDate: LocalDate,
    val endLocalDate: LocalDate,
    val boundaryZoneId: String,
    val startInclusiveEpochMillis: Long,
    val endExclusiveEpochMillis: Long,
    val effectiveTarget: BigDecimal,
    val progress: BigDecimal,
    val status: OccurrenceStatus,
    val pointsEffect: Long,
    val result: OccurrenceResult? = null,
    val resolvedAtEpochMillis: Long? = null,
    val missAcknowledgedAtEpochMillis: Long? = null,
)
