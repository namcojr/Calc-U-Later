// Calculator UI ported from the Android/Compose implementation. Layout,
// colors, gradients, shadows, gestures and keyboard handling mirror the
// original so the desktop app looks and behaves like the Android app.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../calculator/calculator_state.dart';
import '../theme/calc_theme.dart';

const _lcdColors = [
  CalcColors.lcdBase,
  CalcColors.lcdVintageGreen,
  CalcColors.lcdAmber,
];

class CalculatorScreen extends StatefulWidget {
  const CalculatorScreen({
    super.key,
    this.initialFormat,
    this.onFormatChange,
    this.initialLcdIndex = 0,
    this.onLcdIndexChange,
  });

  final NumberFormatStyle? initialFormat;
  final ValueChanged<NumberFormatStyle>? onFormatChange;
  final int initialLcdIndex;
  final ValueChanged<int>? onLcdIndexChange;

  @override
  State<CalculatorScreen> createState() => _CalculatorScreenState();
}

class _CalculatorScreenState extends State<CalculatorScreen> {
  late CalculatorState _state;
  late int _lcdIndex;
  final FocusNode _focusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    _state = CalculatorState(
      localeFormat: widget.initialFormat ?? const CalculatorState().localeFormat,
    );
    _lcdIndex = widget.initialLcdIndex % _lcdColors.length;
  }

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

  void _dispatch(CalculatorAction action) {
    final next = onCalculatorAction(_state, action);
    if (next.localeFormat != _state.localeFormat) {
      widget.onFormatChange?.call(next.localeFormat);
    }
    setState(() => _state = next);
  }

  void _cycleLcd(int delta) {
    final next = (_lcdIndex + delta + _lcdColors.length) % _lcdColors.length;
    setState(() => _lcdIndex = next);
    widget.onLcdIndexChange?.call(next);
  }

  KeyEventResult _handleKey(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent && event is! KeyRepeatEvent) {
      return KeyEventResult.ignored;
    }
    final action = _mapKeyToAction(event);
    if (action != null) {
      _dispatch(action);
      return KeyEventResult.handled;
    }
    return KeyEventResult.ignored;
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
      focusNode: _focusNode,
      autofocus: true,
      onKeyEvent: _handleKey,
      child: Stack(
        fit: StackFit.expand,
        children: [
          Image.asset('assets/images/aluminum.jpg', fit: BoxFit.cover),
          DecoratedBox(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Color(0x80E7E4DD), // CalcBackgroundTop @ 0.5
                  Color(0x80D2CDC7), // CalcBackgroundBottom @ 0.5
                ],
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
              child: LayoutBuilder(
                builder: (context, constraints) {
                  // Design reference (matches the Android phone layout): a
                  // narrow calculator. Cap the content width so the grid never
                  // stretches to fill a wide window, and size the buttons to
                  // fit the available height as well as the width.
                  const designWidth = 312.0; // ~360dp phone minus padding
                  const gridSpacing = 14.0;
                  const displayGap = 16.0;

                  final innerW = constraints.maxWidth;
                  final innerH = constraints.maxHeight;
                  final contentW = innerW < designWidth ? innerW : designWidth;

                  // The display panel keeps a fixed proportion of the height.
                  final displayH = (innerH * 0.24).clamp(140.0, 196.0);

                  // Button size from width: 4 columns + 3 gaps.
                  final bFromWidth = (contentW - gridSpacing * 3) / 4;
                  // Button size from height: 6 rows (first row is 0.65x) + gaps.
                  final gridGaps = gridSpacing * 5;
                  final availGridH =
                      innerH - displayH - displayGap - gridGaps;
                  final bFromHeight = availGridH / 5.65;
                  final buttonSize =
                      (bFromWidth < bFromHeight ? bFromWidth : bFromHeight)
                          .clamp(40.0, 120.0);
                  final gridW = buttonSize * 4 + gridSpacing * 3;

                  return Center(
                    child: SizedBox(
                      width: contentW,
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          _DisplayPanel(
                            state: _state,
                            height: displayH,
                            lcdColor: _lcdColors[_lcdIndex],
                            onLongPress: () =>
                                _dispatch(const ToggleLocaleFormatAction()),
                            onCycle: _cycleLcd,
                          ),
                          const SizedBox(height: displayGap),
                          SizedBox(
                            width: gridW,
                            child: _ButtonGrid(
                              format: _state.localeFormat,
                              buttonSize: buttonSize,
                              spacing: gridSpacing,
                              onAction: _dispatch,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// --- Display ------------------------------------------------------------

class _DisplayPanel extends StatelessWidget {
  const _DisplayPanel({
    required this.state,
    required this.height,
    required this.lcdColor,
    required this.onLongPress,
    required this.onCycle,
  });

  final CalculatorState state;
  final double height;
  final Color lcdColor;
  final VoidCallback onLongPress;
  final ValueChanged<int> onCycle;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onLongPress: onLongPress,
      onHorizontalDragEnd: (details) {
        final v = details.primaryVelocity ?? 0;
        if (v.abs() > 200) {
          if (v > 0) {
            onCycle(-1);
          } else {
            onCycle(1);
          }
        }
      },
      child: Container(
        height: height,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [lcdColor.withValues(alpha: 0.9), lcdColor],
          ),
          border: Border.all(color: CalcColors.lcdBorder, width: 1.8),
          boxShadow: const [
            BoxShadow(
              color: CalcColors.buttonShadow,
              blurRadius: 12,
              offset: Offset(0, 4),
            ),
          ],
        ),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 18),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Memory indicator "M"
            AnimatedOpacity(
              opacity: state.memoryValue == 0.0 ? 0.0 : 1.0,
              duration: const Duration(milliseconds: 200),
              child: const Text(
                'M',
                style: TextStyle(
                  fontFamily: CalcFonts.ledDotMatrix,
                  fontSize: 18,
                  color: CalcColors.lcdTextSecondary,
                ),
              ),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Main number
                  FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.centerRight,
                    child: Text(
                      state.displayValue,
                      maxLines: 1,
                      style: const TextStyle(
                        fontFamily: CalcFonts.ledItalic,
                        fontSize: 52,
                        height: 1.1,
                        letterSpacing: 0.5,
                        color: CalcColors.lcdTextPrimary,
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  // Previous expression
                  Text(
                    state.previousExpression,
                    maxLines: 1,
                    overflow: TextOverflow.clip,
                    softWrap: false,
                    style: const TextStyle(
                      fontFamily: CalcFonts.ledDotMatrix,
                      fontSize: 16,
                      letterSpacing: -0.2,
                      color: CalcColors.lcdTextSecondary,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// --- Buttons ------------------------------------------------------------

enum _ButtonRole { memory, function, operator, numeric, clear, equals }

class _ButtonSpec {
  const _ButtonSpec(this.label, this.role, this.action);
  final String label;
  final _ButtonRole role;
  final CalculatorAction action;
}

class _ButtonPalette {
  const _ButtonPalette(this.top, this.bottom, this.content);
  final Color top;
  final Color bottom;
  final Color content;
}

_ButtonPalette _paletteFor(_ButtonRole role) {
  switch (role) {
    case _ButtonRole.memory:
      return const _ButtonPalette(
          CalcColors.buttonFunctionTop, CalcColors.buttonFunctionBottom, Colors.white);
    case _ButtonRole.function:
      return const _ButtonPalette(
          CalcColors.buttonOperatorTop, CalcColors.buttonOperatorBottom, CalcColors.baseText);
    case _ButtonRole.operator:
      return const _ButtonPalette(
          CalcColors.buttonOperatorTop, CalcColors.buttonOperatorBottom, CalcColors.baseText);
    case _ButtonRole.numeric:
      return const _ButtonPalette(
          CalcColors.buttonNumericTop, CalcColors.buttonNumericBottom, CalcColors.baseText);
    case _ButtonRole.clear:
      return const _ButtonPalette(
          CalcColors.buttonClearTop, CalcColors.buttonClearBottom, Colors.white);
    case _ButtonRole.equals:
      return const _ButtonPalette(
          CalcColors.buttonEqualsTop, CalcColors.buttonEqualsBottom, Colors.white);
  }
}

List<List<_ButtonSpec>> _calculatorButtons(NumberFormatStyle format) {
  final decimal =
      format == NumberFormatStyle.dotGroupDecimalComma ? ',' : '.';
  return [
    [
      const _ButtonSpec('MC', _ButtonRole.memory, MemoryClearAction()),
      const _ButtonSpec('M+', _ButtonRole.memory, MemoryAddAction()),
      const _ButtonSpec('M-', _ButtonRole.memory, MemorySubtractAction()),
      const _ButtonSpec('MR', _ButtonRole.memory, MemoryRecallAction()),
    ],
    [
      const _ButtonSpec('AC', _ButtonRole.clear, ClearAction()),
      const _ButtonSpec('%', _ButtonRole.function, PercentAction()),
      const _ButtonSpec('+/-', _ButtonRole.function, ToggleSignAction()),
      const _ButtonSpec('\u00F7', _ButtonRole.operator,
          OperationAction(CalculatorOperation.divide)),
    ],
    [
      const _ButtonSpec('7', _ButtonRole.numeric, DigitAction(7)),
      const _ButtonSpec('8', _ButtonRole.numeric, DigitAction(8)),
      const _ButtonSpec('9', _ButtonRole.numeric, DigitAction(9)),
      const _ButtonSpec('\u00D7', _ButtonRole.operator,
          OperationAction(CalculatorOperation.multiply)),
    ],
    [
      const _ButtonSpec('4', _ButtonRole.numeric, DigitAction(4)),
      const _ButtonSpec('5', _ButtonRole.numeric, DigitAction(5)),
      const _ButtonSpec('6', _ButtonRole.numeric, DigitAction(6)),
      const _ButtonSpec('\u2212', _ButtonRole.operator,
          OperationAction(CalculatorOperation.subtract)),
    ],
    [
      const _ButtonSpec('1', _ButtonRole.numeric, DigitAction(1)),
      const _ButtonSpec('2', _ButtonRole.numeric, DigitAction(2)),
      const _ButtonSpec('3', _ButtonRole.numeric, DigitAction(3)),
      const _ButtonSpec('+', _ButtonRole.operator,
          OperationAction(CalculatorOperation.add)),
    ],
    [
      const _ButtonSpec('DEL', _ButtonRole.function, BackspaceAction()),
      const _ButtonSpec('0', _ButtonRole.numeric, DigitAction(0)),
      _ButtonSpec(decimal, _ButtonRole.numeric, const DecimalAction()),
      const _ButtonSpec('=', _ButtonRole.equals, EqualsAction()),
    ],
  ];
}

class _ButtonGrid extends StatelessWidget {
  const _ButtonGrid({
    required this.format,
    required this.buttonSize,
    required this.spacing,
    required this.onAction,
  });

  final NumberFormatStyle format;
  final double buttonSize;
  final double spacing;
  final ValueChanged<CalculatorAction> onAction;

  @override
  Widget build(BuildContext context) {
    final rows = _calculatorButtons(format);
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        for (var i = 0; i < rows.length; i++) ...[
          if (i > 0) SizedBox(height: spacing),
          SizedBox(
            height: i == 0 ? buttonSize * 0.65 : buttonSize,
            child: Row(
              children: [
                for (var j = 0; j < rows[i].length; j++) ...[
                  if (j > 0) SizedBox(width: spacing),
                  SizedBox(
                    width: buttonSize,
                    child: _CalcButton(
                      spec: rows[i][j],
                      buttonSize: buttonSize,
                      onTap: () => onAction(rows[i][j].action),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ],
    );
  }
}

class _CalcButton extends StatefulWidget {
  const _CalcButton({
    required this.spec,
    required this.buttonSize,
    required this.onTap,
  });

  final _ButtonSpec spec;
  final double buttonSize;
  final VoidCallback onTap;

  @override
  State<_CalcButton> createState() => _CalcButtonState();
}

class _CalcButtonState extends State<_CalcButton> {
  bool _pressed = false;

  @override
  Widget build(BuildContext context) {
    final palette = _paletteFor(widget.spec.role);
    final highlight =
        _pressed ? _adjust(palette.top, 0.9) : _adjust(palette.top, 1.05);
    final shadow =
        _pressed ? _adjust(palette.bottom, 0.85) : _adjust(palette.bottom, 0.95);

    final usesMemoryFont =
        widget.spec.role == _ButtonRole.memory || widget.spec.label == 'DEL';
    final b = widget.buttonSize;
    final textStyle = usesMemoryFont
        ? TextStyle(
            fontFamily: CalcFonts.mono,
            fontWeight: FontWeight.w800,
            fontSize: b * 0.30,
            letterSpacing: 1,
          )
        : TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: b * 0.46,
            letterSpacing: 0.6,
          );

    return GestureDetector(
      onTapDown: (_) => setState(() => _pressed = true),
      onTapUp: (_) => setState(() => _pressed = false),
      onTapCancel: () => setState(() => _pressed = false),
      onTap: widget.onTap,
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18),
          gradient: LinearGradient(
            colors: [highlight, shadow],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          border: Border.all(
            color: CalcColors.buttonBorder.withValues(alpha: 0.75),
            width: 1.3,
          ),
          boxShadow: const [
            BoxShadow(
              color: Color(0x59000000), // ButtonShadow @ 0.35
              blurRadius: 2,
              offset: Offset(-1, 1),
            ),
          ],
        ),
        alignment: Alignment.center,
        child: FittedBox(
          fit: BoxFit.scaleDown,
          child: Text(
            widget.spec.label,
            maxLines: 1,
            style: textStyle.copyWith(color: palette.content),
          ),
        ),
      ),
    );
  }
}

Color _adjust(Color color, double multiplier) {
  return Color.from(
    alpha: color.a,
    red: (color.r * multiplier).clamp(0.0, 1.0),
    green: (color.g * multiplier).clamp(0.0, 1.0),
    blue: (color.b * multiplier).clamp(0.0, 1.0),
  );
}

// --- Keyboard mapping ---------------------------------------------------

CalculatorAction? _mapKeyToAction(KeyEvent event) {
  final logical = event.logicalKey;

  // Operators / decimal via character first (handles shifted symbols + numpad).
  final ch = event.character;
  if (ch != null && ch.isNotEmpty) {
    final mapped = mapTypedCharToAction(ch);
    if (mapped != null) return mapped;
  }

  switch (logical) {
    case LogicalKeyboardKey.digit0:
    case LogicalKeyboardKey.numpad0:
      return const DigitAction(0);
    case LogicalKeyboardKey.digit1:
    case LogicalKeyboardKey.numpad1:
      return const DigitAction(1);
    case LogicalKeyboardKey.digit2:
    case LogicalKeyboardKey.numpad2:
      return const DigitAction(2);
    case LogicalKeyboardKey.digit3:
    case LogicalKeyboardKey.numpad3:
      return const DigitAction(3);
    case LogicalKeyboardKey.digit4:
    case LogicalKeyboardKey.numpad4:
      return const DigitAction(4);
    case LogicalKeyboardKey.digit5:
    case LogicalKeyboardKey.numpad5:
      return const DigitAction(5);
    case LogicalKeyboardKey.digit6:
    case LogicalKeyboardKey.numpad6:
      return const DigitAction(6);
    case LogicalKeyboardKey.digit7:
    case LogicalKeyboardKey.numpad7:
      return const DigitAction(7);
    case LogicalKeyboardKey.digit8:
    case LogicalKeyboardKey.numpad8:
      return const DigitAction(8);
    case LogicalKeyboardKey.digit9:
    case LogicalKeyboardKey.numpad9:
      return const DigitAction(9);
    case LogicalKeyboardKey.period:
    case LogicalKeyboardKey.numpadDecimal:
    case LogicalKeyboardKey.comma:
      return const DecimalAction();
    case LogicalKeyboardKey.add:
    case LogicalKeyboardKey.numpadAdd:
      return const OperationAction(CalculatorOperation.add);
    case LogicalKeyboardKey.minus:
    case LogicalKeyboardKey.numpadSubtract:
      return const OperationAction(CalculatorOperation.subtract);
    case LogicalKeyboardKey.numpadMultiply:
      return const OperationAction(CalculatorOperation.multiply);
    case LogicalKeyboardKey.slash:
    case LogicalKeyboardKey.numpadDivide:
      return const OperationAction(CalculatorOperation.divide);
    case LogicalKeyboardKey.enter:
    case LogicalKeyboardKey.numpadEnter:
    case LogicalKeyboardKey.equal:
      return const EqualsAction();
    case LogicalKeyboardKey.backspace:
      return const BackspaceAction();
    case LogicalKeyboardKey.delete:
    case LogicalKeyboardKey.escape:
      return const ClearAction();
    default:
      return null;
  }
}
