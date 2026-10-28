package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.*
import com.codetutor.samplehabittrackerapp.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val appStateId: Int = 1,
    val pointsBalance: Long = 0,
)
