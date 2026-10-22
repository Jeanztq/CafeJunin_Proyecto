package pe.cafejunin.application.usecases;

import java.time.Clock;
import pe.cafejunin.application.ports.input.RegistrarCalidadUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.EvaluacionCalidad;
import pe.cafejunin.domain.factory.EventoFactory;

public final class RegistrarCalidadService implements RegistrarCalidadUseCase {
  private final LoteRepositoryPort lotes;
  private final CalidadRepositoryPort calidad;
  private final EventoRepositoryPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock reloj;

  public RegistrarCalidadService(
      LoteRepositoryPort lotes,
      CalidadRepositoryPort calidad,
      EventoRepositoryPort eventos,
      UnidadTrabajoPort tx,
      Clock reloj) {
    this.lotes = lotes;
    this.calidad = calidad;
    this.eventos = eventos;
    this.tx = tx;
    this.reloj = reloj;
  }

  public EvaluacionCalidad ejecutar(EvaluacionCalidad evaluacion) {
    return tx.ejecutar(
        () -> {
          if (lotes.buscarLote(evaluacion.loteId()).isEmpty())
            throw new NoEncontradoException("Lote no encontrado");
          calidad.guardar(evaluacion);
          eventos.agregar(new EventoFactory.Calidad().crear(evaluacion.loteId(), reloj.instant()));
          return evaluacion;
        });
  }
}
