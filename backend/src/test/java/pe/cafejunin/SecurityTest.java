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
}
