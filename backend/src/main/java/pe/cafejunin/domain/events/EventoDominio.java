package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public interface EventoDominio {
  UUID id();

  String tipo();

  UUID agregadoId();

  Instant ocurridoEn();

  default int version() {
    return 1;
  }
}
