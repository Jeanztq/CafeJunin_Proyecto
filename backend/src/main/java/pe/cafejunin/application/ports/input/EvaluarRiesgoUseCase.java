package pe.cafejunin.application.ports.input;

import java.util.UUID;
import pe.cafejunin.domain.entities.EvaluacionRiesgo;

public interface EvaluarRiesgoUseCase {
  EvaluacionRiesgo ejecutar(UUID loteId, double humedad, double horasDemora);
}
