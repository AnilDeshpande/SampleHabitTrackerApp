package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal

const val MAX_INTEGER_DIGITS = 9
const val MAX_FRACTION_DIGITS = 3

/**
 * The canonical form of a stored decimal: no exponent, no redundant trailing zeros, and zero is
 * plain `0`. Targets and progress are stored as this text so comparison happens in Kotlin, never
 * in SQL and never through binary floating point.
 */
fun BigDecimal.normalized(): BigDecimal {
    if (signum() == 0) return BigDecimal.ZERO
    val stripped = stripTrailingZeros()
    return if (stripped.scale() < 0) stripped.setScale(0) else stripped
}

fun BigDecimal.toStorageText(): String = normalized().toPlainString()

/** Reads stored decimal text; rejects anything that is not already in canonical form. */
fun parseStorageText(text: String): BigDecimal {
    val value = BigDecimal(text)
    require(value.toStorageText() == text) { "Decimal text is not normalized: $text" }
    return value.normalized()
}
