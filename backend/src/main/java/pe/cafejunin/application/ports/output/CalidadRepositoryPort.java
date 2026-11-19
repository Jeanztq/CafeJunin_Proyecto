package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.EvaluacionCalidad;

public interface CalidadRepositoryPort {
  Optional<EvaluacionCalidad> buscarParaRegistro(UUID id);

  void guardar(EvaluacionCalidad evaluacion);

  List<EvaluacionCalidad> calidadDe(UUID loteId);
}
