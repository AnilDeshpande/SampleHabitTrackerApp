package com.codetutor.samplehabittrackerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

/** The gate-before-snapshot SQL trace on the platform's SQLite: the minimum-API evidence the LLD asks for. */
@RunWith(AndroidJUnit4::class)
class GateOrderDeviceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun theGateUpdatePrecedesTheClockSnapshotAndEveryDomainStatement() =
        GateOrderScenarios.theGateUpdatePrecedesTheClockSnapshotAndEveryDomainStatement(context)

    @Test fun aContenderWaitingForTheGateSnapshotsTheClockOnlyAfterAcquiringIt() =
        GateOrderScenarios.aContenderWaitingForTheGateSnapshotsTheClockOnlyAfterAcquiringIt(context)
}
