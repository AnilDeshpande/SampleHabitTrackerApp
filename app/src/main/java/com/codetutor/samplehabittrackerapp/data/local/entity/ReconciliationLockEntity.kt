package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "reconciliation_lock")
data class ReconciliationLockEntity(
    @PrimaryKey val lockId: Int = 1,
    val version: Long = 0,
)
