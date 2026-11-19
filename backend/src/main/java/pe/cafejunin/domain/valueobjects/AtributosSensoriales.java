package pe.cafejunin.domain.valueobjects;

import java.math.BigDecimal;

public record AtributosSensoriales(
    Double fragancia, Double sabor, Double acidez, Double cuerpo,
    Double posgusto, Double balance, Double uniformidad, Double dulzor) {
  public AtributosSensoriales {
    validar("Fragancia", fragancia);
    validar("Sabor", sabor);
    validar("Acidez", acidez);
    validar("Cuerpo", cuerpo);
    validar("Posgusto", posgusto);
    validar("Balance", balance);
    validar("Uniformidad", uniformidad);
    validar("Dulzor", dulzor);
  }

  private static void validar(String campo, Double valor) {
    if (valor == null || !Double.isFinite(valor) || valor < 0 || valor > 10)
      throw new IllegalArgumentException(campo + " debe estar entre 0 y 10");
    if (BigDecimal.valueOf(valor).stripTrailingZeros().scale() > 2)
      throw new IllegalArgumentException(campo + " admite hasta dos decimales");
  }
}
