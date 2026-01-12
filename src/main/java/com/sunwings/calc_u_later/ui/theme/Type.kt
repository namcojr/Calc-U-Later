package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.sunwings.calc_u_later.ui.theme.adaptiveDisplayLargeFontSize
import com.sunwings.calc_u_later.ui.theme.adaptiveDisplayMediumFontSize
import com.sunwings.calc_u_later.R


val LcdFontFamily = FontFamily(
    Font(resId = R.font.led_dot_matrix, weight = FontWeight.Normal)
)

val MainDisplayFontFamily = FontFamily(
    Font(resId = R.font.led_italic, weight = FontWeight.Normal)
)

val BodyFontFamily = FontFamily.SansSerif
val MemoryButtonFontFamily = FontFamily.Monospace

@Composable
fun adaptiveTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = MainDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayLargeFontSize(),
        lineHeight = 64.sp,
        letterSpacing = 0.7.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LcdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayMediumFontSize(),
        lineHeight = 28.sp,
        letterSpacing = 1.sp
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