package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.adapters.out.persistence.SqlServerStore;
import pe.cafejunin.application.usecases.*;
import pe.cafejunin.domain.builder.LoteBuilder;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.Probabilidad;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_TESTS", matches = "true")
class SqlServerIntegrationTest {
  @Test
  void procedimientosYOutboxParticipanEnMismaTransaccion() {
    var ds =
        new DriverManagerDataSource(
            System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
    var jdbc = new JdbcTemplate(ds);
    var tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
    var store = new SqlServerStore(jdbc, tx);
    var clock = Clock.systemUTC();
    var id = UUID.randomUUID();
    tx.execute(
        s -> {
          var l =
              new LoteBuilder()
                  .identificado(id)
                  .deParcela(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                  .cosechado(LocalDate.now().minusDays(1))
                  .variedad(Lote.Variedad.BOURBON)
                  .recoleccion(Lote.TipoRecoleccion.SELECTIVA)
                  .peso(BigDecimal.TEN)
                  .build();
          new RegistrarCosechaService(store, store, store, clock).ejecutar(l);
          new RegistrarCalidadService(store, store, store, store, clock)
              .ejecutar(new EvaluacionCalidad(UUID.randomUUID(), id, 0, 1, 85));
          new EvaluarRiesgoService(
                  store,
                  store,
                  (h, d) ->
                      new pe.cafejunin.application.ports.output.PrediccionRiesgoPort.Resultado(
                          new Probabilidad(.9), "TEST", true),
                  store,
                  store,
                  clock)
              .ejecutar(id, 90, 24);
          var a =
              store.listarAlertas().stream()
                  .filter(x -> x.loteId().equals(id))
                  .findFirst()
                  .orElseThrow();
          new RevisarAlertaService(store, store, store, clock)
              .ejecutar(
                  a.id(),
                  UUID.randomUUID(),
                  "CONFIRMADA",
                  "Riesgo probado",
                  "Traslado urgente",
                  "test");
          assertEquals("CONFIRMADA", store.buscarAlerta(a.id()).orElseThrow().estado());
          assertEquals(1, store.calidadDe(id).size());
          assertEquals(
              5,
              jdbc.queryForObject(
                  "SELECT COUNT(*) FROM dbo.outbox_eventos WHERE agregado_id=?",
                  Integer.class,
                  id.toString()));
          s.setRollbackOnly();
          return null;
        });
    assertTrue(store.buscarLote(id).isEmpty());
  }
}
