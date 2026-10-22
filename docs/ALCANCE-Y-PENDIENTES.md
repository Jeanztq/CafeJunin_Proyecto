# Estado de la entrega

Se ha implementado una base académica, no todo el sistema del informe.
Fuente funcional: RF05, parte de RF08 y el contrato de RF16/RF17; fuente técnica: los dos Word del docente.

| Elemento | Estado |
|---|---|
| Tres casos de uso desde REST e interfaz demo | Implementado |
| Dominio sin frameworks y puertos de entrada/salida | Implementado |
| Cinco patrones y pruebas sin infraestructura | Implementado |
| Persistencia memoria/PostgreSQL intercambiable | Implementado |
| Escrituras SQL en procedimientos versionados | Implementado |
| Registro de sobres de eventos en outbox | Implementado; sin publicación ni consumo |
| Riesgo por modelo IA entrenado | Pendiente; se entrega simulador explícito |
| Autenticación central | Configuración opcional JWT; realm y login clientes pendientes |
| Permisos por propietario de datos | Pendiente |
| Portal React y navegación real por actor | Pendiente; demo HTML disponible |
| App Flutter de campo | Sólo estructura y pantalla informativa |
| SQLite cifrado, cola offline, ACK y conflictos | Pendiente |
| Kafka/microservicios/Kubernetes | Pendiente |
| Transporte, recepción, consolidaciones y procesamiento | Pendiente |
| SENAMHI/SENASA y QR firmado público | Pendiente |
| Métricas de rendimiento, disponibilidad y accesibilidad completas | Sin validar |
| Publicación GitHub | No realizada; instrucciones y workflow incluidos |

Orden de avance: completar identidad/pertenencia → ficha de calidad → captura móvil offline y sincronización →
predicción real validada → transporte/telemetría → publicación confiable de eventos → QR público y controles no funcionales.
La prioridad exacta se ajustará a las entregas del curso y la matriz MoSCoW.
No presentar la demo sin autenticación como cumplimiento de RF01/RF02 ni el simulador como cumplimiento de IA.
