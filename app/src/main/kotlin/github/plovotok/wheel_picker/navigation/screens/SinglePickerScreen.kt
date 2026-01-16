package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import github.plovotok.wheel_picker.ui.utils.debounced
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPicker
import io.github.plovotok.wheelpicker.rememberWheelPickerState
import kotlinx.coroutines.launch

@Composable
fun SinglePickerScreen(
    onBack: () -> Unit
) {
    BasicScreen(
        title = "Single Wheel Picker",
        navigationIcon = {
            IconButton(onClick = onBack) {
                BackIcon()
            }
        }
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {

            val list = remember {
                buildList {
                    repeat(10) {
                        add("Item ${it + 1}")
                    }
                }
            }
            val pickerState = rememberWheelPickerState(
                initialIndex = 4,
                infinite = false
            )

            val selectedIndex = pickerState.selectedItemIndex(list.size)
            val debouncedIndex = selectedIndex.debounced(350)

            Text(
                text = "Selected: ${list[selectedIndex]}",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Debounced: ${list[debouncedIndex]}",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            WheelPicker(
                data = list,
                state = pickerState,
                overlay = OverlayConfiguration(
                    scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                ),
                itemContent = {
                    Text(
                        text = list[it],
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            textMotion = TextMotion.Animated
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            )

            val scope = rememberCoroutineScope()

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        pickerState.animateScrollToItem(4, list.size)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Default")
            }
        }
    }
}