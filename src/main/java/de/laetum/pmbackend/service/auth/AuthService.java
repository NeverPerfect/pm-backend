package de.laetum.pmbackend.service.auth;

import de.laetum.pmbackend.controller.auth.LoginRequest;
import de.laetum.pmbackend.controller.auth.LoginResponse;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.AuthenticationException;
import de.laetum.pmbackend.repository.user.UserRepository;
import de.laetum.pmbackend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication and login. Processes login requests and generates
 * JWT tokens.
 */
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthService(
      UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param request Login credentials (username and password)
   * @return LoginResponse with JWT token, user ID, username and role
   * @throws AuthenticationException if username does not exist, password is
   *                                 wrong,
   *                                 or user account is inactive
   */
  public LoginResponse login(LoginRequest request) {
    User user = userRepository
        .findByUsername(request.getUsername())
        .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

    if (!user.isActive()) {
      throw new AuthenticationException("User account is inactive");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new AuthenticationException("Invalid username or password");
    }

    String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

    return new LoginResponse(user.getId(), token, user.getUsername(), user.getRole().name());
  }
}