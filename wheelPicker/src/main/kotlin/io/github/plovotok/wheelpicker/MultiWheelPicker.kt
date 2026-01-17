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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
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
import io.github.plovotok.wheelpicker.WheelPickerDefaults.curveRate
import io.github.plovotok.wheelpicker.WheelPickerDefaults.pickerOverlay
import io.github.plovotok.wheelpicker.WheelPickerDefaults.viewportCurveRate
import kotlin.math.roundToInt

/**
 * Single-wheel configuration in [MultiWheelPicker].
 *
 * Allows you to set the relative weight (width) of the wheel compared to others.
 *
 * @property weight The relative weight of the wheel, which determines its width.
 * Width is distributed in proportion to the weights of all wheels.
 * The default is 1f (evenly distributed).
 */
@Immutable
public data class WheelConfig(
    val weight: Float = 1f,
)

/**
 * A component of a multiple selection wheel (analogous to several [WheelPicker] arranged horizontally).
 *
 * Allows multiple independent selection wheels to be displayed in a single line, each of which is
 * can have its own dataset, state, and alignment. Supports width adjustment
 * of each wheel through weights.
 *
 * Example usage: select date (day, month, year), time (hours, minutes, seconds).
 *
 * @param T Type of data displayed in wheel elements.
 * @param nonFocusedItems Number of visible items (out of focus). Will be adjusted to an odd number.
 * @param itemHeightDp The height of a single item in dp. Applies to all wheels.
 * @param wheelConfig A function that returns the [WheelConfig] configuration for each wheel based on its index.
 * Determines the relative width of the wheel.
 * @param wheelCount Number of wheels. Must be 1 at least.
 * @param data A function that returns a list of data for each wheel by its index.
 * @param itemContent A composable block to display an item. Accepts wheel index and element index.
 * @param state A function that returns the state of [WheelPickerState] for each wheel based on its index.
 * @param contentAlignment Function that returns the content alignment for each wheel.
 * Default is centered.
 * @param overlay Overlay configuration (background, selection) common to all wheels. If 'null', the overlay is not displayed.
 */
@Composable
public fun MultiWheelPicker(
    modifier: Modifier = Modifier,
    nonFocusedItems: Int = WheelPickerDefaults.DEFAULT_UNFOCUSED_ITEMS_COUNT,
    itemHeightDp: Dp = WheelPickerDefaults.DefaultItemHeight,
    wheelConfig: (wheelIndex: Int) -> WheelConfig = { WheelConfig() },
    wheelCount: Int,
    data: (wheelIndex: Int) -> List<*>,
    itemContent: @Composable (wheelIndex: Int, index: Int)  -> Unit,
    state: (wheelIndex: Int) -> WheelPickerState,
    contentAlignment: (wheelIndex: Int) -> Alignment = { Alignment.Center },
    overlay: OverlayConfiguration? = OverlayConfiguration(),
) {
    require(wheelCount > 0) {
        "wheelCount should be 1 at least!"
    }
    // редактируем количество так, чтобы получилось нечетное количество элементов
    val visibleItems = nonFocusedItems / 2 * 2 + 1

    val height = itemHeightDp * (visibleItems)
    val density = LocalDensity.current

    val itemHeightPx by remember {
        mutableIntStateOf(with(density) { itemHeightDp.toPx().roundToInt() })
    }

    BoxWithConstraints(
        modifier = modifier
            .requiredHeight(height / (curveRate / viewportCurveRate))
            .drawWithCache {
                pickerOverlay(
                    edgeOffsetYPx = (size.height - itemHeightPx) / 2,
                    itemHeightPx = itemHeightPx,
                    overlay = overlay
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

                WheelPicker(
                    data = data(wheelIndex),
                    state = state(wheelIndex),
                    itemHeightDp = itemHeightDp,
                    contentAlignment = contentAlignment(wheelIndex),
                    nonFocusedItems = nonFocusedItems,
                    itemContent = {
                        itemContent(wheelIndex, it)
                    },
                    transformOrigin = TransformOrigin(
                        pivotFractionX = 0.5f - (currentCenter - centerW) / wheelWidth,
                        pivotFractionY = 0.5f
                    ),
                    overlay = null,
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                )
            }
        }
    }
}