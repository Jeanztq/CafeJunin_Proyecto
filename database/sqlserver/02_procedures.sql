USE cafejunin;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_lote @id uniqueidentifier,@parcela uniqueidentifier,@fecha date,@variedad varchar(30),@tipo varchar(30),@peso decimal(11,3)
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.lotes(id,parcela_id,fecha_cosecha,variedad,tipo_recoleccion,peso_kg) VALUES(@id,@parcela,@fecha,@variedad,@tipo,@peso);
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_calidad @id uniqueidentifier,@lote uniqueidentifier,@p int,@s int,@puntaje float
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.evaluaciones_calidad(id,lote_id,defectos_primarios,defectos_secundarios,puntaje_sca) VALUES(@id,@lote,@p,@s,@puntaje);
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_riesgo @id uniqueidentifier,@lote uniqueidentifier,@humedad float,@demora float,@prob float,@modelo nvarchar(160),@simulada bit,@alerta bit
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.evaluaciones_riesgo(id,lote_id,humedad,horas_demora,probabilidad,modelo,simulada,genera_alerta) VALUES(@id,@lote,@humedad,@demora,@prob,@modelo,@simulada,@alerta);
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_alerta @id uniqueidentifier,@lote uniqueidentifier,@riesgo uniqueidentifier,@estado varchar(20)
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.alertas(id,lote_id,riesgo_id,estado) VALUES(@id,@lote,@riesgo,@estado);
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_revisar_alerta @id uniqueidentifier,@estado varchar(20),@motivo nvarchar(1000),@instruccion nvarchar(1000),@responsable nvarchar(200),@fecha datetime2,@operacion uniqueidentifier
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
IF @estado NOT IN ('CONFIRMADA','DESCARTADA') THROW 51002,'Estado de revisión inválido',1;
 UPDATE dbo.alertas SET estado=@estado,motivo=@motivo,instruccion=@instruccion,responsable=@responsable,revisada_en=@fecha,operacion_id=@operacion WHERE id=@id AND estado='PENDIENTE';
 IF @@ROWCOUNT<>1 THROW 51001,'La alerta ya fue revisada',1;
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_encolar_evento @id uniqueidentifier,@tipo nvarchar(100),@agregado uniqueidentifier,@fecha datetime2,@version int
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.outbox_eventos(id,tipo,agregado_id,ocurrido_en,version) VALUES(@id,@tipo,@agregado,@fecha,@version);
END;
GO
