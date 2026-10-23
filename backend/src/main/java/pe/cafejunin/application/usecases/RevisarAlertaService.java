package pe.cafejunin.application.usecases;

import java.time.Clock;
import java.util.*;
import pe.cafejunin.application.ports.input.RevisarAlertaUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.Alerta;
import pe.cafejunin.domain.factory.EventoFactory;

public final class RevisarAlertaService implements RevisarAlertaUseCase {
  private final RiesgoRepositoryPort repo;
  private final EventoRepositoryPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock clock;

  public RevisarAlertaService(
      RiesgoRepositoryPort r, EventoRepositoryPort e, UnidadTrabajoPort t, Clock c) {
    repo = r;
    eventos = e;
    tx = t;
    clock = c;
  }

  public Alerta ejecutar(
      UUID id,
      UUID operacion,
      String decision,
      String motivo,
      String instruccion,
      String responsable) {
    return tx.ejecutar(
        () -> {
          var anterior =
              repo.buscarAlerta(id)
                  .orElseThrow(() -> new NoEncontradoException("Alerta no encontrada"));
          String inst = instruccion == null ? "" : instruccion.trim();
          String razon = motivo == null ? "" : motivo.trim();
          if (!anterior.estado().equals("PENDIENTE")) {
            if (Objects.equals(anterior.operacionId(), operacion)
                && anterior.estado().equals(decision)
                && Objects.equals(anterior.motivo(), razon)
                && Objects.equals(anterior.instruccion(), inst)
                && Objects.equals(anterior.responsable(), responsable)) return anterior;
            throw new ConflictoException("La alerta ya tiene una decisión; no se sobrescribe");
          }
          var nueva =
              anterior.revisar(
                  decision, motivo, instruccion, responsable, clock.instant(), operacion);
          repo.actualizarPendiente(nueva);
          eventos.agregar(new EventoFactory.Revision().crear(nueva.loteId(), clock.instant()));
          return nueva;
        });
  }
}
