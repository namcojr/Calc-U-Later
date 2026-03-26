package com.sunwings.calc_u_later.ui

import com.sunwings.calc_u_later.calculator.CalculatorAction
import com.sunwings.calc_u_later.calculator.CalculatorOperation

internal fun mapTypedCharToAction(typedChar: Char): CalculatorAction? {
    return when (typedChar) {
        in '0'..'9' -> CalculatorAction.Digit(typedChar.digitToInt())
        '.', ',' -> CalculatorAction.Decimal
        '+' -> CalculatorAction.Operation(CalculatorOperation.Add)
        '-' -> CalculatorAction.Operation(CalculatorOperation.Subtract)
        '*', '×' -> CalculatorAction.Operation(CalculatorOperation.Multiply)
        '/', '÷' -> CalculatorAction.Operation(CalculatorOperation.Divide)
        '=' -> CalculatorAction.Equals
        else -> null
    }
}
