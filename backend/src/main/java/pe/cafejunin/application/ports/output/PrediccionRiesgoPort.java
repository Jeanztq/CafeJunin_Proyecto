package pe.cafejunin.application.ports.output;

import pe.cafejunin.domain.valueobjects.Probabilidad;

// Strategy: el caso de uso recibe una estrategia intercambiable a través del puerto.
public interface PrediccionRiesgoPort {
  record Resultado(Probabilidad probabilidad, String modelo, boolean simulada) {}

  Resultado predecir(double humedad, double horasDemora);
}
