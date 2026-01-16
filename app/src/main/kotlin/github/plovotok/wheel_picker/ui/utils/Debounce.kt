package github.plovotok.wheel_picker.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 500L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit,
): T {
    val state by rememberUpdatedState(this)

    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}

@Composable
fun <T> T.debounced(
    delayMillis: Long = 500L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): T {
    val state by rememberUpdatedState(this)

    var debouncedState by remember {
        mutableStateOf(this)
    }

    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            debouncedState = state
        }
        onDispose {
            job.cancel()
        }
    }
    return debouncedState
}