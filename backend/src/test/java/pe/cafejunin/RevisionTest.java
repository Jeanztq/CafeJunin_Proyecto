package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import pe.cafejunin.adapters.out.persistence.MemoryStore;
import pe.cafejunin.adapters.out.prediction.*;
import pe.cafejunin.application.usecases.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.*;

class RevisionTest {
  final MemoryStore db = new MemoryStore();
  final Clock clock = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC);

  Alerta alerta() {
    var a = new Alerta(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "PENDIENTE");
    db.guardar(a);
    return a;
  }

  @Test
  void confirmaUnaVezConResponsableYEvento() {
    var a = alerta();
    var op = UUID.randomUUID();
    var uc = new RevisarAlertaService(db, db, db, clock);
    var r =
        uc.ejecutar(
            a.id(), op, "CONFIRMADA", "Riesgo verificado", "Priorizar traslado", "supervisor");
    assertEquals("supervisor", r.responsable());
    assertEquals(clock.instant(), r.revisadaEn());
    assertEquals(
        r,
        uc.ejecutar(
            a.id(), op, "CONFIRMADA", "Riesgo verificado", "Priorizar traslado", "supervisor"));
    assertEquals(1, db.eventosPendientes().size());
    assertThrows(
        ConflictoException.class,
        () ->
            uc.ejecutar(
                a.id(), UUID.randomUUID(), "DESCARTADA", "Dato corregido", null, "supervisor"));
  }

  @Test
  void descartaSinInstruccionYExigeMotivo() {
    var a = alerta();
    var uc = new RevisarAlertaService(db, db, db, clock);
    assertThrows(
        IllegalArgumentException.class,
        () -> uc.ejecutar(a.id(), UUID.randomUUID(), "DESCARTADA", "no", null, "s"));
    var r = uc.ejecutar(a.id(), UUID.randomUUID(), "DESCARTADA", "Sensor sin calibrar", null, "s");
    assertEquals("DESCARTADA", r.estado());
    assertEquals("", r.instruccion());
  }

  @Test
  void fallaEventoRevierteDecision() {
    var a = alerta();
    var uc =
        new RevisarAlertaService(
            db,
            e -> {
              throw new IllegalStateException();
            },
            db,
            clock);
    assertThrows(
        IllegalStateException.class,
        () ->
            uc.ejecutar(
                a.id(),
                UUID.randomUUID(),
                "CONFIRMADA",
                "Revisión correcta",
                "Trasladar ahora",
                "s"));
    assertEquals("PENDIENTE", db.buscarAlerta(a.id()).orElseThrow().estado());
  }

  @Test
  void decoratorMideSinReintentar() {
    var d =
        new MedicionPrediccionDecorator(
            (h, t) -> {
              throw new IllegalStateException();
            });
    assertThrows(IllegalStateException.class, () -> d.predecir(80, 20));
    assertEquals(1L, d.metricas().get("llamadas"));
    assertEquals(1L, d.metricas().get("fallos"));
  }
}
