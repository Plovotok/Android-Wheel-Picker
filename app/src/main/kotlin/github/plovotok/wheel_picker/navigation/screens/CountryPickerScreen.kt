package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.samples.CountryPicker
import github.plovotok.wheel_picker.samples.rememberCountryPickerState
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import github.plovotok.wheel_picker.ui.theme.PickerSampleAppTheme
import github.plovotok.wheel_picker.ui.utils.PickerUtils

@Composable
fun CountryPickerScreen(
    onBack: () -> Unit
) {
    BasicScreen(
        title = "Country Picker",
        navigationIcon = {
            IconButton(onClick = onBack) {
                BackIcon()
            }
        }
    ) {
        val pickerState = rememberCountryPickerState()

        val item = pickerState.settledCountry


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.flag,
                fontSize = 72.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            CountryPicker(
                state = pickerState,
                modifier = Modifier.widthIn(max = PickerUtils.PickerMaxWidth)
            )
        }
    }
}

@Preview
@Composable
private fun CountryPickerScreenPreview() {
    PickerSampleAppTheme {
        CountryPickerScreen {}
    }
}