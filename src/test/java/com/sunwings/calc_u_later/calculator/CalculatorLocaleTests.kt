package com.sunwings.calc_u_later.calculator

import org.junit.Assert.*
import org.junit.Test

class CalculatorLocaleTests {
    @Test
    fun toggleLocale_clearsDisplayAndPending() {
        var state = CalculatorState(displayValue = "1234", accumulator = 12.0, pendingOperation = CalculatorOperation.Add, previousExpression = "12 +")
        state = onCalculatorAction(state, CalculatorAction.ToggleLocaleFormat)
        assertEquals("0", state.displayValue)
        assertNull(state.accumulator)
        assertNull(state.pendingOperation)
        assertEquals("", state.previousExpression)
    }

    @Test
    fun clear_preservesLocaleFormat() {
        var state = CalculatorState(localeFormat = NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT, displayValue = "1234")
        state = onCalculatorAction(state, CalculatorAction.Clear)
        assertEquals(NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT, state.localeFormat)
        assertEquals("0", state.displayValue)
    }

    @Test
    fun grouping_displayInPreviousExpression_respectsLocale() {
        // Input 23666 × 20.3 under DOT_GROUP_DECIMAL_COMMA
        var state = CalculatorState(localeFormat = NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA, displayValue = "23666")
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Multiply))
        state = onCalculatorAction(state, CalculatorAction.Digit(2))
        state = onCalculatorAction(state, CalculatorAction.Digit(0))
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        state = onCalculatorAction(state, CalculatorAction.Digit(3))
        // Evaluate
        state = onCalculatorAction(state, CalculatorAction.Equals)
        // The result should be displayed using the style
        assertEquals("480.419,8", state.displayValue)
        // Now press another operator; previousExpression should use grouped format with comma decimal
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        assertTrue(state.previousExpression.contains("480.419,8") || state.previousExpression.contains("ans"))
    }
}
