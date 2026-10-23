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
}
