package pe.cafejunin.adapters.out.persistence;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import pe.cafejunin.domain.events.EvaluacionCalidadRegistrada;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.application.usecases.ConflictoException;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.EventoDominio;

public final class SqlServerStore extends JdbcStore {
  public SqlServerStore(JdbcTemplate jdbc, TransactionTemplate tx) {
    super(jdbc, tx, ZoneOffset.UTC);
  }

  @Override
  protected String bloqueoRegistro() {
    return "WITH (UPDLOCK,HOLDLOCK) ";
  }

  private static Object arg(Object o) {
    return o instanceof UUID ? o.toString() : o;
  }

  private void call(String name, Object... args) {
    String q =
        "{call dbo." + name + "(" + String.join(",", Collections.nCopies(args.length, "?")) + ")}";
    jdbc.update(q, Arrays.stream(args).map(SqlServerStore::arg).toArray());
  }

  public void guardar(Lote l) {
    call(
        "sp_registrar_lote",
        l.id(),
        l.parcelaId(),
        java.sql.Date.valueOf(l.fechaCosecha()),
        l.variedad().name(),
        l.tipoRecoleccion().name(),
        l.peso().kilogramos());
  }

  public void guardar(EvaluacionCalidad ficha) {
    var atributos = ficha.atributos();
    call("sp_registrar_calidad", ficha.id(), ficha.loteId(),
        ficha.defectosPrimarios(), ficha.defectosSecundarios(), ficha.puntajeSca(),
        ficha.catadorId(), ficha.catadorNombre(), ficha.registradaEn().atOffset(ZoneOffset.UTC).toLocalDateTime(),
        atributos.fragancia(), atributos.sabor(), atributos.acidez(), atributos.cuerpo(),
        atributos.posgusto(), atributos.balance(), atributos.uniformidad(), atributos.dulzor(),
        atributos.tazaLimpia(), atributos.apreciacionGlobal(), ficha.defectosTaza().leves(),
        ficha.defectosTaza().graves(), ficha.protocolo(), ficha.umbralEspecialidad());
  }

  public void guardar(EvaluacionRiesgo e) {
    call(
        "sp_registrar_riesgo",
        e.id(),
        e.loteId(),
        e.humedad(),
        e.horasDemora(),
        e.probabilidad().valor(),
        e.modelo(),
        e.simulada(),
        e.generaAlerta());
  }

  public void guardar(Alerta a) {
    call("sp_registrar_alerta", a.id(), a.loteId(), a.evaluacionRiesgoId(), a.estado());
  }

  public void actualizarPendiente(Alerta a) {
    try {
      call(
          "sp_revisar_alerta",
          a.id(),
          a.estado(),
          a.motivo(),
          a.instruccion(),
          a.responsable(),
          Timestamp.from(a.revisadaEn()),
          a.operacionId());
    } catch (org.springframework.dao.DataAccessException e) {
      Throwable c = e;
      while (c != null) {
        if (c instanceof SQLException s && s.getErrorCode() == 51001)
          throw new ConflictoException("La alerta ya fue revisada");
        c = c.getCause();
      }
      throw e;
    }
  }

  public void asignar(Envio e) {
    call("sp_asignar_envio", e.loteId(), e.transportista(), e.asignadoPor(), LocalDateTime.ofInstant(e.asignadoEn(), ZoneOffset.UTC));
  }

  public void despachar(Envio e) {
    call("sp_despachar_envio", e.loteId(), LocalDateTime.ofInstant(e.salida(), ZoneOffset.UTC), e.placa(), e.pesoCargaKg());
  }

  public void agregar(Envio.Novedad n) {
    call("sp_registrar_novedad_envio", n.loteId(), n.id(), n.tipo().name(), n.lugar(), n.descripcion(),
        LocalDateTime.ofInstant(n.registradaEn(), ZoneOffset.UTC));
  }

  public void agregar(EventoDominio e) {
    call(
        "sp_encolar_evento",
        e.id(),
        e.tipo(),
        e.agregadoId(),
        LocalDateTime.ofInstant(e.ocurridoEn(), ZoneOffset.UTC),
        e.version(),
        e instanceof EvaluacionCalidadRegistrada calidad ? calidad.evaluacionId() : null);
  }

}
