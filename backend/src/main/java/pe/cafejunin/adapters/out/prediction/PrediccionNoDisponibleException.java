package pe.cafejunin.adapters.out.prediction;

public class PrediccionNoDisponibleException extends RuntimeException {
  public PrediccionNoDisponibleException(Throwable causa) {
    super("No se pudo obtener una predicción válida; no se generó un resultado sustituto", causa);
  }
}
