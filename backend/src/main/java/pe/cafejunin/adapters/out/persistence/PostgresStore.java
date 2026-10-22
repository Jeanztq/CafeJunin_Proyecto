package pe.cafejunin.adapters.out.persistence;

import java.util.*;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.EventoDominio;
import pe.cafejunin.domain.valueobjects.*;

// SQL de escritura en procedimientos versionados; SELECT sólo en este adaptador.
public final class PostgresStore
    implements LoteRepositoryPort,
        CalidadRepositoryPort,
        RiesgoRepositoryPort,
        EventoRepositoryPort,
        UnidadTrabajoPort {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  public PostgresStore(JdbcTemplate jdbc, TransactionTemplate tx) {
    this.jdbc = jdbc;
    this.tx = tx;
  }

  public boolean existeParcela(UUID id) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM parcelas WHERE id=?)", Boolean.class, id));
  }

  private Lote mapear(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
    return new Lote(
        rs.getObject("id", UUID.class),
        rs.getObject("parcela_id", UUID.class),
        rs.getDate("fecha_cosecha").toLocalDate(),
        Lote.Variedad.valueOf(rs.getString("variedad")),
        Lote.TipoRecoleccion.valueOf(rs.getString("tipo_recoleccion")),
        new Peso(rs.getBigDecimal("peso_kg")));
  }

  public Optional<Lote> buscarLote(UUID id) {
    return jdbc.query("SELECT * FROM lotes WHERE id=?", this::mapear, id).stream().findFirst();
  }

  public List<Lote> listarLotes() {
    return jdbc.query("SELECT * FROM lotes ORDER BY registrado_en,id", this::mapear);
  }

  public void guardar(Lote l) {
    jdbc.update(
        "CALL sp_registrar_lote(?,?,?,?,?,?)",
        l.id(),
        l.parcelaId(),
        java.sql.Date.valueOf(l.fechaCosecha()),
        l.variedad().name(),
        l.tipoRecoleccion().name(),
        l.peso().kilogramos());
  }

  public void guardar(EvaluacionCalidad e) {
    jdbc.update(
        "CALL sp_registrar_calidad(?,?,?,?,?)",
        e.id(),
        e.loteId(),
        e.defectosPrimarios(),
        e.defectosSecundarios(),
        e.puntajeSca());
  }

  public void guardar(EvaluacionRiesgo e) {
    jdbc.update(
        "CALL sp_registrar_riesgo(?,?,?,?,?,?,?,?)",
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
    jdbc.update(
        "CALL sp_registrar_alerta(?,?,?,?)",
        a.id(),
        a.loteId(),
        a.evaluacionRiesgoId(),
        a.estado());
  }

  public List<Alerta> listarAlertas() {
    return jdbc.query(
        "SELECT * FROM alertas ORDER BY registrada_en,id",
        (rs, n) ->
            new Alerta(
                rs.getObject("id", UUID.class),
                rs.getObject("lote_id", UUID.class),
                rs.getObject("riesgo_id", UUID.class),
                rs.getString("estado")));
  }

  public void agregar(EventoDominio e) {
    jdbc.update(
        "CALL sp_encolar_evento(?,?,?,?,?)",
        e.id(),
        e.tipo(),
        e.agregadoId(),
        java.sql.Timestamp.from(e.ocurridoEn()),
        e.version());
  }

  public <T> T ejecutar(Supplier<T> accion) {
    return tx.execute(status -> accion.get());
  }
}
