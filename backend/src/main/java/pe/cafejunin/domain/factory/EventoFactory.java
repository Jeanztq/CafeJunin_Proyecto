package pe.cafejunin.domain.factory;

import java.time.Instant;
import java.util.UUID;
import pe.cafejunin.domain.events.*;

// Factory Method: los creadores especializados construyen productos concretos.
public abstract class EventoFactory<E extends EventoDominio> {
  public static final class Revision extends EventoFactory<AlertaRevisada> {
    protected AlertaRevisada nuevo(UUID id, UUID agregadoId, Instant instante) {
      return new AlertaRevisada(id, agregadoId, instante);
    }
  }

  protected abstract E nuevo(UUID id, UUID agregadoId, Instant instante);

  public final E crear(UUID agregadoId, Instant instante) {
    return nuevo(UUID.randomUUID(), agregadoId, instante);
  }

  public static final class Cosecha extends EventoFactory<CosechaRegistrada> {
    protected CosechaRegistrada nuevo(UUID id, UUID a, Instant t) {
      return new CosechaRegistrada(id, a, t);
    }
  }

  public static final class Calidad extends EventoFactory<EvaluacionCalidadRegistrada> {
    private final UUID evaluacionId;

    public Calidad(UUID evaluacionId) {
      this.evaluacionId = evaluacionId;
    }

    protected EvaluacionCalidadRegistrada nuevo(UUID id, UUID a, Instant t) {
      return new EvaluacionCalidadRegistrada(id, a, t, evaluacionId);
    }
  }

  public static final class Riesgo extends EventoFactory<RiesgoEvaluado> {
    protected RiesgoEvaluado nuevo(UUID id, UUID a, Instant t) {
      return new RiesgoEvaluado(id, a, t);
    }
  }

  public static final class RiesgoAlto extends EventoFactory<RiesgoAltoDetectado> {
    protected RiesgoAltoDetectado nuevo(UUID id, UUID a, Instant t) {
      return new RiesgoAltoDetectado(id, a, t);
    }
  }
}
