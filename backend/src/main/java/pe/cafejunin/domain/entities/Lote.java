package pe.cafejunin.domain.entities;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import pe.cafejunin.domain.valueobjects.Peso;

public record Lote(
    UUID id,
    UUID parcelaId,
    LocalDate fechaCosecha,
    Variedad variedad,
    TipoRecoleccion tipoRecoleccion,
    Peso peso) {
  public enum Variedad {
    TYPICA,
    GEISHA,
    BOURBON,
    CATIMOR
  }

  public enum TipoRecoleccion {
    SELECTIVA,
    GENERAL
  }

  public Lote {
    Objects.requireNonNull(id, "Falta el identificador");
    Objects.requireNonNull(parcelaId, "Falta la parcela");
    Objects.requireNonNull(fechaCosecha, "Falta la fecha");
    Objects.requireNonNull(variedad, "Falta la variedad");
    Objects.requireNonNull(tipoRecoleccion, "Falta el tipo de recolección");
    Objects.requireNonNull(peso, "Falta el peso");
  }
}
