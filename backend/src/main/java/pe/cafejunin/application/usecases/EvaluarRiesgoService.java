package pe.cafejunin.application.usecases;

import java.time.Clock;
import java.util.UUID;
import pe.cafejunin.application.ports.input.EvaluarRiesgoUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.factory.EventoFactory;

public final class EvaluarRiesgoService implements EvaluarRiesgoUseCase {
  private final LoteRepositoryPort lotes;
  private final RiesgoRepositoryPort riesgos;
  private final PrediccionRiesgoPort predictor;
  private final EventoRepositoryPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock reloj;

  public EvaluarRiesgoService(
      LoteRepositoryPort lotes,
      RiesgoRepositoryPort riesgos,
      PrediccionRiesgoPort predictor,
      EventoRepositoryPort eventos,
      UnidadTrabajoPort tx,
      Clock reloj) {
    this.lotes = lotes;
    this.riesgos = riesgos;
    this.predictor = predictor;
    this.eventos = eventos;
    this.tx = tx;
    this.reloj = reloj;
  }

  public EvaluacionRiesgo ejecutar(UUID loteId, double humedad, double horasDemora) {
    if (!Double.isFinite(humedad)
        || humedad < 0
        || humedad > 100
        || !Double.isFinite(horasDemora)
        || horasDemora < 0) throw new IllegalArgumentException("Humedad o demora inválida");
    if (lotes.buscarLote(loteId).isEmpty()) throw new NoEncontradoException("Lote no encontrado");
    var resultado = predictor.predecir(humedad, horasDemora);
    var evaluacion =
        new EvaluacionRiesgo(
            UUID.randomUUID(),
            loteId,
            humedad,
            horasDemora,
            resultado.probabilidad(),
            resultado.modelo(),
            resultado.simulada());
    return tx.ejecutar(
        () -> {
          riesgos.guardar(evaluacion);
          eventos.agregar(new EventoFactory.Riesgo().crear(loteId, reloj.instant()));
          if (evaluacion.generaAlerta()) {
            riesgos.guardar(new Alerta(UUID.randomUUID(), loteId, evaluacion.id(), "PENDIENTE"));
            eventos.agregar(new EventoFactory.RiesgoAlto().crear(loteId, reloj.instant()));
          }
          return evaluacion;
        });
  }
}
