package pe.cafejunin.adapters.out.persistence;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.application.usecases.ConflictoException;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.EventoDominio;
import pe.cafejunin.domain.valueobjects.*;

public final class SqlServerStore
    implements LoteRepositoryPort,
        CalidadRepositoryPort,
        RiesgoRepositoryPort,
        EventoRepositoryPort,
        UnidadTrabajoPort {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  public SqlServerStore(JdbcTemplate j, TransactionTemplate t) {
    jdbc = j;
    tx = t;
  }

  private static UUID id(ResultSet r, String c) throws SQLException {
    String s = r.getString(c);
    return s == null ? null : UUID.fromString(s);
  }

  private static Object arg(Object o) {
    return o instanceof UUID ? o.toString() : o;
  }

  private void call(String name, Object... args) {
    String q =
        "{call dbo." + name + "(" + String.join(",", Collections.nCopies(args.length, "?")) + ")}";
    jdbc.update(q, Arrays.stream(args).map(SqlServerStore::arg).toArray());
  }

  private Parcela parcela(ResultSet r, int n) throws SQLException {
    return new Parcela(
        id(r, "id"),
        r.getString("nombre"),
        r.getString("ubicacion"),
        r.getString("propietario_subject"));
  }

  public boolean existeParcela(UUID id) {
    return buscarParcela(id).isPresent();
  }

  public Optional<Parcela> buscarParcela(UUID id) {
    return jdbc
        .query("SELECT * FROM dbo.parcelas WHERE id=?", this::parcela, id.toString())
        .stream()
        .findFirst();
  }

  public List<Parcela> listarParcelas() {
    return jdbc.query("SELECT * FROM dbo.parcelas ORDER BY nombre", this::parcela);
  }

  private Lote lote(ResultSet r, int n) throws SQLException {
    return new Lote(
        id(r, "id"),
        id(r, "parcela_id"),
        r.getDate("fecha_cosecha").toLocalDate(),
        Lote.Variedad.valueOf(r.getString("variedad")),
        Lote.TipoRecoleccion.valueOf(r.getString("tipo_recoleccion")),
        new Peso(r.getBigDecimal("peso_kg")));
  }

  public Optional<Lote> buscarLote(UUID id) {
    return jdbc.query("SELECT * FROM dbo.lotes WHERE id=?", this::lote, id.toString()).stream()
        .findFirst();
  }

  public List<Lote> listarLotes() {
    return jdbc.query("SELECT * FROM dbo.lotes ORDER BY registrado_en,id", this::lote);
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

  public void guardar(EvaluacionCalidad e) {
    call(
        "sp_registrar_calidad",
        e.id(),
        e.loteId(),
        e.defectosPrimarios(),
        e.defectosSecundarios(),
        e.puntajeSca());
  }

  public List<EvaluacionCalidad> calidadDe(UUID id) {
    return jdbc.query(
        "SELECT * FROM dbo.evaluaciones_calidad WHERE lote_id=? ORDER BY registrado_en,id",
        (r, n) ->
            new EvaluacionCalidad(
                id(r, "id"),
                id(r, "lote_id"),
                r.getInt("defectos_primarios"),
                r.getInt("defectos_secundarios"),
                r.getDouble("puntaje_sca")),
        id.toString());
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

  public List<EvaluacionRiesgo> riesgosDe(UUID id) {
    return jdbc.query(
        "SELECT * FROM dbo.evaluaciones_riesgo WHERE lote_id=? ORDER BY registrado_en,id",
        (r, n) ->
            new EvaluacionRiesgo(
                id(r, "id"),
                id(r, "lote_id"),
                r.getDouble("humedad"),
                r.getDouble("horas_demora"),
                new Probabilidad(r.getDouble("probabilidad")),
                r.getString("modelo"),
                r.getBoolean("simulada")),
        id.toString());
  }

  public void guardar(Alerta a) {
    call("sp_registrar_alerta", a.id(), a.loteId(), a.evaluacionRiesgoId(), a.estado());
  }

  private Alerta alerta(ResultSet r, int n) throws SQLException {
    Timestamp t = r.getTimestamp("revisada_en");
    return new Alerta(
        id(r, "id"),
        id(r, "lote_id"),
        id(r, "riesgo_id"),
        r.getString("estado"),
        r.getString("motivo"),
        r.getString("instruccion"),
        r.getString("responsable"),
        t == null ? null : t.toInstant(),
        id(r, "operacion_id"));
  }

  public List<Alerta> listarAlertas() {
    return jdbc.query("SELECT * FROM dbo.alertas ORDER BY registrada_en,id", this::alerta);
  }

  public Optional<Alerta> buscarAlerta(UUID id) {
    return jdbc.query("SELECT * FROM dbo.alertas WHERE id=?", this::alerta, id.toString()).stream()
        .findFirst();
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

  public void agregar(EventoDominio e) {
    call(
        "sp_encolar_evento",
        e.id(),
        e.tipo(),
        e.agregadoId(),
        Timestamp.from(e.ocurridoEn()),
        e.version());
  }

  public <T> T ejecutar(Supplier<T> a) {
    return tx.execute(s -> a.get());
  }
}
