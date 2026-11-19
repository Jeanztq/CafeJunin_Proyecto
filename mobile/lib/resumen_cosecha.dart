import 'package:material_ui/material_ui.dart';

class ResumenCosecha extends StatelessWidget {
  const ResumenCosecha({super.key, required this.cosecha});
  final Map<String, dynamic> cosecha;

  @override
  Widget build(BuildContext context) {
    final fecha = DateTime.parse(cosecha['fechaCosecha'] as String);
    final datos = <String, String>{
      'Lote': cosecha['id'] as String,
      'Parcela': cosecha['parcelaId'] as String,
      'Fecha': [fecha.day, fecha.month, fecha.year].join('/'),
      'Variedad': cosecha['variedad'] as String,
      'Recolección': cosecha['tipoRecoleccion'] as String,
      'Peso neto': "${(cosecha['pesoKg'] as num).toStringAsFixed(3)} kg",
    };
    return Scaffold(
      appBar: AppBar(title: const Text('Café Junín · Campo')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              'Registrar cosecha',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xffe6eee5),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text(
                'Guardado en este dispositivo\nPendiente de sincronización',
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Tu cosecha quedó guardada',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            Card(
              color: const Color(0xfffffcf7),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: datos.entries
                      .map(
                        (dato) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 8),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(flex: 2, child: Text(dato.key)),
                              Expanded(
                                flex: 3,
                                child: SelectableText(
                                  dato.value,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                      .toList(),
                ),
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              'Puedes cerrar la aplicación y consultar esta cosecha sin conexión.',
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Volver al formulario'),
            ),
          ],
        ),
      ),
    );
  }
}
