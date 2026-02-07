package id.nearyou.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

fun <T> createEventChannel(): Channel<T> = Channel(Channel.BUFFERED)

@Composable
fun <T> ObserveEvents(
    events: Flow<T>,
    onEvent: (T) -> Unit,
) {
    LaunchedEffect(Unit) { events.collect { onEvent(it) } }
}
