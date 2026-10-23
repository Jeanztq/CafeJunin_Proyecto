package pe.cafejunin.application.facade;

import java.util.*;
import pe.cafejunin.application.ports.input.*;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.application.usecases.*;
import pe.cafejunin.domain.entities.*;

// Facade: coordina acceso y consultas del portal; las reglas de revisión/cosecha permanecen en sus
// casos de uso.
public final class PortalFacade implements PortalUseCase {
  private final CuentaActualPort identidad;
  private final LoteRepositoryPort l;
  private final CalidadRepositoryPort c;
  private final RiesgoRepositoryPort r;
  private final RegistrarCosechaUseCase cosecha;
  private final RegistrarCalidadUseCase calidad;
  private final EvaluarRiesgoUseCase riesgo;
  private final RevisarAlertaUseCase revision;

  public PortalFacade(
      CuentaActualPort i,
      LoteRepositoryPort l,
      CalidadRepositoryPort c,
      RiesgoRepositoryPort r,
      RegistrarCosechaUseCase co,
      RegistrarCalidadUseCase ca,
      EvaluarRiesgoUseCase ri,
      RevisarAlertaUseCase re) {
    identidad = i;
    this.l = l;
    this.c = c;
    this.r = r;
    cosecha = co;
    calidad = ca;
    riesgo = ri;
    revision = re;
  }

  public Cuenta cuenta() {
    return identidad.actual();
  }

  private void exige(String rol) {
    if (!cuenta().tiene(rol)) throw new ProhibidoException();
  }

  private boolean lee(Lote lote) {
    var a = cuenta();
    if (a.tiene("SUPERVISOR") || a.tiene("CATADOR")) return true;
    return a.tiene("PRODUCTOR")
        && l.buscarParcela(lote.parcelaId())
            .map(p -> p.propietarioSubject().equals(a.subject()))
            .orElse(false);
  }

  private Lote autorizado(UUID id) {
    var x = l.buscarLote(id).orElseThrow(() -> new NoEncontradoException("Lote no encontrado"));
    if (!lee(x)) throw new ProhibidoException();
    return x;
  }

  public List<Parcela> parcelas() {
    var a = cuenta();
    return l.listarParcelas().stream()
        .filter(
            p ->
                a.tiene("SUPERVISOR")
                    || a.tiene("CATADOR")
                    || p.propietarioSubject().equals(a.subject()))
        .toList();
  }

  public List<Lote> lotes() {
    return l.listarLotes().stream().filter(this::lee).toList();
  }

  public List<Alerta> alertas() {
    exige("SUPERVISOR");
    return r.listarAlertas();
  }

  public Detalle detalle(UUID id) {
    var lote = autorizado(id);
    return new Detalle(
        lote,
        c.calidadDe(id),
        cuenta().tiene("SUPERVISOR") ? r.riesgosDe(id) : List.of(),
        cuenta().tiene("SUPERVISOR")
            ? r.listarAlertas().stream().filter(x -> x.loteId().equals(id)).toList()
            : List.of());
  }

  public Lote cosecha(Lote lote) {
    exige("PRODUCTOR");
    var p =
        l.buscarParcela(lote.parcelaId())
            .orElseThrow(() -> new NoEncontradoException("Parcela no encontrada"));
    if (!p.propietarioSubject().equals(cuenta().subject())) throw new ProhibidoException();
    return cosecha.ejecutar(lote);
  }

  public EvaluacionCalidad calidad(EvaluacionCalidad e) {
    exige("CATADOR");
    autorizado(e.loteId());
    return calidad.ejecutar(e);
  }

  public EvaluacionRiesgo riesgo(UUID id, double h, double d) {
    exige("SUPERVISOR");
    autorizado(id);
    return riesgo.ejecutar(id, h, d);
  }

  public Alerta revisar(UUID id, UUID op, String decision, String motivo, String instruccion) {
    exige("SUPERVISOR");
    return revision.ejecutar(id, op, decision, motivo, instruccion, cuenta().subject());
  }
}
