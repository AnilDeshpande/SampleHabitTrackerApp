package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.FieldResult
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputResult
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.NameError
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import com.codetutor.samplehabittrackerapp.domain.rules.UnitError
import com.codetutor.samplehabittrackerapp.domain.rules.WeekdaysError
import java.math.BigDecimal
import java.time.DayOfWeek
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitInputValidatorTest {
    private val validator = HabitInputValidator()
    private val us = Locale.US
    private val de = Locale.GERMANY

    private fun target(raw: String, locale: Locale = us) = validator.validateTarget(raw, locale)

    private fun assertTarget(expected: String, raw: String, locale: Locale = us) {
        val result = target(raw, locale)
        assertTrue("$raw -> $result", result is FieldResult.Valid)
        assertEquals(0, BigDecimal(expected).compareTo((result as FieldResult.Valid).value))
    }

    private fun assertTargetError(expected: DecimalError, raw: String, locale: Locale = us) {
        assertEquals("input '$raw'", FieldResult.Invalid(expected), target(raw, locale))
    }

    // ---- name and unit ----

    @Test fun nameIsTrimmed() {
        assertEquals(FieldResult.Valid("Meditation"), validator.validateName("  Meditation \n"))
    }

    @Test fun blankNameIsEmpty() {
        assertEquals(FieldResult.Invalid(NameError.EMPTY), validator.validateName("   "))
        assertEquals(FieldResult.Invalid(NameError.EMPTY), validator.validateName(""))
    }

    @Test fun nameLimitIsFiftyCodePointsNotUtf16Units() {
        assertTrue(validator.validateName("a".repeat(50)) is FieldResult.Valid)
        assertEquals(FieldResult.Invalid(NameError.TOO_LONG), validator.validateName("a".repeat(51)))
        // Each emoji is one code point but two UTF-16 units.
        val emoji = "🌱"
        assertTrue(validator.validateName(emoji.repeat(50)) is FieldResult.Valid)
        assertEquals(FieldResult.Invalid(NameError.TOO_LONG), validator.validateName(emoji.repeat(51)))
    }

    @Test fun unitLimitIsTwentyCodePoints() {
        assertTrue(validator.validateUnit("u".repeat(20)) is FieldResult.Valid)
        assertEquals(FieldResult.Invalid(UnitError.TOO_LONG), validator.validateUnit("u".repeat(21)))
        assertEquals(FieldResult.Invalid(UnitError.EMPTY), validator.validateUnit(" "))
        assertEquals(FieldResult.Valid("minutes"), validator.validateUnit(" minutes "))
    }

    // ---- decimals ----

    @Test fun acceptsPlainAndDecimalTargets() {
        assertTarget("10", "10")
        assertTarget("2.5", "2.5")
        assertTarget("0.001", "0.001")
        assertTarget("999999999.999", "999999999.999")
        assertTarget("0.5", ".5")
        assertTarget("5", "5.")
        assertTarget("5", " 5 ")
    }

    @Test fun trailingFractionZerosAndLeadingZerosDoNotCountAgainstLimits() {
        assertTarget("1.123", "1.1230")
        assertTarget("1", "1.0000")
        assertTarget("1", "0000000001")
        assertTarget("100", "100")
    }

    @Test fun rejectsTooManyDigits() {
        assertTargetError(DecimalError.TOO_MANY_INTEGER_DIGITS, "1000000000")
        assertTargetError(DecimalError.TOO_MANY_FRACTION_DIGITS, "0.0001")
        assertTargetError(DecimalError.TOO_MANY_FRACTION_DIGITS, "1.2345")
    }

    @Test fun rejectsSignsGroupingExponentsAndLetters() {
        assertTargetError(DecimalError.NOT_A_NUMBER, "-5")
        assertTargetError(DecimalError.NOT_A_NUMBER, "+5")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1e3")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1E3")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1,000")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1 000")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1,2.3")
        assertTargetError(DecimalError.NOT_A_NUMBER, "ten")
        assertTargetError(DecimalError.NOT_A_NUMBER, "1.2.3")
        assertTargetError(DecimalError.NOT_A_NUMBER, ".")
    }

    @Test fun emptyTargetIsReportedAsEmpty() {
        assertTargetError(DecimalError.EMPTY, "")
        assertTargetError(DecimalError.EMPTY, "   ")
    }

    @Test fun targetMustBePositiveButProgressMayBeZero() {
        assertTargetError(DecimalError.MUST_BE_POSITIVE, "0")
        assertTargetError(DecimalError.MUST_BE_POSITIVE, "0.000")
        assertEquals(
            0,
            BigDecimal.ZERO.compareTo((validator.validateProgress("0", us) as FieldResult.Valid).value),
        )
        assertTrue(validator.validateProgress("0.0", us) is FieldResult.Valid)
    }

    @Test fun usesTheLocalesDecimalSeparatorOnly() {
        assertTarget("2.5", "2,5", de)
        assertTargetError(DecimalError.NOT_A_NUMBER, "2.5", de)
        assertTargetError(DecimalError.NOT_A_NUMBER, "1.234,5", de)
        assertTargetError(DecimalError.NOT_A_NUMBER, "2,5", us)
    }

    // ---- weekdays ----

    @Test fun customNeedsWeekdaysInRange() {
        assertEquals(
            FieldResult.Invalid(WeekdaysError.NONE_SELECTED),
            validator.validateWeekdays(Frequency.CUSTOM, 0),
        )
        assertEquals(
            FieldResult.Invalid(WeekdaysError.OUT_OF_RANGE),
            validator.validateWeekdays(Frequency.CUSTOM, 128),
        )
        assertEquals(FieldResult.Valid(21), validator.validateWeekdays(Frequency.CUSTOM, 21))
    }

    @Test fun nonCustomIgnoresAndClearsWeekdays() {
        assertEquals(FieldResult.Valid(0), validator.validateWeekdays(Frequency.DAILY, 21))
    }

    // ---- whole form ----

    @Test fun validHabitIsNormalizedWithNameKey() {
        val result = validator.validateHabit(
            RawHabitInput("  Meditation ", " minutes ", "10.0", Frequency.DAILY, 0),
            us,
        )
        result as HabitInputResult.Valid
        assertEquals("Meditation", result.input.name)
        assertEquals("meditation", result.input.nameKey)
        assertEquals("minutes", result.input.unit)
        assertEquals("10", result.input.target.toPlainString())
        assertEquals(Weekdays.NONE, result.input.selectedWeekdays)
    }

    @Test fun nameKeyUsesRootLocaleCaseFolding() {
        val result = validator.validateHabit(
            RawHabitInput("TITLE", "u", "1", Frequency.DAILY, 0),
            Locale.forLanguageTag("tr-TR"),
        ) as HabitInputResult.Valid
        assertEquals("title", result.input.nameKey) // a Turkish locale would produce a dotless i
    }

    @Test fun reportsEveryInvalidFieldAtOnce() {
        val result = validator.validateHabit(
            RawHabitInput("", "", "-1", Frequency.CUSTOM, 0),
            us,
        ) as HabitInputResult.Invalid
        assertEquals(NameError.EMPTY, result.errors.name)
        assertEquals(UnitError.EMPTY, result.errors.unit)
        assertEquals(DecimalError.NOT_A_NUMBER, result.errors.target)
        assertEquals(WeekdaysError.NONE_SELECTED, result.errors.weekdays)
    }

    @Test fun customHabitKeepsItsWeekdayMask() {
        val mask = Weekdays.maskOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        assertEquals(21, mask)
        val result = validator.validateHabit(
            RawHabitInput("Reading", "pages", "20", Frequency.CUSTOM, mask),
            us,
        ) as HabitInputResult.Valid
        assertEquals(21, result.input.selectedWeekdays)
    }
}
