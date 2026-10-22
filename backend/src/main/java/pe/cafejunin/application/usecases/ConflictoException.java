package pe.cafejunin.application.usecases;

public class ConflictoException extends RuntimeException {
  public ConflictoException(String mensaje) {
    super(mensaje);
  }
}
