package pe.cafejunin.adapters.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.cafejunin.application.ports.input.*;
import pe.cafejunin.domain.entities.*;
import pe.cafejunin.domain.valueobjects.Peso;

@RestController
@RequestMapping("/api")
public class TrazabilidadController {
  private final RegistrarCosechaUseCase cosecha;
  private final RegistrarCalidadUseCase calidad;
  private final EvaluarRiesgoUseCase riesgo;
  private final ConsultarTrazabilidadUseCase consulta;
  private final Environment env;
  private final boolean seguridad;

  public TrazabilidadController(
      RegistrarCosechaUseCase cosecha,
      RegistrarCalidadUseCase calidad,
      EvaluarRiesgoUseCase riesgo,
      ConsultarTrazabilidadUseCase consulta,
      Environment env,
      @Value("${app.security.enabled:false}") boolean seguridad) {
    this.cosecha = cosecha;
    this.calidad = calidad;
    this.riesgo = riesgo;
    this.consulta = consulta;
    this.env = env;
    this.seguridad = seguridad;
  }

  public record CosechaRequest(
      @NotNull UUID id,
      @NotNull UUID parcelaId,
      @NotNull LocalDate fechaCosecha,
      @NotNull Lote.Variedad variedad,
      @NotNull Lote.TipoRecoleccion tipoRecoleccion,
      @NotNull @DecimalMin("0.001") @Digits(integer = 8, fraction = 3) BigDecimal pesoKg) {}

  public record CalidadRequest(
      @NotNull @Min(0) Integer defectosPrimarios,
      @NotNull @Min(0) Integer defectosSecundarios,
      @NotNull @DecimalMin("0") @DecimalMax("100") Double puntajeSca) {}

  public record RiesgoRequest(
      @NotNull @DecimalMin("0") @DecimalMax("100") Double humedad,
      @NotNull @DecimalMin("0") Double horasDemora) {}

  @PostMapping("/lotes")
  @ResponseStatus(HttpStatus.CREATED)
  public Lote registrar(@Valid @RequestBody CosechaRequest r) {
    return cosecha.ejecutar(
        new Lote(
            r.id(),
            r.parcelaId(),
            r.fechaCosecha(),
            r.variedad(),
            r.tipoRecoleccion(),
            new Peso(r.pesoKg())));
  }

  @GetMapping("/lotes")
  public List<Lote> listar() {
    return consulta.lotes();
  }

  @PostMapping("/lotes/{id}/evaluaciones")
  @ResponseStatus(HttpStatus.CREATED)
  public EvaluacionCalidad evaluar(@PathVariable UUID id, @Valid @RequestBody CalidadRequest r) {
    return calidad.ejecutar(
        new EvaluacionCalidad(
            UUID.randomUUID(), id, r.defectosPrimarios(), r.defectosSecundarios(), r.puntajeSca()));
  }

  @PostMapping("/lotes/{id}/riesgos")
  @ResponseStatus(HttpStatus.CREATED)
  public EvaluacionRiesgo riesgo(@PathVariable UUID id, @Valid @RequestBody RiesgoRequest r) {
    return riesgo.ejecutar(id, r.humedad(), r.horasDemora());
  }

  @GetMapping("/alertas")
  public List<Alerta> alertas() {
    return consulta.alertas();
  }

  @GetMapping("/info")
  public Map<String, Object> info() {
    String[] p = env.getActiveProfiles();
    return Map.of(
        "sistema",
        "Café Junín",
        "seguridadActiva",
        seguridad,
        "perfiles",
        p.length == 0 ? env.getDefaultProfiles() : p,
        "predictor",
        "SIMULADOR: no IA real",
        "eventos",
        "Outbox pendiente; sin publicador Kafka");
  }
}
