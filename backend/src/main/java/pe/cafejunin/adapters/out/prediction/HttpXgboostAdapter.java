package pe.cafejunin.adapters.out.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import pe.cafejunin.application.ports.output.PrediccionRiesgoPort;
import pe.cafejunin.domain.valueobjects.Probabilidad;

public final class HttpXgboostAdapter implements PrediccionRiesgoPort {
  private final HttpClient client;
  private final URI endpoint;
  private final ObjectMapper json;
  private final String key;

  public HttpXgboostAdapter(HttpClient c, URI u, ObjectMapper j, String k) {
    client = c;
    endpoint = u;
    json = j;
    key = k;
  }

  public Resultado predecir(double humedad, double horasDemora) {
    try {
      var request =
          HttpRequest.newBuilder(endpoint)
              .timeout(Duration.ofSeconds(5))
              .header("Content-Type", "application/json")
              .header("X-API-Key", key)
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      json.writeValueAsString(
                          Map.of("humedad", humedad, "horasDemora", horasDemora))))
              .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200)
        throw new IllegalStateException("Servicio IA respondió " + response.statusCode());
      var n = json.readTree(response.body());
      if (n.path("modelo").asText().isBlank()
          || n.path("modelo").asText().length() > 160
          || !n.path("probabilidad").isNumber()
          || !n.path("modelo").isTextual()
          || !n.path("simulada").isBoolean())
        throw new IllegalStateException("Contrato de predicción inválido");
      return new Resultado(
          new Probabilidad(n.get("probabilidad").doubleValue()),
          n.get("modelo").asText(),
          n.get("simulada").booleanValue());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new PrediccionNoDisponibleException(ex);
    } catch (Exception ex) {
      throw new PrediccionNoDisponibleException(ex);
    }
  }
}
