package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.EvaluacionCalidad;

public interface CalidadRepositoryPort {
  void guardar(EvaluacionCalidad evaluacion);

  List<EvaluacionCalidad> calidadDe(UUID loteId);
}
