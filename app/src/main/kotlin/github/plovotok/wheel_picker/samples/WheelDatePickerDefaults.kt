package github.plovotok.wheel_picker.samples

import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.plovotok.wheelpicker.WheelConfig

@Stable
object WheelDatePickerDefaults {

    fun wheelWeight(
        index: Int,
        maxWidth: Dp,
        state: WheelDatePickerState,
        monthList: List<String>
    ): WheelConfig {
        return if (maxWidth <= 480.dp) {
            when (index) {
                0 -> {
                    WheelConfig(
                        weight = 3f,
                        data = WheelDatePickerState.daysList,
                        state = state.dayState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }

                1 -> {
                    WheelConfig(
                        weight = 6f,
                        data = monthList,
                        state = state.monthState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }

                else -> {
                    WheelConfig(
                        weight = 4f,
                        data = WheelDatePickerState.yearList,
                        state = state.yearState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }
            }
        } else {
            val monthWeight = 160.dp / maxWidth
            val elseWeight = (1f - monthWeight) / 2
            when (index) {
                0 -> {
                    WheelConfig(
                        weight = elseWeight,
                        data = WheelDatePickerState.daysList,
                        state = state.dayState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }

                1 -> {
                    WheelConfig(
                        weight = monthWeight,
                        data = monthList,
                        state = state.monthState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }

                else -> {
                    WheelConfig(
                        weight = elseWeight,
                        data = WheelDatePickerState.yearList,
                        state = state.yearState,
                        contentAlignment = wheelContentAlignment(index)
                    )
                }
            }
        }
    }

    private fun wheelContentAlignment(index: Int): Alignment = when (index) {
        0 -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
}