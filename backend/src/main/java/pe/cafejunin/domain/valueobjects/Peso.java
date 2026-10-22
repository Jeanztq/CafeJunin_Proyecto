package pe.cafejunin.domain.valueobjects;

import java.math.BigDecimal;

public record Peso(BigDecimal kilogramos) {
  public Peso {
    if (kilogramos == null
        || kilogramos.signum() <= 0
        || kilogramos.compareTo(new BigDecimal("99999999.999")) > 0
        || kilogramos.scale() > 3)
      throw new IllegalArgumentException(
          "El peso debe ser positivo, máximo 99999999.999 kg y hasta tres decimales");
    kilogramos = kilogramos.stripTrailingZeros();
  }
}
