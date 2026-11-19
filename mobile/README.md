# Café Junín · Campo

HU05 cubre el registro de cosecha del productor en campo. La aplicación Android guarda un borrador con UUID, parcela, fecha, variedad, recolección y peso en SQLite. Al terminar la escritura muestra el comprobante. El productor puede volver al formulario y abrir cualquier registro desde **Cosechas guardadas**. Una operación fallida conserva los datos del formulario para corregirlos o reintentar.

## Ejecutar

Desde esta carpeta, con Flutter 3.50, Android SDK y un dispositivo o emulador disponible:

```sh
flutter pub get
flutter analyze
flutter test
flutter run
```

La prueba de persistencia en Windows se ejecuta con `flutter test` desde **tests/sqlite_campo**. Allí se instala el adaptador SQLite de escritorio para la prueba. El proyecto Android está en **android/**. Se verificaron el análisis estático, el paquete de depuración de Flutter y dos pruebas que cubren el comprobante, el reintento y la recuperación de datos al reabrir SQLite. El formulario acepta el identificador de una parcela. Todavía faltan la cuenta móvil con OIDC, la selección de parcelas asignadas, el cifrado local y el envío al backend con confirmación. Los borradores permanecen en este dispositivo. HU08, el riesgo y la revisión del supervisor se atienden desde el portal.
