package pe.cafejunin.domain.entities;

import java.time.Instant;
import java.util.Objects;
import pe.cafejunin.domain.valueobjects.AtributosSensoriales;
import java.util.UUID;

public record EvaluacionCalidad(
    UUID id, UUID loteId, int defectosPrimarios, int defectosSecundarios, double puntajeSca,
    AtributosSensoriales atributos, String catadorId, String catadorNombre, Instant registradaEn) {
  public String getClasificacion() {
    return puntajeSca >= 80 ? "Café especial" : "Café comercial";
  }

  public EvaluacionCalidad {
    Objects.requireNonNull(id);
    Objects.requireNonNull(loteId);
    if (defectosPrimarios < 0 || defectosSecundarios < 0)
      throw new IllegalArgumentException("Los defectos no pueden ser negativos");
    if (!Double.isFinite(puntajeSca) || puntajeSca < 0 || puntajeSca > 100)
      throw new IllegalArgumentException("Puntaje SCA fuera de rango");
  }
}
