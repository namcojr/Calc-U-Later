package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sunwings.calc_u_later.R

val LcdFontFamily = FontFamily(
    Font(resId = R.font.led_dot_matrix, weight = FontWeight.Normal)
)

val BodyFontFamily = FontFamily.SansSerif
val MemoryButtonFontFamily = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = LcdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 58.sp,
        lineHeight = 56.sp,
        letterSpacing = 2.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LcdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.2.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MemoryButtonFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.sp
    )
)