import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const VanetApp());
}

class VanetApp extends StatelessWidget {
  const VanetApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'VANET Road Safety',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0A0E21),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF1E88E5),
          secondary: Color(0xFF00B0FF),
          background: Color(0xFF0A0E21),
          surface: Color(0xFF1D1E33),
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF0A0E21),
          foregroundColor: Colors.white,
          elevation: 0,
          centerTitle: true,
        ),
      ),
      home: const HomeScreen(),
    );
  }
}
