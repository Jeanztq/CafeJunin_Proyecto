package pe.cafejunin.domain.valueobjects;

public record DefectosTaza(Integer leves, Integer graves) {
  public DefectosTaza {
    if (leves == null || graves == null || leves < 0 || graves < 0
        || leves > 5 || graves > 5 || leves + graves > 5)
      throw new IllegalArgumentException("Distribuye las tazas afectadas entre las cinco tazas de la muestra");
  }

  public int penalizacion() {
    return 2 * leves + 4 * graves;
  }
}
