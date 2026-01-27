package de.laetum.pmbackend.config;

import de.laetum.pmbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Zentrale Sicherheitskonfiguration für die Backend-Anwendung. Definiert Authentifizierungs- und
 * Autorisierungsregeln sowie JWT-Integration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // Deaktiviert CSRF-Schutz für stateless JWT-Authentifizierung
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS)) // Stateless-Sessions für JWT
        .authorizeHttpRequests(
            auth ->
                auth
                    // Öffentliche Endpunkte (Authentifizierung und Swagger-Dokumentation)
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                    .permitAll()

                    // Benutzerverwaltung - Rollenbasierte Zugriffe
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/users/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/**")
                    .hasRole("ADMIN") // Nur Admins dürfen Benutzer anlegen
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**")
                    .hasRole("ADMIN") // Nur Admins dürfen Benutzer löschen

                    // Team-Endpunkte
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teams/my")
                    .authenticated() // Eigene Teams für alle authentifizierten Benutzer
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN") // Alle Team-Endpunkte für Manager/Admins
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")

                    // Schedule-Endpunkte
                    .requestMatchers("/api/schedules/user/**")
                    .hasAnyRole("MANAGER", "ADMIN") // Admin-Endpunkte für Schedules
                    .requestMatchers("/api/schedules/**")
                    .authenticated() // Eigene Schedules für alle authentifizierten Benutzer

                    // Projekt-Endpunkte
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/my")
                    .authenticated() // Eigene Projekte für alle authentifizierten Benutzer
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN") // Alle Projekt-Endpunkte für Manager/Admins
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")

                    // Standardregel: Alle anderen Anfragen erfordern Authentifizierung
                    .anyRequest()
                    .authenticated())
        // Fügt den JWT-Filter vor dem Standard-Authentifizierungsfilter ein
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
