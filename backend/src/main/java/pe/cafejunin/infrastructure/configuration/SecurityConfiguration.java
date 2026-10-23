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
  org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder(
      @org.springframework.beans.factory.annotation.Value("${app.keycloak.url}") String url,
      @org.springframework.beans.factory.annotation.Value("${app.keycloak.realm}") String realm,
      @org.springframework.beans.factory.annotation.Value("${app.security.audience}")
          String audience) {
    String issuer = url + "/realms/" + realm;
    var decoder =
        org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(
                issuer + "/protocol/openid-connect/certs")
            .build();
    org.springframework.security.oauth2.core.OAuth2TokenValidator<
            org.springframework.security.oauth2.jwt.Jwt>
        aud =
            jwt ->
                jwt.getAudience().contains(audience)
                    ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                    : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                        new org.springframework.security.oauth2.core.OAuth2Error(
                            "invalid_token", "Audiencia inválida", null));
    decoder.setJwtValidator(
        new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithIssuer(issuer),
            aud));
    return decoder;
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
                a.requestMatchers(
                        HttpMethod.GET,
                        "/api/info",
                        "/api/config",
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/favicon.svg")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
        .build();
  }
}
