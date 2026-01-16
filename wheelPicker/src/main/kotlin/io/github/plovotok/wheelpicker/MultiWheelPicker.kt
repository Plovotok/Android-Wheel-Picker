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

@Immutable
public data class WheelConfig(
    val weight: Float = 1f,
)

@Composable
public fun <T> MultiWheelPicker(
    modifier: Modifier = Modifier,
    nonFocusedItems: Int = WheelPickerDefaults.DEFAULT_UNFOCUSED_ITEMS_COUNT,
    itemHeightDp: Dp = WheelPickerDefaults.DefaultItemHeight,
    wheelConfig: (wheelIndex: Int) -> WheelConfig = { WheelConfig() },
    wheelCount: Int,
    data: (wheelIndex: Int) -> List<T>,
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