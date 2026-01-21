package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.LoginRequest;
import de.laetum.pmbackend.dto.LoginResponse;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.exception.AuthenticationException;
import de.laetum.pmbackend.repository.UserRepository;
import de.laetum.pmbackend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

  public LoginResponse login(LoginRequest request) {
    User user =
        userRepository
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
