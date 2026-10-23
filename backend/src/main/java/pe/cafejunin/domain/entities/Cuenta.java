package pe.cafejunin.domain.entities;

import java.util.Set;

public record Cuenta(String subject, String nombre, Set<String> roles, boolean demostracion) {
  public Cuenta {
    roles = Set.copyOf(roles);
  }

  public boolean tiene(String rol) {
    return roles.contains(rol);
  }
}
