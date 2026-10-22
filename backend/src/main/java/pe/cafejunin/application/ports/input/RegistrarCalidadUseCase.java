package pe.cafejunin.application.ports.input;

import pe.cafejunin.domain.entities.EvaluacionCalidad;

public interface RegistrarCalidadUseCase {
  EvaluacionCalidad ejecutar(EvaluacionCalidad evaluacion);
}
