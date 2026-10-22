package pe.cafejunin.application.ports.input;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface ConsultarTrazabilidadUseCase {
  List<Lote> lotes();

  List<Alerta> alertas();
}
