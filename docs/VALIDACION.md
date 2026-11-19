# Validación de la entrega 0.2

## Registro de calidad y persistencia local

- Java y Spring Boot. La suite verifica restricciones sensoriales, cómputo en el servidor, rechazo del puntaje manual, autorización por rol, umbral configurable, reintentos y reversión si falla el consumidor. Incluye reapertura de H2 en archivo. Se ejecutaron 21 pruebas con éxito. La prueba JDBC optativa para SQL Server requiere las credenciales del despliegue.
- React. Pasaron nueve pruebas. Cubren la navegación por rol, el registro de cosecha desde Lotes, la previsualización del puntaje, los reintentos y la cancelación. Para las alertas se comprueba la recuperación de la evidencia tras un fallo de conexión y la consulta de la decisión cuando otro supervisor cierra la revisión.
- SQL Express. Los procedimientos y la actualización del esquema se probaron en una base temporal. Se verificaron las columnas de la cata clásica, el registro del evento y el rollback conjunto.
- Edge y JAR local. Desde una base nueva se registraron cosecha y cata, se corrigió una nota inválida y se obtuvo 85 sobre 100. Un supervisor registró 95 % de humedad y 36 horas de demora. Después de otra evaluación del mismo lote, la alerta siguió mostrando las mediciones originales. Se confirmó desde el navegador y se reinició el JAR con la misma base. El portal recuperó la decisión, su instrucción, la ficha de diez notas y los seis movimientos. Tanto la ficha de cata como la revisión se comprobaron a 390 px de ancho.
- Integración HTTP. Se ejecutó el recorrido del JAR con FastAPI y el modelo XGBoost incluido. Se verificaron el reintento de cosecha, la revisión de alertas y la respuesta ante la caída del predictor.

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
