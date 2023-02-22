package com.subreax.reaction.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// suspends the coroutine until condition is met
suspend fun <T> Flow<T>.waitFor(condition: (T) -> Boolean): T {
    var result: T? = null
    withContext(Dispatchers.IO) {
        launch {
            cancellable().collect {
                if (condition(it)) {
                    result = it
                    cancel()
                }
            }
        }
    }
    return result!!
}

suspend fun <T> Flow<T>.waitFor(value: T) = waitFor { it == value }
suspend fun <T> Flow<T>.waitForAny() = waitFor { true }
