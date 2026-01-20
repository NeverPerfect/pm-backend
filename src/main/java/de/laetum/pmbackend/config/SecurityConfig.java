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
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/users/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**")
                    .hasRole("ADMIN")
                    // Teams - nur MANAGER und ADMIN
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/teams/**")
                    .hasAnyRole("MANAGER", "ADMIN")

                    // Projects - nur MANAGER und ADMIN
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projects/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    .anyRequest()
                    .authenticated())
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
