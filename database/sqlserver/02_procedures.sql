USE cafejunin;
GO
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_lote @id uniqueidentifier,@parcela uniqueidentifier,@fecha date,@variedad varchar(30),@tipo varchar(30),@peso decimal(11,3)
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.lotes(id,parcela_id,fecha_cosecha,variedad,tipo_recoleccion,peso_kg) VALUES(@id,@parcela,@fecha,@variedad,@tipo,@peso);
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_calidad
 @id uniqueidentifier, @lote uniqueidentifier, @p int, @s int, @puntaje float,
 @catador nvarchar(255), @nombre nvarchar(255), @fecha datetime2,
 @fragancia float, @sabor float, @acidez float, @cuerpo float,
 @posgusto float, @balance float, @uniformidad float, @dulzor float,
 @taza_limpia float, @apreciacion_global float, @leves int, @graves int,
 @protocolo varchar(32), @umbral decimal(5,2)
AS BEGIN
 SET NOCOUNT ON;
 SET XACT_ABORT ON;
 BEGIN TRANSACTION;
 INSERT dbo.evaluaciones_calidad(id,lote_id,defectos_primarios,defectos_secundarios,puntaje_sca,registrado_en)
 VALUES(@id,@lote,@p,@s,@puntaje,@fecha);
 INSERT dbo.fichas_cata(evaluacion_id,catador_id,catador_nombre,fragancia,sabor,acidez,cuerpo,posgusto,balance,uniformidad,dulzor,taza_limpia,apreciacion_global,tazas_leves,tazas_graves,protocolo,umbral_especialidad)
 VALUES(@id,@catador,@nombre,@fragancia,@sabor,@acidez,@cuerpo,@posgusto,@balance,@uniformidad,@dulzor,@taza_limpia,@apreciacion_global,@leves,@graves,@protocolo,@umbral);
 COMMIT;
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
CREATE OR ALTER PROCEDURE dbo.sp_encolar_evento @id uniqueidentifier,@tipo nvarchar(100),@agregado uniqueidentifier,@fecha datetime2,@version int,@evaluacion uniqueidentifier=NULL
AS BEGIN SET NOCOUNT ON; SET XACT_ABORT ON;
INSERT dbo.outbox_eventos(id,tipo,agregado_id,ocurrido_en,version,evaluacion_id) VALUES(@id,@tipo,@agregado,@fecha,@version,@evaluacion);
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_asignar_envio
 @lote uniqueidentifier, @transportista nvarchar(255), @supervisor nvarchar(255), @fecha datetime2
AS
 INSERT dbo.envios(lote_id,transportista,asignado_por,asignado_en) VALUES (@lote,@transportista,@supervisor,@fecha);
GO
CREATE OR ALTER PROCEDURE dbo.sp_despachar_envio
 @lote uniqueidentifier, @fecha datetime2, @placa varchar(7), @peso decimal(11,3)
AS
 UPDATE dbo.envios SET salida=@fecha,placa=@placa,peso_carga=@peso WHERE lote_id=@lote AND salida IS NULL;
GO
CREATE OR ALTER PROCEDURE dbo.sp_registrar_novedad_envio
 @lote uniqueidentifier, @id uniqueidentifier, @tipo varchar(20), @lugar nvarchar(160), @descripcion nvarchar(1000), @fecha datetime2
AS
 INSERT dbo.novedades_envio(lote_id,id,tipo,lugar,descripcion,registrada_en) VALUES (@lote,@id,@tipo,@lugar,@descripcion,@fecha);
GO
