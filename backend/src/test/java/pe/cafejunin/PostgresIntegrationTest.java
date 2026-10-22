package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import pe.cafejunin.application.ports.input.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.Peso;

@SpringBootTest
@ActiveProfiles("postgres")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresIntegrationTest {
  @Autowired JdbcTemplate jdbc;
  @Autowired RegistrarCosechaUseCase cosecha;
  @Autowired RegistrarCalidadUseCase calidad;
  @Autowired EvaluarRiesgoUseCase riesgo;

  @Test
  void persisteProcedimientosYOutboxEnPostgres() {
    UUID parcela = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO parcelas(id,nombre,productor_referencia) VALUES(?,?,?)",
        parcela,
        "TEST",
        "TEST");
    try {
      var l =
          new Lote(
              id,
              parcela,
              LocalDate.of(2025, 1, 1),
              Lote.Variedad.BOURBON,
              Lote.TipoRecoleccion.SELECTIVA,
              new Peso(new BigDecimal("120.50")));
      cosecha.ejecutar(l);
      cosecha.ejecutar(l);
      calidad.ejecutar(new EvaluacionCalidad(UUID.randomUUID(), id, 0, 1, 85));
      assertTrue(riesgo.ejecutar(id, 22, 12).generaAlerta());
      assertEquals(
          1, jdbc.queryForObject("SELECT COUNT(*) FROM lotes WHERE id=?", Integer.class, id));
      assertEquals(
          1,
          jdbc.queryForObject("SELECT COUNT(*) FROM alertas WHERE lote_id=?", Integer.class, id));
      assertEquals(
          4,
          jdbc.queryForObject(
              "SELECT COUNT(*) FROM outbox_eventos WHERE agregado_id=?", Integer.class, id));
    } finally {
      jdbc.update("DELETE FROM outbox_eventos WHERE agregado_id=?", id);
      jdbc.update("DELETE FROM alertas WHERE lote_id=?", id);
      jdbc.update("DELETE FROM evaluaciones_riesgo WHERE lote_id=?", id);
      jdbc.update("DELETE FROM evaluaciones_calidad WHERE lote_id=?", id);
      jdbc.update("DELETE FROM lotes WHERE id=?", id);
      jdbc.update("DELETE FROM parcelas WHERE id=?", parcela);
    }
  }
}
