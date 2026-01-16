package com.sunwings.calc_u_later.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.sunwings.calc_u_later.calculator.NumberFormatStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sunwings.calc_u_later.R
import com.sunwings.calc_u_later.calculator.CalculatorAction
import com.sunwings.calc_u_later.calculator.CalculatorOperation
import com.sunwings.calc_u_later.calculator.CalculatorState
import com.sunwings.calc_u_later.calculator.onCalculatorAction
import com.sunwings.calc_u_later.ui.theme.BaseText
import com.sunwings.calc_u_later.ui.theme.ButtonBorder
import com.sunwings.calc_u_later.ui.theme.ButtonClearBottom
import com.sunwings.calc_u_later.ui.theme.ButtonClearTop
import com.sunwings.calc_u_later.ui.theme.ButtonEqualsBottom
import com.sunwings.calc_u_later.ui.theme.ButtonEqualsTop
import com.sunwings.calc_u_later.ui.theme.ButtonFunctionBottom
import com.sunwings.calc_u_later.ui.theme.ButtonFunctionTop
import com.sunwings.calc_u_later.ui.theme.ButtonNumericBottom
import com.sunwings.calc_u_later.ui.theme.ButtonNumericTop
import com.sunwings.calc_u_later.ui.theme.ButtonOperatorBottom
import com.sunwings.calc_u_later.ui.theme.ButtonOperatorTop
import com.sunwings.calc_u_later.ui.theme.ButtonShadow
import com.sunwings.calc_u_later.ui.theme.CalcBackgroundBottom
import com.sunwings.calc_u_later.ui.theme.CalcBackgroundTop
import com.sunwings.calc_u_later.ui.theme.CalcULaterTheme
import com.sunwings.calc_u_later.ui.theme.LcdBorder
import com.sunwings.calc_u_later.ui.theme.LcdTextPrimary
import com.sunwings.calc_u_later.ui.theme.LcdTextSecondary
import com.sunwings.calc_u_later.ui.theme.adaptiveTypography

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, initialFormat: com.sunwings.calc_u_later.calculator.NumberFormatStyle? = null, onFormatChange: ((com.sunwings.calc_u_later.calculator.NumberFormatStyle) -> Unit)? = null) {
        var state by rememberSaveable { mutableStateOf(CalculatorState(localeFormat = initialFormat ?: CalculatorState().localeFormat)) }

        val buttonRows = remember(state.localeFormat) { calculatorButtons(state.localeFormat) }

        Box(modifier = modifier.fillMaxSize()) {
                Image(
                        painter = painterResource(R.drawable.aluminum),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                )
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                Brush.verticalGradient(
                                                        listOf(
                                                                CalcBackgroundTop.copy(
                                                                        alpha = 0.5f
                                                                ),
                                                                CalcBackgroundBottom.copy(
                                                                        alpha = 0.5f
                                                                )
                                                        )
                                                )
                                        )
                                        .padding(horizontal = 24.dp, vertical = 36.dp)
                ) {
                        Column(
                                verticalArrangement =
                                        Arrangement.spacedBy(58.dp), // LCD to Button Grid spacing
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(
                                                        top = 42.dp
                                                ) // Top padding from the top of the phone screen
                        ) {
                                DisplayPanel(state = state) { action ->
                                        val next = onCalculatorAction(state, action)
                                        if (next.localeFormat != state.localeFormat) {
                                                onFormatChange?.invoke(next.localeFormat)
                                        }
                                        state = next
                                }
                                ButtonGrid(buttonRows = buttonRows) { action ->
                                        state = onCalculatorAction(state, action)
                                }
                        }
                }
        }
}

@Composable
private fun DisplayPanel(state: CalculatorState, modifier: Modifier = Modifier, onAction: (CalculatorAction) -> Unit = {}) {
        // Long-press the display to toggle numeric formatting (grouping and decimal).
        // The `onAction` callback receives the ToggleLocaleFormat action; the caller
        // (CalculatorScreen) persists the updated preference when provided.
        // Note: Toggle clears the current result/pending operation to avoid mismatches.
        val lpModifier = modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                        onAction(CalculatorAction.ToggleLocaleFormat)
                })
        }

        Surface(
                modifier = lpModifier.fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .shadow(
                                12.dp,
                                RoundedCornerShape(28.dp),
                                clip = false,
                                ambientColor = ButtonShadow,
                                spotColor = ButtonShadow
                        ),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent
        ) {
                Column(
                        modifier = lpModifier.background(
                                Brush.verticalGradient(
                                        listOf(
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                                                MaterialTheme.colorScheme.tertiary
                                        )
                                )
                        ).border(1.8.dp, LcdBorder, RoundedCornerShape(28.dp)).padding(horizontal = 8.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                        ) {
                                val memoryAlpha by animateFloatAsState(targetValue = if (state.memoryValue == 0.0) 0f else 1f, label = "memoryIndicatorAlpha")
                                Text(
                                        text = "M",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = LcdTextSecondary.copy(alpha = memoryAlpha),
                                        modifier = Modifier.padding(top = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                )
                                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)) {
                                        Column(modifier = Modifier.align(Alignment.TopEnd).height(120.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(modifier = Modifier.height(72.dp)) {
                                                        Text(
                                                                text = state.displayValue + ",",
                                                                style = adaptiveTypography().displayLarge,
                                                                color = LcdTextPrimary.copy(alpha = 0f),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Clip,
                                                                modifier = Modifier.matchParentSize()
                                                        )
                                                        Text(
                                                                text = buildAnnotatedString {
                                                                        append(state.displayValue)
                                                                        withStyle(SpanStyle(color = Color.Transparent)) { append(",") }
                                                                },
                                                                style = adaptiveTypography().displayLarge.copy(lineHeight = adaptiveTypography().displayLarge.fontSize * 1.1f),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Clip
                                                        )
                                                }
                                                Text(
                                                        text = state.previousExpression,
                                                        style = adaptiveTypography().displayMedium,
                                                        color = LcdTextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Clip,
                                                        modifier = Modifier.height(28.dp).padding(top = 12.dp)
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun ButtonGrid(
        buttonRows: List<List<ButtonSpec>>,
        modifier: Modifier = Modifier,
        onAction: (CalculatorAction) -> Unit
) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
                val spacing = 14.dp
                val buttonSize = (maxWidth - spacing * 3) / 4
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        buttonRows.forEachIndexed { index, row ->
                                val height = if (index == 0) buttonSize * 0.65f else buttonSize
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing)
                                ) {
                                        row.forEach { spec ->
                                                CalculatorButton(
                                                        spec = spec,
                                                        modifier =
                                                                Modifier.width(buttonSize)
                                                                        .height(height)
                                                ) { onAction(spec.action) }
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun CalculatorButton(spec: ButtonSpec, modifier: Modifier = Modifier, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val palette = spec.role.palette()
        val highlightColor = if (isPressed) palette.top.adjust(0.9f) else palette.top.adjust(1.05f)
        val shadowColor =
                if (isPressed) palette.bottom.adjust(0.85f) else palette.bottom.adjust(0.95f)
        val usesMemoryFont = spec.role == ButtonRole.Memory || spec.label == "DEL"
        val textStyle =
                if (usesMemoryFont) {
                        MaterialTheme.typography.labelMedium
                } else {
                        MaterialTheme.typography.labelLarge
                }
        val gradient =
                remember(isPressed, palette) {
                        Brush.linearGradient(
                                colors = listOf(highlightColor, shadowColor),
                                start = Offset.Zero,
                                end = Offset.Infinite
                        )
                }
        val buttonShape = RoundedCornerShape(18.dp)
        val density = LocalDensity.current
        val shadowOffsetPx = with(density) { 1.dp.toPx() }
        val cornerPx = with(density) { 18.dp.toPx() }

        Surface(
                modifier =
                        modifier.drawBehind {
                                drawRoundRect(
                                        color = ButtonShadow.copy(alpha = 0.35f),
                                        topLeft = Offset(-shadowOffsetPx, shadowOffsetPx),
                                        size =
                                                Size(
                                                        size.width + shadowOffsetPx,
                                                        size.height + shadowOffsetPx
                                                ),
                                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                                )
                        },
                color = Color.Transparent,
                shape = buttonShape
        ) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .clip(buttonShape)
                                        .background(gradient)
                                        .border(
                                                1.3.dp,
                                                ButtonBorder.copy(alpha = 0.75f),
                                                buttonShape
                                        )
                                        .clickable(
                                                interactionSource = interactionSource,
                                                indication = null,
                                                onClick = onClick
                                        )
                                        .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = spec.label,
                                style = textStyle,
                                color = palette.content,
                                maxLines = 1
                        )
                }
        }
}

private data class ButtonSpec(
        val label: String,
        val role: ButtonRole,
        val action: CalculatorAction
)

private enum class ButtonRole {
        Memory,
        Function,
        Operator,
        Numeric,
        Clear,
        Equals
}

private data class ButtonPalette(val top: Color, val bottom: Color, val content: Color)

private fun ButtonRole.palette(): ButtonPalette =
        when (this) {
                ButtonRole.Memory ->
                        ButtonPalette(ButtonFunctionTop, ButtonFunctionBottom, Color.White)
                ButtonRole.Function ->
                        ButtonPalette(ButtonOperatorTop, ButtonOperatorBottom, BaseText)
                ButtonRole.Operator ->
                        ButtonPalette(ButtonOperatorTop, ButtonOperatorBottom, BaseText)
                ButtonRole.Numeric -> ButtonPalette(ButtonNumericTop, ButtonNumericBottom, BaseText)
                ButtonRole.Clear -> ButtonPalette(ButtonClearTop, ButtonClearBottom, Color.White)
                ButtonRole.Equals -> ButtonPalette(ButtonEqualsTop, ButtonEqualsBottom, Color.White)
        }

private fun calculatorButtons(format: NumberFormatStyle): List<List<ButtonSpec>> =
        run {
                val decimal = when (format) {
                        NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA -> ","
                        NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT -> "."
                }
                listOf(
                listOf(
                        ButtonSpec("MC", ButtonRole.Memory, CalculatorAction.MemoryClear),
                        ButtonSpec("M+", ButtonRole.Memory, CalculatorAction.MemoryAdd),
                        ButtonSpec("M-", ButtonRole.Memory, CalculatorAction.MemorySubtract),
                        ButtonSpec("MR", ButtonRole.Memory, CalculatorAction.MemoryRecall)
                ),
                listOf(
                        ButtonSpec("AC", ButtonRole.Clear, CalculatorAction.Clear),
                        ButtonSpec("%", ButtonRole.Function, CalculatorAction.Percent),
                        ButtonSpec("+/-", ButtonRole.Function, CalculatorAction.ToggleSign),
                        ButtonSpec(
                                "÷",
                                ButtonRole.Operator,
                                CalculatorAction.Operation(CalculatorOperation.Divide)
                        )
                ),
                listOf(
                        ButtonSpec("7", ButtonRole.Numeric, CalculatorAction.Digit(7)),
                        ButtonSpec("8", ButtonRole.Numeric, CalculatorAction.Digit(8)),
                        ButtonSpec("9", ButtonRole.Numeric, CalculatorAction.Digit(9)),
                        ButtonSpec(
                                "×",
                                ButtonRole.Operator,
                                CalculatorAction.Operation(CalculatorOperation.Multiply)
                        )
                ),
                listOf(
                        ButtonSpec("4", ButtonRole.Numeric, CalculatorAction.Digit(4)),
                        ButtonSpec("5", ButtonRole.Numeric, CalculatorAction.Digit(5)),
                        ButtonSpec("6", ButtonRole.Numeric, CalculatorAction.Digit(6)),
                        ButtonSpec(
                                "−",
                                ButtonRole.Operator,
                                CalculatorAction.Operation(CalculatorOperation.Subtract)
                        )
                ),
                listOf(
                        ButtonSpec("1", ButtonRole.Numeric, CalculatorAction.Digit(1)),
                        ButtonSpec("2", ButtonRole.Numeric, CalculatorAction.Digit(2)),
                        ButtonSpec("3", ButtonRole.Numeric, CalculatorAction.Digit(3)),
                        ButtonSpec(
                                "+",
                                ButtonRole.Operator,
                                CalculatorAction.Operation(CalculatorOperation.Add)
                        )
                ),
                listOf(
                        ButtonSpec("DEL", ButtonRole.Function, CalculatorAction.Backspace),
                        ButtonSpec("0", ButtonRole.Numeric, CalculatorAction.Digit(0)),
                        ButtonSpec(decimal, ButtonRole.Numeric, CalculatorAction.Decimal),
                        ButtonSpec("=", ButtonRole.Equals, CalculatorAction.Equals)
                )
        )
        }

private fun Color.adjust(multiplier: Float): Color {
        val r = (red * multiplier).coerceIn(0f, 1f)
        val g = (green * multiplier).coerceIn(0f, 1f)
        val b = (blue * multiplier).coerceIn(0f, 1f)
        return Color(r, g, b, alpha)
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
        CalcULaterTheme { CalculatorScreen() }
}
