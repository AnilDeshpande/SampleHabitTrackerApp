package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.MAX_FRACTION_DIGITS
import com.codetutor.samplehabittrackerapp.domain.model.MAX_INTEGER_DIGITS
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.model.normalized
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

sealed interface FieldResult<out T, out E> {
    data class Valid<T>(val value: T) : FieldResult<T, Nothing>
    data class Invalid<E>(val error: E) : FieldResult<Nothing, E>
}

enum class NameError { EMPTY, TOO_LONG }
enum class UnitError { EMPTY, TOO_LONG }
enum class DecimalError {
    EMPTY,

    /** Anything other than digits and one locale decimal separator: signs, grouping, exponents, letters. */
    NOT_A_NUMBER,
    TOO_MANY_INTEGER_DIGITS,
    TOO_MANY_FRACTION_DIGITS,
    MUST_BE_POSITIVE,
}

enum class WeekdaysError { NONE_SELECTED, OUT_OF_RANGE }

/** Unparsed form values. Kept as typed text so a correction never loses what the user entered. */
data class RawHabitInput(
    val name: String,
    val unit: String,
    val target: String,
    val frequency: Frequency,
    val weekdays: Int,
)

data class ValidHabitInput(
    val name: String,
    val nameKey: String,
    val unit: String,
    val target: BigDecimal,
    val frequency: Frequency,
    /** 0 unless [frequency] is Custom. */
    val selectedWeekdays: Int,
)

data class HabitInputErrors(
    val name: NameError? = null,
    val unit: UnitError? = null,
    val target: DecimalError? = null,
    val weekdays: WeekdaysError? = null,
) {
    val hasErrors: Boolean get() = name != null || unit != null || target != null || weekdays != null
}

sealed interface HabitInputResult {
    data class Valid(val input: ValidHabitInput) : HabitInputResult
    data class Invalid(val errors: HabitInputErrors) : HabitInputResult
}

/**
 * Pure input rules from the PRD's input contract. Name uniqueness is not checked here: it needs
 * the database and is decided inside the command's transaction against `nameKey`.
 *
 * Interpretations the PRD does not spell out (confirm with the creator):
 * - Surrounding whitespace is trimmed from a number before parsing; inner spaces are rejected.
 * - `.5` and `5.` are accepted; a lone separator is not.
 * - Digit limits are counted after dropping leading zeros and trailing fraction zeros, so
 *   `0000000001` and `1.2300` are valid.
 */
class HabitInputValidator {

    fun validateName(raw: String): FieldResult<String, NameError> {
        val trimmed = raw.trimWhitespace()
        val length = trimmed.codePointCount(0, trimmed.length)
        return when {
            length < 1 -> FieldResult.Invalid(NameError.EMPTY)
            length > MAX_NAME_CODE_POINTS -> FieldResult.Invalid(NameError.TOO_LONG)
            else -> FieldResult.Valid(trimmed)
        }
    }

    fun validateUnit(raw: String): FieldResult<String, UnitError> {
        val trimmed = raw.trimWhitespace()
        val length = trimmed.codePointCount(0, trimmed.length)
        return when {
            length < 1 -> FieldResult.Invalid(UnitError.EMPTY)
            length > MAX_UNIT_CODE_POINTS -> FieldResult.Invalid(UnitError.TOO_LONG)
            else -> FieldResult.Valid(trimmed)
        }
    }

    /** A target must be greater than zero. */
    fun validateTarget(raw: String, locale: Locale): FieldResult<BigDecimal, DecimalError> =
        when (val parsed = parseDecimal(raw, locale)) {
            is FieldResult.Invalid -> parsed
            is FieldResult.Valid ->
                if (parsed.value.signum() > 0) parsed else FieldResult.Invalid(DecimalError.MUST_BE_POSITIVE)
        }

    /** A progress total may be zero. */
    fun validateProgress(raw: String, locale: Locale): FieldResult<BigDecimal, DecimalError> =
        parseDecimal(raw, locale)

    fun validateWeekdays(frequency: Frequency, mask: Int): FieldResult<Int, WeekdaysError> =
        when {
            frequency != Frequency.CUSTOM -> FieldResult.Valid(Weekdays.NONE)
            mask == Weekdays.NONE -> FieldResult.Invalid(WeekdaysError.NONE_SELECTED)
            !Weekdays.isValidCustomMask(mask) -> FieldResult.Invalid(WeekdaysError.OUT_OF_RANGE)
            else -> FieldResult.Valid(mask)
        }

    fun validateHabit(raw: RawHabitInput, locale: Locale): HabitInputResult {
        val name = validateName(raw.name)
        val unit = validateUnit(raw.unit)
        val target = validateTarget(raw.target, locale)
        val weekdays = validateWeekdays(raw.frequency, raw.weekdays)

        val errors = HabitInputErrors(
            name = (name as? FieldResult.Invalid)?.error,
            unit = (unit as? FieldResult.Invalid)?.error,
            target = (target as? FieldResult.Invalid)?.error,
            weekdays = (weekdays as? FieldResult.Invalid)?.error,
        )
        if (errors.hasErrors) return HabitInputResult.Invalid(errors)

        val validName = (name as FieldResult.Valid).value
        return HabitInputResult.Valid(
            ValidHabitInput(
                name = validName,
                nameKey = validName.lowercase(Locale.ROOT),
                unit = (unit as FieldResult.Valid).value,
                target = (target as FieldResult.Valid).value,
                frequency = raw.frequency,
                selectedWeekdays = (weekdays as FieldResult.Valid).value,
            ),
        )
    }

    private fun parseDecimal(raw: String, locale: Locale): FieldResult<BigDecimal, DecimalError> {
        val text = raw.trimWhitespace()
        if (text.isEmpty()) return FieldResult.Invalid(DecimalError.EMPTY)

        val separator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
        val integerDigits = StringBuilder()
        val fractionDigits = StringBuilder()
        var seenSeparator = false
        for (ch in text) {
            val digit = Character.digit(ch, 10)
            when {
                digit >= 0 -> (if (seenSeparator) fractionDigits else integerDigits).append(('0' + digit))
                ch == separator && !seenSeparator -> seenSeparator = true
                else -> return FieldResult.Invalid(DecimalError.NOT_A_NUMBER)
            }
        }
        if (integerDigits.isEmpty() && fractionDigits.isEmpty()) {
            return FieldResult.Invalid(DecimalError.NOT_A_NUMBER)
        }

        val integerPart = integerDigits.toString().trimStart('0')
        val fractionPart = fractionDigits.toString().trimEnd('0')
        if (integerPart.length > MAX_INTEGER_DIGITS) {
            return FieldResult.Invalid(DecimalError.TOO_MANY_INTEGER_DIGITS)
        }
        if (fractionPart.length > MAX_FRACTION_DIGITS) {
            return FieldResult.Invalid(DecimalError.TOO_MANY_FRACTION_DIGITS)
        }
        val value = BigDecimal("${integerPart.ifEmpty { "0" }}.${fractionPart.ifEmpty { "0" }}")
        return FieldResult.Valid(value.normalized())
    }

    private fun String.trimWhitespace(): String = trim { it.isWhitespace() }

    companion object {
        const val MAX_NAME_CODE_POINTS = 50
        const val MAX_UNIT_CODE_POINTS = 20
    }
}
