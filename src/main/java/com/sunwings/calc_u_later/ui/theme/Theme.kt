package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CalculatorColorScheme = lightColorScheme(
    primary = ButtonEqualsBottom,
    onPrimary = BaseText,
    secondary = ButtonOperatorTop,
    onSecondary = BaseText,
    tertiary = LcdBase, //LcdVintageGreen,
    onTertiary = BaseText,
    background = CalcBackgroundTop,
    onBackground = BaseText,
    surface = CalcBackgroundTop,
    onSurface = BaseText,
    outline = ButtonBorder
)

@Composable
fun CalcULaterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = adaptiveTypography(),
        content = content
    )
}