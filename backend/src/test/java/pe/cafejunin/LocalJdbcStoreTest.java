package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cafejunin.adapters.out.persistence.LocalJdbcStore;
import pe.cafejunin.application.ports.input.RegistrarCalidadUseCase.Solicitud;
import pe.cafejunin.application.usecases.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.*;

class LocalJdbcStoreTest {
  @TempDir Path directorio;
  final Clock reloj = Clock.systemUTC();

  LocalJdbcStore abrir() {
    var ds = new DriverManagerDataSource("jdbc:h2:file:" + directorio.resolve("cafe"), "sa", "");
    new ResourceDatabasePopulator(new ClassPathResource("db/local.sql")).execute(ds);
    return new LocalJdbcStore(new JdbcTemplate(ds), new TransactionTemplate(new DataSourceTransactionManager(ds)));
  }

  @Test
  void recuperaFichaYEventosAlReabrirYRevierteUnFalloDelConsumidor() {
    var db = abrir();
    var lote = new Lote(UUID.randomUUID(), db.listarParcelas().get(0).id(), LocalDate.now(reloj),
        Lote.Variedad.BOURBON, Lote.TipoRecoleccion.SELECTIVA, new Peso(BigDecimal.TEN));
    new RegistrarCosechaService(db, db, db, reloj).ejecutar(lote);
    var solicitud = new Solicitud(UUID.randomUUID(), lote.id(), 0, 2, new DefectosTaza(1, 0),
        new AtributosSensoriales(8d, 8.5, 8d, 8.5, 8d, 8d, 10d, 10d, 10d, 8d));
    var cuenta = new Cuenta("q1", "Ana", Set.of("CATADOR"), false);
    var falla = new RegistrarCalidadService(db, db, e -> { db.agregar(e); throw new IllegalStateException(); },
        db, reloj, 80);
    assertThrows(IllegalStateException.class, () -> falla.ejecutar(solicitud, cuenta));
    assertTrue(db.calidadDe(lote.id()).isEmpty());
    assertEquals(1, db.historialDe(lote.id()).size());

    var original = new RegistrarCalidadService(db, db, db::agregar, db, reloj, 80).ejecutar(solicitud, cuenta);
    var reabierto = abrir();
    assertEquals(85, original.puntajeSca());
    assertEquals(List.of(original), reabierto.calidadDe(lote.id()));
    var reintento = new RegistrarCalidadService(reabierto, reabierto, reabierto::agregar, reabierto, reloj, 90);
    assertEquals(original, reintento.ejecutar(solicitud, cuenta));
    assertEquals(2, reabierto.historialDe(lote.id()).size());
  }
}
