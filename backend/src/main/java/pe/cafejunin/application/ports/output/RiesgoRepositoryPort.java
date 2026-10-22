package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface RiesgoRepositoryPort {
  void guardar(EvaluacionRiesgo evaluacion);

  void guardar(Alerta alerta);

  List<Alerta> listarAlertas();
}
