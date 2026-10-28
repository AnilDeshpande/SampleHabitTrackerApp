package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "occurrences",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["habitId", "startLocalDate"], unique = true), Index(value = ["status", "endExclusiveEpochMillis", "habitId", "occurrenceId"]), Index(value = ["habitId", "result", "missAcknowledgedAtEpochMillis"])])
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val occurrenceId: Long = 0,
    val habitId: Long,
    val startLocalDate: LocalDate,
    val endLocalDate: LocalDate,
    val boundaryZoneId: String,
    val startInclusiveEpochMillis: Long,
    val endExclusiveEpochMillis: Long,
    val effectiveTarget: BigDecimal,
    val progress: BigDecimal = BigDecimal.ZERO,
    val status: OccurrenceStatus = OccurrenceStatus.OPEN,
    val pointsEffect: Long = 0,
    val result: OccurrenceResult? = null,
    val resolvedAtEpochMillis: Long? = null,
    val missAcknowledgedAtEpochMillis: Long? = null,
)
