package pe.cafejunin.application.ports.input;

import java.math.BigDecimal;
import java.util.UUID;
import pe.cafejunin.domain.entities.Cuenta;
import pe.cafejunin.domain.entities.EvaluacionCalidad;
import pe.cafejunin.domain.valueobjects.AtributosSensoriales;

public interface RegistrarCalidadUseCase {
  record Solicitud(UUID id, UUID loteId, int defectosPrimarios, int defectosSecundarios,
                   double puntajeSca, AtributosSensoriales atributos) {
    public Solicitud {
      if (!Double.isFinite(puntajeSca)
          || BigDecimal.valueOf(puntajeSca).stripTrailingZeros().scale() > 2)
        throw new IllegalArgumentException("El puntaje final admite hasta dos decimales");
      if (id == null || loteId == null || atributos == null)
        throw new IllegalArgumentException("Identifica la ficha, el lote y sus atributos sensoriales");
    }
  }

  EvaluacionCalidad ejecutar(Solicitud solicitud, Cuenta catador);
}
