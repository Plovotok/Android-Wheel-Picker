package io.github.plovotok.wheelpicker

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastFirstOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The state of the [WheelPicker] component, which controls the currently selected element and scrolling.
 *
 * Supports both finite and infinite lists. In the case of infinite mode
 * Uses virtual index shift to simulate infinite scrolling.
 *
 * @param infinite Enables infinite scrolling mode. The default is 'false'.
 * @param initiallySelectedItemIndex The start index of the selected item. The default is '0'.
 */
@Stable
public class WheelPickerState(
    internal val infinite: Boolean = false,
    internal val initiallySelectedItemIndex: Int = 0
) {
    private val nativeIndex = if (infinite) {
        INFINITE_OFFSET + initiallySelectedItemIndex
    } else {
        initiallySelectedItemIndex
    }

    public val canScrollBackward: Boolean
        get() = lazyListState.canScrollBackward

    public val canScrollForward: Boolean
        get() = lazyListState.canScrollForward

    public val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    /**
     * [InteractionSource] that will be used to dispatch drag events when this list is being
     * dragged. If you want to know whether the fling (or animated scroll) is in progress, use
     * [isScrollInProgress].
     */
    public val interactionSource: InteractionSource
        get() = lazyListState.interactionSource


    internal val lazyListState: LazyListState =
        LazyListState(
            firstVisibleItemIndex = nativeIndex
        )

    private val selectedItem by derivedStateOf {
        with(lazyListState.layoutInfo) {
            visibleItemsInfo.fastFirstOrNull {
                it.offset + it.size - viewportStartOffset > viewportSize.height / 2
            }
        }
    }

    // Может быть отрицательным
    private val currentSelectedItemIndex: Int by derivedStateOf {
        if (infinite) {
            selectedItem?.index?.minus(INFINITE_OFFSET)
        } else {
            selectedItem?.index
        } ?: initiallySelectedItemIndex
    }

    public fun selectedItem(itemsCount: Int): Int = currentSelectedItemIndex.modSign(itemsCount)

    public fun selectedItemState(itemsCount : Int) : State<Int> {
        return derivedStateOf { currentSelectedItemIndex.modSign(itemsCount) }
    }

    @Composable
    public fun selectedItemIndex(totalItemsCount: Int): Int =
        remember(totalItemsCount) {
            derivedStateOf {
                selectedItem(totalItemsCount)
            }
        }.value

    /**
     * Animates the wheel to the specified element.
     *
     * In infinite mode, selects the optimal scrolling direction (shortest arc).
     *
     * @param index The target index of the element.
     * @param totalItemsCount The total number of items in the list.
     */
    public suspend fun animateScrollToItem(index: Int, totalItemsCount: Int) {
        if (index >= 0) {
            if (infinite) {
                val currentIndex = currentSelectedItemIndex + INFINITE_OFFSET
                val selectedItem = selectedItem(totalItemsCount)
                if (selectedItem == index) return // Ничего не делаем

                // Находим, на сколько нужно прокрутить список от текущего элемента
                val diff = calculateOptimalShift(totalItemsCount, selectedItem, index)

                // Проверка, что мы не уйдем за границы списка LazyList'а
                val append = if (diff.first > 0) {
                    if (diff.first + currentIndex < Int.MAX_VALUE) diff.first else diff.second
                } else {
                    if (diff.first + currentIndex > 0) diff.first else diff.second
                }
                animateScrollToItemInternal(currentIndex + append)
            } else {
                animateScrollToItemInternal(index)
            }
        }
    }

    /**
     * @return [Pair] Пара первичная и вторичная разницы между выбранным и желаемым элементами
     * @param totalItemsCount общее количество элементов в списке
     * @param selectedIndex текущий выбранный элемент в списке
     * @param targetIndex желаемый индекс
     */
    private fun calculateOptimalShift(totalItemsCount: Int, selectedIndex: Int, targetIndex: Int): Pair<Int, Int> {
        if (totalItemsCount <= 0) return 0 to 0

        // Нормализуем индексы относительно списка
        val normalizedSelected = ((selectedIndex % totalItemsCount) + totalItemsCount) % totalItemsCount
        val normalizedTarget = ((targetIndex % totalItemsCount) + totalItemsCount) % totalItemsCount

        if (normalizedSelected == normalizedTarget) return 0 to 0

        //Смещение вправо
        val forwardShift = (normalizedTarget - normalizedSelected + totalItemsCount) % totalItemsCount
        // Смещение влево (отрицательное)
        val backwardShift = forwardShift - totalItemsCount

        // Возвращаем минимальное по модулю значение
        return if (forwardShift <= -backwardShift) forwardShift to backwardShift else backwardShift to forwardShift
    }

    internal suspend fun animateScrollToItemInternal(index: Int) {
        lazyListState.animateScrollToItem(index)
    }

    internal companion object {
        fun Saver() = listSaver(
            save = {
                listOf(it.infinite,  it.currentSelectedItemIndex)
            },
            restore = {
                WheelPickerState(it[0] as? Boolean ?: false, it[1] as? Int ?: 0)
            }
        )

        const val INFINITE_OFFSET = Int.MAX_VALUE / 2
    }


}

private fun Int.modSign(o: Int): Int = mod(o).let {
    if (it >= 0) it else this - it
}

/**
 * Remembers and saves the state of [WheelPickerState] between recreates.
 *
 * @param initialIndex The start index of the selected item.
 * @param infinite Enable infinite mode.
 * @return instance of [WheelPickerState].
 */
@Composable
public fun rememberWheelPickerState(
    initialIndex: Int = 0,
    infinite: Boolean = false,
): WheelPickerState {

    return rememberSaveable(
        saver = WheelPickerState.Saver()
    ) {
        WheelPickerState(infinite, initialIndex)
    }
}