package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface LoteRepositoryPort {
  boolean existeParcela(UUID id);

  Optional<Parcela> buscarParcela(UUID id);

  List<Parcela> listarParcelas();

  Optional<Lote> buscarLote(UUID id);

  List<Lote> listarLotes();

  void guardar(Lote lote);
}
