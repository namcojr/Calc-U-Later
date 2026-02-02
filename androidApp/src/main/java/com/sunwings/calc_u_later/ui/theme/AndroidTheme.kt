package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.sunwings.calc_u_later.R

/**
 * Android app-specific theme with LCD fonts loaded from res/font/
 * This overrides the shared theme to provide the actual LED fonts
 */

val LcdFontFamilyAndroid = FontFamily(
    Font(resId = R.font.led_dot_matrix, weight = FontWeight.Normal)
)

val MainDisplayFontFamilyAndroid = FontFamily(
    Font(resId = R.font.led_italic, weight = FontWeight.Normal)
)

@Composable
fun adaptiveTypographyAndroid() = Typography(
    displayLarge = TextStyle(
        fontFamily = MainDisplayFontFamilyAndroid,
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayLargeFontSize(),
        lineHeight = 64.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LcdFontFamilyAndroid,
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayMediumFontSize(),
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MainDisplayFontFamilyAndroid,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.sp
    )
)

@Composable
fun adaptiveDisplayLargeFontSize() = 72.sp

@Composable
fun adaptiveDisplayMediumFontSize() = 20.sp
