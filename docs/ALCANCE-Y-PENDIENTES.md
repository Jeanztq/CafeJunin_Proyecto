# Estado y fases

La entrega final se centra en HU08. La tabla distingue su implementación de las funcionalidades de apoyo y de las historias del alcance general.

| Área | Estado en 0.2 | Pendiente |
|---|---|---|
| Hexagonal y patrones solicitados | Código implementado y pruebas del núcleo | Revisión académica con rúbrica del docente |
| Cosecha / HU05 | Web/API, validación, ownership, idempotencia | Captura móvil autenticada y sincronización |
| Calidad / HU08 | Ficha clásica SCA de diez atributos, puntaje calculado, método, umbral comercial y responsable persistidos | Validación académica con el docente |
| Datos IA / HU15 | Captura manual de dos variables | Sensores, fuentes oficiales, calidad de datos y definición de etiqueta |
| Predicción / HU16 | XGBoost integrado por HTTP; entrenamiento sintético | Dataset real, validación por finca/tiempo, calibración y eficacia |
| Alertas / HU17 | Umbral estricto >70%, lista persistida, evento | Notificación inmediata y automatización por telemetría |
| Revisión / HU18 | Confirmar/descartar, motivo, instrucción, autor y auditoría de decisión | Entrega y ejecución de instrucción al transportista |
| Portal React | Navegación por permisos y recorrido integrado probado | Resto de módulos y auditoría de accesibilidad |
| SQL Server | Esquema y procedimientos probados en SQL Express y adaptador JDBC de cata y transporte verificado con SQL Server 2022 | Verificar el adaptador JDBC con credenciales del despliegue, backups y permisos mínimos |
| Identidad | Login y logout con Keycloak real por rol, PKCE, JWT y TOTP para catador y supervisor | Configurar y verificar el proveedor, HTTPS y usuarios del despliegue |
| Flutter | Captura, confirmación y consulta local de borradores en Flutter para Android | APK y prueba en dispositivo, OIDC, cifrado, ACK y sincronización por cuenta |
| Eventos | Consumidor local HU08, outbox atómico y línea de tiempo del lote por rol | Publicación a un broker externo |
| GitHub | Scripts Git y workflow CI | Elegir repositorio y realizar push |
| Despacho y ruta / HU12 | Asignación del supervisor, salida con placa, hora y peso, controles por punto e historial por envío | Recepción firmada en destino (HU14) |
| Incidencias / HU13 | Desvío, derrumbe o falla mecánica con lugar y descripción, visibles en el seguimiento del supervisor | Adjuntos y notificaciones automáticas |
| Procesamiento y consolidación | Pendiente | Beneficio, secado y consolidación de microlotes |
| Consumidor | Pendiente | QR firmado y trazabilidad pública sin datos privados |

## Actores y pantallas

Los roles operativos actuales son productor, catador, supervisor y transportista. HU08 se presenta con la cuenta del catador. La aplicación móvil Flutter cubre la captura local del productor (HU05). El transportista usa el portal adaptable a teléfonos para HU12 y HU13.

El alcance de la semana 9 también contempla al operador de acopio para consolidación y permisos (HU09 y HU11), al operador de certificación para el pasaporte (HU10) y al responsable de recepción en destino (HU14). La asignación concreta de permisos de recepción se confirma al implementar la historia. El consumidor accede por un QR público en HU19. Estas funciones siguen pendientes. Una persona puede cumplir más de una función en la cooperativa.

## Secuencia de trabajo

1. Verificar SQL Server y configurar Keycloak con las cuentas y los dominios del despliegue. Confirmar la matriz de historias del informe para cerrar el alcance de entrega.
2. Completar las funciones web restantes Must/Should antes de ampliar a módulos futuros.
3. Compilar Flutter y completar inicio de sesión, cifrado local y sincronización idempotente. El backend será el mismo; no crear otro sistema por actor.
4. Incorporar datos reales de riesgo y validar el modelo; luego recepción, notificaciones y automatización de eventos según el alcance confirmado.

Las fases 2–4 requieren trabajo adicional y no se presentan como terminadas. El código preparado no garantiza cumplimiento de RNF de rendimiento, disponibilidad, recuperación ni seguridad de producción.
