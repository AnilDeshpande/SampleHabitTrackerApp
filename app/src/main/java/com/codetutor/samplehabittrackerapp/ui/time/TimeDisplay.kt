package com.codetutor.samplehabittrackerapp.ui.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * How the device currently wants dates and times shown: its timezone and its 12/24-hour setting. Both can
 * change while a screen stays composed, so screens read them through [rememberTimeDisplay] and never cache
 * a zone or a formatter across a system time change.
 */
data class TimeDisplay(val zone: ZoneId, val is24Hour: Boolean) {

    /** Formats instants and local dates for [locale] in this zone and clock style. */
    fun formats(locale: Locale): TimeFormats = TimeFormats(this, locale)

    companion object {
        fun read(context: Context): TimeDisplay =
            TimeDisplay(TimeZone.getDefault().toZoneId(), DateFormat.is24HourFormat(context))
    }
}

class TimeFormats(private val display: TimeDisplay, locale: Locale) {
    private val dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN, locale)
    private val timeFormat = DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(locale, if (display.is24Hour) "Hm" else "hm"),
        locale,
    )

    fun date(instant: Instant): String = instant.atZone(display.zone).format(dateFormat)
    fun time(instant: Instant): String = instant.atZone(display.zone).format(timeFormat)
    fun dateTime(instant: Instant): String = "${date(instant)} ${time(instant)}"

    fun date(date: LocalDate): String = date.format(dateFormat)

    /** [date] at the start of its day in the current zone, as a date and time: "Mon 19 Oct 00:00". */
    fun startOfDay(date: LocalDate): String = dateTime(date.atStartOfDay(display.zone).toInstant())

    private companion object {
        const val DATE_PATTERN = "EEE d MMM"
    }
}

/**
 * The current [TimeDisplay], refreshed when the system reports a time, date or timezone change. The 12/24-hour
 * setting is announced on the time-changed broadcast. Registered only while the screen is composed.
 */
@Composable
fun rememberTimeDisplay(): TimeDisplay {
    val context = LocalContext.current
    var display by remember(context) { mutableStateOf(TimeDisplay.read(context)) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                display = TimeDisplay.read(context)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        // A change that arrived between composition and registration must not be missed.
        display = TimeDisplay.read(context)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return display
}
