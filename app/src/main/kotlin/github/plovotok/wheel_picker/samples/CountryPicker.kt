package github.plovotok.wheel_picker.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.ui.utils.PickerUtils
import github.plovotok.wheel_picker.ui.utils.debounced
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPicker
import io.github.plovotok.wheelpicker.WheelPickerState

@Stable
data class Country(val flag: String, val name: String)

@Stable
class CountryPickerState(
    initialItem: Int = 0
) {
    internal val pickerState = WheelPickerState(initiallySelectedItemIndex = initialItem)

    var settledCountry by mutableStateOf(countries[initialItem])
        internal set

    companion object {
        val countries = listOf(
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

        fun Saver() = androidx.compose.runtime.saveable.Saver<CountryPickerState, Int>(
            save = {
                val country = it.settledCountry
                countries.indexOfFirst { it == country }.coerceAtLeast(0)
            },
            restore = {
                CountryPickerState(it)
            }
        )
    }
}

@Composable
fun rememberCountryPickerState(
    initialItem: Int = 0
): CountryPickerState {
    return rememberSaveable(saver = CountryPickerState.Saver()) {
        CountryPickerState(initialItem)
    }

}

@Composable
fun CountryPicker(
    state: CountryPickerState,
    modifier: Modifier = Modifier
) {

    val selectedIndex = state.pickerState.selectedItemIndex(CountryPickerState.countries.size)

    val isScrolling = state.pickerState.isScrollInProgress.debounced(250)
    val isDragging = state.pickerState.interactionSource.collectIsDraggedAsState().value.debounced(250)

    LaunchedEffect(isScrolling, isDragging) {
        if (!isDragging && !isScrolling) {
            state.settledCountry = CountryPickerState.countries[selectedIndex]
        }
    }

    WheelPicker(
        data = CountryPickerState.countries,
        state = state.pickerState,
        overlay = OverlayConfiguration.create(
            scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
            selectionScale = 1.19f
        ),
        itemContent = { index ->
            val country = CountryPickerState.countries[index]
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
        },
        modifier = Modifier.widthIn(max = PickerUtils.PickerMaxWidth).then(modifier)
    )
}

@Preview
@Composable
private fun CountryPickerPreview() {
    Box(
        modifier = Modifier.background(Color.White)
    ) {
        CountryPicker(rememberCountryPickerState())
    }
}