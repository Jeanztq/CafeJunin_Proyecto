package pe.cafejunin.application.usecases;

import java.time.*;
import pe.cafejunin.application.ports.input.RegistrarCosechaUseCase;
import pe.cafejunin.application.ports.output.*;
import pe.cafejunin.domain.entities.Lote;
import pe.cafejunin.domain.factory.EventoFactory;

public final class RegistrarCosechaService implements RegistrarCosechaUseCase {
  private final LoteRepositoryPort lotes;
  private final EventoRepositoryPort eventos;
  private final UnidadTrabajoPort tx;
  private final Clock reloj;

  public RegistrarCosechaService(
      LoteRepositoryPort lotes, EventoRepositoryPort eventos, UnidadTrabajoPort tx, Clock reloj) {
    this.lotes = lotes;
    this.eventos = eventos;
    this.tx = tx;
    this.reloj = reloj;
  }

  public Lote ejecutar(Lote lote) {
    if (lote.fechaCosecha().isAfter(LocalDate.now(reloj)))
      throw new IllegalArgumentException("La cosecha no puede tener fecha futura");
    return tx.ejecutar(
        () -> {
          var anterior = lotes.buscarLote(lote.id());
          if (anterior.isPresent()) {
            if (!anterior.get().equals(lote))
              throw new ConflictoException("El identificador ya corresponde a otra cosecha");
            return anterior.get();
          }
          if (!lotes.existeParcela(lote.parcelaId()))
            throw new NoEncontradoException("Parcela no encontrada");
          lotes.guardar(lote);
          eventos.agregar(new EventoFactory.Cosecha().crear(lote.id(), reloj.instant()));
          return lote;
        });
  }
}
