package pe.cafejunin.domain.valueobjects;

import java.math.BigDecimal;

public record AtributosSensoriales(
    Double fragancia, Double sabor, Double acidez, Double cuerpo,
    Double posgusto, Double balance, Double uniformidad, Double dulzor,
    Double tazaLimpia, Double apreciacionGlobal) {
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

  public double subtotalClasico() {
    for (Double valor : new Double[] {fragancia, sabor, posgusto, acidez, cuerpo, balance, apreciacionGlobal}) {
      if (valor == null || !Double.isFinite(valor) || valor < 6 || valor > 10 || valor * 4 != Math.rint(valor * 4))
        throw new IllegalArgumentException("Califica los atributos de calidad de 6 a 10 en pasos de 0.25");
    }
    for (Double valor : new Double[] {uniformidad, dulzor, tazaLimpia}) {
      if (valor == null || !Double.isFinite(valor) || valor < 0 || valor > 10 || valor % 2 != 0)
        throw new IllegalArgumentException("Uniformidad, dulzor y taza limpia aportan dos puntos por taza");
    }
    return fragancia + sabor + posgusto + acidez + cuerpo + balance + apreciacionGlobal
        + uniformidad + dulzor + tazaLimpia;
  }

  private static void validar(String campo, Double valor) {
    if (valor == null || !Double.isFinite(valor) || valor < 0 || valor > 10)
      throw new IllegalArgumentException(campo + " debe estar entre 0 y 10");
    if (BigDecimal.valueOf(valor).stripTrailingZeros().scale() > 2)
      throw new IllegalArgumentException(campo + " admite hasta dos decimales");
  }
}
