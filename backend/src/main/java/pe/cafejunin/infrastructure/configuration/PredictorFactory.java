package pe.cafejunin.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import pe.cafejunin.adapters.out.prediction.*;
import pe.cafejunin.application.ports.output.PrediccionRiesgoPort;

// Fábrica de configuración; Factory Method del dominio permanece en EventoFactory.
public final class PredictorFactory {
  private PredictorFactory() {}

  public static PrediccionRiesgoPort crear(
      String modo, String url, String apiKey, ObjectMapper json) {
    return switch (modo) {
      case "rules" -> new SimuladorRiesgoAdapter();
      case "xgboost" ->
          new HttpXgboostAdapter(
              HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build(),
              URI.create(url),
              json,
              apiKey);
      default -> throw new IllegalArgumentException("PREDICTOR_MODE debe ser rules o xgboost");
    };
  }
}
