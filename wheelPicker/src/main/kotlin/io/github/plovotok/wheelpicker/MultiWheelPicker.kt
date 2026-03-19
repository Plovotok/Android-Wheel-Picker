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

import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.plovotok.wheelpicker.WheelPickerDefaults.pickerOverlay
import io.github.plovotok.wheelpicker.WheelPickerDefaults.viewportCurveRate
import kotlin.math.roundToInt

/**
 * Configuration for a single wheel inside [MultiWheelPicker].
 *
 * Each wheel can provide its own data source, selection state, width ratio, and item alignment.
 * Width is calculated relative to the weights of the other wheels in the same row.
 *
 * @property weight Relative width of this wheel. For example, `1f, 2f, 1f` produces a `25/50/25`
 * split across three wheels.
 * @property contentAlignment Alignment applied to every item rendered in this wheel.
 * @property data Backing data for this wheel. The [MultiWheelPicker] passes only item indices to
 * [itemContent][MultiWheelPicker], so this list is primarily used for sizing and scrolling.
 * @property state [WheelPickerState] that controls selection and scrolling for this wheel.
 */
@Immutable
public data class WheelConfig(
    val weight: Float = 1f,
    val contentAlignment: Alignment = Alignment.Center,
    val contentPaddings: PaddingValues = PaddingValues(horizontal = 20.dp),
    val data: List<*>,
    val state: WheelPickerState
)

/**
 * Displays several [WheelPicker] instances in a single horizontal row with a shared overlay.
 *
 * This composable is useful for compound pickers such as date, time, or duration inputs where
 * each column scrolls independently but the full control should look like one continuous picker.
 * Every wheel gets its own [WheelConfig], while [overlay], [itemHeightDp], and
 * [nonFocusedItems] are applied consistently across the entire control.
 *
 * The centered selection band is drawn once for the whole row, while each child wheel also draws
 * its scaled focused content so the selected items stay visually aligned across columns.
 *
 * @param modifier Modifier applied to the outer picker container.
 * @param wheelCount Number of wheels to display. Must be greater than `0`.
 * @param wheelConfig Returns the [WheelConfig] for a given `wheelIndex`. This is called for every
 * wheel, so it should be stable and cheap.
 * @param itemContent Renders an item for the specified wheel and item index. Use `wheelIndex` to
 * choose the correct data source.
 * @param overlay Shared [OverlayConfiguration] for the combined picker. Use
 * `overlayTranslate` when the highlighted content for a wheel needs horizontal adjustment.
 * @param nonFocusedItems Number of visible off-center items. The value is normalized to an odd
 * total so the selected row always remains centered.
 * @param itemHeightDp Height of one row in every wheel.
 * @param curveRate Controls the curvature of the 3D cylinder effect applied to every wheel.
 * Must be in the range `[WheelPickerDefaults.MIN_CURVE_RATE, WheelPickerDefaults.MAX_CURVE_RATE]`.
 * Lower values produce a flatter appearance; higher values produce a more pronounced drum shape.
 * Defaults to [WheelPickerDefaults.MAX_CURVE_RATE].
 */
@Composable
public fun MultiWheelPicker(
    modifier: Modifier = Modifier,
    wheelCount: Int,
    wheelConfig: (wheelIndex: Int) -> WheelConfig,
    itemContent: @Composable (wheelIndex: Int, index: Int)  -> Unit,
    overlay: OverlayConfiguration = OverlayConfiguration.create(),
    nonFocusedItems: Int = WheelPickerDefaults.DEFAULT_UNFOCUSED_ITEMS_COUNT,
    itemHeightDp: Dp = WheelPickerDefaults.DefaultItemHeight,
    curveRate: Float = WheelPickerDefaults.MAX_CURVE_RATE,
) {
    require(wheelCount > 0) {
        "wheelCount should be 1 at least!"
    }

    // Ограничиваем curveRate допустимым диапазоном — за его пределами геометрия цилиндра
    // даёт визуально некорректный результат
    val effectiveCurveRate = curveRate.coerceIn(WheelPickerDefaults.MIN_CURVE_RATE, WheelPickerDefaults.MAX_CURVE_RATE)

    // редактируем количество так, чтобы получилось нечетное количество элементов
    val visibleItems = nonFocusedItems / 2 * 2 + 1

    val height = itemHeightDp * (visibleItems)
    val density = LocalDensity.current

    val itemHeightPx by remember {
        mutableIntStateOf(with(density) { itemHeightDp.toPx().roundToInt() })
    }

    BoxWithConstraints(
        modifier = modifier
            .requiredHeight(height / (effectiveCurveRate / viewportCurveRate))
            .drawWithCache {
                pickerOverlay(
                    edgeOffsetYPx = (size.height - itemHeightPx) / 2,
                    itemHeightPx = itemHeightPx,
                    overlay = overlay.copy(
                        drawRect = true,
                        drawScaledContent = false,
                        overlayTranslate = { 0.dp}
                    ),
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    wheelIndex = -1
                )
            }
            .clipScrollableContainer(orientation = Orientation.Vertical)
    ) {
        val fullWidth = maxWidth.toFloatPx()
        // Центр всего колеса
        val centerW = fullWidth / 2

        val configs = List(wheelCount) {
            wheelConfig(it)
        }

        val weights = configs.map { it.weight }

        // Ширина каждого колеса
        val widthList = List(wheelCount) {
            fullWidth * (weights[it] / weights.sum())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            repeat(wheelCount) { wheelIndex ->
                val weight = weights[wheelIndex]

                val wheelWidth = widthList[wheelIndex]

                val previousWidth = widthList.take(wheelIndex).sum()

                // Центр текущего колеса
                val currentCenter = previousWidth + wheelWidth / 2

                val config = wheelConfig(wheelIndex)

                CompositionLocalProvider(
                    LocalWheelIndex provides wheelIndex
                ) {
                    WheelPicker(
                        data = config.data,
                        state = config.state,
                        itemHeightDp = itemHeightDp,
                        contentAlignment = config.contentAlignment,
                        nonFocusedItems = nonFocusedItems,
                        curveRate = effectiveCurveRate,
                        itemContent = {
                            itemContent(wheelIndex, it)
                        },
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.5f - (currentCenter - centerW) / wheelWidth,
                            pivotFractionY = 0.5f
                        ),
                        contentPaddings = config.contentPaddings,
                        overlay = overlay.copy(
                            clipStart = wheelIndex == 0,
                            clipEnd = wheelIndex == wheelCount - 1,
                            drawRect = false,
                            drawScaledContent = true
                        ),
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
