package pe.cafejunin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class SecurityTest {
  @Autowired MockMvc mvc;
  @MockBean JwtDecoder decoder;

  @Test
  void exigeTokenYRol() throws Exception {
    mvc.perform(get("/api/lotes")).andExpect(status().isUnauthorized());
    mvc.perform(
            get("/api/alertas")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PRODUCTOR"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/alertas")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))))
        .andExpect(status().isOk());
  }

  @Test
  void productorNoPuedeRegistrarEnParcelaAjena() throws Exception {
    String body =
        "{\"id\":\""
            + java.util.UUID.randomUUID()
            + "\",\"parcelaId\":\"11111111-1111-1111-1111-111111111111\",\"fechaCosecha\":\"2025-01-01\",\"variedad\":\"BOURBON\",\"tipoRecoleccion\":\"SELECTIVA\",\"pesoKg\":20}";
    mvc.perform(
            post("/api/lotes")
                .with(
                    jwt()
                        .jwt(j -> j.subject("otro-productor"))
                        .authorities(new SimpleGrantedAuthority("ROLE_PRODUCTOR")))
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void usuarioSinEspecialidadNoVeLotesYNoPuedeEvaluar() throws Exception {
    mvc.perform(get("/api/lotes").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    mvc.perform(
            post("/api/lotes/" + java.util.UUID.randomUUID() + "/riesgos")
                .with(jwt())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"humedad\":80,\"horasDemora\":5}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void fichaUsaElCatadorDelToken() throws Exception {
    var lote = java.util.UUID.randomUUID();
    String cosecha = """
        {"id":"%s","parcelaId":"22222222-2222-2222-2222-222222222222","fechaCosecha":"2025-01-01",
         "variedad":"BOURBON","tipoRecoleccion":"SELECTIVA","pesoKg":120.5}
        """.formatted(lote);
    var productor = jwt().jwt(j -> j.subject("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1"))
        .authorities(new SimpleGrantedAuthority("ROLE_PRODUCTOR"));
    var json = org.springframework.http.MediaType.APPLICATION_JSON;
    mvc.perform(post("/api/lotes").with(productor).contentType(json).content(cosecha))
        .andExpect(status().isCreated());
    String ficha = """
        {"id":"%s","defectosPrimarios":0,"defectosSecundarios":2,"defectosTaza":{"leves":0,"graves":0},
         "atributos":{"fragancia":8,"sabor":8,"acidez":8,"cuerpo":8,"posgusto":8,
                      "balance":8,"uniformidad":10,"dulzor":10,"tazaLimpia":10,"apreciacionGlobal":8}}
        """.formatted(java.util.UUID.randomUUID());
    var ruta = "/api/lotes/" + lote + "/evaluaciones";
    mvc.perform(post(ruta).with(productor).contentType(json).content(ficha))
        .andExpect(status().isForbidden());
    var catador = jwt().jwt(j -> j.subject("q1").claim("preferred_username", "Ana"))
        .authorities(new SimpleGrantedAuthority("ROLE_CATADOR"));
    mvc.perform(post(ruta).with(catador).contentType(json).content(ficha))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.catadorId").value("q1"))
        .andExpect(jsonPath("$.catadorNombre").value("Ana"));
  }
}
