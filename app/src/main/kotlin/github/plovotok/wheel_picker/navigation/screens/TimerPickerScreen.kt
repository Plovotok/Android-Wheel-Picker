package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.samples.TimerPicker
import github.plovotok.wheel_picker.samples.rememberTimerPickerState
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import github.plovotok.wheel_picker.ui.theme.PickerSampleAppTheme
import github.plovotok.wheel_picker.ui.utils.useDebounce

@Composable
fun TimerPickerScreen(
    onBack: () -> Unit
) {
    BasicScreen(
        title = "Timer",
        navigationIcon = {
            IconButton(onClick = onBack) {
                BackIcon()
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val state = rememberTimerPickerState()

            var text by remember {
                mutableStateOf("${state.selectedSeconds} seconds")
            }

            state.selectedSeconds.useDebounce(250) {
                text = "$it seconds"
            }

            Text(
                text = text,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimerPicker(
                state = state
            )
        }
    }
}

@Preview(device = Devices.PIXEL_9_PRO_XL)
@PreviewFontScale
@Composable
private fun TimerPickerScreenPreview() {
    PickerSampleAppTheme {
        TimerPickerScreen {}
    }
}