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

import androidx.compose.runtime.Stable
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
public object WheelPickerDefaults {

    public const val DEFAULT_UNFOCUSED_ITEMS_COUNT: Int = 8
    public val DefaultItemHeight: Dp = 44.dp

    internal fun CacheDrawScope.pickerOverlay(
        edgeOffsetYPx: Float,
        itemHeightPx: Int,
        overlay: OverlayConfiguration,
        transformOrigin: TransformOrigin,
        wheelIndex: Int,
    ): DrawResult {
        return if (!overlay.isWheelItem) {
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
            onDrawWithContent {
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
        } else onDrawWithContent {
            drawContent()

            drawScaleContent(
                scrimColor = overlay.scrimColor,
                itemHeightPx = itemHeightPx,
                overlayConfig = overlay,
                transformOrigin = transformOrigin,
                wheelIndex = wheelIndex
            )
        }
    }

    private fun ContentDrawScope.drawScaleContent(
        scrimColor: Color,
        itemHeightPx: Int,
        overlayConfig: OverlayConfiguration,
        transformOrigin: TransformOrigin,
        wheelIndex: Int
    ) {
        val overlayTranslate: Dp = overlayConfig.overlayTranslate(wheelIndex)
        drawRect( // draw full background color
            color = scrimColor.copy(alpha = 1f),
            topLeft = Offset(
                x = 0f,
                y = drawContext.size.height / 2 - itemHeightPx / 2 + overlayConfig.verticalPadding.toPx()
            ),
            size = Size(
                width = drawContext.size.width,
                height = itemHeightPx.toFloat() - (overlayConfig.verticalPadding.toPx() * 2)
            )
        )

        clipRect( // clip by overlay size
            left = if (overlayConfig.clipStart) overlayConfig.horizontalPadding.toPx() else 0f,
            right = drawContext.size.width - (if (overlayConfig.clipEnd) overlayConfig.horizontalPadding.toPx() else 0f),
            top = drawContext.size.height / 2 - itemHeightPx / 2 + overlayConfig.verticalPadding.toPx(),
            bottom = drawContext.size.height / 2 + itemHeightPx / 2 - overlayConfig.verticalPadding.toPx()
        ) {
            scale(
                overlayConfig.selectionScale, overlayConfig.selectionScale, // scale overlay content
                pivot = Offset(
                    x = drawContext.size.width * transformOrigin.pivotFractionX,
                    y = drawContext.size.height * transformOrigin.pivotFractionY
                )
            ) {
                clipRect( // clip by scaled overlay content size
                    top = drawContext.size.height / 2 - itemHeightPx / 2 + overlayConfig.verticalPadding.toPx(),
                    bottom = drawContext.size.height / 2 + itemHeightPx / 2 - overlayConfig.verticalPadding.toPx()
                ) {
                    translate(
                        left = overlayTranslate.toPx() // translate if necessary
                    ) {
                        this@drawScaleContent.drawContent()
                    }
                }
            }
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

    internal const val curveRate = 1.0f
    internal const val viewportCurveRate = 0.653f //  При этом коэффициенте заполняется весь viewport, получен эмпирически
}