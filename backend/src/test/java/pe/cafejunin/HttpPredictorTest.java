package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import pe.cafejunin.adapters.out.prediction.*;

class HttpPredictorTest {
  @Test
  void contratoRechazaRespuestaInvalidaSinFallback() throws Exception {
    var s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    s.createContext(
        "/predict",
        e -> {
          byte[] b =
              "{\"probabilidad\":1.5,\"modelo\":\"test\",\"simulada\":true}"
                  .getBytes(StandardCharsets.UTF_8);
          e.sendResponseHeaders(200, b.length);
          e.getResponseBody().write(b);
          e.close();
        });
    s.start();
    try {
      var a =
          new HttpXgboostAdapter(
              HttpClient.newHttpClient(),
              URI.create("http://127.0.0.1:" + s.getAddress().getPort() + "/predict"),
              new ObjectMapper(),
              "test");
      assertThrows(PrediccionNoDisponibleException.class, () -> a.predecir(95, 36));
    } finally {
      s.stop(0);
    }
  }
}
