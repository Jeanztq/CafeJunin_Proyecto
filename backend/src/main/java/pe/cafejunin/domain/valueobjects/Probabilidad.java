package pe.cafejunin.domain.valueobjects;

public record Probabilidad(double valor) {
  public Probabilidad {
    if (!Double.isFinite(valor) || valor < 0 || valor > 1)
      throw new IllegalArgumentException("La probabilidad debe estar entre 0 y 1");
  }

  public boolean superaUmbral() {
    return valor > 0.70;
  }
}
