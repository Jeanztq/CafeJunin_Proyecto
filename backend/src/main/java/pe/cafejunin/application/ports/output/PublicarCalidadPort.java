package pe.cafejunin.application.ports.output;

import pe.cafejunin.domain.events.EvaluacionCalidadRegistrada;

public interface PublicarCalidadPort {
  void publicar(EvaluacionCalidadRegistrada evento);
}
