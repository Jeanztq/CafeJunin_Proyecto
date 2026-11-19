package pe.cafejunin.adapters.in.eventos;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.cafejunin.application.ports.output.EventoRepositoryPort;
import pe.cafejunin.domain.events.EvaluacionCalidadRegistrada;

@Component
public final class EncolarCalidadRegistrada {
  private final EventoRepositoryPort outbox;

  public EncolarCalidadRegistrada(EventoRepositoryPort outbox) {
    this.outbox = outbox;
  }

  @EventListener
  public void recibir(EvaluacionCalidadRegistrada evento) {
    outbox.agregar(evento);
  }
}
