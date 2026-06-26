// Ported from the Android unit tests
// (src/test/.../calculator/*.kt) to verify identical calculator behaviour.

import 'package:flutter_test/flutter_test.dart';
import 'package:calc_u_later/calculator/calculator_state.dart';

CalculatorState _digits(CalculatorState s, List<int> ds) {
  for (final d in ds) {
    s = onCalculatorAction(s, DigitAction(d));
  }
  return s;
}

void main() {
  group('Operations', () {
    test('additionWithGrouping_preservesInternalValue', () {
      var state = const CalculatorState();
      state = _digits(state, [8, 8, 0, 0]);
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      state = _digits(state, [2, 1, 0, 0]);
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '10.900');

      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      state = _digits(state, [1, 0, 0]);
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '11.000');
    });

    test('decimalAddition_respectsCommaDecimal', () {
      var state = const CalculatorState();
      state = _digits(state, [1, 0]);
      state = onCalculatorAction(state, const DecimalAction());
      state = onCalculatorAction(state, const DigitAction(5));
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      state = onCalculatorAction(state, const DigitAction(0));
      state = onCalculatorAction(state, const DecimalAction());
      state = _digits(state, [4, 0, 3]);
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '10,903');
    });

    test('multiplyAndDivide_withDecimals', () {
      var state = const CalculatorState();
      state = onCalculatorAction(state, const DigitAction(1));
      state = onCalculatorAction(state, const DecimalAction());
      state = onCalculatorAction(state, const DigitAction(5));
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.multiply));
      state = onCalculatorAction(state, const DigitAction(2));
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '3');

      state = const CalculatorState();
      state = _digits(state, [1, 0]);
      state = onCalculatorAction(state, const DecimalAction());
      state = onCalculatorAction(state, const DigitAction(5));
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.divide));
      state = onCalculatorAction(state, const DigitAction(2));
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '5,25');
    });

    test('memoryOperations_handleGroupingAndDecimal', () {
      var state = const CalculatorState(displayValue: '1.234');
      state = onCalculatorAction(state, const MemoryAddAction());
      expect(state.memoryValue, 1234.0);

      state = state.copyWith(memoryValue: 1000.0, displayValue: '10,5');
      state = onCalculatorAction(state, const MemorySubtractAction());
      expect(state.memoryValue, 989.5);

      state = state.copyWith(memoryValue: 1234567.0);
      state = onCalculatorAction(state, const MemoryRecallAction());
      expect(state.displayValue, '1.234.567');

      state = onCalculatorAction(state, const MemoryClearAction());
      expect(state.memoryValue, 0.0);
    });
  });

  group('Locale', () {
    test('toggleLocale_clearsDisplayAndPending', () {
      var state = const CalculatorState(
        displayValue: '1234',
        accumulator: 12.0,
        pendingOperation: CalculatorOperation.add,
        previousExpression: '12 +',
      );
      state = onCalculatorAction(state, const ToggleLocaleFormatAction());
      expect(state.displayValue, '0');
      expect(state.accumulator, isNull);
      expect(state.pendingOperation, isNull);
      expect(state.previousExpression, '');
    });

    test('clear_preservesLocaleFormat', () {
      var state = const CalculatorState(
        localeFormat: NumberFormatStyle.commaGroupDecimalDot,
        displayValue: '1234',
      );
      state = onCalculatorAction(state, const ClearAction());
      expect(state.localeFormat, NumberFormatStyle.commaGroupDecimalDot);
      expect(state.displayValue, '0');
    });

    test('grouping_displayInPreviousExpression_respectsLocale', () {
      var state = const CalculatorState(
        localeFormat: NumberFormatStyle.dotGroupDecimalComma,
        displayValue: '23666',
      );
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.multiply));
      state = onCalculatorAction(state, const DigitAction(2));
      state = onCalculatorAction(state, const DigitAction(0));
      state = onCalculatorAction(state, const DecimalAction());
      state = onCalculatorAction(state, const DigitAction(3));
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '480.419,8');
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      expect(
        state.previousExpression.contains('480.419,8') ||
            state.previousExpression.contains('ans'),
        isTrue,
      );
    });
  });

  group('Edge cases', () {
    double displayToDouble(String display) {
      final raw = display.trim();
      if (raw.toLowerCase() == 'error' || raw.toLowerCase() == 'overflow') {
        return 0.0;
      }
      if (raw.contains('e') || raw.contains('E')) {
        return double.tryParse(
                raw.replaceAll(' ', '').replaceAll('E', 'e')) ??
            0.0;
      }
      if (raw.contains(',')) {
        return double.tryParse(raw.replaceAll('.', '').replaceAll(',', '.')) ??
            0.0;
      }
      if (RegExp(r'^[-+]?\d{1,3}(?:\.\d{3})+$').hasMatch(raw)) {
        return double.tryParse(raw.replaceAll('.', '')) ?? 0.0;
      }
      return double.tryParse(raw) ?? 0.0;
    }

    test('negativeNumbers_additionAndToggleSign', () {
      var state = const CalculatorState();
      state = onCalculatorAction(state, const DigitAction(5));
      state = onCalculatorAction(state, const ToggleSignAction());
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      state = onCalculatorAction(state, const DigitAction(3));
      state = onCalculatorAction(state, const EqualsAction());
      expect(state.displayValue, '-2');
    });

    test('scientificInput_parsedAndUsedInOperation', () {
      var state = const CalculatorState();
      state = state.copyWith(memoryValue: 1e12);
      state = onCalculatorAction(state, const MemoryRecallAction());
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.multiply));
      state = onCalculatorAction(state, const DigitAction(2));
      state = onCalculatorAction(state, const EqualsAction());
      expect(displayToDouble(state.displayValue), closeTo(2e12, 1e6));
    });

    test('verySmallNumber_handling', () {
      var state = const CalculatorState();
      state = state.copyWith(memoryValue: 0.00000012);
      state = onCalculatorAction(state, const MemoryRecallAction());
      state = onCalculatorAction(
          state, const OperationAction(CalculatorOperation.add));
      state = onCalculatorAction(state, const DigitAction(0));
      state = onCalculatorAction(state, const EqualsAction());
      expect(displayToDouble(state.displayValue), closeTo(0.00000012, 1e-12));
    });
  });

  group('Digit limit', () {
    test('allowed_examples', () {
      expect(canAppendDigit('-12345,12345', false), isFalse);
      expect(canAppendDigit('1,123456789', false), isFalse);
      expect(canAppendDigit('-300', false), isTrue);
      expect(canAppendDigit('+8987656374', false), isFalse);
    });

    test('disallowed_examples', () {
      expect(canAppendDigit('12345678901', false), isFalse);
      expect(canAppendDigit('12345,123456798', false), isFalse);
      expect(canAppendDigit('-8756475902,22', false), isFalse);
    });

    test('respects_overwriteDisplay', () {
      expect(canAppendDigit('12345678901', true), isTrue);
    });

    test('multiple_operations_segmenting', () {
      expect(canAppendDigit('1234567890+', false), isTrue);
      expect(canAppendDigit('123+', false), isTrue);
      expect(canAppendDigit('123+1234567890', false), isFalse);
    });
  });

  group('Display fit', () {
    const visibleLimit = 15;
    test('severalEdgeCases_fitDisplay', () {
      for (final v in [
        1000.0,
        1000000.0,
        123456789.0,
        9999999999.0,
        9999999.1234,
        9999999999.2,
      ]) {
        var state = CalculatorState(memoryValue: v);
        state = onCalculatorAction(state, const MemoryRecallAction());
        expect(state.displayValue.length <= visibleLimit, isTrue,
            reason: 'Value $v displayed as "${state.displayValue}"');
      }
    });
  });
}
