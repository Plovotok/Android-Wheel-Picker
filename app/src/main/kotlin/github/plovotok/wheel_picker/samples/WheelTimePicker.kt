package github.plovotok.wheel_picker.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.plovotok.wheelpicker.MultiWheelPicker
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelConfig
import io.github.plovotok.wheelpicker.WheelPickerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Composable
fun WheelTimePicker(
    modifier: Modifier = Modifier,
    overlay: OverlayConfiguration? = OverlayConfiguration(),
    state: WheelTimePickerState,
) {
    val textStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight(450),
        textMotion = TextMotion.Animated
    )

    MultiWheelPicker(
        data = {
            when (it) {
                0 -> state.hoursList
                else -> state.minutesList
            }
        },
        nonFocusedItems = 8,
        wheelCount = 2,
        state = {
            when (it) {
                0 -> state.hoursState
                else -> state.minutesState
            }
        },
        itemHeightDp = 34.dp,
        wheelConfig = {
            WheelConfig(1f)
        },
        overlay = overlay,
        contentAlignment = {
            if (it == 0) {
                Alignment.CenterEnd
            } else Alignment.CenterStart
        },
        itemContent = { wheelIndex, index ->
            val text = if (wheelIndex == 0) {
                state.hoursList[index]
            } else {
                state.minutesList[index]
            }
            Text(
                text = text,
                style = textStyle,
                modifier = Modifier.padding(
                    horizontal = 22.dp
                )
            )

        },
        modifier = modifier
    )

}

@Composable
fun rememberTimePickerState(
    initialTime: LocalTime = LocalDateTime.now().time,
): WheelTimePickerState {
    return rememberSaveable(saver = WheelTimePickerState.Saver()) {
        WheelTimePickerState(initialTime)
    }
}

@Stable
class WheelTimePickerState(
    initialTime: LocalTime,
) {

    internal val hoursList = buildList {
        repeat(HOUR_LIST_SIZE) {
            add(it.toStringWithLeadingZero())
        }
    }

    internal val minutesList = buildList {
        repeat(MINUTES_LIST_SIZE) {
            add(it.toStringWithLeadingZero())
        }
    }

    internal val hoursState = WheelPickerState(
        infinite = true,
        initiallySelectedItemIndex = initialTime.hour
    )

    internal val minutesState = WheelPickerState(
        infinite = true,
        initiallySelectedItemIndex = initialTime.minute
    )

    val timeState by derivedStateOf {
        val h = hoursState.selectedItem(HOUR_LIST_SIZE)
        val m = minutesState.selectedItem(MINUTES_LIST_SIZE)
        LocalTime(hour = h, minute = m)
    }

    suspend fun setSelectedTime(time: LocalTime) {
        coroutineScope {
            val hourJob = launch {
                hoursState.animateScrollToItem(time.hour, HOUR_LIST_SIZE)
            }

            val minuteJob = launch {
                minutesState.animateScrollToItem(time.minute, MINUTES_LIST_SIZE)
            }

            listOf(hourJob, minuteJob).joinAll()
        }
    }

    companion object {

        const val HOUR_LIST_SIZE = 24
        const val MINUTES_LIST_SIZE = 60
        fun Saver() = listSaver(
            save = {
                val startHour = with(it.hoursState) {
                    selectedItem(HOUR_LIST_SIZE)
                }
                val startMinute = with(it.minutesState) {
                    selectedItem(MINUTES_LIST_SIZE)
                }
                listOf(startHour, startMinute)
            },
            restore = {
                WheelTimePickerState(LocalTime(it[0] as Int, it[1] as Int))
            }
        )
    }
}

fun Int.toStringWithLeadingZero() =
    if (this > 9) this.toString() else buildString { append("0"); append(this@toStringWithLeadingZero.toString()) }

@Preview
@Composable
private fun TimePickerPreview() {

    val state = rememberTimePickerState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WheelTimePicker(
            state = state,
            modifier = Modifier
        )
    }
}