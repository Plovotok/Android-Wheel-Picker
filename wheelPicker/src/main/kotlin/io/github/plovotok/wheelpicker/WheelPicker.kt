/*
Copyright 2026 Plovotok

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.github.plovotok.wheelpicker

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.plovotok.wheelpicker.WheelPickerDefaults.curveRate
import io.github.plovotok.wheelpicker.WheelPickerDefaults.pickerOverlay
import io.github.plovotok.wheelpicker.WheelPickerDefaults.viewportCurveRate
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

internal val LocalWheelIndex = compositionLocalOf { 0 }

/**
 * Overlay (underlay) display configuration for the selection wheel.
 *
 * Allows you to customize the appearance of the background and highlighted area.
 *
 * @property scrimColor The color of the darkened background outside the active zone.
 * Default is white with 70% transparency.
 * @property focusColor The color of the selected (focus) area where the active element is located.
 * Default is gray with 40% transparency.
 * @property cornerRadius The radius of the rounded corners of the overlay. Defaul value is 7.dp.
 * @property horizontalPadding Horizontal overlay padding. The default is 0.dp.
 * @property verticalPadding Vertical indentation of the overlay. The default is -2.dp (slightly out of bounds).
 * @property selectionScale The scale of the selected element. The default is 1.0f.
 * @param overlayTranslate A function that returns the translation of the overlay's content for each wheel.
 */
@Stable
public data class OverlayConfiguration internal constructor(
    val scrimColor: Color = Color.White.copy(alpha = 0.7f),
    val focusColor: Color = Color.Gray.copy(alpha = 0.4f),
    val cornerRadius: Dp = 7.dp,
    val horizontalPadding: Dp = 0.dp,
    val verticalPadding: Dp = -2.dp,
    val selectionScale: Float = 1.0f,
    val overlayTranslate: (wheelIndex: Int) -> Dp = { 0.dp },

    internal val clipStart: Boolean,
    internal val clipEnd: Boolean,
    internal val isWheelItem: Boolean
) {

    public companion object {
        /**
         * Overlay (underlay) display configuration for the selection wheel.
         *
         * Allows you to customize the appearance of the background and highlighted area.
         *
         * @property scrimColor The color of the darkened background outside the active zone.
         * Default is white with 70% transparency.
         * @property focusColor The color of the selected (focus) area where the active element is located.
         * Default is gray with 40% transparency.
         * @property cornerRadius The radius of the rounded corners of the overlay. Defaul value is 7.dp.
         * @property horizontalPadding Horizontal overlay padding. The default is 0.dp.
         * @property verticalPadding Vertical indentation of the overlay. The default is -2.dp (slightly out of bounds).
         * @property selectionScale The scale of the selected element. The default is 1.0f.
         * @param overlayTranslate A function that returns the translation of the overlay's content for each wheel.
         */
        public fun create(
            scrimColor: Color = Color.White.copy(alpha = 0.7f),
            focusColor: Color = Color.Gray.copy(alpha = 0.4f),
            cornerRadius: Dp = 7.dp,
            horizontalPadding: Dp = 0.dp,
            verticalPadding: Dp = -2.dp,
            selectionScale: Float = 1.0f,
            overlayTranslate: (Int) -> Dp = { 0.dp }
        ): OverlayConfiguration {

            return OverlayConfiguration(
                scrimColor = scrimColor,
                focusColor = focusColor,
                cornerRadius = cornerRadius,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                selectionScale = selectionScale,
                overlayTranslate = overlayTranslate,
                clipStart = true,
                clipEnd = true,
                isWheelItem = false
            )
        }
    }
}

/**
 * A selection wheel component (similar to a 3D picker) that implements the spinning drum effect.
 *
 * Elements are displayed with a 3D perspective: the central element is closer to the user,
 * and the side ones are rotated and reduced. Supports infinite scrolling, clicking on an element
 * for quick selection and animated positioning.
 *
 * @param data The list of data from which the wheel elements are formed.
 * @param key A function that returns a unique key for each item based on its index.
 * @param itemContent A composable block that displays an item by its index in a list.
 * @param state The state of the selection wheel, which controls the current position and scrolling.
 * @param nonFocusedItems Number of visible items (out of focus). Will be adjusted to an odd number.
 * @param contentAlignment Alignment of the content within each element. The default is centered.
 * @param itemHeightDp The height of a single item in dp. The default value is from [WheelPickerDefaults.DefaultItemHeight].
 * @param transformOrigin Transform point for 3D effects. The default is the center.
 * @param overlay Overlay configuration (background, selection, padding, scale, ...).
 */
@Composable
public fun WheelPicker(
    modifier: Modifier = Modifier,
    data: List<*>,
    key: (index: Int) -> String? = { null },
    itemContent: @Composable (Int) -> Unit,
    state: WheelPickerState,
    nonFocusedItems: Int = WheelPickerDefaults.DEFAULT_UNFOCUSED_ITEMS_COUNT,
    contentAlignment: Alignment = Alignment.Center,
    itemHeightDp: Dp = WheelPickerDefaults.DefaultItemHeight,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    overlay: OverlayConfiguration = OverlayConfiguration.create(),
) {

    // редактируем количество так, чтобы получилось нечетное количество элементов
    val visibleItems = nonFocusedItems / 2 * 2 + 1

    // Функция для преобразования индекса LazyList'a в индекс в списке
    fun getListIndex(index: Int) =
        if (state.infinite) {
            ((index - WheelPickerState.INFINITE_OFFSET) % data.size).let {
                if (it >= 0) it else data.size - abs(it)
            }
        } else {
            index
        }

    val height = itemHeightDp * (visibleItems)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val itemHeightPx by remember {
        mutableIntStateOf(with(density) { itemHeightDp.toPx().roundToInt() })
    }

    // отступы для ContentPadding'а
    val edgeOffsetPx = remember(visibleItems, itemHeightPx) {
        (with(density) { height.toPx() } - itemHeightPx) / 2
    }
    val edgeOffsetDp = remember(visibleItems) {
        with(density) { edgeOffsetPx.absoluteValue.toDp() }
    }

    val index = LocalWheelIndex.current

    Box(
        modifier = modifier
            .height(height / (curveRate / viewportCurveRate))
            .clipScrollableContainer(orientation = Orientation.Vertical) // обрезаем контент по вертикали, чтобы не накладывался оверлей на контент вне колеса
    ) {
        CompositionLocalProvider(
            LocalOverscrollFactory provides null
        ) {
            LazyColumn(
                state = state.lazyListState,
                modifier = Modifier
                    .requiredHeight(height)
                    .disableParentNestedVerticalScroll() // Блокируем скролл родителя
                    .drawWithCache {
                        pickerOverlay(
                            edgeOffsetYPx = edgeOffsetPx,
                            itemHeightPx = itemHeightPx,
                            overlay = overlay,
                            transformOrigin = transformOrigin,
                            wheelIndex = index
                        )
                    }
                    .pointerInput(state.lazyListState) {
                        detectTapGestures {
                            if (state.isScrollInProgress) return@detectTapGestures
                            val clickedItem = calculateTapItem(
                                tapOffset = it,
                                getLayoutInfo = {
                                    state.lazyListState.layoutInfo
                                }
                            )

                            clickedItem?.let {
                                scope.launch {
                                    state.animateScrollToItemInternal(it)
                                }
                            }
                        }
                    },
                contentPadding = PaddingValues(vertical = edgeOffsetDp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = state.lazyListState)
            ) {
                items(
                    count = if (state.infinite) Int.MAX_VALUE else data.size,
                    key = { index -> key(getListIndex(index)) + "_$index" }
                ) { index ->

                    // Находим индекс элемента в списке
                    val listIndex = getListIndex(index)

                    ItemWrapper(
                        modifier = Modifier.fillMaxWidth(),
                        itemHeightDp = itemHeightDp,
                        contentAlignment = contentAlignment,
                        index = index,
                        transformOrigin = transformOrigin,
                        getLayoutInfo = {
                            state.lazyListState.layoutInfo
                        }
                    ) {
                        itemContent(listIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemWrapper(
    modifier: Modifier,
    itemHeightDp: Dp,
    contentAlignment: Alignment,
    index: Int,
    getLayoutInfo: () -> LazyListLayoutInfo,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .requiredHeight(itemHeightDp)
            .graphicsLayer {
                render3DVerticalItemEffect(
                    index = index,
                    getLayoutInfo = getLayoutInfo,
                    transformOrigin = transformOrigin
                )
            },
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

private fun Modifier.disableParentNestedVerticalScroll() =
    this.nestedScroll(VerticalParentScrollConsumer)

private val VerticalParentScrollConsumer = object : NestedScrollConnection {

    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = available

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = available
}

private fun GraphicsLayerScope.render3DVerticalItemEffect(
    index: Int,
    getLayoutInfo: () -> LazyListLayoutInfo,
    transformOrigin: TransformOrigin,
) {
    val layoutInfo = getLayoutInfo()
    // Информацию об элементе можно получить из LazyListLayoutInfo
    val itemInfo = layoutInfo.visibleItemsInfo.find { item -> item.index == index }
        ?: return

    val itemCenterY = getItemCenter(itemInfo) + layoutInfo.beforeContentPadding
    val viewportCenterY = layoutInfo.viewportSize.height / 2F

    val offsetFraction = (itemCenterY - viewportCenterY) / viewportCenterY

    // Визуальное сужение элемента (квадратичная функция с коэффициентом создает более плавный эффект)
    val scale = 1 - (offsetFraction.absoluteValue).pow(2) * 0.11f
    scaleX = scale

    // Не показываем элементы, которые не попадают в viewport
    if (offsetFraction.absoluteValue >= 1.0f) {
        alpha = 0f
    }

    rotationX = -90 * offsetFraction

    // Определяем радиус колеса (L = π * r = viewportHeight / curveRate (считаем, что длина кривой
    // равна сумме высот всех элементов, т.е. весь viewport, деленный на коэффициент кривой, т.к.
    // длина окружности меньше суммы всех видимых элементов)
    val r = (2f * viewportCenterY / curveRate / Math.PI).toFloat()

    translationY = if (offsetFraction == 0f) {
        0f
    } else {
        // Определяем положение элемента по вертикали
        val h =
            (sin(Math.toRadians(offsetFraction.absoluteValue * 90.0)) * r).toFloat()

        // Вычисляем смещение элемента по вертикали
        val diffY = if (offsetFraction < 0) {
            (viewportCenterY - h.absoluteValue) - itemCenterY.absoluteValue
        } else {
            (viewportCenterY + h.absoluteValue) - itemCenterY.absoluteValue
        }
        diffY
    }
    // Добавляем перспективу (значение вычислено эмпирически)
    this.cameraDistance = layoutInfo.viewportSize.height.toFloat() / 25f
    this.transformOrigin = transformOrigin
}

private fun getItemCenter(itemInfo: LazyListItemInfo): Float {
    val itemCenterY = itemInfo.size / 2F
    return itemInfo.offset.toFloat() + itemCenterY
}

private fun calculateTapItem(
    tapOffset: Offset,
    getLayoutInfo: () -> LazyListLayoutInfo,
): Int? {
    val layoutInfo = getLayoutInfo()

    // Центр колеса
    val viewportCenterY = layoutInfo.viewportSize.height / 2F

    // Радиус колеса
    val r = (2f * viewportCenterY / curveRate / Math.PI).toFloat()

    // Высота от центра колеса до точки касания
    val h = (viewportCenterY - tapOffset.y)

    val tapFraction = Math.toDegrees(-asin((h / r).coerceIn(-1f, 1f)).toDouble()) / 90

    // Точка касания относительно реального положения элементов
    val tapY = (viewportCenterY * (tapFraction + 1)).toInt()

    // Бинарный поиск

    var left: Pair<Int, IntRange> = getItemBounds(0, layoutInfo)
    var right: Pair<Int, IntRange> = getItemBounds(layoutInfo.visibleItemsInfo.size - 1, layoutInfo)

    if (tapY < left.second.first || tapY > right.second.last) return null // Не попали ни в какой элемент

    while (left.first <= right.first) {

        // центральный элемент
        val midBounds = (left.first + (right.first - left.first) / 2).let {
            getItemBounds(it, layoutInfo)
        }

        when {
            tapY < midBounds.second.first -> {
                val newIndex = midBounds.first - 1
                if (newIndex < 0) break // Не попали ни в какой элемент
                right = getItemBounds(midBounds.first - 1, layoutInfo)
            }

            tapY > midBounds.second.last -> {
                val newIndex = midBounds.first + 1
                if (newIndex >= layoutInfo.visibleItemsInfo.size) break // Не попали ни в какой элемент
                left = getItemBounds(midBounds.first + 1, layoutInfo)
            }

            midBounds.second.contains(tapY) -> { // Попали в элемент
                return layoutInfo.visibleItemsInfo[midBounds.first].index
            }
        }
    }
    return null
}

private fun getItemBounds(index: Int, layoutInfo: LazyListLayoutInfo): Pair<Int, IntRange> {
    val item = layoutInfo.visibleItemsInfo[index]
    val bounds =
        layoutInfo.beforeContentPadding + item.offset..layoutInfo.beforeContentPadding + item.offset + item.size
    return index to bounds
}
