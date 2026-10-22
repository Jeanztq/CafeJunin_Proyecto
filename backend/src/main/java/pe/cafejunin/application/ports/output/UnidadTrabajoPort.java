package pe.cafejunin.application.ports.output;

import java.util.function.Supplier;

public interface UnidadTrabajoPort {
  <T> T ejecutar(Supplier<T> accion);
}
