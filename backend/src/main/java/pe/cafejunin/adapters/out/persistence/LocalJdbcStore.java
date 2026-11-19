package pe.cafejunin.adapters.out.persistence;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.application.usecases.ConflictoException;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.*;

public final class LocalJdbcStore extends JdbcStore {
  public LocalJdbcStore(JdbcTemplate jdbc, TransactionTemplate tx) {
    super(jdbc, tx, java.time.ZoneId.systemDefault());
  }

  @Override
  protected String bloqueoRegistro() { return ""; }

  // H2 en archivo permite un proceso propietario. Sus escrituras se confirman en orden.
  @Override
  public synchronized <T> T ejecutar(Supplier<T> operacion) {
    return super.ejecutar(operacion);
  }

  public void asignar(Envio e) {
    jdbc.update("INSERT INTO dbo.envios(lote_id,transportista,asignado_por,asignado_en) VALUES (?,?,?,?)",
        e.loteId(), e.transportista(), e.asignadoPor(), LocalDateTime.ofInstant(e.asignadoEn(), ZoneOffset.UTC));
  }

  public void despachar(Envio e) {
    jdbc.update("UPDATE dbo.envios SET salida=?,placa=?,peso_carga=? WHERE lote_id=?",
        LocalDateTime.ofInstant(e.salida(), ZoneOffset.UTC), e.placa(), e.pesoCargaKg(), e.loteId());
  }

  public void agregar(Envio.Novedad n) {
    jdbc.update("INSERT INTO dbo.novedades_envio(lote_id,id,tipo,lugar,descripcion,registrada_en) VALUES (?,?,?,?,?,?)",
        n.loteId(), n.id(), n.tipo().name(), n.lugar(), n.descripcion(), LocalDateTime.ofInstant(n.registradaEn(), ZoneOffset.UTC));
  }

  public void guardar(Lote l) {
    jdbc.update("""
        INSERT INTO dbo.lotes (id, parcela_id, fecha_cosecha, variedad, tipo_recoleccion, peso_kg)
        VALUES (?, ?, ?, ?, ?, ?)""", l.id(), l.parcelaId(), l.fechaCosecha(),
        l.variedad().name(), l.tipoRecoleccion().name(), l.peso().kilogramos());
  }

  public void guardar(EvaluacionCalidad ficha) {
    var a = ficha.atributos();
    jdbc.update("""
        INSERT INTO dbo.evaluaciones_calidad
        (id, lote_id, defectos_primarios, defectos_secundarios, puntaje_sca, registrado_en)
        VALUES (?, ?, ?, ?, ?, ?)""", ficha.id(), ficha.loteId(), ficha.defectosPrimarios(),
        ficha.defectosSecundarios(), ficha.puntajeSca(), LocalDateTime.ofInstant(ficha.registradaEn(), ZoneOffset.UTC));
    jdbc.update("""
        INSERT INTO dbo.fichas_cata
        (evaluacion_id, catador_id, catador_nombre, fragancia, sabor, acidez, cuerpo, posgusto,
         balance, uniformidad, dulzor, taza_limpia, apreciacion_global, tazas_leves, tazas_graves,
         protocolo, umbral_especialidad) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        ficha.id(), ficha.catadorId(), ficha.catadorNombre(), a.fragancia(), a.sabor(), a.acidez(),
        a.cuerpo(), a.posgusto(), a.balance(), a.uniformidad(), a.dulzor(), a.tazaLimpia(),
        a.apreciacionGlobal(), ficha.defectosTaza().leves(), ficha.defectosTaza().graves(),
        ficha.protocolo(), ficha.umbralEspecialidad());
  }

  public void guardar(EvaluacionRiesgo e) {
    jdbc.update("""
        INSERT INTO dbo.evaluaciones_riesgo
        (id, lote_id, humedad, horas_demora, probabilidad, modelo, simulada, genera_alerta)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)""", e.id(), e.loteId(), e.humedad(), e.horasDemora(),
        e.probabilidad().valor(), e.modelo(), e.simulada(), e.generaAlerta());
  }

  public void guardar(Alerta a) {
    jdbc.update("INSERT INTO dbo.alertas (id,lote_id,riesgo_id,estado) VALUES (?,?,?,?)",
        a.id(), a.loteId(), a.evaluacionRiesgoId(), a.estado());
  }

  public void actualizarPendiente(Alerta a) {
    int filas = jdbc.update("""
        UPDATE dbo.alertas SET estado=?,motivo=?,instruccion=?,responsable=?,revisada_en=?,operacion_id=?
        WHERE id=? AND estado='PENDIENTE'""", a.estado(), a.motivo(), a.instruccion(), a.responsable(),
        java.sql.Timestamp.from(a.revisadaEn()), a.operacionId(), a.id());
    if (filas != 1) throw new ConflictoException("La alerta ya fue revisada");
  }

  public void agregar(EventoDominio e) {
    jdbc.update("""
        INSERT INTO dbo.outbox_eventos (id,tipo,agregado_id,ocurrido_en,version,evaluacion_id)
        VALUES (?,?,?,?,?,?)""", e.id(), e.tipo(), e.agregadoId(), LocalDateTime.ofInstant(e.ocurridoEn(), ZoneOffset.UTC),
        e.version(), e instanceof EvaluacionCalidadRegistrada calidad ? calidad.evaluacionId() : null);
  }
}
