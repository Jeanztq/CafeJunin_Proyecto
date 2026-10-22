-- Esquema del incremento 1. Flyway es el único encargado de ejecutarlo.
CREATE TABLE parcelas (
 id UUID PRIMARY KEY,
 nombre VARCHAR(150) NOT NULL,
 productor_referencia VARCHAR(150) NOT NULL
);
CREATE TABLE lotes (
 id UUID PRIMARY KEY,
 parcela_id UUID NOT NULL REFERENCES parcelas(id),
 fecha_cosecha DATE NOT NULL,
 variedad VARCHAR(20) NOT NULL CHECK(variedad IN ('TYPICA','GEISHA','BOURBON','CATIMOR')),
 tipo_recoleccion VARCHAR(20) NOT NULL CHECK(tipo_recoleccion IN ('SELECTIVA','GENERAL')),
 peso_kg NUMERIC(11,3) NOT NULL CHECK(peso_kg > 0),
 registrado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE evaluaciones_calidad (
 id UUID PRIMARY KEY,
 lote_id UUID NOT NULL REFERENCES lotes(id),
 defectos_primarios INTEGER NOT NULL CHECK(defectos_primarios >= 0),
 defectos_secundarios INTEGER NOT NULL CHECK(defectos_secundarios >= 0),
 puntaje_sca DOUBLE PRECISION NOT NULL CHECK(puntaje_sca BETWEEN 0 AND 100 AND puntaje_sca <> 'NaN'::float8),
 registrada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE evaluaciones_riesgo (
 id UUID PRIMARY KEY,
 lote_id UUID NOT NULL REFERENCES lotes(id),
 humedad DOUBLE PRECISION NOT NULL CHECK(humedad BETWEEN 0 AND 100 AND humedad <> 'NaN'::float8),
 horas_demora DOUBLE PRECISION NOT NULL CHECK(horas_demora >= 0 AND horas_demora NOT IN ('Infinity'::float8,'NaN'::float8)),
 probabilidad DOUBLE PRECISION NOT NULL CHECK(probabilidad BETWEEN 0 AND 1 AND probabilidad <> 'NaN'::float8),
 modelo VARCHAR(100) NOT NULL,
 simulada BOOLEAN NOT NULL,
 genera_alerta BOOLEAN NOT NULL,
 registrada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(id,lote_id),
 CHECK(genera_alerta = (probabilidad > 0.70))
);
CREATE TABLE alertas (
 id UUID PRIMARY KEY,
 lote_id UUID NOT NULL REFERENCES lotes(id),
 riesgo_id UUID NOT NULL UNIQUE,
 estado VARCHAR(20) NOT NULL CHECK(estado IN ('PENDIENTE','CONFIRMADA','DESCARTADA')),
 registrada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(riesgo_id,lote_id) REFERENCES evaluaciones_riesgo(id,lote_id)
);
CREATE TABLE outbox_eventos (
 id UUID PRIMARY KEY,
 tipo VARCHAR(80) NOT NULL,
 agregado_id UUID NOT NULL REFERENCES lotes(id),
 ocurrido_en TIMESTAMPTZ NOT NULL,
 version INTEGER NOT NULL CHECK(version > 0),
 estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','PUBLICADO'))
);
CREATE INDEX idx_lotes_parcela ON lotes(parcela_id);
CREATE INDEX idx_calidad_lote ON evaluaciones_calidad(lote_id);
CREATE INDEX idx_riesgo_lote ON evaluaciones_riesgo(lote_id);
CREATE INDEX idx_alertas_lote ON alertas(lote_id);
CREATE INDEX idx_outbox_pendiente ON outbox_eventos(ocurrido_en) WHERE estado='PENDIENTE';

CREATE PROCEDURE sp_registrar_lote(p_id UUID,p_parcela UUID,p_fecha DATE,p_variedad VARCHAR,p_tipo VARCHAR,p_peso NUMERIC)
LANGUAGE SQL AS $$
 INSERT INTO lotes(id,parcela_id,fecha_cosecha,variedad,tipo_recoleccion,peso_kg)
 VALUES(p_id,p_parcela,p_fecha,p_variedad,p_tipo,p_peso);
$$;
CREATE PROCEDURE sp_registrar_calidad(p_id UUID,p_lote UUID,p_primarios INTEGER,p_secundarios INTEGER,p_sca DOUBLE PRECISION)
LANGUAGE SQL AS $$
 INSERT INTO evaluaciones_calidad(id,lote_id,defectos_primarios,defectos_secundarios,puntaje_sca)
 VALUES(p_id,p_lote,p_primarios,p_secundarios,p_sca);
$$;
CREATE PROCEDURE sp_registrar_riesgo(p_id UUID,p_lote UUID,p_humedad DOUBLE PRECISION,p_demora DOUBLE PRECISION,p_prob DOUBLE PRECISION,p_modelo VARCHAR,p_simulada BOOLEAN,p_alerta BOOLEAN)
LANGUAGE SQL AS $$
 INSERT INTO evaluaciones_riesgo(id,lote_id,humedad,horas_demora,probabilidad,modelo,simulada,genera_alerta)
 VALUES(p_id,p_lote,p_humedad,p_demora,p_prob,p_modelo,p_simulada,p_alerta);
$$;
CREATE PROCEDURE sp_registrar_alerta(p_id UUID,p_lote UUID,p_riesgo UUID,p_estado VARCHAR)
LANGUAGE SQL AS $$
 INSERT INTO alertas(id,lote_id,riesgo_id,estado) VALUES(p_id,p_lote,p_riesgo,p_estado);
$$;
CREATE PROCEDURE sp_encolar_evento(p_id UUID,p_tipo VARCHAR,p_agregado UUID,p_fecha TIMESTAMPTZ,p_version INTEGER)
LANGUAGE SQL AS $$
 INSERT INTO outbox_eventos(id,tipo,agregado_id,ocurrido_en,version) VALUES(p_id,p_tipo,p_agregado,p_fecha,p_version);
$$;
