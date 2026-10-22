package pe.cafejunin.adapters.out.prediction;

import pe.cafejunin.application.ports.output.PrediccionRiesgoPort;
import pe.cafejunin.domain.valueobjects.Probabilidad;

// Mock determinista para probar el contrato. NO es IA, ni estimación agronómica válida.
public final class SimuladorRiesgoAdapter implements PrediccionRiesgoPort {
  public Resultado predecir(double humedad, double horasDemora) {
    double valor =
        Math.min(1, Math.max(0, 0.10 + Math.max(0, humedad - 12) * 0.08 + horasDemora * 0.01));
    return new Resultado(new Probabilidad(valor), "SIMULADOR-REGLAS-v1", true);
  }
}
