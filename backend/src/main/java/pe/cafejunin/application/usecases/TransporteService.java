package pe.cafejunin.application.usecases;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import pe.cafejunin.application.ports.input.TransporteUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.entities.Envio.*;
import pe.cafejunin.domain.events.MovimientoTransporte;

public final class TransporteService implements TransporteUseCase {
  private final CuentaActualPort identidad;
  private final LoteRepositoryPort lotes;
  private final EnviosPort viajes;
  private final EventoRepositoryPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock reloj;

  public TransporteService(CuentaActualPort identidad, LoteRepositoryPort lotes, EnviosPort viajes,
      EventoRepositoryPort eventos, UnidadTrabajoPort tx, Clock reloj) {
    this.identidad = identidad;
    this.lotes = lotes;
    this.viajes = viajes;
    this.eventos = eventos;
    this.tx = tx;
    this.reloj = reloj;
  }

  private Cuenta permite(String rol) {
    var cuenta = identidad.actual();
    if (!cuenta.tiene(rol)) throw new ProhibidoException();
    return cuenta;
  }

  public List<Transportista> transportistas() {
    permite("SUPERVISOR");
    return viajes.transportistas();
  }

  public List<Envio> envios() {
    var cuenta = identidad.actual();
    if (cuenta.tiene("SUPERVISOR")) return viajes.envios(Optional.empty());
    if (!cuenta.tiene("TRANSPORTISTA")) throw new ProhibidoException();
    return viajes.envios(Optional.of(cuenta.subject()));
  }

  private Lote lote(UUID id) {
    return lotes.buscarLote(id).orElseThrow(() -> new NoEncontradoException("Lote no encontrado"));
  }

  private Envio visible(UUID id, Cuenta cuenta, boolean supervision) {
    var envio = viajes.envioParaRegistro(id)
        .orElseThrow(() -> new NoEncontradoException("Envío no encontrado"));
    if (!(supervision && cuenta.tiene("SUPERVISOR")) && !envio.transportista().equals(cuenta.subject()))
      throw new ProhibidoException();
    return envio;
  }

  public Ficha detalle(UUID id) {
    var cuenta = identidad.actual();
    if (!cuenta.tiene("SUPERVISOR") && !cuenta.tiene("TRANSPORTISTA")) throw new ProhibidoException();
    return tx.ejecutar(() -> new Ficha(visible(id, cuenta, true), lote(id), viajes.novedades(id)));
  }

  public Envio asignar(UUID id, String transportista) {
    var supervisor = permite("SUPERVISOR");
    return tx.ejecutar(() -> {
      lote(id);
      var anterior = viajes.envioParaRegistro(id);
      if (anterior.isPresent()) {
        if (anterior.get().transportista().equals(transportista)) return anterior.get();
        throw new ConflictoException("El lote ya está asignado a otro transportista");
      }
      if (viajes.transportistas().stream().noneMatch(t -> t.subject().equals(transportista)))
        throw new IllegalArgumentException("Selecciona un transportista habilitado");
      var envio = new Envio(id, transportista, supervisor.subject(), reloj.instant(), null, null, null);
      viajes.asignar(envio);
      emitir(id, "EnvioAsignado");
      return envio;
    });
  }

  public Envio despachar(UUID id, String placa, BigDecimal peso) {
    var cuenta = permite("TRANSPORTISTA");
    return tx.ejecutar(() -> {
      var envio = visible(id, cuenta, false);
      var salida = envio.despachar(placa, peso, lote(id).peso(), reloj.instant());
      if (envio.enTransito()) {
        if (envio.placa().equals(salida.placa()) && envio.pesoCargaKg().compareTo(peso) == 0) return envio;
        throw new ConflictoException("La salida de este envío ya está registrada");
      }
      viajes.despachar(salida);
      emitir(id, "DespachoRegistrado");
      return salida;
    });
  }

  public Novedad registrar(UUID loteId, UUID id, Tipo tipo, String lugar, String descripcion) {
    var cuenta = permite("TRANSPORTISTA");
    return tx.ejecutar(() -> {
      var envio = visible(loteId, cuenta, false);
      if (!envio.enTransito()) throw new ConflictoException("Registra la salida antes de informar el trayecto");
      var nueva = new Novedad(id, loteId, tipo, lugar, descripcion, reloj.instant());
      var anterior = viajes.novedades(loteId).stream().filter(n -> n.id().equals(id)).findFirst();
      if (anterior.isPresent()) {
        if (anterior.get().mismoRegistro(nueva)) return anterior.get();
        throw new ConflictoException("Este registro ya existe con datos diferentes");
      }
      viajes.agregar(nueva);
      emitir(loteId, tipo == Tipo.CONTROL ? "ControlRutaRegistrado" : "IncidenciaReportada");
      return nueva;
    });
  }

  private void emitir(UUID lote, String tipo) {
    eventos.agregar(new MovimientoTransporte(UUID.randomUUID(), tipo, lote, reloj.instant()));
  }
}
