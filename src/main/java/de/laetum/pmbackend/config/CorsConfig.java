package de.laetum.pmbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Globale CORS-Konfiguration für die Never Perfect Backend-Anwendung. Erlaubt Anfragen von lokalen
 * Frontend-Instanzen (z. B. Angular Dev-Server).
 */
@Configuration
public class CorsConfig {

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:4200",
                "http://127.0.0.1:4200") // Frontend-URLs für Entwicklungsumgebung
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true); // Wichtig für Authentifizierung (z. B. Cookies/JWT)
      }
    };
  }
}
