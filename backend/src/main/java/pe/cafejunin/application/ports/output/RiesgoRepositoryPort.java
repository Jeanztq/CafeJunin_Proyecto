package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface RiesgoRepositoryPort {
  void guardar(EvaluacionRiesgo evaluacion);

  void guardar(Alerta alerta);

  List<Alerta> listarAlertas();

  Optional<EvaluacionRiesgo> buscarRiesgo(UUID id);

  Optional<Alerta> buscarAlerta(UUID id);

  void actualizarPendiente(Alerta alerta);

  List<EvaluacionRiesgo> riesgosDe(UUID loteId);
}
