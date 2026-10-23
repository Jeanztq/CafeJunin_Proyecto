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
import pe.cafejunin.application.ports.input.PortalUseCase;
import pe.cafejunin.domain.builder.LoteBuilder;
import pe.cafejunin.domain.entities.*;

@RestController
@RequestMapping("/api")
public class TrazabilidadController {
  private final PortalUseCase portal;
  private final Environment env;
  private final boolean seguridad;
  private final String url, realm, client, predictor;

  public TrazabilidadController(
      PortalUseCase p,
      Environment e,
      @Value("${app.security.enabled:false}") boolean s,
      @Value("${app.keycloak.url}") String u,
      @Value("${app.keycloak.realm}") String r,
      @Value("${app.keycloak.client}") String c,
      @Value("${app.predictor.mode}") String m) {
    portal = p;
    env = e;
    seguridad = s;
    url = u;
    realm = r;
    client = c;
    predictor = m;
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

  public record DecisionRequest(
      @NotNull UUID operacionId,
      @NotBlank String estado,
      @NotBlank @Size(max = 1000) String motivo,
      @Size(max = 1000) String instruccion) {}

  @GetMapping("/config")
  public Map<String, Object> config() {
    return Map.of(
        "seguridadActiva",
        seguridad,
        "url",
        url,
        "realm",
        realm,
        "clientId",
        client,
        "predictor",
        predictor);
  }

  @GetMapping("/info")
  public Map<String, Object> info() {
    return Map.of(
        "sistema",
        "Café Junín",
        "seguridadActiva",
        seguridad,
        "perfiles",
        env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles(),
        "predictor",
        predictor,
        "eventos",
        "Outbox transaccional; publicador externo pendiente");
  }

  @GetMapping("/me")
  public Cuenta cuenta() {
    return portal.cuenta();
  }

  @GetMapping("/parcelas")
  public List<Parcela> parcelas() {
    return portal.parcelas();
  }

  @GetMapping("/lotes")
  public List<Lote> lotes() {
    return portal.lotes();
  }

  @GetMapping("/lotes/{id}/detalle")
  public PortalUseCase.Detalle detalle(@PathVariable UUID id) {
    return portal.detalle(id);
  }

  @PostMapping("/lotes")
  @ResponseStatus(HttpStatus.CREATED)
  public Lote cosecha(@Valid @RequestBody CosechaRequest r) {
    return portal.cosecha(
        new LoteBuilder()
            .identificado(r.id())
            .deParcela(r.parcelaId())
            .cosechado(r.fechaCosecha())
            .variedad(r.variedad())
            .recoleccion(r.tipoRecoleccion())
            .peso(r.pesoKg())
            .build());
  }

  @PostMapping("/lotes/{id}/evaluaciones")
  @ResponseStatus(HttpStatus.CREATED)
  public EvaluacionCalidad calidad(@PathVariable UUID id, @Valid @RequestBody CalidadRequest r) {
    return portal.calidad(
        new EvaluacionCalidad(
            UUID.randomUUID(), id, r.defectosPrimarios(), r.defectosSecundarios(), r.puntajeSca()));
  }

  @PostMapping("/lotes/{id}/riesgos")
  @ResponseStatus(HttpStatus.CREATED)
  public EvaluacionRiesgo riesgo(@PathVariable UUID id, @Valid @RequestBody RiesgoRequest r) {
    return portal.riesgo(id, r.humedad(), r.horasDemora());
  }

  @GetMapping("/alertas")
  public List<Alerta> alertas() {
    return portal.alertas();
  }

  @PutMapping("/alertas/{id}/decision")
  public Alerta decision(@PathVariable UUID id, @Valid @RequestBody DecisionRequest r) {
    return portal.revisar(id, r.operacionId(), r.estado(), r.motivo(), r.instruccion());
  }
}
