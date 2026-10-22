package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public record CosechaRegistrada(UUID id, UUID agregadoId, Instant ocurridoEn)
    implements EventoDominio {
  public String tipo() {
    return "CosechaRegistrada";
  }
}
