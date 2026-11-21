package com.codetutor.samplehabittrackerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

/** The two foreground-tracking regressions on real devices, with the platform's SQLite and Room threads. */
@RunWith(AndroidJUnit4::class)
class TrackingRegressionDeviceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun firstCreateFromAnEmptyOverviewRearmsTheTicker() =
        TrackingScenarios.firstCreateFromAnEmptyOverviewRearmsTheTicker(context)

    @Test fun aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges() =
        TrackingScenarios.aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges(context)
}
