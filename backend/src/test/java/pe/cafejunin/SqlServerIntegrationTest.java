package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import pe.cafejunin.application.ports.input.RegistrarCalidadUseCase.Solicitud;
import pe.cafejunin.domain.valueobjects.AtributosSensoriales;
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
          var calidad = new RegistrarCalidadService(store, store, store::agregar, store, clock, 80);
          var catador = new Cuenta("q1", "Ana", Set.of("CATADOR"), false);
          var solicitud = new Solicitud(UUID.randomUUID(), id, 0, 1, new pe.cafejunin.domain.valueobjects.DefectosTaza(1, 0),
              new AtributosSensoriales(8d, 8.5, 8d, 8.5, 8d, 8d, 10d, 10d, 10d, 8d));
          var ficha = calidad.ejecutar(solicitud, catador);
          assertEquals(ficha, calidad.ejecutar(solicitud, catador));
          assertEquals(ficha, store.calidadDe(id).get(0));
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
          var conductor = new Cuenta("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4", "Conductor", Set.of("TRANSPORTISTA"), false);
          var supervisor = new Cuenta("supervisor", "Supervisor", Set.of("SUPERVISOR"), false);
          var seguimiento = new TransporteService(() -> supervisor, store, store, store, store, clock);
          var viaje = new TransporteService(() -> conductor, store, store, store, store, clock);
          seguimiento.asignar(id, conductor.subject());
          viaje.despachar(id, "ABC123", BigDecimal.TEN);
          var paso = UUID.randomUUID();
          viaje.registrar(id, paso, Envio.Tipo.CONTROL, "Huancayo", "");
          viaje.registrar(id, paso, Envio.Tipo.CONTROL, "Huancayo", "");
          viaje.registrar(id, UUID.randomUUID(), Envio.Tipo.DERRUMBE, "La Oroya", "Paso interrumpido");
          var trayecto = seguimiento.detalle(id);
          assertEquals("ABC-123", trayecto.envio().placa());
          assertEquals(List.of(Envio.Tipo.CONTROL, Envio.Tipo.DERRUMBE),
              trayecto.novedades().stream().map(Envio.Novedad::tipo).toList());
          assertEquals(
              9,
              jdbc.queryForObject(
                  "SELECT COUNT(*) FROM dbo.outbox_eventos WHERE agregado_id=?",
                  Integer.class,
                  id.toString()));
          s.setRollbackOnly();
          return null;
        });
    assertTrue(store.buscarLote(id).isEmpty());
    assertTrue(store.novedades(id).isEmpty());
  }
}
