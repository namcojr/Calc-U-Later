package com.sunwings.calc_u_later.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEdgeCasesTest {

    // Helper to normalize display strings (Portuguese grouping '.' and ',' decimal)
    private fun displayToDouble(display: String): Double {
        val raw = display.trim()
        if (raw.equals("Error", true) || raw.equals("Overflow", true)) return 0.0
        if (raw.contains('e') || raw.contains('E')) {
            return raw.replace(" ", "").replace('E', 'e').toDoubleOrNull() ?: 0.0
        }
        return when {
            raw.contains(',') -> raw.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            Regex("^[-+]?\\d{1,3}(?:\\.\\d{3})+$").matches(raw) -> raw.replace(".", "").toDoubleOrNull() ?: 0.0
            else -> raw.toDoubleOrNull() ?: 0.0
        }
    }

    @Test
    fun negativeNumbers_additionAndToggleSign() {
        var state = CalculatorState()
        // Enter -5 (simulate toggle after entering 5)
        state = onCalculatorAction(state, CalculatorAction.Digit(5))
        state = onCalculatorAction(state, CalculatorAction.ToggleSign)
        // Add 3 -> expect -2
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        state = onCalculatorAction(state, CalculatorAction.Digit(3))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        assertEquals("-2", state.displayValue)
    }

    @Test
    fun scientificInput_parsedAndUsedInOperation() {
        // Use memory to inject a scientific-formatted value and recall it
        var state = CalculatorState()
        // Directly set memory to a very large number and recall
        state = state.copy(memoryValue = 1e12)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        // Multiply by 2 -> expect 2e12 displayed in scientific or grouped form
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Multiply))
        state = onCalculatorAction(state, CalculatorAction.Digit(2))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        // Parse display back to double using test-local normalizer
        val result = displayToDouble(state.displayValue)
        assertEquals(2e12, result, 0.0)
    }

    @Test
    fun verySmallNumber_handling() {
        var state = CalculatorState()
        // Enter a very small number by setting memory and recalling
        state = state.copy(memoryValue = 0.00000012)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        // Add 0 -> expect the same small number
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        state = onCalculatorAction(state, CalculatorAction.Digit(0))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        val parsed = displayToDouble(state.displayValue)
        // Allow tiny delta
        assertEquals(0.00000012, parsed, 1e-12)
    }

    @Test
    fun veryLargeNumber_groupingAndArithmetic() {
        var state = CalculatorState()
        // Build a large grouped number by setting memory and recalling
        state = state.copy(memoryValue = 9_999_999_999.0)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        // MemoryRecall sets overwriteDisplay=true; clear it so we append to recalled value
        state = state.copy(overwriteDisplay = false)
        // Add 0,2 (use comma decimal) by simulating decimal entry
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        state = onCalculatorAction(state, CalculatorAction.Digit(2))
        // Now add 0 -> ensure evaluation keeps precision
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        state = onCalculatorAction(state, CalculatorAction.Digit(0))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        val parsed = displayToDouble(state.displayValue)
        // Allow small display/formatting rounding differences for very large values
        assertEquals(9_999_999_999.2, parsed, 1.0)
        // Also assert the visual length does not exceed a safe visible limit (15)
        val VISIBLE_LIMIT = 15
        assert(state.displayValue.length <= VISIBLE_LIMIT)
    }
}
