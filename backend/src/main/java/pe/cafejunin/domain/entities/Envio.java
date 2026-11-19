package pe.cafejunin.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import pe.cafejunin.domain.valueobjects.Peso;

public record Envio(UUID loteId, String transportista, String asignadoPor, Instant asignadoEn,
                    Instant salida, String placa, BigDecimal pesoCargaKg) {
  public boolean enTransito() { return salida != null; }

  public Envio despachar(String placaVehiculo, BigDecimal carga, Peso pesoLote, Instant instante) {
    var peso = new Peso(carga);
    if (peso.kilogramos().compareTo(pesoLote.kilogramos()) > 0)
      throw new IllegalArgumentException("El peso de carga supera el peso del lote");
    String normalizada = placaVehiculo == null ? "" : placaVehiculo.strip().toUpperCase(Locale.ROOT);
    if (!normalizada.matches("[A-Z0-9]{3}-?[A-Z0-9]{3}"))
      throw new IllegalArgumentException("La placa debe tener seis caracteres, por ejemplo ABC-123");
    String compacta = normalizada.replace("-", "");
    return new Envio(loteId, transportista, asignadoPor, asignadoEn, instante,
        compacta.substring(0, 3) + "-" + compacta.substring(3), carga);
  }

  public record Transportista(String subject, String nombre) {}

  public enum Tipo { CONTROL, DESVIO, DERRUMBE, FALLA_MECANICA }

  public record Novedad(UUID id, UUID loteId, Tipo tipo, String lugar, String descripcion, Instant registradaEn) {
    public Novedad {
      if (tipo == null) throw new IllegalArgumentException("Selecciona el tipo de registro");
      lugar = lugar == null ? "" : lugar.strip();
      descripcion = descripcion == null ? "" : descripcion.strip();
      if (lugar.isEmpty() || lugar.length() > 160)
        throw new IllegalArgumentException("Indica un punto de ruta de hasta 160 caracteres");
      if (descripcion.length() > 1000 || (tipo != Tipo.CONTROL && descripcion.isEmpty()))
        throw new IllegalArgumentException("La incidencia requiere una descripción de hasta 1000 caracteres");
    }
    public boolean mismoRegistro(Novedad otra) {
      return loteId.equals(otra.loteId) && tipo == otra.tipo && lugar.equals(otra.lugar)
          && descripcion.equals(otra.descripcion);
    }
  }
}
