package pe.cafejunin.application.usecases;

public class NoEncontradoException extends RuntimeException {
  public NoEncontradoException(String mensaje) {
    super(mensaje);
  }
}
