import 'dart:io';

import 'package:cafejunin_mobile/data/draft_store.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

void main() {
  test('recupera la cosecha al cerrar y volver a abrir SQLite', () async {
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
    final carpeta = await Directory.systemTemp.createTemp('cafejunin-campo-');
    await databaseFactory.setDatabasesPath(carpeta.path);
    final cosecha = <String, Object>{
      'id': '33333333-3333-3333-3333-333333333333',
      'parcelaId': '22222222-2222-2222-2222-222222222222',
      'variedad': 'BOURBON',
      'tipoRecoleccion': 'SELECTIVA',
      'fechaCosecha': '2026-10-20',
      'pesoKg': 120.5,
    };
    final primero = DraftStore();
    try {
      await primero.save(cosecha);
      await expectLater(
        primero.save(cosecha),
        throwsA(isA<DatabaseException>()),
      );
    } finally {
      await (await primero.database).close();
    }
    final reabierto = DraftStore();
    try {
      expect(await reabierto.all(), [cosecha]);
    } finally {
      await (await reabierto.database).close();
      await carpeta.delete(recursive: true);
    }
  });
}
