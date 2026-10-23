# Criterios de aceptación del incremento

Convención: GIVEN–WHEN–THEN (Dado–Cuando–Entonces).

| Caso | GIVEN | WHEN | THEN |
|---|---|---|---|
| Acceso central | Cuenta con rol y token válido | Solicita una operación | Se verifican permisos de cuenta en servidor |
| Propiedad | Productor y parcela de otra cuenta | Registra una cosecha | 403 sin guardar |
| Cosecha | Parcela propia y campos válidos | Guarda | Lote y evento atómicos |
| Reintento cosecha | UUID ya registrado con idénticos datos | Reenvía | Un solo lote y evento |
| Conflicto cosecha | UUID existente con otro peso | Reenvía | 409 sin sobrescribir |
| Calidad | Catador y lote existente | Registra defectos/puntaje válidos | Evaluación y evento asociados al lote |
| Umbral IA | Predictor devuelve exactamente 0.70 | Evalúa | No genera alerta |
| Umbral IA | Predictor devuelve 0.7001 | Evalúa | Genera alerta PENDIENTE y evento |
| Caída IA | Servicio no disponible | Evalúa | 503 y ninguna evaluación nueva |
| Confirmación | Supervisor y alerta pendiente | Confirma con motivo e instrucción | CONFIRMADA, responsable, instante y evento |
| Descarte | Supervisor y alerta pendiente | Descarta con motivo | DESCARTADA sin instrucción correctiva |
| Reintento revisión | Operación/contenido/responsable idénticos | Reenvía decisión | Mismo resultado y un solo evento |
| Revisión final | Alerta ya decidida | Intenta cambiar decisión | 409 |
| Atomicidad | Repositorio de eventos falla | Guarda operación | Se revierte el cambio de negocio |

La ficha de calidad completa, la telemetría y las notificaciones en tiempo real no se incluyen en este incremento. La deduplicación de cosecha todavía no resuelve por sí sola todos los conflictos offline.
