package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.Envio;
import pe.cafejunin.domain.entities.Envio.*;

public interface EnviosPort {
  List<Transportista> transportistas();
  List<Envio> envios(Optional<String> transportista);
  Optional<Envio> envioParaRegistro(UUID loteId);
  void asignar(Envio envio);
  void despachar(Envio envio);
  List<Novedad> novedades(UUID loteId);
  void agregar(Novedad novedad);
}
