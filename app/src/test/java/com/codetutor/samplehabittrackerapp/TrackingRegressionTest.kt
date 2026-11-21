package com.codetutor.samplehabittrackerapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Host tier for the Increment 1 review's two tracking regressions; the same scenarios run on devices. */
@RunWith(RobolectricTestRunner::class)
class TrackingRegressionTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun firstCreateFromAnEmptyOverviewRearmsTheTicker() =
        TrackingScenarios.firstCreateFromAnEmptyOverviewRearmsTheTicker(context)

    @Test fun aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges() =
        TrackingScenarios.aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges(context)
}
