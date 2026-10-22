package pe.cafejunin.application.ports.input;

import pe.cafejunin.domain.entities.Lote;

public interface RegistrarCosechaUseCase {
  Lote ejecutar(Lote lote);
}
