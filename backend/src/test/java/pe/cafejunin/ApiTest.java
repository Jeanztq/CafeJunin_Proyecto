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

@SpringBootTest(properties = "app.security.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class ApiTest {
  @Autowired MockMvc mvc;
  @Autowired pe.cafejunin.adapters.out.persistence.MemoryStore memoria;

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
    String ficha = """
{"id":"%s","defectosPrimarios":0,"defectosSecundarios":2,"defectosTaza":{"leves":1,"graves":0},"atributos":{"fragancia":8,"sabor":8.5,"acidez":8,"cuerpo":8.5,"posgusto":8,"balance":8,"uniformidad":10,"dulzor":10,"tazaLimpia":10,"apreciacionGlobal":8}}
""".formatted(UUID.randomUUID());
    mvc.perform(
            post("/api/lotes/" + id + "/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ficha))
        .andExpect(status().isCreated());
    var evaluaciones = "/api/lotes/" + id + "/evaluaciones";
    mvc.perform(post(evaluaciones).contentType(MediaType.APPLICATION_JSON).content(ficha))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.catadorId").value("operador-local"));
    mvc.perform(post(evaluaciones).contentType(MediaType.APPLICATION_JSON).content(ficha.replace("\"leves\":1", "\"leves\":0")))
        .andExpect(status().isConflict());
    for (String invalida : new String[] {ficha.replace("\"atributos\":", "\"puntajeSca\":100,\"atributos\":"),
        ficha.replace("\"leves\":1", "\"leves\":6"), ficha.replace("\"tazaLimpia\":10,", ""),
        ficha.replace("\"dulzor\":10", "\"dulzor\":9"), ficha.replace("8.5", "8.55"), ficha.replace("8.5", "12.5"),
        ficha.replace("\"dulzor\":10", "\"dulzor\":null"), ficha.replace("\"fragancia\":8,", ""),
        ficha.replace("\"defectosPrimarios\":0", "\"defectosPrimarios\":0.5")}) {
      mvc.perform(post(evaluaciones).contentType(MediaType.APPLICATION_JSON).content(invalida))
          .andExpect(status().isBadRequest());
    }
    var eventos = memoria.eventosPendientes().stream()
        .filter(e -> e.agregadoId().toString().equals(id) && e.tipo().equals("EvaluacionCalidadRegistrada")).toList();
    org.junit.jupiter.api.Assertions.assertEquals(1, eventos.size());
    org.junit.jupiter.api.Assertions.assertEquals(2, eventos.get(0).version());
    mvc.perform(get("/api/lotes/" + id + "/detalle")).andExpect(jsonPath("$.calidad.length()").value(1))
        .andExpect(jsonPath("$.calidad[0].puntajeSca").value(85))
        .andExpect(jsonPath("$.historial.length()").value(2))
        .andExpect(jsonPath("$.calidad[0].protocolo").value("SCA-2004"))
        .andExpect(jsonPath("$.calidad[0].atributos.sabor").value(8.5))
        .andExpect(jsonPath("$.calidad[0].clasificacion").value("Café especial"));
    mvc.perform(
            post("/api/lotes/" + id + "/riesgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"humedad\":95,\"horasDemora\":36}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.simulada").value(true));
    var alerta = memoria.listarAlertas().stream().filter(a -> a.loteId().toString().equals(id))
        .findFirst().orElseThrow();
    // Una evaluación posterior del mismo lote no sustituye la evidencia de la alerta.
    mvc.perform(post("/api/lotes/" + id + "/riesgos").contentType(MediaType.APPLICATION_JSON)
        .content("{\"humedad\":40,\"horasDemora\":1}")).andExpect(status().isCreated());
    mvc.perform(get("/api/alertas/" + alerta.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alerta.registradaEn").isString())
        .andExpect(jsonPath("$.evaluacion.id").value(alerta.evaluacionRiesgoId().toString()))
        .andExpect(jsonPath("$.evaluacion.humedad").value(95.0))
        .andExpect(jsonPath("$.evaluacion.horasDemora").value(36.0))
        .andExpect(jsonPath("$.umbralAlerta").value(0.7))
        .andExpect(jsonPath("$.lote.id").value(id))
        .andExpect(jsonPath("$.parcela.id").value("11111111-1111-1111-1111-111111111111"));
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
  void portalDisponible() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }
}
