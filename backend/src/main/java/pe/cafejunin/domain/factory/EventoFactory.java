package pe.cafejunin.domain.factory;

import java.time.Instant;
import java.util.UUID;
import pe.cafejunin.domain.events.*;

// Factory Method: los creadores especializados construyen productos concretos.
public abstract class EventoFactory {
  protected abstract EventoDominio nuevo(UUID id, UUID agregadoId, Instant instante);

  public final EventoDominio crear(UUID agregadoId, Instant instante) {
    return nuevo(UUID.randomUUID(), agregadoId, instante);
  }

  public static final class Cosecha extends EventoFactory {
    protected EventoDominio nuevo(UUID id, UUID a, Instant t) {
      return new CosechaRegistrada(id, a, t);
    }
  }

  public static final class Calidad extends EventoFactory {
    protected EventoDominio nuevo(UUID id, UUID a, Instant t) {
      return new EvaluacionCalidadRegistrada(id, a, t);
    }
  }

  public static final class Riesgo extends EventoFactory {
    protected EventoDominio nuevo(UUID id, UUID a, Instant t) {
      return new RiesgoEvaluado(id, a, t);
    }
  }

  public static final class RiesgoAlto extends EventoFactory {
    protected EventoDominio nuevo(UUID id, UUID a, Instant t) {
      return new RiesgoAltoDetectado(id, a, t);
    }
  }
}
