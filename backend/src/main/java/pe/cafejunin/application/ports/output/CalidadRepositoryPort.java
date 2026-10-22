package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface CalidadRepositoryPort {
  void guardar(EvaluacionCalidad evaluacion);
}
