package pe.cafejunin.domain.events;

import java.time.Instant;
import java.util.UUID;

public record MovimientoTransporte(UUID id, String tipo, UUID agregadoId, Instant ocurridoEn)
    implements EventoDominio {}
