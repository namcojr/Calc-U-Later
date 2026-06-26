// Color palette and text styles ported from the Android/Kotlin theme
// (shared/.../ui/theme/Color.kt and Type.kt). Values match exactly.

import 'package:flutter/material.dart';

class CalcColors {
  CalcColors._();

  static const calcBackgroundTop = Color(0xFFE7E4DD);
  static const calcBackgroundBottom = Color(0xFFD2CDC7);

  static const lcdBase = Color(0xFF84B9B4);
  static const lcdHighlight = Color(0xFFA9D0CB);
  static const lcdBorder = Color(0xFF4E6F6C);
  static const lcdTextPrimary = Color(0xFF111111);
  static const lcdTextSecondary = Color(0x66111111);

  static const lcdVintageGreen = Color(0xFF77AA44);
  static const lcdAmber = Color(0xFFFFB84D);

  static const buttonNumericTop = Color(0xFFF6F0E3);
  static const buttonNumericBottom = Color(0xFFE5DCCB);

  static const buttonFunctionTop = Color(0xFF939CA6);
  static const buttonFunctionBottom = Color(0xFF7D8790);

  static const buttonOperatorTop = Color(0xFFE3E7EB);
  static const buttonOperatorBottom = Color(0xFFC9CDD3);

  static const buttonClearTop = Color(0xFFD77B73);
  static const buttonClearBottom = Color(0xFFB35E56);

  static const buttonEqualsTop = Color(0xFFF3A44E);
  static const buttonEqualsBottom = Color(0xFFE28626);

  static const buttonBorder = Color(0xFF4C4943);
  static const buttonShadow = Color(0x33000000);
  static const baseText = Color(0xFF1C1C1C);
}

/// Font family constants registered in pubspec.yaml.
class CalcFonts {
  CalcFonts._();

  /// LCD italic font, used for the main number (Android: led_italic.ttf).
  static const ledItalic = 'LedItalic';

  /// LCD dot-matrix font, used for the secondary line + "M" (led_dot_matrix.ttf).
  static const ledDotMatrix = 'LedDotMatrix';

  /// Monospace font for memory buttons + DEL.
  static const mono = 'CalcMono';

  /// Default sans-serif for numeric/operator buttons. Flutter bundles Roboto
  /// on every platform, so leaving this null uses the same default as Android.
  static const String? sans = null;
}
