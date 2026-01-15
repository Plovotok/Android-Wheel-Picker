package io.github.plovotok.wheelpicker

import android.util.Log
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

@Stable
class WheelPickerState(
    internal val infinite: Boolean = false,
    internal val initiallySelectedItemIndex: Int = 0
) {
    private val nativeIndex = if (infinite) {
        INFINITE_OFFSET + initiallySelectedItemIndex
    } else {
        initiallySelectedItemIndex
    }

    val canScrollBackward: Boolean
        get() = lazyListState.canScrollBackward

    val canScrollForward: Boolean
        get() = lazyListState.canScrollForward

    val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    val interactionSource: InteractionSource
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
    val currentSelectedItemIndex by derivedStateOf {
        if (infinite) {
            selectedItem?.index?.minus(INFINITE_OFFSET)
        } else {
            selectedItem?.index
        } ?: initiallySelectedItemIndex
    }

    fun selectedItem(itemsCount: Int) = currentSelectedItemIndex.modSign(itemsCount)

    private fun selectedItemState(itemsCount : Int) : State<Int> {
        return derivedStateOf { currentSelectedItemIndex.modSign(itemsCount) }
    }

    @Composable
    fun selectedItemIndex(totalItemsCount: Int): Int =
        remember(totalItemsCount) {
            derivedStateOf {
                selectedItem(totalItemsCount)
            }
        }.value

    var isChangingProgrammatically: Boolean by mutableStateOf(false)

    suspend fun animateScrollToItem(index: Int, totalItemsCount: Int) {
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
                coroutineScope {
                    val job = launch {
                        isChangingProgrammatically = true
                        animateScrollToItemInternal(currentIndex + append)
                    }
                    job.invokeOnCompletion {
                        isChangingProgrammatically = false
                    }
                    job.join()
                }
            } else {
                coroutineScope {
                    val job = launch {
                        isChangingProgrammatically = true
                        animateScrollToItemInternal(index)
                    }
                    job.invokeOnCompletion {
                        isChangingProgrammatically = false
                    }
                    job.join()
                }
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

    companion object {
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

fun Int.modSign(o: Int): Int = mod(o).let {
    if (it >= 0) it else this - it
}

@Composable
fun rememberWheelPickerState(
    initialIndex: Int = 0,
    infinite: Boolean = false,
): WheelPickerState {

    return rememberSaveable(
        saver = WheelPickerState.Saver()
    ) {
        WheelPickerState(infinite, initialIndex)
    }
}