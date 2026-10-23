package pe.cafejunin.domain.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import pe.cafejunin.domain.entities.Lote;
import pe.cafejunin.domain.valueobjects.Peso;

// Builder por operación, nunca compartido como singleton. El producto también valida sus
// invariantes.
public final class LoteBuilder {
  private UUID id, parcela;
  private LocalDate fecha;
  private Lote.Variedad variedad;
  private Lote.TipoRecoleccion tipo;
  private BigDecimal peso;

  public LoteBuilder identificado(UUID valor) {
    id = valor;
    return this;
  }

  public LoteBuilder deParcela(UUID valor) {
    parcela = valor;
    return this;
  }

  public LoteBuilder cosechado(LocalDate valor) {
    fecha = valor;
    return this;
  }

  public LoteBuilder variedad(Lote.Variedad valor) {
    variedad = valor;
    return this;
  }

  public LoteBuilder recoleccion(Lote.TipoRecoleccion valor) {
    tipo = valor;
    return this;
  }

  public LoteBuilder peso(BigDecimal valor) {
    peso = valor;
    return this;
  }

  public Lote build() {
    return new Lote(id, parcela, fecha, variedad, tipo, new Peso(peso));
  }
}
