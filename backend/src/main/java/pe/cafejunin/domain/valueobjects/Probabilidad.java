package pe.cafejunin.domain.valueobjects;

public record Probabilidad(double valor) {
  public static final double UMBRAL_ALERTA = 0.70;

  public Probabilidad {
    if (!Double.isFinite(valor) || valor < 0 || valor > 1)
      throw new IllegalArgumentException("La probabilidad debe estar entre 0 y 1");
  }

  public boolean superaUmbral() {
    return valor > UMBRAL_ALERTA;
  }
}
