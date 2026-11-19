package pe.cafejunin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:transporte;DB_CLOSE_DELAY=-1")
@ActiveProfiles("local")
@AutoConfigureMockMvc
class TransporteTest {
  @Autowired MockMvc mvc;
  private static final String CONDUCTOR = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4";
  private static final String JSON = "application/json";

  private RequestPostProcessor cuenta(String rol, String subject) {
    return jwt().jwt(j -> j.subject(subject)).authorities(new SimpleGrantedAuthority("ROLE_" + rol));
  }

  @Test
  void dosRolesCompartenElTrayectoSinDuplicarEventosNiExponerEnviosAjenos() throws Exception {
    var productor = cuenta("PRODUCTOR", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
    var conductor = cuenta("TRANSPORTISTA", CONDUCTOR);
    var otro = cuenta("TRANSPORTISTA", "otro-conductor");
    var supervisor = cuenta("SUPERVISOR", "supervisor");
    var lote = UUID.randomUUID();
    String ruta = "/api/transporte/envios/" + lote;
    String asignacion = "{\"transportista\":\"" + CONDUCTOR + "\"}";
    String salida = "{\"placa\":\"abc123\",\"pesoCargaKg\":95.5}";
    String control = "{\"tipo\":\"CONTROL\",\"lugar\":\"Huancayo\"}";
    String incidente = "{\"tipo\":\"DERRUMBE\",\"lugar\":\"La Oroya\",\"descripcion\":\"Vía interrumpida\"}";
    String punto = ruta + "/novedades/" + UUID.randomUUID();
    mvc.perform(post("/api/lotes").with(productor).contentType(JSON).content("""
        {"id":"%s","parcelaId":"22222222-2222-2222-2222-222222222222","variedad":"BOURBON",
        "fechaCosecha":"2025-01-01","pesoKg":100,"tipoRecoleccion":"SELECTIVA"}
        """.formatted(lote))).andExpect(status().isCreated());
    mvc.perform(get(ruta)).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/transporte/envios").with(cuenta("CATADOR","catador"))).andExpect(status().isForbidden());
    mvc.perform(put(ruta).with(conductor).contentType(JSON).content(asignacion)).andExpect(status().isForbidden());
    mvc.perform(put(ruta).with(supervisor).contentType(JSON).content("{\"transportista\":\"desconocido\"}"))
        .andExpect(status().isBadRequest());
    for (int i=0; i<2; i++)
      mvc.perform(put(ruta).with(supervisor).contentType(JSON).content(asignacion)).andExpect(status().isOk());
    mvc.perform(get("/api/transporte/envios").with(otro)).andExpect(content().json("[]"));
    mvc.perform(get(ruta).with(otro)).andExpect(status().isForbidden());
    mvc.perform(put(ruta+"/despacho").with(otro).contentType(JSON).content(salida)).andExpect(status().isForbidden());
    mvc.perform(put(ruta+"/despacho").with(supervisor).contentType(JSON).content(salida)).andExpect(status().isForbidden());
    mvc.perform(put(punto).with(conductor).contentType(JSON).content(control)).andExpect(status().isConflict());
    mvc.perform(put(ruta+"/despacho").with(conductor).contentType(JSON).content(salida.replace("95.5","101")))
        .andExpect(status().isBadRequest());
    for (int i=0; i<2; i++) {
      mvc.perform(put(ruta+"/despacho").with(conductor).contentType(JSON).content(salida))
          .andExpect(status().isOk()).andExpect(jsonPath("$.placa").value("ABC-123"));
      mvc.perform(put(punto).with(conductor).contentType(JSON).content(control)).andExpect(status().isOk());
    }
    mvc.perform(put(punto).with(conductor).contentType(JSON).content(incidente)).andExpect(status().isConflict());
    String reporte = ruta+"/novedades/"+UUID.randomUUID();
    mvc.perform(put(reporte).with(conductor).contentType(JSON).content(incidente.replace("Vía interrumpida"," ")))
        .andExpect(status().isBadRequest());
    mvc.perform(put(reporte).with(conductor).contentType(JSON).content(incidente)).andExpect(status().isOk());
    mvc.perform(get(ruta).with(supervisor)).andExpect(status().isOk())
        .andExpect(jsonPath("$.envio.pesoCargaKg").value(95.5))
        .andExpect(jsonPath("$.novedades.length()").value(2))
        .andExpect(jsonPath("$.novedades[0].tipo").value("CONTROL"))
        .andExpect(jsonPath("$.novedades[1].descripcion").value("Vía interrumpida"));
    mvc.perform(get("/api/lotes/"+lote+"/detalle").with(supervisor)).andExpect(jsonPath("$.historial.length()").value(5));
  }
}
