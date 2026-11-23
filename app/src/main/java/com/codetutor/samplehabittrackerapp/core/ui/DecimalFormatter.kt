package com.codetutor.samplehabittrackerapp.core.ui

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Shows an exact stored decimal with the device locale's separator, without grouping, up to 3 fraction digits. */
@Singleton
class DecimalFormatter @Inject constructor() {
    fun format(value: BigDecimal, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            minimumFractionDigits = 0
            maximumFractionDigits = 3
        }.format(value)
}
