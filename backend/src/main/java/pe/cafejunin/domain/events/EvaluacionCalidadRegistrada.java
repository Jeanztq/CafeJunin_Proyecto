package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public record EvaluacionCalidadRegistrada(UUID id, UUID agregadoId, Instant ocurridoEn)
    implements EventoDominio {
  public String tipo() {
    return "EvaluacionCalidadRegistrada";
  }
}
