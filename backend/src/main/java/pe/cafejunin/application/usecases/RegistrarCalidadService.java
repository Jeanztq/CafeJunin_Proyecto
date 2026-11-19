package pe.cafejunin.application.usecases;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import pe.cafejunin.application.ports.input.RegistrarCalidadUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.EvaluacionCalidad;
import pe.cafejunin.domain.entities.Cuenta;
import pe.cafejunin.domain.factory.EventoFactory;

public final class RegistrarCalidadService implements RegistrarCalidadUseCase {
  private final LoteRepositoryPort lotes;
  private final CalidadRepositoryPort calidad;
  private final PublicarCalidadPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock reloj;

  public RegistrarCalidadService(
      LoteRepositoryPort lotes,
      CalidadRepositoryPort calidad,
      PublicarCalidadPort eventos,
      UnidadTrabajoPort tx,
      Clock reloj) {
    this.lotes = lotes;
    this.calidad = calidad;
    this.eventos = eventos;
    this.tx = tx;
    this.reloj = reloj;
  }

  public EvaluacionCalidad ejecutar(Solicitud solicitud, Cuenta catador) {
    if (!catador.tiene("CATADOR")) throw new ProhibidoException();
    return tx.ejecutar(() -> {
      if (lotes.buscarLote(solicitud.loteId()).isEmpty())
        throw new NoEncontradoException("Lote no encontrado");
      var anterior = calidad.buscarParaRegistro(solicitud.id());
      if (anterior.isPresent()) {
        var ficha = anterior.get();
        if (catador.subject().equals(ficha.catadorId())
            && solicitud.loteId().equals(ficha.loteId())
            && solicitud.defectosPrimarios() == ficha.defectosPrimarios()
            && solicitud.defectosSecundarios() == ficha.defectosSecundarios()
            && solicitud.puntajeSca() == ficha.puntajeSca()
            && solicitud.atributos().equals(ficha.atributos())) return ficha;
        throw new ConflictoException("El identificador ya corresponde a otra ficha");
      }
      var instante = reloj.instant().truncatedTo(ChronoUnit.MILLIS);
      var ficha = new EvaluacionCalidad(solicitud.id(), solicitud.loteId(),
          solicitud.defectosPrimarios(), solicitud.defectosSecundarios(), solicitud.puntajeSca(),
          solicitud.atributos(), catador.subject(), catador.nombre(), instante);
      calidad.guardar(ficha);
      eventos.publicar(new EventoFactory.Calidad(ficha.id()).crear(ficha.loteId(), instante));
      return ficha;
    });
  }
}
