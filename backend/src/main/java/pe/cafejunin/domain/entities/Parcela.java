package pe.cafejunin.domain.entities;

import java.util.UUID;

public record Parcela(UUID id, String nombre, String ubicacion, String propietarioSubject) {}
