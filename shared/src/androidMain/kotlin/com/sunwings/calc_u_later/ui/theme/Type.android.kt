package com.sunwings.calc_u_later.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Android implementation - fonts are loaded from app resources
 * The androidApp module loads these via R.font references
 * Here we use system defaults that the androidApp will override
 */

// For Android in KMP, we use system fonts as defaults
// The actual LCD fonts are loaded by the androidApp's MainActivity/CalculatorScreen
actual val LcdFontFamily: FontFamily = FontFamily.SansSerif

actual val MainDisplayFontFamily: FontFamily = FontFamily.SansSerif

actual val BodyFontFamily: FontFamily = FontFamily.SansSerif

actual val MemoryButtonFontFamily: FontFamily = FontFamily.Monospace



