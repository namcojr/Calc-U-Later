package com.sunwings.calc_u_later.calculator

import org.junit.Assert.*
import org.junit.Test

class DisplayFitTest {
    // Maximum visible characters on main display (including '.' and ',')
    private val VISIBLE_LIMIT = 15

    @Test
    fun largestIntegerBeforeScientific_fitsDisplay() {
        // formatNumber switches to scientific for abs >= 1e10, so we test just below that
        val value = 9999999999.0 // 9,999,999,999
        var state = CalculatorState(memoryValue = value)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        val displayed = state.displayValue
        // Display should be grouped: "9.999.999.999"
        assertTrue("Displayed '$displayed' should be <= $VISIBLE_LIMIT chars", displayed.length <= VISIBLE_LIMIT)
    }

    @Test
    fun integerWithOneDecimalBeforeScientific_fitsDisplay() {
        // Test a value with one decimal place just under 1e10
        val value = 9999999999.2
        var state = CalculatorState(memoryValue = value)
        state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
        val displayed = state.displayValue
        // Example visual: "9.999.999.999,2" -> length should be <= VISIBLE_LIMIT
        assertTrue("Displayed '$displayed' should be <= $VISIBLE_LIMIT chars", displayed.length <= VISIBLE_LIMIT)
    }

    @Test
    fun severalEdgeCases_fitDisplay() {
        val cases = listOf(
            1_000.0, // 1.000
            1000000.0, // 1.000.000
            123456789.0, // 123.456.789
            9999999999.0,
            9999999.1234
        )
        cases.forEach { v ->
            var state = CalculatorState(memoryValue = v)
            state = onCalculatorAction(state, CalculatorAction.MemoryRecall)
            val d = state.displayValue
            assertTrue("Value $v displayed as '$d' exceeds $VISIBLE_LIMIT chars", d.length <= VISIBLE_LIMIT)
        }
    }
}
