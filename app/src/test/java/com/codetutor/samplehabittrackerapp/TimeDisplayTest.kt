package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.ui.time.TimeDisplay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Display zone and 12/24-hour style for boundary text. The Android setting itself is exercised on devices. */
@RunWith(RobolectricTestRunner::class)
class TimeDisplayTest {
    private val endsAt = Instant.parse("2026-10-19T00:00:00Z")
    private val utc24 = TimeDisplay(ZoneId.of("UTC"), is24Hour = true).formats(Locale.US)

    @Test fun twentyFourHourStyleShowsTheEndAsMidnight() {
        assertEquals("Mon 19 Oct 00:00", utc24.dateTime(endsAt))
        assertEquals("00:00", utc24.time(endsAt))
    }

    @Test fun twelveHourStyleShowsAnAmPmClock() {
        val text = TimeDisplay(ZoneId.of("UTC"), is24Hour = false).formats(Locale.US).time(endsAt)
        assertTrue("12-hour time, got '$text'", text.startsWith("12:00") && text.contains("AM"))
    }

    @Test fun theZoneInUseDecidesTheLocalDateAndTime() {
        val tokyo = TimeDisplay(ZoneId.of("Asia/Tokyo"), is24Hour = true).formats(Locale.US)
        assertEquals("Mon 19 Oct 09:00", tokyo.dateTime(endsAt))
        val losAngeles = TimeDisplay(ZoneId.of("America/Los_Angeles"), is24Hour = true).formats(Locale.US)
        assertEquals("Sun 18 Oct 17:00", losAngeles.dateTime(endsAt))
    }

    @Test fun aLocalDateStartsAtMidnightInTheCurrentZone() {
        assertEquals("Mon 19 Oct 00:00", utc24.startOfDay(LocalDate.parse("2026-10-19")))
        val tokyo = TimeDisplay(ZoneId.of("Asia/Tokyo"), is24Hour = true).formats(Locale.US)
        assertEquals("Mon 19 Oct 00:00", tokyo.startOfDay(LocalDate.parse("2026-10-19")))
        assertEquals("Mon 19 Oct", utc24.date(LocalDate.parse("2026-10-19")))
    }
}
