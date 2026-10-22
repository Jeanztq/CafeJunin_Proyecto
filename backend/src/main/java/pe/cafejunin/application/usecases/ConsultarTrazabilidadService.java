package pe.cafejunin.application.usecases;

import java.util.List;
import pe.cafejunin.application.ports.input.ConsultarTrazabilidadUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.*;

public final class ConsultarTrazabilidadService implements ConsultarTrazabilidadUseCase {
  private final LoteRepositoryPort lotes;
  private final RiesgoRepositoryPort riesgos;

  public ConsultarTrazabilidadService(LoteRepositoryPort lotes, RiesgoRepositoryPort riesgos) {
    this.lotes = lotes;
    this.riesgos = riesgos;
  }

  public List<Lote> lotes() {
    return lotes.listarLotes();
  }

  public List<Alerta> alertas() {
    return riesgos.listarAlertas();
  }
}
