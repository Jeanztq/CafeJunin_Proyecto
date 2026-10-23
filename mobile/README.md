# Flutter · captura preliminar offline

Código Dart para formulario de cosecha y persistencia de borradores en SQLite local con UUID estable. Sin autenticación, cifrado ni sincronización. No es todavía una app conectada al sistema.

En un equipo con Flutter instalado, desde `mobile`: `flutter create --platforms=android,ios --project-name cafejunin_mobile .`, `flutter pub get`, `flutter analyze`, `flutter run`. Revisa los archivos generados y los requisitos de Android/iOS. El proyecto se entrega sin carpetas nativas generadas. No hay SDK Flutter en el entorno de entrega: no se compiló ni se probó en emulador/dispositivo.

Siguiente incremento: OIDC con navegador del sistema/PKCE, tokens en almacenamiento seguro, vínculo de parcela por cuenta, base cifrada y cola de sincronización con ACK/conflictos. No enviar borradores de una cuenta con credenciales de otra. No usar datos sensibles mientras no se completen estos controles.
