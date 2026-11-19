# Criterios de aceptación del incremento

Convención: GIVEN–WHEN–THEN (Dado–Cuando–Entonces).

| Caso | GIVEN | WHEN | THEN |
|---|---|---|---|
| Acceso central | Cuenta con rol y token válido | Solicita una operación | Se verifican permisos de cuenta en servidor |
| Propiedad | Productor y parcela de otra cuenta | Registra una cosecha | 403 sin guardar |
| Cosecha | Parcela propia y campos válidos | Guarda | Lote y evento atómicos |
| Reintento cosecha | UUID ya registrado con idénticos datos | Reenvía | Un solo lote y evento |
| Conflicto cosecha | UUID existente con otro peso | Reenvía | 409 sin sobrescribir |
| HU08 · CA01 | Catador y lote existente | Confirma defectos, diez atributos y defectos de taza | Ficha, catador, fecha y evento dentro de la misma transacción |
| HU08 · CA02 | Ficha registrada | Abre el detalle del lote | Puede consultar todos los atributos, defectos, puntaje y clasificación |
| HU08 · CA03 | Ficha incompleta o fuera de escala | Intenta registrar | Puede corregir los campos sin guardar una evaluación |
| HU08 · Reintento | Ficha ya confirmada | Repite el mismo envío | Devuelve la ficha original y conserva un único evento |
| HU12 · Asignación y salida | Supervisor, un lote y un conductor habilitado | Asigna y el conductor confirma placa y peso | La hora de salida queda registrada y el envío pasa a tránsito |
| HU12 · Control | Conductor del envío en tránsito | Registra el punto de ruta | El supervisor ve el control al actualizar el seguimiento |
| HU13 · Incidencia | Envío en tránsito | Reporta un desvío, derrumbe o falla con lugar y descripción | El reporte se incorpora al mismo trayecto |
| Transporte · Permisos y reintentos | Otro conductor o un envío previamente registrado | Intenta operar o vuelve a enviar la misma solicitud | Se restringe el acceso por cuenta y los reintentos idénticos conservan un único movimiento |
| Umbral IA | Predictor devuelve exactamente 0.70 | Evalúa | No genera alerta |
| Umbral IA | Predictor devuelve 0.7001 | Evalúa | Genera alerta PENDIENTE y evento |
| Caída IA | Servicio no disponible | Evalúa | 503 y ninguna evaluación nueva |
| Evidencia de alerta | Dos evaluaciones del mismo lote | Abre la alerta de la primera | Recupera las mediciones y el modelo de la evaluación que la originó |
| Confirmación | Supervisor y alerta pendiente | Confirma con motivo e instrucción | CONFIRMADA, responsable, instante y evento |
| Descarte | Supervisor y alerta pendiente | Descarta con motivo | DESCARTADA sin instrucción correctiva |
| Reintento revisión | Operación/contenido/responsable idénticos | Reenvía decisión | Mismo resultado y un solo evento |
| Revisión final | Alerta ya decidida | Intenta cambiar decisión | 409 |
| Atomicidad | Repositorio de eventos falla | Guarda operación | Se revierte el cambio de negocio |

La telemetría y las notificaciones en tiempo real no se incluyen en este incremento. La deduplicación de cosecha todavía no resuelve por sí sola todos los conflictos offline.
