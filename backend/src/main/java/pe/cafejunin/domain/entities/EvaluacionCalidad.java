package pe.cafejunin.domain.entities;

import java.util.Objects;
import java.util.UUID;

public record EvaluacionCalidad(
    UUID id, UUID loteId, int defectosPrimarios, int defectosSecundarios, double puntajeSca) {
  public EvaluacionCalidad {
    Objects.requireNonNull(id);
    Objects.requireNonNull(loteId);
    if (defectosPrimarios < 0 || defectosSecundarios < 0)
      throw new IllegalArgumentException("Los defectos no pueden ser negativos");
    if (!Double.isFinite(puntajeSca) || puntajeSca < 0 || puntajeSca > 100)
      throw new IllegalArgumentException("Puntaje SCA fuera de rango");
  }
}
