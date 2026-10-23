USE cafejunin;
GO
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO
-- Aquí continúa el resto de tu script original (CREATE TABLE, CREATE INDEX, etc.)

IF OBJECT_ID(N'dbo.parcelas',N'U') IS NULL
 CREATE TABLE dbo.parcelas (id uniqueidentifier PRIMARY KEY,nombre nvarchar(160) NOT NULL,ubicacion nvarchar(250) NOT NULL,propietario_subject nvarchar(200) NOT NULL);
GO
IF OBJECT_ID(N'dbo.lotes',N'U') IS NULL
 CREATE TABLE dbo.lotes (id uniqueidentifier PRIMARY KEY,parcela_id uniqueidentifier NOT NULL REFERENCES dbo.parcelas(id),fecha_cosecha date NOT NULL,variedad varchar(30) NOT NULL CHECK(variedad IN ('TYPICA','GEISHA','BOURBON','CATIMOR')),tipo_recoleccion varchar(30) NOT NULL CHECK(tipo_recoleccion IN ('SELECTIVA','GENERAL')),peso_kg decimal(11,3) NOT NULL CHECK(peso_kg>0),registrado_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME());
GO
IF OBJECT_ID(N'dbo.evaluaciones_calidad',N'U') IS NULL
 CREATE TABLE dbo.evaluaciones_calidad (id uniqueidentifier PRIMARY KEY,lote_id uniqueidentifier NOT NULL REFERENCES dbo.lotes(id),defectos_primarios int NOT NULL CHECK(defectos_primarios>=0),defectos_secundarios int NOT NULL CHECK(defectos_secundarios>=0),puntaje_sca float NOT NULL CHECK(puntaje_sca BETWEEN 0 AND 100),registrado_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME());
GO
IF OBJECT_ID(N'dbo.evaluaciones_riesgo',N'U') IS NULL
 CREATE TABLE dbo.evaluaciones_riesgo (id uniqueidentifier PRIMARY KEY,lote_id uniqueidentifier NOT NULL REFERENCES dbo.lotes(id),humedad float NOT NULL CHECK(humedad BETWEEN 0 AND 100),horas_demora float NOT NULL CHECK(horas_demora>=0),probabilidad float NOT NULL CHECK(probabilidad BETWEEN 0 AND 1),modelo nvarchar(160) NOT NULL,simulada bit NOT NULL,genera_alerta bit NOT NULL,registrado_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),CONSTRAINT ck_umbral CHECK((probabilidad>0.7 AND genera_alerta=1) OR (probabilidad<=0.7 AND genera_alerta=0)));
GO
IF OBJECT_ID(N'dbo.alertas',N'U') IS NULL
 CREATE TABLE dbo.alertas (id uniqueidentifier PRIMARY KEY,lote_id uniqueidentifier NOT NULL REFERENCES dbo.lotes(id),riesgo_id uniqueidentifier NOT NULL UNIQUE REFERENCES dbo.evaluaciones_riesgo(id),estado varchar(20) NOT NULL CHECK(estado IN ('PENDIENTE','CONFIRMADA','DESCARTADA')),motivo nvarchar(1000),instruccion nvarchar(1000),responsable nvarchar(200),revisada_en datetime2,operacion_id uniqueidentifier,registrada_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),CONSTRAINT ck_revision CHECK((estado='PENDIENTE' AND revisada_en IS NULL AND operacion_id IS NULL) OR (estado IN ('CONFIRMADA','DESCARTADA') AND revisada_en IS NOT NULL AND operacion_id IS NOT NULL AND LEN(motivo)>=5 AND responsable IS NOT NULL)),CONSTRAINT ck_instruccion CHECK(estado<>'CONFIRMADA' OR LEN(COALESCE(instruccion,''))>=5));
GO
IF OBJECT_ID(N'dbo.outbox_eventos',N'U') IS NULL
 CREATE TABLE dbo.outbox_eventos (id uniqueidentifier PRIMARY KEY,tipo nvarchar(100) NOT NULL,agregado_id uniqueidentifier NOT NULL,ocurrido_en datetime2 NOT NULL,version int NOT NULL,publicado_en datetime2 NULL,intentos int NOT NULL DEFAULT 0);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_lotes_parcela' AND object_id=OBJECT_ID('dbo.lotes')) CREATE INDEX ix_lotes_parcela ON dbo.lotes(parcela_id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_outbox_pendientes' AND object_id=OBJECT_ID('dbo.outbox_eventos')) CREATE INDEX ix_outbox_pendientes ON dbo.outbox_eventos(ocurrido_en) WHERE publicado_en IS NULL;
GO
