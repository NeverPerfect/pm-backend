package de.laetum.pmbackend.config;

import de.laetum.pmbackend.entity.Role;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisiert Standarddaten beim Anwendungsstart, z. B. einen Admin-Benutzer. Wird einmalig bei
 * der ersten Ausführung der Anwendung verwendet.
 */
@Component
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    // Erstellt einen Standard-Admin-Benutzer, falls noch nicht vorhanden
    if (!userRepository.existsByUsername("admin")) {
      User admin = new User();
      admin.setUsername("admin");
      admin.setFirstName("System");
      admin.setLastName("Administrator");
      admin.setPassword(passwordEncoder.encode("admin123"));
      admin.setActive(true);
      admin.setRole(Role.ADMIN);

      userRepository.save(admin);
      System.out.println("Admin-Benutzer angelegt: Benutzername = admin, Passwort = admin123");
    }
  }
}
