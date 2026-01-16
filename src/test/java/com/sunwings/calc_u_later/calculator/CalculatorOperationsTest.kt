package com.sunwings.calc_u_later.calculator

import org.junit.Assert.*
import org.junit.Test

class CalculatorOperationsTest {

    @Test
    fun additionWithGrouping_preservesInternalValue() {
        var state = CalculatorState()
        // Enter 8800
        listOf(8, 8, 0, 0).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }
        // + 2100
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        listOf(2, 1, 0, 0).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }
        // Evaluate -> should display grouped Portuguese "10.900"
        state = onCalculatorAction(state, CalculatorAction.Equals)
        assertEquals("10.900", state.displayValue)

        // Now add 100 -> 10900 + 100 = 11000 -> displayed as "11.000"
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        listOf(1, 0, 0).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }
        state = onCalculatorAction(state, CalculatorAction.Equals)
        assertEquals("11.000", state.displayValue)
    }

    @Test
    fun decimalAddition_respectsCommaDecimal() {
        var state = CalculatorState()
        // Enter 10,5
        listOf(1, 0).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        state = onCalculatorAction(state, CalculatorAction.Digit(5))

        // + 0,403
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Add))
        state = onCalculatorAction(state, CalculatorAction.Digit(0))
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        listOf(4, 0, 3).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }

        state = onCalculatorAction(state, CalculatorAction.Equals)
        // 10.5 + 0.403 = 10.903 -> displayed "10,903"
        assertEquals("10,903", state.displayValue)
    }

    @Test
    fun multiplyAndDivide_withDecimals() {
        var state = CalculatorState()
        // 1,5 × 2 = 3
        state = onCalculatorAction(state, CalculatorAction.Digit(1))
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        state = onCalculatorAction(state, CalculatorAction.Digit(5))
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Multiply))
        state = onCalculatorAction(state, CalculatorAction.Digit(2))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        assertEquals("3", state.displayValue)

        // 10,5 ÷ 2 = 5,25
        state = CalculatorState()
        listOf(1, 0).forEach { state = onCalculatorAction(state, CalculatorAction.Digit(it)) }
        state = onCalculatorAction(state, CalculatorAction.Decimal)
        state = onCalculatorAction(state, CalculatorAction.Digit(5))
        state = onCalculatorAction(state, CalculatorAction.Operation(CalculatorOperation.Divide))
        state = onCalculatorAction(state, CalculatorAction.Digit(2))
        state = onCalculatorAction(state, CalculatorAction.Equals)
        assertEquals("5,25", state.displayValue)
    }

    @Test
    fun memoryOperations_handleGroupingAndDecimal() {
        // M+ with grouping string
        var state = CalculatorState(displayValue = "1.234")
        state = onCalculatorAction(state, CalculatorAction.MemoryAdd)
        assertEquals(1234.0, state.memoryValue, 0.0)

        // M- with comma decimal
        state = state.copy(memoryValue = 1000.0, displayValue = "10,5")
        state = onCalculatorAction(state, CalculatorAction.MemorySubtract)
        assertEquals(989.5, state.memoryValue, 0.0)

        // MR (recall) should set display to formatted memory value
        state = state.copy(memoryValue = 1234567.0)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        // Recall will format memory: 1.234.567
        assertEquals("1.234.567", state.displayValue)

        // MC clears memory
        state = onCalculatorAction(state, CalculatorAction.MemoryClear)
        assertEquals(0.0, state.memoryValue, 0.0)
    }
}
