import 'dart:convert';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart' as path;

/// Adaptador SQLite local. No contiene credenciales ni envía datos por red.
class DraftStore {
  Database? _database;
  Future<Database> get database async => _database ??= await openDatabase(
    path.join(await getDatabasesPath(), 'cafejunin_drafts.db'),
    version: 1,
    onCreate: (db, version) async {
      await db.execute('CREATE TABLE drafts (id TEXT PRIMARY KEY, payload TEXT NOT NULL, saved_at TEXT NOT NULL)');
    },
  );
  Future<void> save(Map<String, Object> data) async {
    final db = await database;
    await db.insert('drafts', {'id':data['id'], 'payload':jsonEncode(data), 'saved_at':DateTime.now().toUtc().toIso8601String()}, conflictAlgorithm:ConflictAlgorithm.abort);
  }
  Future<List<Map<String, dynamic>>> all() async {
    final db = await database;
    final rows = await db.query('drafts', orderBy:'saved_at DESC');
    return rows.map((row) => jsonDecode(row['payload']! as String) as Map<String,dynamic>).toList();
  }
}
