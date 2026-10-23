# Estado y fases

Este incremento cubre partes del alcance priorizado; no declara completas las 14 historias Must y 5 Should. Se conservaron referencias HU05/HU08/HU15–HU18 de la base anterior. Antes de dar por completada una HU hay que contrastar todos los criterios del informe y la matriz original.

| Área | Estado en 0.2 | Pendiente |
|---|---|---|
| Hexagonal y patrones solicitados | Código implementado y pruebas del núcleo | Revisión académica con rúbrica del docente |
| Cosecha / HU05 | Web/API, validación, ownership, idempotencia | Captura móvil autenticada y sincronización |
| Calidad / HU08 | Registro básico de defectos/puntaje | Atributos sensoriales, ficha completa, identidad de evaluador persistida |
| Datos IA / HU15 | Captura manual de dos variables | Sensores, fuentes oficiales, calidad de datos y definición de etiqueta |
| Predicción / HU16 | XGBoost integrado por HTTP; entrenamiento sintético | Dataset real, validación por finca/tiempo, calibración y eficacia |
| Alertas / HU17 | Umbral estricto >70%, lista persistida, evento | Notificación inmediata y automatización por telemetría |
| Revisión / HU18 | Confirmar/descartar, motivo, instrucción, autor y auditoría de decisión | Entrega y ejecución de instrucción al transportista |
| Portal React | Navegación por permisos y recorrido integrado probado | Resto de módulos, QA visual en navegador y accesibilidad completa |
| SQL Server | Adapter, esquema, procedimientos y prueba de integración preparada | Ejecutar prueba contra instancia real; backups y permisos mínimos |
| Identidad | Cliente OIDC PKCE, JWT issuer/audiencia, realm y TOTP requeridos | Verificar login/logout/MFA con Keycloak real |
| Flutter | Código de borradores SQLite offline | SDK, compilación, pruebas físicas, OIDC, cifrado, ACK, sincronización/conflictos |
| Eventos | Outbox atómico de sobres | Payloads, relay/broker, consumidores y deduplicación |
| GitHub | Scripts Git y workflow CI | Elegir repositorio y realizar push |
| Transporte y procesamiento | Pendiente | Asignaciones, recepción, consolidación, evidencias y telemetría |
| Consumidor | Pendiente | QR firmado y trazabilidad pública sin datos privados |

## Secuencia de trabajo

1. Conectar SQL Server y Keycloak en tu equipo y ejecutar sus verificaciones. Confirmar la matriz de historias del informe para cerrar el alcance de entrega.
2. Completar calidad y las funciones web restantes Must/Should antes de ampliar a módulos futuros.
3. Compilar Flutter y completar inicio de sesión, cifrado local y sincronización idempotente. El backend será el mismo; no crear otro sistema por actor.
4. Incorporar datos reales de riesgo y validar el modelo; luego transporte, notificaciones y automatización de eventos según el alcance confirmado.

Las fases 2–4 requieren trabajo adicional y no se presentan como terminadas. El código preparado no garantiza cumplimiento de RNF de rendimiento, disponibilidad, recuperación ni seguridad de producción.
