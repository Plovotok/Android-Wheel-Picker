package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import github.plovotok.wheel_picker.ui.theme.PickerSampleAppTheme
import github.plovotok.wheel_picker.ui.utils.debounced
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPicker
import io.github.plovotok.wheelpicker.rememberWheelPickerState

@Stable
private data class Country(val flag: String, val name: String)

private val countries = listOf(
    Country("🇦🇺", "Australia"),
    Country("🇦🇹", "Austria"),
    Country("🇧🇷", "Brazil"),
    Country("🇨🇦", "Canada"),
    Country("🇨🇳", "China"),
    Country("🇨🇿", "Czech Republic"),
    Country("🇩🇰", "Denmark"),
    Country("🇪🇬", "Egypt"),
    Country("🇫🇮", "Finland"),
    Country("🇫🇷", "France"),
    Country("🇩🇪", "Germany"),
    Country("🇬🇷", "Greece"),
    Country("🇮🇳", "India"),
    Country("🇮🇩", "Indonesia"),
    Country("🇮🇷", "Iran"),
    Country("🇮🇪", "Ireland"),
    Country("🇮🇱", "Israel"),
    Country("🇮🇹", "Italy"),
    Country("🇯🇵", "Japan"),
    Country("🇰🇿", "Kazakhstan"),
    Country("🇲🇽", "Mexico"),
    Country("🇳🇱", "Netherlands"),
    Country("🇳🇿", "New Zealand"),
    Country("🇳🇴", "Norway"),
    Country("🇵🇱", "Poland"),
    Country("🇵🇹", "Portugal"),
    Country("🇷🇴", "Romania"),
    Country("🇷🇺", "Russia"),
    Country("🇸🇦", "Saudi Arabia"),
    Country("🇿🇦", "South Africa"),
    Country("🇰🇷", "South Korea"),
    Country("🇪🇸", "Spain"),
    Country("🇸🇪", "Sweden"),
    Country("🇨🇭", "Switzerland"),
    Country("🇹🇷", "Turkey"),
    Country("🇺🇦", "Ukraine"),
    Country("🇦🇪", "UAE"),
    Country("🇬🇧", "United Kingdom"),
    Country("🇺🇸", "United States"),
    Country("🇻🇳", "Vietnam"),
)

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
        val pickerState = rememberWheelPickerState(
            initialIndex = 0,
            infinite = false
        )

        val selectedIndex = pickerState.selectedItemIndex(countries.size)

        val isScrolling = pickerState.isScrollInProgress.debounced(250)
        val isDragging = pickerState.interactionSource.collectIsDraggedAsState().value.debounced(250)

        var selected by remember {
            mutableStateOf(countries[selectedIndex])
        }

        LaunchedEffect(isScrolling, isDragging) {
            if (!isDragging && !isScrolling) {
                selected = countries[selectedIndex]
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selected.flag,
                fontSize = 72.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selected.name,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            WheelPicker(
                data = countries,
                state = pickerState,
                overlay = OverlayConfiguration.create(
                    scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    selectionScale = 1.19f
                ),
                itemContent = { index ->
                    val country = countries[index]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = country.flag,
                            fontSize = 28.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = country.name,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                textMotion = TextMotion.Animated
                            ),
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 16.sp, maxFontSize = 18.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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