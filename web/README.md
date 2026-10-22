# Interfaz web inicial

`src/index.html` es una interfaz HTML/CSS/JavaScript de demostración, sin dependencias npm.
Maven la incluye en el backend: abrir http://localhost:8080 al arrancar en modo demo.
Permite ejecutar cosecha, calidad y riesgo, y consultar lotes/alertas.

Se utiliza HTML para disponer de un recorrido funcional inicial. El portal React completo, las pantallas por actor,
el inicio de sesión OIDC y la consulta pública QR **todavía no están implementados**.
Con seguridad OIDC activada se usan clientes REST autenticados; esta interfaz demo queda deshabilitada por la política del servidor.
No abrir el archivo por doble clic para probar persistencia: necesita el backend en el mismo origen.
