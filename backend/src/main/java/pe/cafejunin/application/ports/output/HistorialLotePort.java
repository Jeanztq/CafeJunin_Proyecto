package pe.cafejunin.application.ports.output;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface HistorialLotePort {
  record Hito(UUID id, String tipo, Instant ocurridoEn) {}

  List<Hito> historialDe(UUID loteId);
}
