package pe.cafejunin.adapters.out.prediction;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import pe.cafejunin.application.ports.output.PrediccionRiesgoPort;

// Decorator: añade métricas sin modificar el contrato ni repetir operaciones.
public final class MedicionPrediccionDecorator implements PrediccionRiesgoPort {
  private final PrediccionRiesgoPort delegado;
  private final AtomicLong llamadas = new AtomicLong(),
      fallos = new AtomicLong(),
      nanosegundos = new AtomicLong();

  public MedicionPrediccionDecorator(PrediccionRiesgoPort p) {
    delegado = p;
  }

  public Resultado predecir(double h, double d) {
    long inicio = System.nanoTime();
    llamadas.incrementAndGet();
    try {
      return delegado.predecir(h, d);
    } catch (RuntimeException e) {
      fallos.incrementAndGet();
      throw e;
    } finally {
      nanosegundos.addAndGet(System.nanoTime() - inicio);
    }
  }

  public Map<String, Long> metricas() {
    return Map.of(
        "llamadas",
        llamadas.get(),
        "fallos",
        fallos.get(),
        "tiempoTotalMs",
        nanosegundos.get() / 1000000);
  }
}
