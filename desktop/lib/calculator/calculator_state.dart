// Calculator engine ported 1:1 from the Android/Kotlin implementation
// (shared/src/commonMain/.../calculator/CalculatorState.kt).
//
// Behaviour, formatting rules, locale handling and the per-number digit limit
// are intentionally identical to the original so the desktop app matches the
// Android app exactly.

import 'dart:math' as math;

// Display can show longer computed results; keep input digit limit separate.
const int _maxDisplayLength = 24;

// When showing the previous expression, if the left operand (formatted with
// grouping and decimal separators) would exceed this visible length, replace it
// with the literal "ans" to avoid UI clipping of the operator and right operand.
const int _leftPreviousVisibleLimit = 14;

const int _maxDigitsPerNumber = 10;

/// Supported display formats for numbers.
/// - dotGroupDecimalComma: grouping '.' and decimal ',' (Portuguese, default)
/// - commaGroupDecimalDot: grouping ',' and decimal '.' (US/EN)
enum NumberFormatStyle {
  dotGroupDecimalComma,
  commaGroupDecimalDot,
}

enum CalculatorOperation {
  add('+'),
  subtract('\u2212'), // − U+2212
  multiply('\u00D7'), // × U+00D7
  divide('\u00F7'); // ÷ U+00F7

  const CalculatorOperation(this.symbol);
  final String symbol;
}

class CalculatorState {
  const CalculatorState({
    this.displayValue = '0',
    this.previousExpression = '',
    this.accumulator,
    this.pendingOperation,
    this.overwriteDisplay = true,
    this.memoryValue = 0.0,
    this.localeFormat = NumberFormatStyle.dotGroupDecimalComma,
    this.isError = false,
  });

  final String displayValue;
  final String previousExpression;
  final double? accumulator;
  final CalculatorOperation? pendingOperation;
  final bool overwriteDisplay;
  final double memoryValue;
  final NumberFormatStyle localeFormat;
  final bool isError;

  CalculatorState copyWith({
    String? displayValue,
    String? previousExpression,
    double? accumulator,
    bool clearAccumulator = false,
    CalculatorOperation? pendingOperation,
    bool clearPendingOperation = false,
    bool? overwriteDisplay,
    double? memoryValue,
    NumberFormatStyle? localeFormat,
    bool? isError,
  }) {
    return CalculatorState(
      displayValue: displayValue ?? this.displayValue,
      previousExpression: previousExpression ?? this.previousExpression,
      accumulator: clearAccumulator ? null : (accumulator ?? this.accumulator),
      pendingOperation: clearPendingOperation
          ? null
          : (pendingOperation ?? this.pendingOperation),
      overwriteDisplay: overwriteDisplay ?? this.overwriteDisplay,
      memoryValue: memoryValue ?? this.memoryValue,
      localeFormat: localeFormat ?? this.localeFormat,
      isError: isError ?? this.isError,
    );
  }
}

// --- Actions ------------------------------------------------------------

sealed class CalculatorAction {
  const CalculatorAction();
}

class DigitAction extends CalculatorAction {
  const DigitAction(this.value);
  final int value;
}

class DecimalAction extends CalculatorAction {
  const DecimalAction();
}

class OperationAction extends CalculatorAction {
  const OperationAction(this.type);
  final CalculatorOperation type;
}

class EqualsAction extends CalculatorAction {
  const EqualsAction();
}

class ClearAction extends CalculatorAction {
  const ClearAction();
}

class PercentAction extends CalculatorAction {
  const PercentAction();
}

class ToggleSignAction extends CalculatorAction {
  const ToggleSignAction();
}

class BackspaceAction extends CalculatorAction {
  const BackspaceAction();
}

class MemoryClearAction extends CalculatorAction {
  const MemoryClearAction();
}

class MemoryRecallAction extends CalculatorAction {
  const MemoryRecallAction();
}

class MemoryAddAction extends CalculatorAction {
  const MemoryAddAction();
}

class MemorySubtractAction extends CalculatorAction {
  const MemorySubtractAction();
}

class ToggleLocaleFormatAction extends CalculatorAction {
  const ToggleLocaleFormatAction();
}

// --- Reducer ------------------------------------------------------------

CalculatorState onCalculatorAction(
  CalculatorState state,
  CalculatorAction action,
) {
  if (state.isError && action is! ClearAction) {
    return state;
  }

  switch (action) {
    case DigitAction(:final value):
      return _appendDigit(state, value);
    case DecimalAction():
      return _appendDecimal(state);
    case OperationAction(:final type):
      return _applyOperation(state, type);
    case EqualsAction():
      return _evaluate(state);
    case ClearAction():
      // Resets display and pending operation but preserves user preferences
      // such as localeFormat and memoryValue.
      return _reset(state);
    case PercentAction():
      return _applyPercent(state);
    case ToggleSignAction():
      return _toggleSign(state);
    case BackspaceAction():
      return _backspace(state);
    case MemoryClearAction():
      return state.copyWith(memoryValue: 0.0);
    case MemoryRecallAction():
      return _withDisplay(state, state.memoryValue);
    case MemoryAddAction():
      return state.copyWith(
        memoryValue:
            state.memoryValue + _toSafeDouble(state.displayValue, state.localeFormat),
      );
    case MemorySubtractAction():
      return state.copyWith(
        memoryValue:
            state.memoryValue - _toSafeDouble(state.displayValue, state.localeFormat),
      );
    case ToggleLocaleFormatAction():
      // Switching style clears current result/pending to avoid format mismatch.
      return state.copyWith(
        localeFormat: state.localeFormat == NumberFormatStyle.dotGroupDecimalComma
            ? NumberFormatStyle.commaGroupDecimalDot
            : NumberFormatStyle.dotGroupDecimalComma,
        displayValue: '0',
        previousExpression: '',
        clearAccumulator: true,
        clearPendingOperation: true,
        overwriteDisplay: true,
        isError: false,
      );
  }
}

CalculatorState _appendDigit(CalculatorState state, int digit) {
  final cleanDigit = digit.clamp(0, 9).toString();
  if (!canAppendDigit(state.displayValue, state.overwriteDisplay)) return state;

  final String nextValue;
  if (state.overwriteDisplay || state.displayValue == '0') {
    nextValue = cleanDigit;
  } else if (state.displayValue.length >= _maxDisplayLength) {
    nextValue = state.displayValue;
  } else {
    nextValue = state.displayValue + cleanDigit;
  }
  return state.copyWith(
    displayValue: nextValue,
    overwriteDisplay: false,
    isError: false,
  );
}

CalculatorState _appendDecimal(CalculatorState state) {
  final base = state.overwriteDisplay ? '0' : state.displayValue;
  final decimalSep =
      state.localeFormat == NumberFormatStyle.dotGroupDecimalComma ? ',' : '.';
  if (base.contains(decimalSep)) return state;

  final next = '$base$decimalSep';
  return state.copyWith(
    displayValue: _take(next, _maxDisplayLength),
    overwriteDisplay: false,
  );
}

/// Returns true if a digit may be appended to the current input considering the
/// per-number digit limit (ignores sign and decimal separator).
bool canAppendDigit(String displayValue, bool overwriteDisplay) {
  if (overwriteDisplay) return true;
  const ops = ['+', '\u2212', '\u00D7', '\u00F7'];
  var lastOpIndex = -1;
  for (var i = 0; i < displayValue.length; i++) {
    if (ops.contains(displayValue[i])) lastOpIndex = i;
  }
  final segment =
      lastOpIndex == -1 ? displayValue : displayValue.substring(lastOpIndex + 1);
  final digitCount =
      segment.split('').where((c) => c.codeUnitAt(0) >= 48 && c.codeUnitAt(0) <= 57).length;
  return digitCount < _maxDigitsPerNumber;
}

CalculatorState _applyOperation(
  CalculatorState state,
  CalculatorOperation operation,
) {
  final currentValue = _toSafeDouble(state.displayValue, state.localeFormat);

  if (state.accumulator != null &&
      state.pendingOperation != null &&
      !state.overwriteDisplay) {
    final result = _performBinaryOperation(
      state.accumulator!,
      currentValue,
      state.pendingOperation!,
    );
    if (result == null) return _errorState(state);
    final formattedResult = _formatNumber(result, state.localeFormat);
    final leftForPrev =
        _formatNumber(result, state.localeFormat).length > _leftPreviousVisibleLimit
            ? 'ans'
            : _formatNumber(result, state.localeFormat);
    return state.copyWith(
      displayValue: formattedResult,
      accumulator: result,
      pendingOperation: operation,
      overwriteDisplay: true,
      previousExpression: '$leftForPrev ${operation.symbol}',
      isError: false,
    );
  }

  final baseValue = state.accumulator ?? currentValue;
  final leftForPrev =
      _formatNumber(baseValue, state.localeFormat).length > _leftPreviousVisibleLimit
          ? 'ans'
          : _formatNumber(baseValue, state.localeFormat);
  return state.copyWith(
    accumulator: baseValue,
    pendingOperation: operation,
    overwriteDisplay: true,
    previousExpression: '$leftForPrev ${operation.symbol}',
    isError: false,
  );
}

CalculatorState _evaluate(CalculatorState state) {
  final op = state.pendingOperation;
  if (op == null) return state;
  final accumulator = state.accumulator;
  if (accumulator == null) return state;
  final currentValue = _toSafeDouble(state.displayValue, state.localeFormat);
  final result = _performBinaryOperation(accumulator, currentValue, op);
  if (result == null) return _errorState(state);
  final leftForPrev =
      _formatNumber(accumulator, state.localeFormat).length > _leftPreviousVisibleLimit
          ? 'ans'
          : _formatNumber(accumulator, state.localeFormat);
  final rightForPrev = _formatNumber(currentValue, state.localeFormat);
  return state.copyWith(
    displayValue: _formatNumber(result, state.localeFormat),
    previousExpression: '$leftForPrev ${op.symbol} $rightForPrev =',
    clearAccumulator: true,
    clearPendingOperation: true,
    overwriteDisplay: true,
    isError: false,
  );
}

CalculatorState _applyPercent(CalculatorState state) {
  final currentValue = _toSafeDouble(state.displayValue, state.localeFormat);
  final double basis;
  if (state.accumulator != null && state.pendingOperation != null) {
    basis = state.accumulator! * (currentValue / 100.0);
  } else {
    basis = currentValue / 100.0;
  }
  return state.copyWith(
    displayValue: _formatNumber(basis, state.localeFormat),
    overwriteDisplay: true,
    isError: false,
  );
}

CalculatorState _toggleSign(CalculatorState state) {
  final currentValue = _toSafeDouble(state.displayValue, state.localeFormat);
  if (currentValue == 0.0) return state;
  final toggled = -currentValue;
  return state.copyWith(
    displayValue: _formatNumber(toggled, state.localeFormat),
    overwriteDisplay: false,
    isError: false,
  );
}

CalculatorState _backspace(CalculatorState state) {
  if (state.overwriteDisplay) {
    return state.copyWith(displayValue: '0', overwriteDisplay: true);
  }
  var trimmed = state.displayValue.isEmpty
      ? '0'
      : state.displayValue.substring(0, state.displayValue.length - 1);
  if (trimmed.isEmpty) trimmed = '0';
  final sanitized = (trimmed == '-' || trimmed == '-0') ? '0' : trimmed;
  return state.copyWith(displayValue: sanitized, overwriteDisplay: false);
}

CalculatorState _reset(CalculatorState state) => state.copyWith(
      displayValue: '0',
      previousExpression: '',
      clearAccumulator: true,
      clearPendingOperation: true,
      overwriteDisplay: true,
      isError: false,
    );

CalculatorState _withDisplay(CalculatorState state, double value) => state.copyWith(
      displayValue: _formatNumber(value, state.localeFormat),
      overwriteDisplay: true,
      isError: false,
    );

CalculatorState _errorState(CalculatorState state) => state.copyWith(
      displayValue: 'Error',
      previousExpression: '',
      clearAccumulator: true,
      clearPendingOperation: true,
      overwriteDisplay: true,
      isError: true,
    );

double? _performBinaryOperation(
  double lhs,
  double rhs,
  CalculatorOperation operation,
) {
  switch (operation) {
    case CalculatorOperation.add:
      return lhs + rhs;
    case CalculatorOperation.subtract:
      return lhs - rhs;
    case CalculatorOperation.multiply:
      return lhs * rhs;
    case CalculatorOperation.divide:
      return rhs.abs() < 1e-12 ? null : lhs / rhs;
  }
}

// --- Formatting ---------------------------------------------------------

String _formatNumber(double value, NumberFormatStyle style) {
  if (value.isNaN || value.isInfinite) return 'Error';
  final absValue = value.abs();
  if (absValue >= 1e100) return 'Overflow';
  final plain = _plainScaled8(value);
  try {
    if (absValue >= 1e10 || (value != 0.0 && absValue < 1e-4)) {
      final exp = _javaExponential(value);
      return exp.length <= _maxDisplayLength ? exp : _take(exp, _maxDisplayLength);
    } else {
      final formatted = _formatForDisplay(plain, style);
      return formatted.length <= _maxDisplayLength
          ? formatted
          : _take(formatted, _maxDisplayLength);
    }
  } catch (_) {
    return plain.length <= _maxDisplayLength ? plain : _take(plain, _maxDisplayLength);
  }
}

/// Pure display-only formatter. `value` is a locale-neutral numeric string.
String _formatForDisplay(String value, NumberFormatStyle style) {
  try {
    final v = value.trim();
    if (v.toLowerCase() == 'error' || v.toLowerCase() == 'overflow') return v;
    if (v == '-0') return '0';

    if (v.contains('e') || v.contains('E')) {
      return v.replaceAll(' ', '').replaceAll('E', 'e');
    }

    final bd = double.tryParse(v);
    if (bd == null) return v;

    final absBd = bd.abs();
    if (absBd >= 1e100) return 'Overflow';

    if (absBd >= 1e10 || (bd != 0.0 && absBd < 1e-4)) {
      return _javaExponential(bd);
    }

    var plain = _plainScaled8(bd);
    if (plain == '-0') plain = '0';

    final parts = plain.split('.');
    final intRaw = parts[0];
    final sign = intRaw.startsWith('-') ? '-' : '';
    final digits = sign.isEmpty ? intRaw : intRaw.substring(1);

    final grouped = switch (style) {
      NumberFormatStyle.dotGroupDecimalComma =>
        digits.replaceAllMapped(RegExp(r'(\d)(?=(\d{3})+(?!\d))'), (m) => '${m[1]}.'),
      NumberFormatStyle.commaGroupDecimalDot =>
        digits.replaceAllMapped(RegExp(r'(\d)(?=(\d{3})+(?!\d))'), (m) => '${m[1]},'),
    };
    final intPart = sign + grouped;

    final decPart = (parts.length > 1 && parts[1].isNotEmpty)
        ? switch (style) {
            NumberFormatStyle.dotGroupDecimalComma => ',${parts[1]}',
            NumberFormatStyle.commaGroupDecimalDot => '.${parts[1]}',
          }
        : '';

    return intPart + decPart;
  } catch (_) {
    return value;
  }
}

double _toSafeDouble(String input, NumberFormatStyle style) {
  final raw = input.trim();
  if (raw.toLowerCase() == 'error' || raw.toLowerCase() == 'overflow') return 0.0;

  if (raw.contains('e') || raw.contains('E')) {
    final s = raw.replaceAll(' ', '').replaceAll('E', 'e');
    final direct = double.tryParse(s);
    if (direct != null) return direct;
    final sciMatch = RegExp(r'^([\-\d.]+)e([+\-]?\d+)$').firstMatch(s);
    if (sciMatch != null) {
      final num = double.tryParse(sciMatch.group(1)!);
      final exp = double.tryParse(sciMatch.group(2)!);
      if (num != null && exp != null) {
        return num * math.pow(10.0, exp).toDouble();
      }
    }
    return 0.0;
  }

  final String normalized;
  switch (style) {
    case NumberFormatStyle.dotGroupDecimalComma:
      if (raw.contains(',')) {
        normalized = raw.replaceAll('.', '').replaceAll(',', '.');
      } else if (RegExp(r'^[-+]?\d{1,3}(?:\.\d{3})+$').hasMatch(raw)) {
        normalized = raw.replaceAll('.', '');
      } else {
        normalized = raw;
      }
      break;
    case NumberFormatStyle.commaGroupDecimalDot:
      if (raw.contains('.') && raw.contains(',')) {
        normalized = raw.replaceAll(',', '');
      } else if (raw.contains(',')) {
        normalized = raw.replaceAll(',', '');
      } else if (RegExp(r'^[-+]?\d{1,3}(?:,\d{3})+$').hasMatch(raw)) {
        normalized = raw.replaceAll(',', '');
      } else {
        normalized = raw;
      }
      break;
  }

  return double.tryParse(normalized) ?? 0.0;
}

// --- Numeric helpers ----------------------------------------------------

/// Mimics BigDecimal.valueOf(value).setScale(8, HALF_UP).stripTrailingZeros()
/// followed by toPlainString(): a fixed-notation decimal rounded to 8 places
/// (half-up) with trailing zeros removed and no scientific notation.
///
/// Like Java's BigDecimal.valueOf, this works from the *shortest* round-trip
/// decimal (Dart's double.toString) rather than the raw binary expansion, so
/// values like 9999999999.2 stay clean instead of showing precision noise.
String _plainScaled8(double value) {
  if (value == 0.0) return '0';
  var s = value.toString();
  if (s.contains('e') || s.contains('E')) {
    // Defensive: this branch is only reached for out-of-display-range values
    // (used solely in the catch fallback); expand to a fixed representation.
    s = value.toStringAsFixed(8);
  }
  s = _roundDecimalStringHalfUp(s, 8);
  if (s.contains('.')) {
    s = s.replaceAll(RegExp(r'0+$'), '');
    s = s.replaceAll(RegExp(r'\.$'), '');
  }
  if (s == '-0') s = '0';
  return s;
}

/// Rounds a plain decimal string to [places] fractional digits, half-up,
/// using exact string arithmetic (no floating-point round-trip).
String _roundDecimalStringHalfUp(String input, int places) {
  var s = input;
  final negative = s.startsWith('-');
  if (negative || s.startsWith('+')) s = s.substring(1);

  final dot = s.indexOf('.');
  final intPart = dot < 0 ? s : s.substring(0, dot);
  final fracPart = dot < 0 ? '' : s.substring(dot + 1);

  if (fracPart.length <= places) {
    final res = fracPart.isEmpty ? intPart : '$intPart.$fracPart';
    return negative ? '-$res' : res;
  }

  final keep = fracPart.substring(0, places);
  final roundDigit = fracPart.codeUnitAt(places) - 48;
  var digits = intPart + keep;
  if (roundDigit >= 5) {
    digits = _incrementDigits(digits);
  }
  digits = digits.padLeft(places + 1, '0');
  final cut = digits.length - places;
  var newInt = digits.substring(0, cut).replaceFirst(RegExp(r'^0+(?=\d)'), '');
  final newFrac = digits.substring(cut);
  final res = '$newInt.$newFrac';
  return negative ? '-$res' : res;
}

/// Adds 1 to a non-negative integer represented as a digit string.
String _incrementDigits(String digits) {
  final chars = digits.split('');
  var i = chars.length - 1;
  while (i >= 0) {
    final d = chars[i].codeUnitAt(0) - 48;
    if (d < 9) {
      chars[i] = String.fromCharCode(d + 1 + 48);
      return chars.join();
    }
    chars[i] = '0';
    i--;
  }
  return '1${chars.join()}';
}

/// Mimics Java's String.format("%.6e", value): mantissa with 6 fractional
/// digits and a signed, at-least-two-digit exponent (e.g. 2.000000e+12).
String _javaExponential(double value) {
  var s = value.toStringAsExponential(6); // e.g. "1.2e-7" -> "1.200000e-7"
  final match = RegExp(r'^(.*)e([+-]?)(\d+)$').firstMatch(s);
  if (match == null) return s.replaceAll(' ', '');
  final mantissa = match.group(1)!;
  final sign = match.group(2)!.isEmpty ? '+' : match.group(2)!;
  var exp = match.group(3)!;
  if (exp.length < 2) exp = exp.padLeft(2, '0');
  return '${mantissa}e$sign$exp';
}

String _take(String s, int n) => s.length <= n ? s : s.substring(0, n);

/// Maps a typed character to a calculator action (used by keyboard handling).
CalculatorAction? mapTypedCharToAction(String typedChar) {
  if (typedChar.length != 1) return null;
  final code = typedChar.codeUnitAt(0);
  if (code >= 48 && code <= 57) {
    return DigitAction(code - 48);
  }
  switch (typedChar) {
    case '.':
    case ',':
      return const DecimalAction();
    case '+':
      return const OperationAction(CalculatorOperation.add);
    case '-':
      return const OperationAction(CalculatorOperation.subtract);
    case '*':
    case '\u00D7':
      return const OperationAction(CalculatorOperation.multiply);
    case '/':
    case '\u00F7':
      return const OperationAction(CalculatorOperation.divide);
    case '=':
      return const EqualsAction();
    default:
      return null;
  }
}
