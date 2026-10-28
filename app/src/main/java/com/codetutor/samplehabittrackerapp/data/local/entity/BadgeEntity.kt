package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "badges",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["habitId"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["habitId", "type"], unique = true), Index(value = ["habitId", "awardedForLocalDate"])])
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val badgeId: Long = 0,
    val habitId: Long,
    val type: BadgeType,
    val awardedForLocalDate: LocalDate,
    val awardedAtEpochMillis: Long,
)
