# Persistencia SQL Server

Tablas: parcelas → lotes → evaluaciones_calidad / evaluaciones_riesgo → alertas. Outbox_eventos conserva sobres de eventos asociados al agregado. UUID en uniqueidentifier; peso decimal(11,3); estados y rangos con CHECK; claves foráneas; índices de parcela y eventos pendientes.

Parcelas incluye propietario_subject vinculado al `sub` OIDC. No se almacenan contraseñas en estas tablas: las administra Keycloak. No hay migración de datos desde PostgreSQL en esta entrega.

Una alerta referencia una evaluación de riesgo y permite una decisión final con motivo/instrucción, responsable, instante y operacion_id. `sp_revisar_alerta` actualiza solo PENDIENTE y detecta competencia con THROW 51001; el adaptador lo traduce a conflicto. Negocio y outbox participan en la misma transacción de Spring.

Los scripts 00..03 no eliminan tablas existentes. CREATE OR ALTER actualiza procedimientos; para cambios futuros de columnas se deberán crear migraciones incrementales. Tablas sensoriales, transporte, sesiones móviles, auditoría general y cooperativas múltiples aún no están modeladas. No asumir que este esquema es la base de datos definitiva del informe.
