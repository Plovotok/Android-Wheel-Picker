package github.plovotok.wheel_picker.samples

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.plovotok.wheelpicker.WheelConfig

@Stable
object WheelDatePickerDefaults {

    fun config(index: Int, maxWidth: Dp): WheelConfig {
        return if (maxWidth <= 480.dp) {
            when (index) {
                0 -> {
                    WheelConfig(3f)
                }

                1 -> {
                    WheelConfig(6f)
                }

                else -> {
                    WheelConfig(4f)
                }
            }
        } else {
            val monthWeight = 160.dp / maxWidth
            val elseWeight = (1f - monthWeight) / 2
            when (index) {
                0 -> {
                    WheelConfig(elseWeight)
                }

                1 -> {
                    WheelConfig(monthWeight)
                }

                else -> {
                    WheelConfig(elseWeight)
                }
            }
        }
    }
}