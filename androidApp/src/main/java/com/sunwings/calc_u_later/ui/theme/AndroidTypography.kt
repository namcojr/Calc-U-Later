package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sunwings.calc_u_later.R

/**
 * Android-specific fonts loaded from res/font/
 */
val LcdFontFamily_Android = FontFamily(
    Font(resId = R.font.led_dot_matrix, weight = FontWeight.Normal)
)

val MainDisplayFontFamily_Android = FontFamily(
    Font(resId = R.font.led_italic, weight = FontWeight.Normal)
)

/**
 * Android-specific typography with actual LED fonts
 */
fun getAndroidTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = MainDisplayFontFamily_Android,  // led_italic.ttf
        fontWeight = FontWeight.Normal,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LcdFontFamily_Android,  // led_dot_matrix.ttf
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 28.sp,
        letterSpacing = -0.2.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,  // Default Android font for buttons
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,  // Monospace for memory buttons
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.sp
    )
)
