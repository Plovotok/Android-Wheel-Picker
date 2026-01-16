package io.github.plovotok.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified

internal fun Density.dpToFloatPx(dp: Dp) = if (dp.isSpecified) dp.toPx() else Float.NaN


@Composable
internal fun Dp.toFloatPx() = LocalDensity.current.dpToFloatPx(this)