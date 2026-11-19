# Validación de la entrega 0.2

## Verificación del incremento HU08

- Java y Spring Boot. La suite ejecutó 20 pruebas con éxito. Incluye rechazo de datos incompletos, autorización, umbrales de 79.5 y 80, idempotencia y una sola entrada de evento por ficha. La prueba JDBC optativa requiere configurar una conexión.
- React. Pasaron seis pruebas, incluidas corrección de campos, reintento tras un fallo de red y cancelación.
- Navegador. Edge recorrió el JAR en memoria desde el lote de ejemplo hasta la confirmación y la consulta tras recargar. Se comprobó que el formulario cabe a 390 px de ancho.
- SQL Express. En una base temporal se aplicaron los scripts dos veces, se consultó una evaluación histórica y se verificó que la cabecera, los atributos y el evento se pueden confirmar o revertir en una transacción.

## Evidencia histórica anterior al incremento HU08

- Java 17: `mvn clean verify`, 20 pruebas descubiertas, **19 correctas y 1 omitida** (SQL Server). Dominio, aislamiento de capas, roles, propiedad, REST, rollback, umbral estricto, revisión, idempotencia, decorator y rechazo de respuesta IA inválida.
- React: **4 pruebas correctas** con Vitest/jsdom. Menús de productor/supervisor, acceso seguro sin elegir privilegios y presentación de errores. Compilación Vite correcta. No se realizó QA visual en un navegador real.
- Python: **1 prueba correcta** con varios casos de contrato: autenticación por clave, rangos, respuesta y procedencia sintética. FastAPI TestClient emitió una advertencia de deprecación de su integración con httpx, sin fallos.
- Integración HTTP entre procesos reales: JAR sirviendo React → API → repositorio de memoria; API → FastAPI → modelo XGBoost. Cosecha sin duplicación, calidad, predicción, alerta, revisión y rechazo de cambio de decisión. Al detener el servicio IA se obtiene 503 sin nuevas evaluaciones. Resultado detallado: `evidence/http-integration.json`.

El JAR de `dist` incluye el portal compilado. El modelo incluido tiene ficha de procedencia y hash. Sus métricas corresponden a un dataset sintético, no a eficacia en campo.

## Alcance de las pruebas de infraestructura

- El ensayo de procedimientos de HU08 usa SQL Express real. La suite JDBC optativa se habilita mediante RUN_SQLSERVER_TESTS y las variables de conexión indicadas en CONEXIONES.md.
- Keycloak: realm, TOTP requerido, cliente PKCE y validación JWT implementados. Las pruebas HTTP usan JWT de prueba; no verifican un login interactivo, MFA ni logout contra un servidor Keycloak real.
- Flutter: sin SDK disponible. Código de borradores SQLite entregado, sin compilación, emulador ni prueba física. No hay integración móvil autenticada ni sincronización.
- Sin evaluación de carga, accesibilidad completa, tolerancia a fallos, backups ni cumplimiento íntegro de RNF. No se desplegó ni publicó en GitHub.

## Reproducir

`web`: `npm ci`, `npm test`, `npm run build`.
`backend`: `mvn verify` después de construir React.
`ai-service`: crear entorno Python3.12, instalar requirements y `pytest -q`.
Con dependencias Python del servicio instaladas, desde la raíz: `python tests/integration_http.py --jar dist/cafejunin-backend-0.2.0.jar`.
SQL Server y Keycloak: sigue `CONEXIONES.md`.
