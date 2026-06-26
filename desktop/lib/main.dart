import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:window_manager/window_manager.dart';

import 'calculator/calculator_state.dart';
import 'theme/calc_theme.dart';
import 'ui/calculator_screen.dart';

const _prefFormatKey = 'number_format_style';
const _prefLcdKey = 'lcd_index';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await windowManager.ensureInitialized();

  const windowOptions = WindowOptions(
    size: Size(360, 720),
    minimumSize: Size(360, 720),
    maximumSize: Size(360, 720),
    center: true,
    title: 'Calc-U-Later',
    titleBarStyle: TitleBarStyle.normal,
  );
  windowManager.waitUntilReadyToShow(windowOptions, () async {
    await windowManager.setResizable(false);
    await windowManager.show();
    await windowManager.focus();
  });

  final prefs = await SharedPreferences.getInstance();
  final formatName = prefs.getString(_prefFormatKey);
  final initialFormat = formatName == 'commaGroupDecimalDot'
      ? NumberFormatStyle.commaGroupDecimalDot
      : NumberFormatStyle.dotGroupDecimalComma;
  final initialLcdIndex = prefs.getInt(_prefLcdKey) ?? 0;

  runApp(CalcULaterApp(
    initialFormat: initialFormat,
    initialLcdIndex: initialLcdIndex,
    prefs: prefs,
  ));
}

class CalcULaterApp extends StatelessWidget {
  const CalcULaterApp({
    super.key,
    required this.initialFormat,
    required this.initialLcdIndex,
    required this.prefs,
  });

  final NumberFormatStyle initialFormat;
  final int initialLcdIndex;
  final SharedPreferences prefs;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Calc-U-Later',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: CalcColors.buttonEqualsBottom,
          brightness: Brightness.light,
        ),
      ),
      home: Scaffold(
        backgroundColor: CalcColors.calcBackgroundTop,
        body: CalculatorScreen(
          initialFormat: initialFormat,
          initialLcdIndex: initialLcdIndex,
          onFormatChange: (format) {
            prefs.setString(_prefFormatKey, format.name);
          },
          onLcdIndexChange: (index) {
            prefs.setInt(_prefLcdKey, index);
          },
        ),
      ),
    );
  }
}
