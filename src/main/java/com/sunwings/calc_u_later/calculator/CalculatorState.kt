package com.sunwings.calc_u_later.calculator

import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

private const val MAX_DISPLAY_LENGTH = 14

data class CalculatorState(
    val displayValue: String = "0",
    val previousExpression: String = "",
    val accumulator: Double? = null,
    val pendingOperation: CalculatorOperation? = null,
    val overwriteDisplay: Boolean = true,
    val memoryValue: Double = 0.0,
    val isError: Boolean = false
) : Serializable

enum class CalculatorOperation(val symbol: String) {
    Add("+"),
    Subtract("−"),
    Multiply("×"),
    Divide("÷")
}

sealed interface CalculatorAction {
    data class Digit(val value: Int) : CalculatorAction
    object Decimal : CalculatorAction
    data class Operation(val type: CalculatorOperation) : CalculatorAction
    object Equals : CalculatorAction
    object Clear : CalculatorAction
    object Percent : CalculatorAction
    object ToggleSign : CalculatorAction
    object Backspace : CalculatorAction
    object MemoryClear : CalculatorAction
    object MemoryRecall : CalculatorAction
    object MemoryAdd : CalculatorAction
    object MemorySubtract : CalculatorAction
}

fun onCalculatorAction(state: CalculatorState, action: CalculatorAction): CalculatorState {
    if (state.isError && action !is CalculatorAction.Clear) {
        return state
    }

    return when (action) {
        is CalculatorAction.Digit -> appendDigit(state, action.value)
        CalculatorAction.Decimal -> appendDecimal(state)
        is CalculatorAction.Operation -> applyOperation(state, action.type)
        CalculatorAction.Equals -> evaluate(state)
        CalculatorAction.Clear -> state.reset()
        CalculatorAction.Percent -> applyPercent(state)
        CalculatorAction.ToggleSign -> toggleSign(state)
        CalculatorAction.Backspace -> backspace(state)
        CalculatorAction.MemoryClear -> state.copy(memoryValue = 0.0)
        CalculatorAction.MemoryRecall -> state.withDisplay(state.memoryValue)
        CalculatorAction.MemoryAdd -> state.copy(memoryValue = state.memoryValue + state.displayValue.toSafeDouble())
        CalculatorAction.MemorySubtract -> state.copy(memoryValue = state.memoryValue - state.displayValue.toSafeDouble())
    }
}

private fun appendDigit(state: CalculatorState, digit: Int): CalculatorState {
    val cleanDigit = digit.coerceIn(0, 9).toString()
    val nextValue = when {
        state.overwriteDisplay || state.displayValue == "0" -> cleanDigit
        state.displayValue.length >= MAX_DISPLAY_LENGTH -> state.displayValue
        else -> state.displayValue + cleanDigit
    }
    return state.copy(
        displayValue = nextValue,
        overwriteDisplay = false,
        isError = false
    )
}

private fun appendDecimal(state: CalculatorState): CalculatorState {
    val base = if (state.overwriteDisplay) "0" else state.displayValue
    if (base.contains('.')) return state
    val next = "$base."
    return state.copy(displayValue = next.take(MAX_DISPLAY_LENGTH), overwriteDisplay = false)
}

private fun applyOperation(state: CalculatorState, operation: CalculatorOperation): CalculatorState {
    val currentValue = state.displayValue.toSafeDouble()

    if (state.accumulator != null && state.pendingOperation != null && !state.overwriteDisplay) {
        val result = performBinaryOperation(state.accumulator, currentValue, state.pendingOperation)
            ?: return state.errorState()
        val formattedResult = formatNumber(result)
        return state.copy(
            displayValue = formattedResult,
            accumulator = result,
            pendingOperation = operation,
            overwriteDisplay = true,
            previousExpression = "${formatNumber(result)} ${operation.symbol}",
            isError = false
        )
    }

    val baseValue = state.accumulator ?: currentValue
    return state.copy(
        accumulator = baseValue,
        pendingOperation = operation,
        overwriteDisplay = true,
        previousExpression = "${formatNumber(baseValue)} ${operation.symbol}",
        isError = false
    )
}

private fun evaluate(state: CalculatorState): CalculatorState {
    val op = state.pendingOperation ?: return state
    val accumulator = state.accumulator ?: return state
    val currentValue = state.displayValue.toSafeDouble()
    val result = performBinaryOperation(accumulator, currentValue, op) ?: return state.errorState()
    return state.copy(
        displayValue = formatNumber(result),
        previousExpression = "${formatNumber(accumulator)} ${op.symbol} ${formatNumber(currentValue)} =",
        accumulator = null,
        pendingOperation = null,
        overwriteDisplay = true,
        isError = false
    )
}

private fun applyPercent(state: CalculatorState): CalculatorState {
    val currentValue = state.displayValue.toSafeDouble()
    val basis = if (state.accumulator != null && state.pendingOperation != null) {
        state.accumulator * (currentValue / 100.0)
    } else {
        currentValue / 100.0
    }
    return state.copy(
        displayValue = formatNumber(basis),
        overwriteDisplay = true,
        isError = false
    )
}

private fun toggleSign(state: CalculatorState): CalculatorState {
    val currentValue = state.displayValue.toSafeDouble()
    if (currentValue == 0.0) return state
    val toggled = -currentValue
    return state.copy(
        displayValue = formatNumber(toggled),
        overwriteDisplay = false,
        isError = false
    )
}

private fun backspace(state: CalculatorState): CalculatorState {
    if (state.overwriteDisplay) {
        return state.copy(displayValue = "0", overwriteDisplay = true)
    }
    val trimmed = state.displayValue.dropLast(1).ifEmpty { "0" }
    val sanitized = if (trimmed == "-" || trimmed == "-0") "0" else trimmed
    return state.copy(displayValue = sanitized, overwriteDisplay = false)
}

private fun CalculatorState.reset(): CalculatorState = CalculatorState(memoryValue = memoryValue)

private fun CalculatorState.withDisplay(value: Double): CalculatorState = copy(
    displayValue = formatNumber(value),
    overwriteDisplay = true,
    isError = false
)

private fun CalculatorState.errorState(): CalculatorState = copy(
    displayValue = "Error",
    previousExpression = "",
    accumulator = null,
    pendingOperation = null,
    overwriteDisplay = true,
    isError = true
)

private fun performBinaryOperation(lhs: Double, rhs: Double, operation: CalculatorOperation): Double? = when (operation) {
    CalculatorOperation.Add -> lhs + rhs
    CalculatorOperation.Subtract -> lhs - rhs
    CalculatorOperation.Multiply -> lhs * rhs
    CalculatorOperation.Divide -> if (abs(rhs) < 1e-12) null else lhs / rhs
}

private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val absValue = kotlin.math.abs(value)
    if (absValue >= 1e100) return "Overflow"
    // Use scientific notation if too large or too small for display
    if ((absValue >= 1e10 || (absValue != 0.0 && absValue < 1e-4))) {
        val exp = String.format("%.6e", value)
        // Format as <number> e<exp> (e.g., 1.234567e+12 -> 1.234567 e12)
        val match = Regex("""([\-\d.]+)e([+-]?\d+)""").find(exp)
        return if (match != null) {
            val (num, expPart) = match.destructured
            "$num e$expPart"
        } else exp
    }
    // Limit to 8 decimal places for normal display
    val bigDecimal = BigDecimal.valueOf(value).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
    val plain = bigDecimal.toPlainString()
    return if (plain.length <= MAX_DISPLAY_LENGTH) plain else plain.take(MAX_DISPLAY_LENGTH)
}

private fun String.toSafeDouble(): Double {
    // Try normal parse
    toDoubleOrNull()?.let { return it }
    // Try scientific notation with ' e' (e.g., "1.23 e12")
    val sciMatch = Regex("""^([\-\d.]+)\s*e([+\-]?\d+)$""").find(this.trim())
    if (sciMatch != null) {
        val (num, exp) = sciMatch.destructured
        return try {
            num.toDouble() * Math.pow(10.0, exp.toDouble())
        } catch (_: Exception) {
            0.0
        }
    }
    return 0.0
}