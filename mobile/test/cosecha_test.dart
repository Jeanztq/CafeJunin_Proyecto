import 'package:cafejunin_mobile/data/draft_store.dart';
import 'package:cafejunin_mobile/main.dart';
import 'package:material_ui/material_ui.dart';
import 'package:flutter_test/flutter_test.dart';

class AlmacenDePrueba extends DraftStore {
  bool disponible = false;
  final filas = <Map<String, dynamic>>[];
  @override
  Future<void> save(Map<String, Object> data) async {
    if (!disponible) throw StateError('Almacenamiento no disponible');
    filas.add(data);
  }

  @override
  Future<List<Map<String, dynamic>>> all() async => filas;
}

void main() {
  testWidgets(
    'una cosecha se confirma al guardarse y se vuelve a abrir desde la lista',
    (tester) async {
      final db = AlmacenDePrueba();
      await tester.pumpWidget(MaterialApp(home: CapturePage(store: db)));
      await tester.pumpAndSettle();
      await tester.enterText(
        find.byType(TextFormField).first,
        '22222222-2222-2222-2222-222222222222',
      );
      await tester.enterText(find.byType(TextFormField).last, '120,500');
      final guardar = find.text('Guardar borrador offline');
      await tester.ensureVisible(guardar);
      await tester.tap(guardar);
      await tester.pumpAndSettle();
      expect(find.text('Tu cosecha quedó guardada'), findsNothing);
      expect(db.filas, isEmpty);
      db.disponible = true;
      await tester.ensureVisible(guardar);
      await tester.tap(guardar);
      await tester.pumpAndSettle();
      expect(find.text('120.500 kg'), findsOneWidget);
      expect(find.text(db.filas.single['id'] as String), findsOneWidget);
      final volver = find.text('Volver al formulario');
      await tester.scrollUntilVisible(
        volver,
        160,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(volver);
      await tester.pumpAndSettle();
      final fila = find.textContaining('BOURBON · 120.5 kg');
      await tester.scrollUntilVisible(
        fila,
        180,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(fila);
      await tester.pumpAndSettle();
      expect(find.text('Tu cosecha quedó guardada'), findsOneWidget);
      expect(db.filas, hasLength(1));
    },
  );
}
