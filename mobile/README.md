# Cliente móvil: base inicial

Esta entrega contiene el paquete Flutter y una pantalla informativa. **No es una app offline implementada**.
Requiere Flutter estable instalado. Desde esta carpeta:

```powershell
flutter create --platforms=android .
flutter pub get
flutter run
```

La generación de `android/` es deliberadamente local, con la versión de Flutter instalada por el equipo.
No se ejecutó Flutter en el entorno donde se preparó este proyecto.

Próximo recorrido: productor autorizado → registrar cosecha en SQLite cifrado → cola local → REST → ACK del servidor.
Usar un UUID estable por registro/reintento. No borrar la operación local antes de recibir el ACK.
Una cuenta nueva requiere autenticación online; el acceso offline posterior necesita una política explícita.
La API actual sólo deduplica el registro de cosecha por UUID y contenido; no implementa sincronización por lotes ni resolución general de conflictos.
En un emulador Android, el host suele ser `10.0.2.2`; el backend escucha sólo en loopback por defecto y no está configurado aquí para acceso desde dispositivos.
