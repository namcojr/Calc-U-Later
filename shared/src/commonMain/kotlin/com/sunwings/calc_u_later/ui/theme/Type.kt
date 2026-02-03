package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable

// Platform-specific font families (defined in androidMain and desktopMain)
expect val LcdFontFamily: FontFamily
expect val MainDisplayFontFamily: FontFamily
expect val BodyFontFamily: FontFamily
expect val MemoryButtonFontFamily: FontFamily

@Composable
fun adaptiveDisplayLargeFontSize() = 56.sp

@Composable
fun adaptiveDisplayMediumFontSize() = 20.sp

@Composable
fun adaptiveTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = MainDisplayFontFamily,  // LCD italic font for main calculator display
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayLargeFontSize(),
        lineHeight = 64.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LcdFontFamily,  // LCD font for secondary display
        fontWeight = FontWeight.Normal,
        fontSize = adaptiveDisplayMediumFontSize(),
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,  // Default font for calculator buttons
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MemoryButtonFontFamily,  // Monospace font for memory buttons
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.sp
    )
)
