# Persistencia SQL Server

Tablas: parcelas → lotes → evaluaciones_calidad / evaluaciones_riesgo → alertas. Outbox_eventos conserva sobres de eventos asociados al agregado. UUID en uniqueidentifier; peso decimal(11,3); estados y rangos con CHECK; claves foráneas; índices de parcela y eventos pendientes.

Parcelas incluye propietario_subject vinculado al `sub` OIDC. No se almacenan contraseñas en estas tablas: las administra Keycloak. No hay migración de datos desde PostgreSQL en esta entrega.

Una alerta referencia una evaluación de riesgo y permite una decisión final con motivo/instrucción, responsable, instante y operacion_id. `sp_revisar_alerta` actualiza solo PENDIENTE y detecta competencia con THROW 51001; el adaptador lo traduce a conflicto. Negocio y outbox participan en la misma transacción de Spring.

Los scripts 00..03 no eliminan tablas existentes. CREATE OR ALTER actualiza procedimientos; para cambios futuros de columnas se deberán crear migraciones incrementales. Transporte, sesiones móviles, auditoría general y cooperativas múltiples aún no están modeladas. No asumir que este esquema es la base de datos definitiva del informe.

En los perfiles H2 y SQL Server, HU08 guarda la cabecera en evaluaciones_calidad y el perfil sensorial en fichas_cata. Ambas comparten el UUID de la evaluación. Las fichas clásicas tienen diez atributos, conteos de defectos de taza, método y umbral congelado al registrar. Los registros históricos de ocho atributos conservan sus valores originales. Una evaluación histórica sin perfil sensorial conserva el puntaje original. La identidad del catador y su nombre al registrar permiten reconstruir quién evaluó cada muestra.

## Despacho y seguimiento

La tabla `transportistas` vincula el nombre operativo al identificador de la cuenta. `envios` enlaza el lote, el conductor y el supervisor que lo asignó. Al confirmar la salida se guardan juntos la hora, la placa y el peso de carga. Este incremento admite un envío por lote.

Cada fila de `novedades_envio` conserva el tipo de movimiento, el lugar, la descripción, la fecha y un UUID de operación. El orden de inserción permite reconstruir el trayecto incluso si dos movimientos tienen la misma marca de tiempo. La escritura del dato y la del evento en `outbox_eventos` se confirman juntas.
