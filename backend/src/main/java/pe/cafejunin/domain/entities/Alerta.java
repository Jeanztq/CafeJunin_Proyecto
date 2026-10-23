package pe.cafejunin.domain.entities;

import java.time.Instant;
import java.util.UUID;

public record Alerta(
    UUID id,
    UUID loteId,
    UUID evaluacionRiesgoId,
    String estado,
    String motivo,
    String instruccion,
    String responsable,
    Instant revisadaEn,
    UUID operacionId) {
  public Alerta(UUID id, UUID loteId, UUID riesgoId, String estado) {
    this(id, loteId, riesgoId, estado, null, null, null, null, null);
  }

  public Alerta revisar(
      String decision,
      String motivo,
      String instruccion,
      String responsable,
      Instant instante,
      UUID operacion) {
    if (!"PENDIENTE".equals(estado)) throw new IllegalStateException("La alerta ya fue revisada");
    if (!"CONFIRMADA".equals(decision) && !"DESCARTADA".equals(decision))
      throw new IllegalArgumentException("Decisión inválida");
    if (motivo == null || motivo.trim().length() < 5 || motivo.length() > 1000)
      throw new IllegalArgumentException("Registra un motivo de entre 5 y 1000 caracteres");
    if ("CONFIRMADA".equals(decision) && (instruccion == null || instruccion.trim().length() < 5))
      throw new IllegalArgumentException("La confirmación requiere una instrucción correctiva");
    if (instruccion != null && instruccion.length() > 1000)
      throw new IllegalArgumentException("Instrucción demasiado extensa");
    if ("DESCARTADA".equals(decision) && instruccion != null && !instruccion.isBlank())
      throw new IllegalArgumentException("El descarte no envía instrucciones correctivas");
    java.util.Objects.requireNonNull(operacion);
    java.util.Objects.requireNonNull(instante);
    java.util.Objects.requireNonNull(responsable);
    return new Alerta(
        id,
        loteId,
        evaluacionRiesgoId,
        decision,
        motivo.trim(),
        instruccion == null ? "" : instruccion.trim(),
        responsable,
        instante,
        operacion);
  }
}
