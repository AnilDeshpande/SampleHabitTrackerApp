package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.rules.TargetEffect
import com.codetutor.samplehabittrackerapp.domain.rules.TargetPolicy
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TargetPolicyTest {
    private val policy = TargetPolicy()
    private val open = LocalDate.parse("2026-10-11")
    private val next = LocalDate.parse("2026-10-12")

    @Test fun dailyAndCustomChangeTheOpenOccurrenceImmediately() {
        for (frequency in listOf(Frequency.DAILY, Frequency.CUSTOM)) {
            assertEquals(
                TargetEffect.AppliesToCurrentOccurrence(open),
                policy.resolve(frequency, open, next),
            )
        }
    }

    @Test fun periodCadencesStartNextPeriod() {
        for (frequency in listOf(Frequency.WEEKLY, Frequency.FORTNIGHTLY, Frequency.MONTHLY)) {
            assertEquals(
                TargetEffect.AppliesFromNextOccurrence(next),
                policy.resolve(frequency, open, next),
            )
        }
    }

    @Test fun withNoOpenOccurrenceEveryCadenceStartsAtTheNextOne() {
        for (frequency in Frequency.entries) {
            assertEquals(
                TargetEffect.AppliesFromNextOccurrence(next),
                policy.resolve(frequency, null, next),
            )
        }
    }
}
