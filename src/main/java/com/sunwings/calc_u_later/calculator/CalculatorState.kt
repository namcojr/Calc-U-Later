package com.sunwings.calc_u_later.calculator

import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

// Display can show longer computed results; keep input digit limit separate
private const val MAX_DISPLAY_LENGTH = 24
private const val MAX_DIGITS_PER_NUMBER = 10

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
    // Enforce per-number digit limit (ignoring sign and decimal separator)
    if (!canAppendDigit(state.displayValue, state.overwriteDisplay)) return state

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
    // If current number already has a decimal separator, ignore.
    if (base.contains(',')) return state

    // Allow decimal only if digit count won't exceed limit after adding digits.
    // Adding a decimal doesn't increase digit count but avoids a later digit append if limit reached.
    val next = "$base,"
    return state.copy(displayValue = next.take(MAX_DISPLAY_LENGTH), overwriteDisplay = false)
}

/**
 * Returns true if a digit may be appended to the current input considering
 * the per-number digit limit (ignores sign and decimal separator).
 */
internal fun canAppendDigit(displayValue: String, overwriteDisplay: Boolean): Boolean {
    if (overwriteDisplay) return true
    // Determine the current number segment (after the last operator symbol)
    // Operators use symbols: +, −, ×, ÷ (note: subtraction uses Unicode minus)
    val ops = listOf('+', '−', '×', '÷')
    val lastOpIndex = displayValue.lastIndexOfAny(ops.toCharArray())
    val segment = if (lastOpIndex == -1) displayValue else displayValue.substring(lastOpIndex + 1)

    // Count digits (0-9) in the segment, ignore sign and comma
    val digitCount = segment.count { it in '0'..'9' }
    return digitCount < MAX_DIGITS_PER_NUMBER
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
    // Prepare a locale-neutral plain string from the double
    val bigDecimal = BigDecimal.valueOf(value).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
    val plain = bigDecimal.toPlainString()
    // Decide when to use scientific notation (visual only)
    return try {
        val absBd = bigDecimal.abs()
        if (absBd >= BigDecimal("1e10") || (bigDecimal.compareTo(BigDecimal.ZERO) != 0 && absBd < BigDecimal("1e-4"))) {
            // Use scientific notation visually, but keep invariant format
            val exp = String.format("%.6e", value).replace(" ", "")
            // Ensure we don't exceed display length
            if (exp.length <= MAX_DISPLAY_LENGTH) exp else exp.take(MAX_DISPLAY_LENGTH)
        } else {
            // Normal number: apply Portuguese visual formatting
            val formatted = formatForDisplay(plain, false)
            if (formatted.length <= MAX_DISPLAY_LENGTH) formatted else formatted.take(MAX_DISPLAY_LENGTH)
        }
    } catch (e: Exception) {
        // Fallback to plain representation on any unexpected error
        if (plain.length <= MAX_DISPLAY_LENGTH) plain else plain.take(MAX_DISPLAY_LENGTH)
    }
}

/**
 * Pure display-only formatter.
 * - `value`: locale-neutral numeric string (e.g. "1000000.22" or "1.23456e+15").
 * - `isScientific`: hint to force scientific rendering (when true, return invariant scientific form).
 *
 * Returns a display string formatted for Portuguese locale for normal numbers (grouping '.')
 * and ',' as decimal separator. Scientific notation is left invariant (uses '.' decimal and 'e').
 * This function never mutates input and never throws.
 */
private fun formatForDisplay(value: String, isScientific: Boolean): String {
    try {
        val v = value.trim()
        if (v.equals("Error", true) || v.equals("Overflow", true)) return v
        if (v == "-0") return "0"

        // Detect scientific input or forced scientific
        if (isScientific || v.contains('e') || v.contains('E')) {
            // Return invariant scientific notation (no localization). Remove any whitespace.
            return v.replace(" ", "").replace('E', 'e')
        }

        // Parse as BigDecimal to preserve precision for formatting
        val bd = try {
            BigDecimal(v)
        } catch (e: Exception) {
            // If it's not a valid number, return as-is
            return v
        }

        val absBd = bd.abs()
        if (absBd >= BigDecimal("1e100")) return "Overflow"

        // Very large/small numbers -> scientific (invariant)
        if (absBd >= BigDecimal("1e10") || (bd.compareTo(BigDecimal.ZERO) != 0 && absBd < BigDecimal("1e-4"))) {
            val d = try { bd.toDouble() } catch (_: Exception) { return bd.toPlainString() }
            return String.format("%.6e", d).replace(" ", "")
        }

        // Limit to 8 decimal places, half-up, and strip trailing zeros (display-only)
        val scaled = bd.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
        var plain = scaled.toPlainString()
        if (plain == "-0") plain = "0"

        // Split integer and decimal parts (plain uses '.')
        val parts = plain.split('.')
        val intRaw = parts[0]
        val sign = if (intRaw.startsWith("-")) "-" else ""
        val digits = if (sign == "") intRaw else intRaw.substring(1)

        // Group thousands with '.' (visual only)
        val grouped = digits.replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")
        val intPart = sign + grouped

        val decPart = if (parts.size > 1 && parts[1].isNotEmpty()) "," + parts[1] else ""

        return intPart + decPart
    } catch (e: Exception) {
        return value
    }
}

private fun String.toSafeDouble(): Double {
    val raw = this.trim()
    if (raw.equals("Error", true) || raw.equals("Overflow", true)) return 0.0

    // If scientific notation present, parse directly after removing spaces
    if (raw.contains('e') || raw.contains('E')) {
        val s = raw.replace(" ", "").replace('E', 'e')
        return s.toDoubleOrNull() ?: run {
            // Try regex fallback
            val sciMatch = Regex("""^([\-\d.]+)e([+\-]?\d+)$""").find(s)
            if (sciMatch != null) {
                val (num, exp) = sciMatch.destructured
                try {
                    num.toDouble() * Math.pow(10.0, exp.toDouble())
                } catch (_: Exception) { 0.0 }
            } else 0.0
        }
    }

    // If string uses Portuguese formatting (contains ',') then normalize: remove grouping '.' and replace ',' with '.'
    val normalized = if (raw.contains(',')) {
        raw.replace(".", "").replace(",", ".")
    } else raw

    // Try normal parse on normalized string
    return normalized.toDoubleOrNull() ?: 0.0
}