package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.domain.model.Occurrence
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus

data class Continuity(val activeRun: Int, val successStreak: Int)

/**
 * Derives Active run and Success streak from a habit's occurrence rows. Neither is stored, so
 * they cannot drift from the history they describe.
 */
class ContinuityCalculator {

    /**
     * Active run counts every non-cancelled row, including the open one; a miss never resets it.
     * Success streak counts consecutive finalized successes from the newest row back, stopping at
     * the first miss. Open and cancelled rows neither extend nor break the streak.
     */
    fun calculate(occurrences: List<Occurrence>): Continuity {
        val activeRun = occurrences.count { it.status != OccurrenceStatus.CANCELLED }
        val newestFirst = occurrences
            .filter { it.status == OccurrenceStatus.FINALIZED }
            .sortedWith(compareByDescending<Occurrence> { it.startInclusiveEpochMillis }.thenByDescending { it.occurrenceId })

        var streak = 0
        for (row in newestFirst) {
            if (row.result == OccurrenceResult.SUCCESS) streak++ else break
        }
        return Continuity(activeRun, streak)
    }
}
