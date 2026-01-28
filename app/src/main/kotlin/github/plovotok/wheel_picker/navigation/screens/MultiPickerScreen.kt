package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import io.github.plovotok.wheelpicker.MultiWheelPicker
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelConfig
import io.github.plovotok.wheelpicker.rememberWheelPickerState

@Composable
fun MultiPickerScreen(
    onBack: () -> Unit
) {
    BasicScreen(
        title = "Multi Wheel Picker",
        navigationIcon = {
            IconButton(onClick = onBack) {
                BackIcon()
            }
        }
    ) {
        val list = buildList {
            repeat(10) {
                add("Item ${(it + 1)}")
            }
        }
        val state1 = rememberWheelPickerState(2)
        val state2 = rememberWheelPickerState(3)
        val state3 = rememberWheelPickerState(4)
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MultiWheelPicker(
                wheelCount = 3,
                overlay = OverlayConfiguration.create(
                    scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                ),
                itemHeightDp = 38.dp,
                wheelConfig = {
                    WheelConfig(
                        data = list,
                        state = when (it) {
                            0 -> state1
                            1 -> state2
                            else -> state3
                        }
                    )
                },
                itemContent = { _, listIdex ->
                    Text(
                        text = list[listIdex],
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                },
            )
        }

    }
}