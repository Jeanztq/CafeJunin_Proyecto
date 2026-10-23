package pe.cafejunin.adapters.out.auth;

import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.cafejunin.application.ports.output.CuentaActualPort;
import pe.cafejunin.application.usecases.ProhibidoException;
import pe.cafejunin.domain.entities.Cuenta;

public final class CuentaSpringAdapter implements CuentaActualPort {
  private final boolean seguridad;

  public CuentaSpringAdapter(boolean activa) {
    seguridad = activa;
  }

  public Cuenta actual() {
    if (!seguridad)
      return new Cuenta(
          "demo", "Demostración local", Set.of("PRODUCTOR", "CATADOR", "SUPERVISOR"), true);
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) throw new ProhibidoException();
    Set<String> roles = new HashSet<>();
    auth.getAuthorities()
        .forEach(
            a -> {
              if (a.getAuthority().startsWith("ROLE_")) roles.add(a.getAuthority().substring(5));
            });
    return new Cuenta(
        jwt.getSubject(),
        Optional.ofNullable(jwt.getClaimAsString("preferred_username")).orElse(jwt.getSubject()),
        roles,
        false);
  }
}
