package pe.cafejunin.domain.entities;

import java.util.UUID;
import pe.cafejunin.domain.valueobjects.Probabilidad;

public record EvaluacionRiesgo(
    UUID id,
    UUID loteId,
    double humedad,
    double horasDemora,
    Probabilidad probabilidad,
    String modelo,
    boolean simulada) {
  public EvaluacionRiesgo {
    java.util.Objects.requireNonNull(id);
    java.util.Objects.requireNonNull(loteId);
    java.util.Objects.requireNonNull(probabilidad);
    if (!Double.isFinite(humedad)
        || humedad < 0
        || humedad > 100
        || !Double.isFinite(horasDemora)
        || horasDemora < 0) throw new IllegalArgumentException("Humedad o demora inválida");
    if (modelo == null || modelo.isBlank())
      throw new IllegalArgumentException("Falta identificar el modelo");
  }

  public boolean generaAlerta() {
    return probabilidad.superaUmbral();
  }
}
