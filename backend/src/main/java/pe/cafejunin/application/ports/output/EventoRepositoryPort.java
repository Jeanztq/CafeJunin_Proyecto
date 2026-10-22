package pe.cafejunin.application.ports.output;

import java.util.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.EventoDominio;

public interface EventoRepositoryPort {
  void agregar(EventoDominio evento);
}
