package com.sunwings.calc_u_later.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.sunwings.calc_u_later.ui.theme.*

private val CalculatorColorScheme = lightColorScheme(
    primary = ButtonEqualsBottom,
    onPrimary = BaseText,
    secondary = ButtonOperatorTop,
    onSecondary = BaseText,
    tertiary = LcdBase,
    onTertiary = BaseText,
    background = CalcBackgroundTop,
    onBackground = BaseText,
    surface = CalcBackgroundTop,
    onSurface = BaseText,
    outline = ButtonBorder
)

@Composable
fun CalcULaterThemeAndroid(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = getAndroidTypography(),
        content = content
    )
}
