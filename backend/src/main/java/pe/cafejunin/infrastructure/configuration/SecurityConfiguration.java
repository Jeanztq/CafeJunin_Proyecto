package pe.cafejunin.infrastructure.configuration;

import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
  @Bean
  @ConditionalOnProperty(
      name = "app.security.enabled",
      havingValue = "false",
      matchIfMissing = true)
  SecurityFilterChain demo(HttpSecurity http) throws Exception {
    // Demo local sin autenticación. No contiene inicio de sesión ni selección de privilegios.
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true")
  SecurityFilterChain oidc(HttpSecurity http) throws Exception {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          Object realm = jwt.getClaims().get("realm_access");
          if (!(realm instanceof Map<?, ?> map)
              || !(map.get("roles") instanceof Collection<?> roles)) return List.of();
          return roles.stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .<org.springframework.security.core.GrantedAuthority>map(
                  r -> new SimpleGrantedAuthority("ROLE_" + r))
              .toList();
        });
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(HttpMethod.GET, "/api/info")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/lotes")
                    .hasRole("PRODUCTOR")
                    .requestMatchers(HttpMethod.POST, "/api/lotes/*/evaluaciones")
                    .hasRole("CATADOR")
                    .requestMatchers(HttpMethod.POST, "/api/lotes/*/riesgos")
                    .hasRole("SUPERVISOR")
                    .requestMatchers(HttpMethod.GET, "/api/alertas")
                    .hasRole("SUPERVISOR")
                    .requestMatchers(HttpMethod.GET, "/api/lotes")
                    .hasAnyRole("PRODUCTOR", "CATADOR", "SUPERVISOR", "TRANSPORTISTA")
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
        .build();
  }
}
