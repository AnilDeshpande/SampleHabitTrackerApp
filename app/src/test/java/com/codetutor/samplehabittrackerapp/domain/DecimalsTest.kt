package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.normalized
import com.codetutor.samplehabittrackerapp.domain.model.parseStorageText
import com.codetutor.samplehabittrackerapp.domain.model.toStorageText
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DecimalsTest {

    @Test fun normalizesToPlainCanonicalText() {
        assertEquals("10", BigDecimal("10.000").toStorageText())
        assertEquals("100", BigDecimal("1E+2").toStorageText())
        assertEquals("0", BigDecimal("0.000").toStorageText())
        assertEquals("0.5", BigDecimal("0.50").toStorageText())
        assertEquals("1.123", BigDecimal("1.1230").toStorageText())
    }

    @Test fun normalizedValuesHaveNoNegativeScale() {
        assertEquals(0, BigDecimal("1E+2").normalized().scale())
    }

    @Test fun readsOnlyCanonicalStorageText() {
        assertEquals(0, BigDecimal("2.5").compareTo(parseStorageText("2.5")))
        assertEquals(0, BigDecimal.ZERO.compareTo(parseStorageText("0")))
        assertThrows(IllegalArgumentException::class.java) { parseStorageText("2.50") }
        assertThrows(IllegalArgumentException::class.java) { parseStorageText("1E3") }
        assertThrows(IllegalArgumentException::class.java) { parseStorageText("-0") }
        assertThrows(IllegalArgumentException::class.java) { parseStorageText("abc") }
        assertThrows(IllegalArgumentException::class.java) { parseStorageText("") }
    }
}
