package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.samples.WheelDatePicker
import github.plovotok.wheel_picker.samples.WheelTimePicker
import github.plovotok.wheel_picker.samples.now
import github.plovotok.wheel_picker.samples.rememberDatePickerState
import github.plovotok.wheel_picker.samples.rememberTimePickerState
import github.plovotok.wheel_picker.samples.toStringWithLeadingZero
import github.plovotok.wheel_picker.ui.components.icons.BackIcon
import github.plovotok.wheel_picker.ui.utils.useDebounce
import io.github.plovotok.wheelpicker.OverlayConfiguration
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateAndTimeScreen(
    onBack: () -> Unit,
) {
    BasicScreen(
        title = "Date and time",
        navigationIcon = {
            IconButton(onClick = onBack) {
                BackIcon()
            }
        },
        scrollable = true
    ) {
        val scope = rememberCoroutineScope()

        var date by remember {
            mutableStateOf(LocalDate.now())
        }

        var time by remember {
            mutableStateOf(LocalDateTime.now().time)
        }

        var showDatePicker by remember {
            mutableStateOf(true)
        }

        var showTimePicker by remember {
            mutableStateOf(false)
        }

        PickerListItem(
            title = "Date",
            showPicker = showDatePicker,
            value = {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable {
                            showDatePicker = !showDatePicker
                            if (showTimePicker) {
                                showTimePicker = false
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.atTime(0, 0).formatWithPattern(DatePattern.StandardWithoutTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (showDatePicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialDate = date
            )

            datePickerState.dateState.useDebounce { date = it }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                WheelDatePicker(
                    state = datePickerState,
                    overlay = OverlayConfiguration(
                        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    ),
                )

                Button(
                    onClick = {
                        scope.launch {
                            datePickerState.setSelectedDate(LocalDate.now())
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Today")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))

        PickerListItem(
            title = "Time",
            showPicker = showTimePicker,
            value = {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable {
                            showTimePicker = !showTimePicker
                            if (showDatePicker) {
                                showDatePicker = false
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildString {
                            append(time.hour.toStringWithLeadingZero())
                            append(":")
                            append(time.minute.toStringWithLeadingZero())
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (showTimePicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        ) {
            val timePickerState = rememberTimePickerState(initialTime = time)

            timePickerState.timeState.useDebounce { time = it }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WheelTimePicker(
                    state = timePickerState,
                    overlay = OverlayConfiguration(
                        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    ),
                )

                Button(
                    onClick = {
                        scope.launch {
                            timePickerState.setSelectedTime(LocalDateTime.now().time)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Now")
                }
            }
        }
    }

}

@Composable
private fun PickerListItem(
    modifier: Modifier = Modifier,
    title: String,
    showPicker: Boolean,
    value: @Composable () -> Unit,
    picker: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ListItem(
            headlineContent = {
                Text(text = title)
            },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                value()
            }
        )

        AnimatedVisibility(
            visible = showPicker,
            modifier = Modifier.fillMaxWidth(),
            enter = slideInVertically { -it } +
                    expandVertically(expandFrom = Alignment.Bottom),
            exit = slideOutVertically { -it } +
                    shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            picker()
        }
    }

}

enum class DatePattern(val pattern: String) {
    StandardWithoutTime("dd.MM.yyyy"),
    DayOfWeekMonthWithDay("d MMMM, YYYY"),
}

private fun LocalDateTime.formatWithPattern(pattern: DatePattern) = formatWithPattern(pattern.pattern)

fun LocalDateTime.formatWithPattern(pattern: String) =
    DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("ru")).format(toJavaLocalDateTime()).lowercase()