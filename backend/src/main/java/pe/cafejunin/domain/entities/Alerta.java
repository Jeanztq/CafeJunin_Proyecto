package pe.cafejunin.domain.entities;

import java.util.UUID;

public record Alerta(UUID id, UUID loteId, UUID evaluacionRiesgoId, String estado) {}
