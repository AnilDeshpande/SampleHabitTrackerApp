package com.codetutor.samplehabittrackerapp.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

data class AppDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
