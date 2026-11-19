package pe.cafejunin.adapters.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import pe.cafejunin.application.ports.input.TransporteUseCase;
import pe.cafejunin.domain.entities.Envio;
import pe.cafejunin.domain.entities.Envio.*;

@RestController
@RequestMapping("/api/transporte")
public class TransporteController {
  private final TransporteUseCase transporte;
  public TransporteController(TransporteUseCase transporte) { this.transporte = transporte; }

  public record Asignacion(@NotBlank String transportista) {}
  public record Despacho(@NotBlank String placa, @NotNull BigDecimal pesoCargaKg) {}
  public record Registro(@NotNull Tipo tipo, @NotBlank String lugar, String descripcion) {}

  @GetMapping("/transportistas")
  public List<Transportista> transportistas() { return transporte.transportistas(); }
  @GetMapping("/envios")
  public List<Envio> envios() { return transporte.envios(); }
  @GetMapping("/envios/{lote}")
  public TransporteUseCase.Ficha detalle(@PathVariable UUID lote) { return transporte.detalle(lote); }
  @PutMapping("/envios/{lote}")
  public Envio asignar(@PathVariable UUID lote, @Valid @RequestBody Asignacion body) {
    return transporte.asignar(lote, body.transportista());
  }
  @PutMapping("/envios/{lote}/despacho")
  public Envio despachar(@PathVariable UUID lote, @Valid @RequestBody Despacho body) {
    return transporte.despachar(lote, body.placa(), body.pesoCargaKg());
  }
  @PutMapping("/envios/{lote}/novedades/{id}")
  public Novedad registrar(@PathVariable UUID lote, @PathVariable UUID id, @Valid @RequestBody Registro body) {
    return transporte.registrar(lote, id, body.tipo(), body.lugar(), body.descripcion());
  }
}
