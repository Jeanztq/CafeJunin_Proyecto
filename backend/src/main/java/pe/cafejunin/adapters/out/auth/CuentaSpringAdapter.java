package pe.cafejunin.adapters.out.auth;

import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.cafejunin.application.ports.output.CuentaActualPort;
import pe.cafejunin.application.usecases.ProhibidoException;
import pe.cafejunin.domain.entities.Cuenta;

public final class CuentaSpringAdapter implements CuentaActualPort {
  private static final Set<String> ROLES_PORTAL = Set.of("PRODUCTOR", "CATADOR", "SUPERVISOR", "TRANSPORTISTA");
  private final boolean seguridad;

  public CuentaSpringAdapter(boolean activa) {
    seguridad = activa;
  }

  public Cuenta actual() {
    if (!seguridad)
      return new Cuenta(
          "operador-local", "Operador local", Set.of("PRODUCTOR", "CATADOR", "SUPERVISOR"), true);
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) throw new ProhibidoException();
    Set<String> roles = new HashSet<>();
    for (var rol : ROLES_PORTAL) {
      if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + rol))) {
        roles.add(rol);
      }
    }
    return new Cuenta(
        jwt.getSubject(),
        Optional.ofNullable(jwt.getClaimAsString("preferred_username")).orElse(jwt.getSubject()),
        roles,
        false);
  }
}
