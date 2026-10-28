package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.data.local.StorageConverters
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class StorageConvertersTest {
    private val converters = StorageConverters()

    @Test fun decimalsRemainExactAndCanonical() {
        listOf("0.000" to "0", "1E+3" to "1000", "999999999.999" to "999999999.999", "12.3400" to "12.34").forEach { (input, expected) ->
            assertEquals(expected, converters.decimalToText(BigDecimal(input)))
            assertEquals(0, BigDecimal(input).compareTo(converters.textToDecimal(expected)))
        }
    }

    @Test fun nonCanonicalDatabaseValuesFailInsteadOfSilentlyChangingHistory() {
        listOf("1.00", "01", "1e3", "-0", "+1", "NaN").forEach { input ->
            assertThrows(IllegalArgumentException::class.java) { converters.textToDecimal(input) }
        }
    }
}
