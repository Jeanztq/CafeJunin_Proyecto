# API 0.2

Base `/api`. JSON. Seguridad activa: Bearer JWT salvo config/info. Las operaciones verifican rol y pertenencia en la aplicación.

| Método/ruta | Operación |
|---|---|
| GET /config, /info | Configuración pública del cliente y modo de ejecución; sin secretos |
| GET /me | Identidad y roles de la cuenta actual |
| GET /parcelas | Parcelas autorizadas |
| GET /lotes | Lotes autorizados |
| POST /lotes | Productor: cosecha |
| GET /lotes/{id}/detalle | Lote, calidad y, solo supervisor, riesgos/alertas |
| POST /lotes/{id}/evaluaciones | Catador · registro de ficha física y sensorial |
| POST /lotes/{id}/riesgos | Supervisor: llamar predictor y guardar riesgo |
| GET /alertas | Supervisor: alertas |
| GET /alertas/{id} | Supervisor · alerta, lote, parcela, evaluación que la originó y umbral de alerta |
| PUT /alertas/{id}/decision | Supervisor: confirmar/descartar |

Cosecha: `id` UUID estable, `parcelaId` UUID, `fechaCosecha` YYYY-MM-DD, `variedad` TYPICA/GEISHA/BOURBON/CATIMOR, `tipoRecoleccion` SELECTIVA/GENERAL, `pesoKg` >0 con hasta tres decimales y ocho enteros. La fecha no puede ser futura. Un reintento idéntico devuelve 201 sin duplicar lote/evento; el mismo UUID con otro contenido devuelve 409.

**HU08.** El cliente genera un UUID para cada nueva ficha y lo conserva durante el reintento. El servidor toma el catador de la sesión y asigna la fecha. El cuerpo de ejemplo es

```json
{
  "id": "22987e8f-ed86-4fd7-9fd1-1410d930fab8",
  "defectosPrimarios": 0,
  "defectosSecundarios": 2,
  "atributos": {
    "fragancia": 8, "sabor": 8.5, "acidez": 8, "cuerpo": 8.5,
    "posgusto": 8, "balance": 8, "uniformidad": 10, "dulzor": 10, "tazaLimpia": 10, "apreciacionGlobal": 8
  },
  "defectosTaza": {"leves": 1, "graves": 0}
}
```

Los defectos físicos son enteros no negativos. Los diez atributos y ambos conteos de defectos sensoriales son obligatorios. El servidor calcula el puntaje como la suma de las notas menos la penalización de taza. El ejemplo obtiene 85. La API rechaza un puntaje aportado por el cliente. La respuesta y el detalle incluyen protocolo, umbral aplicado, responsable y fecha. El detalle devuelve también los movimientos cronológicos visibles para la cuenta. Un envío idéntico con el mismo UUID y catador devuelve la ficha original. Si cambia el contenido o el catador, devuelve 409. Cada nueva muestra usa otro UUID. Las evaluaciones básicas anteriores se leen con atributos y catador nulos.

Riesgo: `humedad` 0..100 (relativa ambiental, no del grano), `horasDemora` >=0. Respuesta: `id,loteId,humedad,horasDemora,probabilidad:{valor},modelo,simulada`. Solo si `probabilidad.valor>0.70` se guarda una alerta. Cada solicitud crea una evaluación; no tiene clave idempotente.

Decisión: `operacionId` UUID estable, `estado` CONFIRMADA/DESCARTADA, `motivo` 5..1000 caracteres, `instruccion` 5..1000 al confirmar; null/vacía al descartar. Estado final inmutable. Exacto reintento con misma operación/contenido/responsable devuelve la misma decisión; cualquier otro intento final devuelve 409. Se registra el subject del supervisor y la hora del servidor.

Errores: 400 datos inválidos, 401 token ausente/inválido, 403 acceso no autorizado, 404 inexistente, 409 conflicto, 503 predictor/persistencia no disponible. Errores de aplicación: `{estado,mensaje}`. Los 401/403 del filtro pueden tener cuerpo vacío.

## Despacho e incidencias

Las rutas siguientes parten de `/api/transporte`. La aplicación usa un envío por lote en este incremento. El atributo `salida` pasa de nulo a la hora UTC del servidor al confirmar el despacho.

| Método y ruta | Cuenta y operación |
|---|---|
| GET /transportistas | El supervisor consulta el directorio de conductores habilitados. |
| GET /envios | El supervisor consulta las asignaciones de la cooperativa. El conductor recibe solo sus envíos. |
| GET /envios/{lote} | El supervisor y el conductor asignado consultan lote, despacho y novedades en orden de registro. |
| PUT /envios/{lote} | El supervisor asigna el lote usando el campo `transportista` con el subject del conductor. |
| PUT /envios/{lote}/despacho | El conductor confirma la salida con `placa` y `pesoCargaKg`, positivo y hasta el peso registrado del lote. |
| PUT /envios/{lote}/novedades/{id} | El conductor registra `tipo`, `lugar` y `descripcion` durante el tránsito. El identificador UUID se conserva al reintentar. |

Los tipos previstos son CONTROL, DESVIO, DERRUMBE y FALLA_MECANICA. Las incidencias exigen una descripción. El reintento con los mismos valores recupera el registro existente. Cambiar los datos de una asignación, salida o novedad ya confirmada devuelve 409. Cada operación nueva guarda el movimiento y el evento del lote en la misma transacción.
