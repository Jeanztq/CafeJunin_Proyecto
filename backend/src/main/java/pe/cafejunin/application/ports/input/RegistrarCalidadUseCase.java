package pe.cafejunin.application.ports.input;

import pe.cafejunin.domain.valueobjects.DefectosTaza;
import java.util.UUID;
import pe.cafejunin.domain.entities.Cuenta;
import pe.cafejunin.domain.entities.EvaluacionCalidad;
import pe.cafejunin.domain.valueobjects.AtributosSensoriales;

public interface RegistrarCalidadUseCase {
  record Solicitud(UUID id, UUID loteId, int defectosPrimarios, int defectosSecundarios,
                   DefectosTaza defectosTaza, AtributosSensoriales atributos) {
    public Solicitud {
      if (id == null || loteId == null || atributos == null || defectosTaza == null)
        throw new IllegalArgumentException("Identifica la ficha, el lote y sus atributos sensoriales");
    }
  }

  EvaluacionCalidad ejecutar(Solicitud solicitud, Cuenta catador);
}
