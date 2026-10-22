import 'package:flutter/material.dart';

void main() => runApp(const CafeJuninApp());

class CafeJuninApp extends StatelessWidget {
  const CafeJuninApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Café Junín',
    theme: ThemeData(colorSchemeSeed: const Color(0xff285b42)),
    home: Scaffold(
      appBar: AppBar(title: const Text('Café Junín · Móvil')),
      body: ListView(padding: const EdgeInsets.all(24), children: const [
        Text('Base del cliente móvil', style: TextStyle(fontSize: 24)),
        SizedBox(height: 16),
        Text('Pendiente: autenticación OIDC, captura de cosecha, SQLite cifrado y sincronización.'),
        SizedBox(height: 16),
        Text('Esta pantalla no guarda datos ni inicia sesión. Los casos de uso ejecutables están en el backend y la web de demostración.'),
      ]),
    ),
  );
}
