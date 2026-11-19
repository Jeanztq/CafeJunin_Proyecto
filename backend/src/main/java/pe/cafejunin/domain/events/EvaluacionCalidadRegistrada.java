package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public record EvaluacionCalidadRegistrada(UUID id, UUID agregadoId, Instant ocurridoEn, UUID evaluacionId)
    implements EventoDominio {
  public int version() { return 2; }

  public String tipo() {
    return "EvaluacionCalidadRegistrada";
  }
}
