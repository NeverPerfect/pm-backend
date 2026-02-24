package de.laetum.pmbackend.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import de.laetum.pmbackend.controller.auth.LoginRequest; 
import de.laetum.pmbackend.controller.auth.LoginResponse; 
import de.laetum.pmbackend.repository.user.Role;   
import de.laetum.pmbackend.repository.user.User; 
import de.laetum.pmbackend.exception.AuthenticationException;
import de.laetum.pmbackend.repository.user.UserRepository; 
import de.laetum.pmbackend.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import de.laetum.pmbackend.service.auth.AuthService; 

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  private User activeUser;
  private User inactiveUser;

  @BeforeEach
  void setUp() {
    activeUser = new User();
    activeUser.setUsername("testuser");
    activeUser.setPassword("encodedPassword");
    activeUser.setRole(Role.EMPLOYEE);
    activeUser.setActive(true);

    inactiveUser = new User();
    inactiveUser.setUsername("inactive");
    inactiveUser.setPassword("encodedPassword");
    inactiveUser.setRole(Role.EMPLOYEE);
    inactiveUser.setActive(false);
  }

  @Test
  @DisplayName("Login succeeds with valid credentials")
  void login_WithValidCredentials_ReturnsToken() {
    // Arrange
    LoginRequest request = new LoginRequest("testuser", "correctPassword");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
    when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);
    when(jwtService.generateToken("testuser", "EMPLOYEE")).thenReturn("fake-jwt-token");

    // Act
    LoginResponse response = authService.login(request);

    // Assert
    assertNotNull(response);
    assertEquals("fake-jwt-token", response.getToken());
    assertEquals("testuser", response.getUsername());
    assertEquals("EMPLOYEE", response.getRole());
  }

  @Test
  @DisplayName("Login fails with non-existent username")
  void login_WithInvalidUsername_ThrowsException() {
    // Arrange
    LoginRequest request = new LoginRequest("unknown", "password");
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    // Act & Assert
    AuthenticationException exception =
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    assertEquals("Invalid username or password", exception.getMessage());
  }

  @Test
  @DisplayName("Login fails with wrong password")
  void login_WithWrongPassword_ThrowsException() {
    // Arrange
    LoginRequest request = new LoginRequest("testuser", "wrongPassword");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    // Act & Assert
    AuthenticationException exception =
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    assertEquals("Invalid username or password", exception.getMessage());
  }

  @Test
  @DisplayName("Login fails for inactive user")
  void login_WithInactiveUser_ThrowsException() {
    // Arrange
    LoginRequest request = new LoginRequest("inactive", "password");
    when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(inactiveUser));

    // Act & Assert
    AuthenticationException exception =
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    assertEquals("User account is inactive", exception.getMessage());
  }
}
