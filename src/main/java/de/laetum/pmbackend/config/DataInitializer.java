package de.laetum.pmbackend.config;

import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.repository.user.UserRepository;
import de.laetum.pmbackend.service.user.PasswordGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default data on application startup,
 * e.g. a default admin user. Runs once on first startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordGenerator passwordGenerator;

  public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
      PasswordGenerator passwordGenerator) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordGenerator = passwordGenerator;
  }

  @Override
  public void run(String... args) {
    // Create default admin user if not present
    if (!userRepository.existsByUsername("admin")) {
      String generatedPassword = passwordGenerator.generate();

      User admin = new User();
      admin.setUsername("admin");
      admin.setFirstName("System");
      admin.setLastName("Administrator");
      admin.setPassword(passwordEncoder.encode(generatedPassword));
      admin.setActive(true);
      admin.setRole(Role.ADMIN);

      userRepository.save(admin);
      System.out.println("========================================");
      System.out.println("  Default admin account created");
      System.out.println("  Username: admin");
      System.out.println("  Password: " + generatedPassword);
      System.out.println("  CHANGE THIS PASSWORD IMMEDIATELY!");
      System.out.println("========================================");
    }
  }
}