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
 * Central security configuration for the backend application.
 * Defines authentication and authorization rules as well as JWT integration.
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
    http.csrf(csrf -> csrf.disable()) // Disable CSRF protection for stateless JWT authentication
        .sessionManagement(
            session -> session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS)) // Stateless sessions for JWT
        .authorizeHttpRequests(
            auth -> auth
                // Public endpoints (authentication and Swagger documentation)
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

                // User management - role-based access
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/users/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/**")
                .hasRole("ADMIN") // Only admins can create users
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**")
                .hasRole("ADMIN") // Only admins can delete users

                // Team endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teams/my")
                .authenticated() // Own teams for all authenticated users
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teams/**")
                .hasAnyRole("MANAGER", "ADMIN") // All team endpoints for managers/admins
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/teams/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/teams/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/teams/**")
                .hasAnyRole("MANAGER", "ADMIN")

                // Schedule endpoints
                .requestMatchers("/api/schedules/user/**")
                .hasAnyRole("MANAGER", "ADMIN") // Admin endpoints for schedules
                .requestMatchers("/api/schedules/**")
                .authenticated() // Own schedules for all authenticated users

                // Project endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/my")
                .authenticated() // Own projects for all authenticated users
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/**")
                .hasAnyRole("MANAGER", "ADMIN") // All project endpoints for managers/admins
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projects/**")
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projects/**")
                .hasAnyRole("MANAGER", "ADMIN")

                // Category endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**")
                .authenticated() // All authenticated users can read categories
                .requestMatchers("/api/categories/**")
                .hasAnyRole("MANAGER", "ADMIN") // Only managers/admins can manage categories

                // Default: all other requests require authentication
                .anyRequest()
                .authenticated())
        // Add JWT filter before the default authentication filter
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
