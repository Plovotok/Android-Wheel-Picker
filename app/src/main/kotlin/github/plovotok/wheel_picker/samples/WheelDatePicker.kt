package github.plovotok.wheel_picker.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.plovotok.wheel_picker.ui.utils.debounced
import io.github.plovotok.wheelpicker.MultiWheelPicker
import io.github.plovotok.wheelpicker.OverlayConfiguration
import io.github.plovotok.wheelpicker.WheelPickerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toKotlinYearMonth
import java.time.format.TextStyle
import java.util.Locale

fun LocalDate.Companion.now() = java.time.LocalDate.now().toKotlinLocalDate()
fun LocalDateTime.Companion.now() = java.time.LocalDateTime.now().toKotlinLocalDateTime()

@Composable
fun rememberDatePickerState(
    initialDate: LocalDate = LocalDate.now(),
): WheelDatePickerState {
    return rememberSaveable(
        saver = WheelDatePickerState.Saver()
    ) {
        WheelDatePickerState(initialDate)
    }
}
@Composable
fun WheelDatePicker(
    state: WheelDatePickerState,
    modifier: Modifier = Modifier,
    scrimColor: Color,
    languageTag: String = Locale.getDefault().language,
) {

    val textStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight(450),
        textMotion = TextMotion.Animated
    )

    val yearMonth by remember {
        derivedStateOf { state.yearMonthState }
    }
    val debouncedYearMonth = yearMonth.debounced(500)

    val selectedDayIndex = state.dayState.selectedItemIndex(WheelDatePickerState.daysList.size).debounced(500)

    val isDayDragging by state.dayState.interactionSource.collectIsDraggedAsState().debounced(500)

    val isMonthScrolling = state.monthState.isScrollInProgress
    val isYearScrolling = state.yearState.isScrollInProgress

    val isScrolling = isMonthScrolling || isYearScrolling

    val monthList = remember(languageTag) { WheelDatePickerState.monthList(languageTag) }

    LaunchedEffect(debouncedYearMonth, selectedDayIndex, isDayDragging, isScrolling) {
        if (selectedDayIndex + 1 > debouncedYearMonth.numberOfDays && !isDayDragging && !isScrolling) {
            val targetIndex = selectedDayIndex - (selectedDayIndex + 1 - debouncedYearMonth.numberOfDays)
            state.dayState.animateScrollToItem(targetIndex, WheelDatePickerState.DAYS_LIST_SIZE)
        }
    }

    BoxWithConstraints(
        modifier = modifier
    ) {

        val width = maxWidth

        MultiWheelPicker(
            data = {
                when (it) {
                    0 -> WheelDatePickerState.daysList
                    1 -> monthList
                    else -> WheelDatePickerState.yearList
                }
            },
            overlay = OverlayConfiguration.create(
                scrimColor = scrimColor,
                selectionScale = 1.08f,
                overlayTranslate = {
                    if (it == 0) 10.dp else if (it == 1) 6.dp else -8.dp
                }
            ),
            state = {
                when (it) {
                    0 -> state.dayState
                    1 -> state.monthState
                    else -> state.yearState
                }
            },
            wheelConfig = {
                WheelDatePickerDefaults.config(it, width)
            },
            contentAlignment = {
                when (it) {
                    0 -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
            },
            itemHeightDp = 34.dp,
            itemContent = { wheelIndex, itemIndex ->
                val text = when (wheelIndex) {
                    0 -> WheelDatePickerState.daysList[itemIndex]
                    1 -> monthList[itemIndex]
                    else -> WheelDatePickerState.yearList[itemIndex]
                }
                val itemModifier = Modifier
                    .padding(
                        horizontal = if (wheelIndex == 1) 8.dp else 16.dp
                    )
                if (wheelIndex == 0) {
                    Text(
                        text = text,
                        style = textStyle,
                        modifier = itemModifier.graphicsLayer {
                            alpha =
                                if (itemIndex + 1 <= debouncedYearMonth.numberOfDays) 1f else 0.5f
                        }
                    )
                } else {
                    Text(
                        text = text,
                        style = textStyle,
                        modifier = itemModifier
                    )
                }
            },
            nonFocusedItems = 8,
            wheelCount = 3
        )
    }
}

@Composable
fun rememberDatePickerState(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
) = rememberDatePickerState(LocalDate(initialYear, initialMonth, initialDay))

@Stable
class WheelDatePickerState(
    initialDate: LocalDate,
) {

    internal val dayState = WheelPickerState(
        infinite = true,
        initiallySelectedItemIndex = initialDate.day - 1
    )

    internal val monthState = WheelPickerState(
        infinite = true,
        initiallySelectedItemIndex = initialDate.month.number - 1
    )

    internal val yearState = WheelPickerState(
        infinite = false,
        initiallySelectedItemIndex = initialDate.year - 1
    )

    internal val yearMonthState by derivedStateOf {
        val month = monthState.selectedItem(MONTH_LIST_SIZE) + 1
        val year = yearState.selectedItem(YEAR_LIST_SIZE) + 1

        java.time.YearMonth.of(year, month).toKotlinYearMonth()
    }

    val dateState by derivedStateOf {
        val day = dayState.selectedItem(DAYS_LIST_SIZE) + 1
        val month = monthState.selectedItem(MONTH_LIST_SIZE) + 1
        val year = yearState.selectedItem(YEAR_LIST_SIZE) + 1

        val daysCount = java.time.YearMonth.of(year, month).toKotlinYearMonth().numberOfDays

        LocalDate(year, month, day.coerceAtMost(daysCount))
    }

    suspend fun setSelectedDate(date: LocalDate) {
        coroutineScope {
            val dayJob = launch {
                dayState.animateScrollToItem(date.day - 1, DAYS_LIST_SIZE)
            }
            val monthJob = launch {
                monthState.animateScrollToItem(date.month.number - 1, MONTH_LIST_SIZE)
            }

            val yearJob = launch {
                yearState.animateScrollToItem(date.year - 1, YEAR_LIST_SIZE)
            }
            listOf(dayJob, monthJob, yearJob).joinAll()
        }
    }

    companion object {
        const val DAYS_LIST_SIZE = 31
        const val MONTH_LIST_SIZE = 12
        const val YEAR_LIST_SIZE = 3000

        internal fun Month.pickerFormat(languageTag: String) = this.toJavaMonth().getDisplayName(
            TextStyle.FULL,
            Locale.forLanguageTag(languageTag)
        ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag(languageTag)) else it.toString() }

        internal val daysList = buildList {
            repeat(DAYS_LIST_SIZE) {
                add((it + 1).toString())
            }
        }

        internal fun monthList(languageTag: String) = Month.entries.toList().map {
            it.pickerFormat(languageTag)
        }

        internal val yearList = buildList {
            repeat(YEAR_LIST_SIZE) {
                add((it + 1).toString())
            }
        }

        fun Saver() = listSaver(
            save = {
                listOf(
                    it.dayState.selectedItem(DAYS_LIST_SIZE),
                    it.monthState.selectedItem(MONTH_LIST_SIZE),
                    it.yearState.selectedItem(YEAR_LIST_SIZE)
                )
            },
            restore = {
                WheelDatePickerState(LocalDate((it[2] as Int) + 1, (it[1] as Int) + 1, (it[0] as Int) + 1))
            }
        )
    }
}

@Preview
@Composable
private fun WheelDatePickerPreview() {
    Column(
        modifier = Modifier.background(Color.White)
    ) {
        WheelDatePicker(
            state = rememberDatePickerState(),
            modifier = Modifier.width(300.dp),
            scrimColor = Color.White.copy(alpha = 0.7f)
        )
    }
}