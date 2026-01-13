package com.sunwings.calc_u_later.calculator

import org.junit.Assert.*
import org.junit.Test

class CalculatorDigitLimitTest {

    @Test
    fun allowed_examples() {
        // -12345,12345 -> digits = 10 -> cannot append another digit
        assertFalse(canAppendDigit("-12345,12345", overwriteDisplay = false))
        // 1,123456789 -> digits = 10 (1 + 9) -> should NOT allow another digit
        assertFalse(canAppendDigit("1,123456789", overwriteDisplay = false))
        // -300 -> digits = 3
        assertTrue(canAppendDigit("-300", overwriteDisplay = false))
        // +8987656374 -> digits = 10 -> cannot append more
        assertFalse(canAppendDigit("+8987656374", overwriteDisplay = false))
    }

    @Test
    fun disallowed_examples() {
        assertFalse(canAppendDigit("12345678901", overwriteDisplay = false))
        assertFalse(canAppendDigit("12345,123456798", overwriteDisplay = false))
        assertFalse(canAppendDigit("-8756475902,22", overwriteDisplay = false))
    }

    @Test
    fun respects_overwriteDisplay() {
        // When overwriteDisplay is true, allow appending (starting new number)
        assertTrue(canAppendDigit("12345678901", overwriteDisplay = true))
    }

    @Test
    fun multiple_operations_segmenting() {
        // After an operator, digit counting resets
        assertTrue(canAppendDigit("1234567890+", overwriteDisplay = false))
        assertTrue(canAppendDigit("123+", overwriteDisplay = false))
        // If there's an operator and the current segment has 10 digits, appending should be disallowed
        assertFalse(canAppendDigit("123+1234567890", overwriteDisplay = false))
    }
}
