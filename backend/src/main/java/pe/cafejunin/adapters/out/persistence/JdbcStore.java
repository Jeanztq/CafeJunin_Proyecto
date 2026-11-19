package pe.cafejunin.adapters.out.persistence;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.*;

public abstract class JdbcStore
    implements LoteRepositoryPort,
        CalidadRepositoryPort,
        RiesgoRepositoryPort,
        EventoRepositoryPort,
        HistorialLotePort,
        UnidadTrabajoPort {
  protected final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  private final ZoneId zonaRegistro;

  protected JdbcStore(JdbcTemplate j, TransactionTemplate t, ZoneId zonaRegistro) {
    this.zonaRegistro = zonaRegistro;
    jdbc = j;
    tx = t;
  }

  private static UUID id(ResultSet r, String c) throws SQLException {
    String s = r.getString(c);
    return s == null ? null : UUID.fromString(s);
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

  private static final String COLUMNAS_CALIDAD =
      "SELECT e.*, f.catador_id, f.catador_nombre, f.fragancia, f.sabor, f.acidez, f.cuerpo, "
          + "f.posgusto, f.balance, f.uniformidad, f.dulzor, f.taza_limpia, f.apreciacion_global, "
          + "f.protocolo, f.umbral_especialidad, f.tazas_leves, f.tazas_graves FROM dbo.evaluaciones_calidad e ";
  private static final String FICHA_DEL_LOTE =
      "LEFT JOIN dbo.fichas_cata f ON f.evaluacion_id=e.id ";

  private EvaluacionCalidad ficha(ResultSet r, int fila) throws SQLException {
    String catador = r.getString("catador_id");
    var atributos = catador == null ? null : new AtributosSensoriales(
        r.getDouble("fragancia"), r.getDouble("sabor"), r.getDouble("acidez"), r.getDouble("cuerpo"),
        r.getDouble("posgusto"), r.getDouble("balance"), r.getDouble("uniformidad"), r.getDouble("dulzor"),
        (Double) r.getObject("taza_limpia"), (Double) r.getObject("apreciacion_global"));
    String protocolo = r.getString("protocolo");
    var defectos = protocolo == null ? null : new DefectosTaza(r.getInt("tazas_leves"), r.getInt("tazas_graves"));
    return new EvaluacionCalidad(id(r, "id"), id(r, "lote_id"),
        r.getInt("defectos_primarios"), r.getInt("defectos_secundarios"), r.getDouble("puntaje_sca"),
        atributos, catador, r.getString("catador_nombre"),
        r.getObject("registrado_en", LocalDateTime.class).toInstant(ZoneOffset.UTC),
        protocolo == null ? EvaluacionCalidad.MANUAL : protocolo,
        protocolo == null ? 80 : r.getDouble("umbral_especialidad"), defectos);
  }

  protected abstract String bloqueoRegistro();

  public Optional<EvaluacionCalidad> buscarParaRegistro(UUID id) {
    return jdbc.query(COLUMNAS_CALIDAD + bloqueoRegistro() + FICHA_DEL_LOTE
        + "WHERE e.id=?", this::ficha, id.toString()).stream().findFirst();
  }

  public List<EvaluacionCalidad> calidadDe(UUID id) {
    return jdbc.query(COLUMNAS_CALIDAD + FICHA_DEL_LOTE + "WHERE e.lote_id=? ORDER BY e.registrado_en,e.id",
        this::ficha, id.toString());
  }

  private EvaluacionRiesgo riesgo(ResultSet r, int fila) throws SQLException {
    return new EvaluacionRiesgo(id(r, "id"), id(r, "lote_id"), r.getDouble("humedad"),
        r.getDouble("horas_demora"), new Probabilidad(r.getDouble("probabilidad")),
        r.getString("modelo"), r.getBoolean("simulada"));
  }

  public Optional<EvaluacionRiesgo> buscarRiesgo(UUID id) {
    return jdbc.query("SELECT * FROM dbo.evaluaciones_riesgo WHERE id=?", this::riesgo,
        id.toString()).stream().findFirst();
  }

  public List<EvaluacionRiesgo> riesgosDe(UUID id) {
    return jdbc.query("SELECT * FROM dbo.evaluaciones_riesgo WHERE lote_id=? ORDER BY registrado_en,id",
        this::riesgo, id.toString());
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
        id(r, "operacion_id"),
        r.getObject("registrada_en", LocalDateTime.class).atZone(zonaRegistro).toInstant());
  }

  public List<Alerta> listarAlertas() {
    return jdbc.query("SELECT * FROM dbo.alertas ORDER BY registrada_en,id", this::alerta);
  }

  public Optional<Alerta> buscarAlerta(UUID id) {
    return jdbc.query("SELECT * FROM dbo.alertas WHERE id=?", this::alerta, id.toString()).stream()
        .findFirst();
  }

  public List<Hito> historialDe(UUID loteId) {
    return jdbc.query("""
        SELECT id,tipo,ocurrido_en FROM dbo.outbox_eventos
        WHERE agregado_id=? ORDER BY ocurrido_en,id""",
        (r, n) -> new Hito(id(r, "id"), r.getString("tipo"),
            r.getObject("ocurrido_en", LocalDateTime.class).toInstant(ZoneOffset.UTC)),
        loteId.toString());
  }

  public <T> T ejecutar(Supplier<T> a) {
    return tx.execute(s -> a.get());
  }
}
