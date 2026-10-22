# Casos de uso implementados

## UC01 — Registrar cosecha (avance de HU05)

Actor: Productor. Entrada: UUID estable del lote, parcela, fecha, variedad, tipo de recolección y peso.
Flujo: validar dominio → abrir unidad de trabajo → comprobar reintento → comprobar parcela → guardar lote → encolar evento → confirmar.
Resultado: lote y CosechaRegistrada. HTTP 201. Un reintento idéntico también devuelve 201, conservando el mismo lote.

- Given: parcela existente y datos válidos. When: se registra la cosecha. Then: existe un lote y un evento pendiente.
- Given: un UUID ya registrado con los mismos datos. When: se reenvía. Then: no se duplican lote ni evento.
- Given: un UUID ya registrado con otro peso. When: se reenvía. Then: se responde 409 sin sobrescribir.
- Given: peso cero o fecha futura. When: se intenta registrar. Then: se responde 400 y no se persiste.
- Given: parcela inexistente. When: se registra. Then: se responde 404.

La deduplicación actual no es una solución completa de sincronización offline. Dos primeras inserciones concurrentes pueden producir un 409 por la PK; el cliente debe consultar/reintentar, conservando el UUID y contenido originales.

## UC02 — Registrar evaluación de calidad (avance de HU08)

Actor: Catador. Entrada: lote, defectos primarios/secundarios y puntaje SCA final.
Flujo: validar rangos → comprobar lote → persistir evaluación y evento en una transacción.
Resultado: evaluación y EvaluacionCalidadRegistrada. HTTP 201.

- Given: lote existente y puntaje 85. When: el catador registra la evaluación. Then: se conserva la evaluación asociada al lote.
- Given: puntaje mayor que 100 o defectos negativos. When: se envía. Then: se responde 400.
- Given: lote inexistente. When: se evalúa. Then: se responde 404 sin crear registros.

Pendiente de HU08: atributos sensoriales detallados, identidad del catador y ficha completa. Cada POST válido crea una evaluación nueva; no reintentar automáticamente sin implementar antes idempotencia.

## UC03 — Evaluar riesgo (avance de HU16/HU17)

Actor: Supervisor, que solicita la evaluación en el prototipo. En la solución completa podrá dispararse por telemetría.
Entrada: lote, humedad y horas de demora. Flujo: comprobar datos/lote → llamar al puerto de predicción → guardar evaluación → crear alerta si probabilidad >0.70 → encolar eventos → confirmar.
Resultado: evaluación etiquetada como simulada y, si corresponde, alerta PENDIENTE.

- Given: predictor responde exactamente 0.70. When: se evalúa. Then: no se crea alerta.
- Given: predictor responde 0.7001. When: se evalúa. Then: se crea alerta pendiente.
- Given: falla la persistencia de un evento. When: se ejecuta el caso. Then: se revierte la unidad de trabajo.

El simulador no constituye IA real, no fue entrenado y no completa HU16. No hay notificaciones inmediatas, confirmación humana ni corrección logística implementadas.
