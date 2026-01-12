package ru.plovotok.wheel_picker.ui.components.wheel_picker

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

object WheelPickerDefaults {

    const val DEFAULT_UNFOCUSED_ITEMS_COUNT = 8
    val DefaultItemHeight = 44.dp

    fun CacheDrawScope.pickerOverlay(
        edgeOffsetYPx: Float,
        itemHeightPx: Int,
        overlay: OverlayConfiguration,
    ): DrawResult {
        val w = this.size.width
        val h = this.size.height
        val radius = overlay.cornerRadius.toPx()
        val verticalPadding = overlay.verticalPadding.toPx()
        val horizontalPadding = overlay.horizontalPadding.toPx()

        val scrimHeight = edgeOffsetYPx + verticalPadding
        val highlightHeight = itemHeightPx - verticalPadding * 2
        val path = getCenterItemPath(
            width = w,
            height = h,
            itemHeightPx = itemHeightPx,
            edgeOffsetY = edgeOffsetYPx,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            radius = radius
        )
        return onDrawWithContent {
            drawContent()
            this.drawPath(path, overlay.scrimColor)

            this.drawRoundRect(
                color = overlay.focusColor,
                topLeft = Offset(x = horizontalPadding, y = scrimHeight),
                size = Size(w - horizontalPadding * 2, highlightHeight),
                cornerRadius = CornerRadius(radius),
                style = Fill,
            )
        }
    }


    // Path с вырезанным скругленным прямоугольником посередине
    private fun getCenterItemPath(
        width: Float,
        height: Float,
        itemHeightPx: Int,
        edgeOffsetY: Float,
        horizontalPadding: Float = 0f,
        verticalPadding: Float = 0f,
        radius: Float
    ): Path {
        val boundPath = Path().apply {
            addRect(Rect(0f,0f,width,height))
        }
        val focusPath = Path().apply {
            val left = horizontalPadding
            val top = edgeOffsetY + verticalPadding
            val right = width - horizontalPadding
            val bottom = edgeOffsetY + itemHeightPx - verticalPadding
            addRoundRect(RoundRect(left, top, right, bottom, radius, radius))
        }
        val resPath = Path()
        resPath.op(boundPath,focusPath, PathOperation.Difference)
        return resPath
    }
}