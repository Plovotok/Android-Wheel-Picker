package ru.plovotok.wheel_picker.ui.components.wheel_picker

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.plovotok.wheel_picker.ui.components.wheel_picker.WheelPickerDefaults.pickerOverlay
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

@Stable
data class OverlayConfiguration(
    val scrimColor: Color = Color.White.copy(alpha = 0.7f),
    val focusColor: Color = Color.Gray.copy(alpha = 0.4f),
    val cornerRadius: Dp = 8.dp,
    val horizontalPadding: Dp = 8.dp,
    val verticalPadding: Dp = -6.dp
)

@Composable
fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    data: List<T>,
    key: (index: Int) -> String? = { null },
    itemContent: @Composable (Int) -> Unit,
    state: WheelPickerState,
    nonFocusedItems: Int = WheelPickerDefaults.DEFAULT_UNFOCUSED_ITEMS_COUNT,
    contentAlignment: Alignment = Alignment.Center,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    itemHeightDp: Dp = WheelPickerDefaults.DefaultItemHeight,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    overlay: OverlayConfiguration = OverlayConfiguration()
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

    Box(
        modifier = modifier
            .height(height / (viewportCurveRate / curveRate)) // Для curveRate = 1.29f, получено эмпирически
            .clipScrollableContainer(orientation = Orientation.Vertical)
    ) {
        CompositionLocalProvider(
            LocalOverscrollFactory provides null
        ) {
            LazyColumn(
                state = state.lazyListState,
                modifier = Modifier
                    .requiredHeight(height)
                    .disableParentNestedVerticalScroll()
                    .drawWithCache {
                        pickerOverlay(
                            edgeOffsetYPx = edgeOffsetPx,
                            itemHeightPx = itemHeightPx,
                            overlay = overlay
                        )
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
                        onItemClick = {
                            scope.launch {
                                state.lazyListState.animateScrollToItem(index)
                            }
                        },
                        itemHeightDp = itemHeightDp,
                        contentPadding = contentPadding,
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
    onItemClick: () -> Unit,
    itemHeightDp: Dp,
    contentPadding: PaddingValues,
    contentAlignment: Alignment,
    index: Int,
    getLayoutInfo: () -> LazyListLayoutInfo,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onItemClick
            )
            .requiredHeight(itemHeightDp)
            .graphicsLayer {
                render3DVerticalItemEffect(
                    index = index,
                    getLayoutInfo = getLayoutInfo,
                    transformOrigin = transformOrigin
                )
            }
            .padding(contentPadding),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

private fun Modifier.disableParentNestedVerticalScroll() = this.nestedScroll(VerticalParentScrollConsumer)

private val VerticalParentScrollConsumer = object : NestedScrollConnection {

    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = available

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = available
}

// Коэффициент кривой, можно поставить свой
private val curveRate = 1.29f // Наиболее похожий коэффициент на iOS
private const val viewportCurveRate = 1.53f //  При этом коэффициенте заполняется весь viewport

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
    val scale = 1 - (offsetFraction.absoluteValue).pow(2) * 0.1f
    scaleX = scale

    // Не показываем элементы, которые не попадают в viewport
    if (offsetFraction.absoluteValue >= 1.0f) {
        alpha = 0f
    }

    rotationX = -90 * offsetFraction

    // Определяем радиус колеса (L = π * r = viewportHeight * curveRate (считаем, что длина кривой равна сумме высот всех элементов, т.е. весь viewport, умноженный на коэффициент кривой))
    val r = (2f * viewportCenterY * curveRate / Math.PI).toFloat()

    translationY = if (offsetFraction == 0f) {
        0f
    } else {
        // Определяем видимую высоту элемента (вспоминаем тригонометрию)
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
    this.cameraDistance = layoutInfo.viewportSize.height.toFloat() / 22f
    this.transformOrigin = transformOrigin
}

private fun getItemCenter(itemInfo: LazyListItemInfo): Float {
    val itemCenterY = itemInfo.size / 2F
    return itemInfo.offset.toFloat() + itemCenterY
}


@Preview
@Composable
private fun WheelPickerPreview() {
    val list = buildList {
        repeat(10) {
            add("Item ${(it + 1)}")
        }
    }
    Column(
        modifier = Modifier.background(Color.White)
    ) {
        WheelPicker(
            data = list,
            itemContent = {
                Text(
                    text = list[it],
                    color = Color.Black,
                    fontSize = 18.sp
                )
            },
            nonFocusedItems = 6,
            state = rememberWheelPickerState(
                initialIndex = 4
            )
        )
    }
}