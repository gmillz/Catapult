package app.catapult.launcher.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun <T> Flow<T>.firstBlocking() = runBlocking { first() }

fun <T> Flow<T>.dropWhileBusy(): Flow<T> = channelFlow {
    collect { trySend(it) }
}.buffer(0)

fun <T> Flow<T>.subscribeBlocking(
    scope: CoroutineScope,
    block: (T) -> Unit
) {
    block(firstBlocking())
    onEach { block(it) }
        .drop(1)
        .distinctUntilChanged()
        .launchIn(scope = scope)
}