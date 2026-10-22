package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.cafejunin.adapters.out.persistence.MemoryStore;
import pe.cafejunin.adapters.out.prediction.SimuladorRiesgoAdapter;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.application.usecases.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.*;

class CasosDeUsoTest {
  final MemoryStore db = new MemoryStore();
  final Clock clock = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC);

  Lote lote() {
    return new Lote(
        UUID.randomUUID(),
        MemoryStore.PARCELA_DEMO,
        LocalDate.of(2026, 10, 1),
        Lote.Variedad.BOURBON,
        Lote.TipoRecoleccion.SELECTIVA,
        new Peso(new BigDecimal("120.5")));
  }

  RegistrarCosechaService cosecha() {
    return new RegistrarCosechaService(db, db, db, clock);
  }

  @Test
  void registraYReintentaSinDuplicarLoteNiEvento() {
    Lote l = lote();
    assertEquals(l, cosecha().ejecutar(l));
    cosecha().ejecutar(l);
    assertEquals(1, db.listarLotes().size());
    assertEquals(1, db.eventosPendientes().size());
  }

  @Test
  void rechazaConflictoDeSincronizacion() {
    Lote l = lote();
    cosecha().ejecutar(l);
    Lote distinto =
        new Lote(
            l.id(),
            l.parcelaId(),
            l.fechaCosecha(),
            l.variedad(),
            l.tipoRecoleccion(),
            new Peso(BigDecimal.ONE));
    assertThrows(ConflictoException.class, () -> cosecha().ejecutar(distinto));
  }

  @Test
  void noRegistraSinParcela() {
    Lote l = lote();
    var sinParcela =
        new Lote(
            l.id(),
            UUID.randomUUID(),
            l.fechaCosecha(),
            l.variedad(),
            l.tipoRecoleccion(),
            l.peso());
    assertThrows(NoEncontradoException.class, () -> cosecha().ejecutar(sinParcela));
    assertTrue(db.listarLotes().isEmpty());
  }

  @Test
  void rechazaFechaFuturaYPesoCero() {
    Lote l = lote();
    var futuro =
        new Lote(
            l.id(),
            l.parcelaId(),
            LocalDate.of(2030, 1, 1),
            l.variedad(),
            l.tipoRecoleccion(),
            l.peso());
    assertThrows(IllegalArgumentException.class, () -> cosecha().ejecutar(futuro));
    assertThrows(IllegalArgumentException.class, () -> new Peso(BigDecimal.ZERO));
  }

  @Test
  void calidadExigeLoteExistente() {
    var uc = new RegistrarCalidadService(db, db, db, db, clock);
    assertThrows(
        NoEncontradoException.class,
        () -> uc.ejecutar(new EvaluacionCalidad(UUID.randomUUID(), UUID.randomUUID(), 0, 1, 85)));
    Lote l = cosecha().ejecutar(lote());
    assertEquals(
        85, uc.ejecutar(new EvaluacionCalidad(UUID.randomUUID(), l.id(), 0, 1, 85)).puntajeSca());
  }

  @Test
  void setentaExactoNoGeneraAlertaYMayorSi() {
    Lote l = cosecha().ejecutar(lote());
    PrediccionRiesgoPort exacto =
        (h, d) -> new PrediccionRiesgoPort.Resultado(new Probabilidad(0.70), "TEST", true);
    var uc = new EvaluarRiesgoService(db, db, exacto, db, db, clock);
    assertFalse(uc.ejecutar(l.id(), 12, 0).generaAlerta());
    assertTrue(db.listarAlertas().isEmpty());
    PrediccionRiesgoPort alto =
        (h, d) -> new PrediccionRiesgoPort.Resultado(new Probabilidad(0.7001), "TEST", true);
    assertTrue(
        new EvaluarRiesgoService(db, db, alto, db, db, clock)
            .ejecutar(l.id(), 12, 0)
            .generaAlerta());
    assertEquals(1, db.listarAlertas().size());
  }

  @Test
  void falloDelEventoRevierteEscritura() {
    EventoRepositoryPort falla =
        e -> {
          throw new IllegalStateException("fallo simulado");
        };
    assertThrows(
        IllegalStateException.class,
        () -> new RegistrarCosechaService(db, falla, db, clock).ejecutar(lote()));
    assertTrue(db.listarLotes().isEmpty());
  }

  @Test
  void simuladorSeIdentificaYNoAceptaNaN() {
    assertTrue(new SimuladorRiesgoAdapter().predecir(20, 10).simulada());
    assertThrows(IllegalArgumentException.class, () -> new Probabilidad(Double.NaN));
  }
}
