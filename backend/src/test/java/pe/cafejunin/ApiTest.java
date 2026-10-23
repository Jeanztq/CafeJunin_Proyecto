package pe.cafejunin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class ApiTest {
  @Autowired MockMvc mvc;

  @Test
  void recorridoCompletoYValidaciones() throws Exception {
    String id = UUID.randomUUID().toString();
    String body =
        """
{"id":"%s","parcelaId":"11111111-1111-1111-1111-111111111111","fechaCosecha":"2025-01-01","variedad":"BOURBON","tipoRecoleccion":"SELECTIVA","pesoKg":120.5}
"""
            .formatted(id);
    mvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/lotes/" + id + "/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"defectosPrimarios\":0,\"defectosSecundarios\":2,\"puntajeSca\":85}"))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/lotes/" + id + "/riesgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"humedad\":95,\"horasDemora\":36}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.simulada").value(true));
    mvc.perform(get("/api/alertas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    mvc.perform(
            post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("120.5", "0")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/lotes/" + UUID.randomUUID() + "/riesgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"humedad\":95,\"horasDemora\":36}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void interfazDemoDisponible() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }
}
