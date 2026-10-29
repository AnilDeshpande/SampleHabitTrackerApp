package com.codetutor.samplehabittrackerapp.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/** Injected dispatcher holder so tests can drive app coroutines from one scheduler. */
class AppDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
