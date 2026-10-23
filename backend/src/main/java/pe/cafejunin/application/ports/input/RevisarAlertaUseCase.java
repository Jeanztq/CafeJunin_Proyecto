package pe.cafejunin.application.ports.input;

import java.util.UUID;
import pe.cafejunin.domain.entities.Alerta;

public interface RevisarAlertaUseCase {
  Alerta ejecutar(
      UUID id,
      UUID operacion,
      String decision,
      String motivo,
      String instruccion,
      String responsable);
}
