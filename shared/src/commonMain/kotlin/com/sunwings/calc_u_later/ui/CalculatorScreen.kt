package com.sunwings.calc_u_later.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sunwings.calc_u_later.calculator.CalculatorAction
import com.sunwings.calc_u_later.calculator.CalculatorOperation
import com.sunwings.calc_u_later.calculator.CalculatorState
import com.sunwings.calc_u_later.calculator.NumberFormatStyle
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
import com.sunwings.calc_u_later.ui.theme.LcdAmber
import com.sunwings.calc_u_later.ui.theme.LcdBase
import com.sunwings.calc_u_later.ui.theme.LcdBorder
import com.sunwings.calc_u_later.ui.theme.LcdTextPrimary
import com.sunwings.calc_u_later.ui.theme.LcdTextSecondary
import com.sunwings.calc_u_later.ui.theme.LcdVintageGreen
import com.sunwings.calc_u_later.ui.theme.adaptiveTypography

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    initialFormat: NumberFormatStyle? = null,
    onFormatChange: ((NumberFormatStyle) -> Unit)? = null,
    initialLcdIndex: Int? = null,
    onLcdIndexChange: ((Int) -> Unit)? = null
) {
    var state by rememberSaveable { mutableStateOf(CalculatorState(localeFormat = initialFormat ?: CalculatorState().localeFormat)) }

    // LCD color cycling state: 0 = base (light blue), 1 = vintage green, 2 = amber
    val lcdColors = listOf(LcdBase, LcdVintageGreen, LcdAmber)
    var lcdIndex by rememberSaveable { mutableStateOf(initialLcdIndex ?: 0) }
    val currentLcdColor = lcdColors[lcdIndex]

    val buttonRows = remember(state.localeFormat) { calculatorButtons(state.localeFormat) }

    Box(modifier = modifier.fillMaxSize().background(CalcBackgroundTop)) {
        Box(
            modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CalcBackgroundTop.copy(
                                alpha = 1.0f
                            ),
                            CalcBackgroundBottom.copy(
                                alpha = 1.0f
                            ),
                            CalcBackgroundTop.copy(
                                alpha = 0.8f
                            )
                        )
                    )
                )
                .drawBehind {
                    // Add brushed aluminum texture to calculator body
                    val aluminumGradient = Brush.verticalGradient(
                        listOf(
                            Color(0xFFE8E8E8),  // Light metallic
                            Color(0xFFD0D0D0),  // Mid tone
                            Color(0xFFC0C0C0)   // Darker metallic
                        )
                    )
                    // Draw texture lines for brushed aluminum effect
                    val lineColor = Color.Black.copy(alpha = 0.02f)
                    for (y in 0..size.height.toInt() step 3) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                }
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val baseWidth = 360.dp
                val baseHeight = 780.dp

                // Platform-specific scaling:
                // - Android: More aggressive scaling for device adaptation
                // - Desktop: Minimal scaling to maintain fixed window appearance
                val MIN_SCALE = 0.6f  // Desktop: don't shrink below 60%
                val MAX_SCALE = 1.0f
                val scaleWidth = (maxWidth / baseWidth)
                val scaleHeight = (baseHeight / maxHeight)
                val scale = minOf(scaleWidth, scaleHeight).coerceIn(MIN_SCALE, MAX_SCALE)

                // Limit content width so button sizes derive from the scaled
                // container (instead of the full screen width). This keeps
                // buttons proportional when centered on tablets.
                val contentWidth = maxWidth * scale

                // Scale everything proportionally including LCD display
                val displayScale = scale

                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp * scale),
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp * scale, bottom = 16.dp * scale)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        Column(
                            modifier = Modifier.width(contentWidth).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            DisplayPanel(state = state, modifier = Modifier.fillMaxWidth(), scale = displayScale, lcdColor = currentLcdColor, onCycle = { delta ->
                                val next = (lcdIndex + delta + lcdColors.size) % lcdColors.size
                                lcdIndex = next
                                onLcdIndexChange?.invoke(next)
                            }) { action ->
                                val next = onCalculatorAction(state, action)
                                if (next.localeFormat != state.localeFormat) {
                                    onFormatChange?.invoke(next.localeFormat)
                                }
                                state = next
                            }
                            ButtonGrid(buttonRows = buttonRows, modifier = Modifier.fillMaxWidth(), scale = scale) { action ->
                                state = onCalculatorAction(state, action)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayPanel(
    state: CalculatorState,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    lcdColor: Color = MaterialTheme.colorScheme.tertiary,
    onCycle: (Int) -> Unit = {},
    onAction: (CalculatorAction) -> Unit = {}
) {
    val lpModifier = modifier
        .pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                onAction(CalculatorAction.ToggleLocaleFormat)
            })
        }
        .pointerInput(Unit) {
            var totalDrag = 0f
            detectDragGestures(
                onDrag = { _, dragAmount ->
                    totalDrag += dragAmount.x
                },
                onDragEnd = {
                    if (kotlin.math.abs(totalDrag) > 120f) {
                        if (totalDrag > 0f) onCycle(-1) else onCycle(1)
                    }
                    totalDrag = 0f
                },
                onDragCancel = {
                    totalDrag = 0f
                }
            )
        }

    Surface(
        modifier = lpModifier.fillMaxWidth()
            .height(180.dp * scale)
            .shadow(
                12.dp * scale,
                RoundedCornerShape(28.dp * scale),
                clip = false,
                ambientColor = ButtonShadow,
                spotColor = ButtonShadow
            ),
        shape = RoundedCornerShape(28.dp * scale),
        color = Color.Transparent
    ) {
        Column(
            modifier = lpModifier.background(
                Brush.verticalGradient(
                    listOf(
                        lcdColor.copy(alpha = 0.9f),
                        lcdColor
                    )
                )
            ).border(1.8.dp * scale, LcdBorder, RoundedCornerShape(28.dp * scale)).padding(horizontal = 12.dp * scale, vertical = 20.dp * scale),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp * scale),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val memoryAlpha by animateFloatAsState(targetValue = if (state.memoryValue == 0.0) 0f else 1f, label = "memoryIndicatorAlpha")
                Text(
                    text = "M",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = MaterialTheme.typography.displayMedium.fontSize * scale * 0.7f),
                    color = LcdTextSecondary.copy(alpha = memoryAlpha),
                    modifier = Modifier.padding(top = 2.dp * scale),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp * scale)
                ) {
                    Column(modifier = Modifier.align(Alignment.TopEnd).height(120.dp * scale), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp * scale)) {
                        Box(modifier = Modifier.height(72.dp * scale)) {
                            Text(
                                text = state.displayValue + ",",
                                style = adaptiveTypography().displayLarge.copy(fontSize = adaptiveTypography().displayLarge.fontSize * scale * 0.75f),
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
                                style = adaptiveTypography().displayLarge.copy(
                                    fontSize = adaptiveTypography().displayLarge.fontSize * scale * 0.75f,
                                    lineHeight = adaptiveTypography().displayLarge.fontSize * scale * 0.75f * 1.1f
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                        Text(
                            text = state.previousExpression,
                            style = adaptiveTypography().displayMedium.copy(fontSize = adaptiveTypography().displayMedium.fontSize * scale * 0.85f),
                            color = LcdTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.height(28.dp * scale).padding(top = 12.dp * scale)
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
    scale: Float = 1f,
    onAction: (CalculatorAction) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 14.dp * scale
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
            MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 0.85f)
        } else {
            MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.85f)
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
