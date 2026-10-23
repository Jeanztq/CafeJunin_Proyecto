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
    return buscarParcela(id).isPresent();
  }

  public synchronized Optional<Parcela> buscarParcela(UUID id) {
    return listarParcelas().stream().filter(p -> p.id().equals(id)).findFirst();
  }

  public synchronized List<Parcela> listarParcelas() {
    return List.of(
        new Parcela(PARCELA_DEMO, "El Mirador · Demo local", "Satipo, Junín", "demo"),
        new Parcela(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "El Mirador · Productor de prueba",
            "Satipo, Junín",
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1"));
  }

  public synchronized List<EvaluacionCalidad> calidadDe(UUID id) {
    return calidad.values().stream().filter(x -> x.loteId().equals(id)).toList();
  }

  public synchronized List<EvaluacionRiesgo> riesgosDe(UUID id) {
    return riesgos.values().stream().filter(x -> x.loteId().equals(id)).toList();
  }

  public synchronized Optional<Alerta> buscarAlerta(UUID id) {
    return Optional.ofNullable(alertas.get(id));
  }

  public synchronized void actualizarPendiente(Alerta a) {
    var actual = alertas.get(a.id());
    if (actual == null || !actual.estado().equals("PENDIENTE"))
      throw new ConflictoException("La alerta ya fue revisada");
    alertas.put(a.id(), a);
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
