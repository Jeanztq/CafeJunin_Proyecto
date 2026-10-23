# API 0.2

Base `/api`. JSON. Seguridad activa: Bearer JWT salvo config/info. Las operaciones verifican rol y pertenencia en PortalFacade.

| Método/ruta | Operación |
|---|---|
| GET /config, /info | Configuración pública del cliente y modo de ejecución; sin secretos |
| GET /me | Identidad y roles de la cuenta actual |
| GET /parcelas | Parcelas autorizadas |
| GET /lotes | Lotes autorizados |
| POST /lotes | Productor: cosecha |
| GET /lotes/{id}/detalle | Lote, calidad y, solo supervisor, riesgos/alertas |
| POST /lotes/{id}/evaluaciones | Catador: registro básico de calidad |
| POST /lotes/{id}/riesgos | Supervisor: llamar predictor y guardar riesgo |
| GET /alertas | Supervisor: alertas |
| PUT /alertas/{id}/decision | Supervisor: confirmar/descartar |

Cosecha: `id` UUID estable, `parcelaId` UUID, `fechaCosecha` YYYY-MM-DD, `variedad` TYPICA/GEISHA/BOURBON/CATIMOR, `tipoRecoleccion` SELECTIVA/GENERAL, `pesoKg` >0 con hasta tres decimales y ocho enteros. La fecha no puede ser futura. Un reintento idéntico devuelve 201 sin duplicar lote/evento; el mismo UUID con otro contenido devuelve 409.

Calidad: `defectosPrimarios`, `defectosSecundarios` enteros no negativos y `puntajeSca` 0..100. El servidor asigna el UUID. No es idempotente; no reintentar automáticamente tras una respuesta incierta. No es la ficha sensorial completa.

Riesgo: `humedad` 0..100 (relativa ambiental, no del grano), `horasDemora` >=0. Respuesta: `id,loteId,humedad,horasDemora,probabilidad:{valor},modelo,simulada`. Solo si `probabilidad.valor>0.70` se guarda una alerta. Cada solicitud crea una evaluación; no tiene clave idempotente.

Decisión: `operacionId` UUID estable, `estado` CONFIRMADA/DESCARTADA, `motivo` 5..1000 caracteres, `instruccion` 5..1000 al confirmar; null/vacía al descartar. Estado final inmutable. Exacto reintento con misma operación/contenido/responsable devuelve la misma decisión; cualquier otro intento final devuelve 409. Se registra el subject del supervisor y la hora del servidor.

Errores: 400 datos inválidos, 401 token ausente/inválido, 403 acceso no autorizado, 404 inexistente, 409 conflicto, 503 predictor/persistencia no disponible. Errores de aplicación: `{estado,mensaje}`. Los 401/403 del filtro pueden tener cuerpo vacío.
