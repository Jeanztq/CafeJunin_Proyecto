package pe.cafejunin.adapters.out.persistence;

import java.util.*;
import java.util.function.Supplier;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.application.usecases.ConflictoException;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.events.EventoDominio;

public final class MemoryStore
    implements LoteRepositoryPort,
        CalidadRepositoryPort,
        RiesgoRepositoryPort,
        EventoRepositoryPort,
        UnidadTrabajoPort {
  public static final UUID PARCELA_DEMO = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private Map<UUID, Lote> lotes = new LinkedHashMap<>();
  private Map<UUID, EvaluacionCalidad> calidad = new LinkedHashMap<>();
  private Map<UUID, EvaluacionRiesgo> riesgos = new LinkedHashMap<>();
  private Map<UUID, Alerta> alertas = new LinkedHashMap<>();
  private List<EventoDominio> eventos = new ArrayList<>();

  public synchronized boolean existeParcela(UUID id) {
    return PARCELA_DEMO.equals(id);
  }

  public synchronized Optional<Lote> buscarLote(UUID id) {
    return Optional.ofNullable(lotes.get(id));
  }

  public synchronized List<Lote> listarLotes() {
    return List.copyOf(lotes.values());
  }

  public synchronized void guardar(Lote lote) {
    if (lotes.containsKey(lote.id())) throw new ConflictoException("Lote duplicado");
    lotes.put(lote.id(), lote);
  }

  public synchronized void guardar(EvaluacionCalidad e) {
    if (calidad.containsKey(e.id())) throw new ConflictoException("Evaluación duplicada");
    calidad.put(e.id(), e);
  }

  public synchronized void guardar(EvaluacionRiesgo e) {
    riesgos.put(e.id(), e);
  }

  public synchronized void guardar(Alerta e) {
    alertas.put(e.id(), e);
  }

  public synchronized List<Alerta> listarAlertas() {
    return List.copyOf(alertas.values());
  }

  public synchronized void agregar(EventoDominio e) {
    eventos.add(e);
  }

  public synchronized List<EventoDominio> eventosPendientes() {
    return List.copyOf(eventos);
  }

  public synchronized <T> T ejecutar(Supplier<T> accion) {
    var l = new LinkedHashMap<>(lotes);
    var c = new LinkedHashMap<>(calidad);
    var r = new LinkedHashMap<>(riesgos);
    var a = new LinkedHashMap<>(alertas);
    var ev = new ArrayList<>(eventos);
    try {
      return accion.get();
    } catch (RuntimeException | Error ex) {
      lotes = l;
      calidad = c;
      riesgos = r;
      alertas = a;
      eventos = ev;
      throw ex;
    }
  }
}
