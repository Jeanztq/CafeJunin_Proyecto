package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public record RiesgoAltoDetectado(UUID id, UUID agregadoId, Instant ocurridoEn)
    implements EventoDominio {
  public String tipo() {
    return "RiesgoAltoDetectado";
  }
}
