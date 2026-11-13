package com.codetutor.samplehabittrackerapp.domain.model

import java.time.DayOfWeek

/**
 * Weekday selection as a bit mask: Monday is bit 0 through Sunday bit 6, so Monday, Wednesday and
 * Friday is 21. A non-Custom habit stores 0; a Custom habit stores 1..127.
 */
object Weekdays {
    const val NONE = 0
    const val ALL = 127

    fun bit(day: DayOfWeek): Int = 1 shl (day.value - 1)

    fun maskOf(vararg days: DayOfWeek): Int = days.fold(NONE) { mask, day -> mask or bit(day) }

    fun contains(mask: Int, day: DayOfWeek): Boolean = mask and bit(day) != 0

    fun isValidCustomMask(mask: Int): Boolean = mask in 1..ALL
}
