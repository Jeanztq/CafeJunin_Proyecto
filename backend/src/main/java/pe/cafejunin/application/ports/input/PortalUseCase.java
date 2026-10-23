package pe.cafejunin.application.ports.input;

import java.util.*;
import pe.cafejunin.domain.entities.*;

public interface PortalUseCase {
  record Detalle(
      Lote lote,
      List<EvaluacionCalidad> calidad,
      List<EvaluacionRiesgo> riesgos,
      List<Alerta> alertas) {}

  Cuenta cuenta();

  List<Parcela> parcelas();

  List<Lote> lotes();

  Detalle detalle(UUID id);

  List<Alerta> alertas();

  Lote cosecha(Lote lote);

  EvaluacionCalidad calidad(EvaluacionCalidad evaluacion);

  EvaluacionRiesgo riesgo(UUID id, double humedad, double demora);

  Alerta revisar(UUID id, UUID operacion, String decision, String motivo, String instruccion);
}
