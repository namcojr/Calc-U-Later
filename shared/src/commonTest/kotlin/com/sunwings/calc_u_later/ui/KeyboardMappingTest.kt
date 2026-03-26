package com.sunwings.calc_u_later.ui

import com.sunwings.calc_u_later.calculator.CalculatorAction
import com.sunwings.calc_u_later.calculator.CalculatorOperation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeyboardMappingTest {
    @Test
    fun mapsDigitsAndDecimal() {
        assertEquals(CalculatorAction.Digit(0), mapTypedCharToAction('0'))
        assertEquals(CalculatorAction.Digit(9), mapTypedCharToAction('9'))
        assertEquals(CalculatorAction.Decimal, mapTypedCharToAction('.'))
        assertEquals(CalculatorAction.Decimal, mapTypedCharToAction(','))
    }

    @Test
    fun mapsBasicOperators() {
        assertEquals(CalculatorAction.Operation(CalculatorOperation.Add), mapTypedCharToAction('+'))
        assertEquals(CalculatorAction.Operation(CalculatorOperation.Subtract), mapTypedCharToAction('-'))
        assertEquals(CalculatorAction.Operation(CalculatorOperation.Multiply), mapTypedCharToAction('*'))
        assertEquals(CalculatorAction.Operation(CalculatorOperation.Divide), mapTypedCharToAction('/'))
    }

    @Test
    fun mapsEquals() {
        assertEquals(CalculatorAction.Equals, mapTypedCharToAction('='))
    }

    @Test
    fun ignoresUnsupportedCharacters() {
        assertNull(mapTypedCharToAction('a'))
    }
}
