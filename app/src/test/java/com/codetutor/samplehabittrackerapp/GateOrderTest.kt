package com.codetutor.samplehabittrackerapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Host tier of the gate-before-snapshot trace; the same scenarios are the minimum-API evidence on devices. */
@RunWith(RobolectricTestRunner::class)
class GateOrderTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun theGateUpdatePrecedesTheClockSnapshotAndEveryDomainStatement() =
        GateOrderScenarios.theGateUpdatePrecedesTheClockSnapshotAndEveryDomainStatement(context)

    @Test fun aContenderWaitingForTheGateSnapshotsTheClockOnlyAfterAcquiringIt() =
        GateOrderScenarios.aContenderWaitingForTheGateSnapshotsTheClockOnlyAfterAcquiringIt(context)
}
