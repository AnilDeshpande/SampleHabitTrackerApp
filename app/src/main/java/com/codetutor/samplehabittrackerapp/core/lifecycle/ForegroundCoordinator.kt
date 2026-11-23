package com.codetutor.samplehabittrackerapp.core.lifecycle

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.codetutor.samplehabittrackerapp.core.time.BoundaryTicker
import com.codetutor.samplehabittrackerapp.core.work.TrackingCatchUp
import com.codetutor.samplehabittrackerapp.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * The single process-lifecycle observer (LLD: no Activity `onStart`/`onResume` hooks). When the
 * process starts it runs catch-up and starts the boundary ticker; when the process stops it stops
 * the ticker without cancelling accepted commands. Rotation and screen navigation never reach it,
 * because `ProcessLifecycleOwner` filters configuration changes.
 *
 * Notification dispatch and cleanup join this sequence in a later increment.
 */
@Singleton
class ForegroundCoordinator @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val catchUp: TrackingCatchUp,
    private val ticker: BoundaryTicker,
) : DefaultLifecycleObserver {

    fun install(processLifecycle: Lifecycle) {
        processLifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        ticker.start()
        requestCatchUp()
    }

    override fun onStop(owner: LifecycleOwner) {
        ticker.stop()
    }

    /** Opening the overview asks for catch-up through the same pipeline, never a second observer. */
    fun requestCatchUp() {
        scope.launch {
            try {
                catchUp.run()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                Log.w(TAG, "Foreground catch-up failed; commands still catch up under the gate", failure)
            }
        }
    }

    private companion object {
        const val TAG = "ForegroundCoordinator"
    }
}
