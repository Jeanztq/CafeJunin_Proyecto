package pe.cafejunin.application.ports.input;

import java.math.BigDecimal;
import java.util.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.entities.Envio.*;

public interface TransporteUseCase {
  record Ficha(Envio envio, Lote lote, List<Novedad> novedades) {}
  List<Transportista> transportistas();
  List<Envio> envios();
  Ficha detalle(UUID loteId);
  Envio asignar(UUID loteId, String transportista);
  Envio despachar(UUID loteId, String placa, BigDecimal peso);
  Novedad registrar(UUID loteId, UUID id, Tipo tipo, String lugar, String descripcion);
}
