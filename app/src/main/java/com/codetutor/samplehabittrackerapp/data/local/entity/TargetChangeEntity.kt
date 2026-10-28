package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "target_changes",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["habitId", "effectiveStartLocalDate", "changeId"])])
data class TargetChangeEntity(
    @PrimaryKey(autoGenerate = true) val changeId: Long = 0,
    val habitId: Long,
    val requestedTarget: BigDecimal,
    val effectiveStartLocalDate: LocalDate,
    val changedAtEpochMillis: Long,
)
