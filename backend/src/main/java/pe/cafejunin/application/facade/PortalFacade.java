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
  private final LoteRepositoryPort lotes;
  private final CalidadRepositoryPort evaluaciones;
  private final RiesgoRepositoryPort riesgos;
  private final RegistrarCosechaUseCase cosecha;
  private final RegistrarCalidadUseCase calidad;
  private final EvaluarRiesgoUseCase riesgo;
  private final RevisarAlertaUseCase revision;

  public PortalFacade(
      CuentaActualPort identidad,
      LoteRepositoryPort lotes,
      CalidadRepositoryPort evaluaciones,
      RiesgoRepositoryPort riesgos,
      RegistrarCosechaUseCase cosecha,
      RegistrarCalidadUseCase calidad,
      EvaluarRiesgoUseCase riesgo,
      RevisarAlertaUseCase revision) {
    this.identidad = identidad;
    this.lotes = lotes;
    this.evaluaciones = evaluaciones;
    this.riesgos = riesgos;
    this.cosecha = cosecha;
    this.calidad = calidad;
    this.riesgo = riesgo;
    this.revision = revision;
  }

  public Cuenta cuenta() {
    return identidad.actual();
  }

  private void exige(String rol) {
    if (!cuenta().tiene(rol)) throw new ProhibidoException();
  }

  private boolean lee(Lote lote) {
    var cuentaActual = cuenta();
    if (cuentaActual.tiene("SUPERVISOR") || cuentaActual.tiene("CATADOR")) return true;
    return cuentaActual.tiene("PRODUCTOR")
        && lotes.buscarParcela(lote.parcelaId())
            .map(p -> p.propietarioSubject().equals(cuentaActual.subject()))
            .orElse(false);
  }

  private Lote autorizado(UUID id) {
    var registro = lotes.buscarLote(id).orElseThrow(() -> new NoEncontradoException("Lote no encontrado"));
    if (!lee(registro)) throw new ProhibidoException();
    return registro;
  }

  public List<Parcela> parcelas() {
    var cuentaActual = cuenta();
    return lotes.listarParcelas().stream()
        .filter(
            p ->
                cuentaActual.tiene("SUPERVISOR")
                    || cuentaActual.tiene("CATADOR")
                    || p.propietarioSubject().equals(cuentaActual.subject()))
        .toList();
  }

  public List<Lote> lotes() {
    return lotes.listarLotes().stream().filter(this::lee).toList();
  }

  public List<Alerta> alertas() {
    exige("SUPERVISOR");
    return riesgos.listarAlertas();
  }

  public Detalle detalle(UUID id) {
    var lote = autorizado(id);
    return new Detalle(
        lote,
        evaluaciones.calidadDe(id),
        cuenta().tiene("SUPERVISOR") ? riesgos.riesgosDe(id) : List.of(),
        cuenta().tiene("SUPERVISOR")
            ? riesgos.listarAlertas().stream().filter(registro -> registro.loteId().equals(id)).toList()
            : List.of());
  }

  public Lote cosecha(Lote lote) {
    exige("PRODUCTOR");
    var p =
        lotes.buscarParcela(lote.parcelaId())
            .orElseThrow(() -> new NoEncontradoException("Parcela no encontrada"));
    if (!p.propietarioSubject().equals(cuenta().subject())) throw new ProhibidoException();
    return cosecha.ejecutar(lote);
  }

  public EvaluacionCalidad calidad(RegistrarCalidadUseCase.Solicitud solicitud) {
    return calidad.ejecutar(solicitud, cuenta());
  }

  public EvaluacionRiesgo riesgo(UUID id, double humedad, double horasDemora) {
    exige("SUPERVISOR");
    autorizado(id);
    return riesgo.ejecutar(id, humedad, horasDemora);
  }

  public Alerta revisar(UUID id, UUID op, String decision, String motivo, String instruccion) {
    exige("SUPERVISOR");
    return revision.ejecutar(id, op, decision, motivo, instruccion, cuenta().subject());
  }
}
