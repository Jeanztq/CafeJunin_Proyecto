package pe.cafejunin.application.usecases;

public class ProhibidoException extends RuntimeException {
  public ProhibidoException() {
    super("La cuenta no tiene acceso a este recurso");
  }
}
