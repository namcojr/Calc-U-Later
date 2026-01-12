package com.sunwings.calc_u_later.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

@Composable
fun adaptiveDisplayLargeFontSize(): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    // Multiplier: base size * (screenWidthDp / referenceWidth)
    val referenceWidth = 420 // Pixel 9XL dp width (example)
    val baseSize = 56 // base font size in sp
    val multiplier = screenWidthDp / referenceWidth.toFloat()
    return (baseSize * multiplier).sp
}

@Composable
fun adaptiveDisplayMediumFontSize(): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val referenceWidth = 420 // Pixel 9XL dp width (example)
    val baseSize = 18 // base font size in sp
    val multiplier = screenWidthDp / referenceWidth.toFloat()
    return (baseSize * multiplier).sp
}
