package de.laetum.pmbackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.dto.CreateUserRequest;
import de.laetum.pmbackend.dto.UpdateUserRequest;
import de.laetum.pmbackend.dto.UserDto;
import de.laetum.pmbackend.entity.Role;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setRole(Role.EMPLOYEE);
    testUser.setActive(true);
  }

  // ==================== getAllUsers ====================

  @Test
  @DisplayName("getAllUsers returns list of all users")
  void getAllUsers_ReturnsAllUsers() {
    // Arrange
    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setFirstName("Second");
    user2.setLastName("User");
    user2.setRole(Role.MANAGER);
    user2.setActive(true);

    when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

    // Act
    List<UserDto> result = userService.getAllUsers();

    // Assert
    assertEquals(2, result.size());
    assertEquals("testuser", result.get(0).getUsername());
    assertEquals("user2", result.get(1).getUsername());
  }

  @Test
  @DisplayName("getAllUsers returns empty list when no users exist")
  void getAllUsers_WhenNoUsers_ReturnsEmptyList() {
    // Arrange
    when(userRepository.findAll()).thenReturn(Arrays.asList());

    // Act
    List<UserDto> result = userService.getAllUsers();

    // Assert
    assertTrue(result.isEmpty());
  }

  // ==================== getUserById ====================

  @Test
  @DisplayName("getUserById returns user when found")
  void getUserById_WhenUserExists_ReturnsUser() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act
    UserDto result = userService.getUserById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("Test", result.getFirstName());
  }

  @Test
  @DisplayName("getUserById throws exception when user not found")
  void getUserById_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    assertTrue(exception.getMessage().contains("99"));
  }

  // ==================== createUser ====================

  @Test
  @DisplayName("createUser creates new user successfully")
  void createUser_WithValidData_CreatesUser() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword("password123");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    // Act
    UserDto result = userService.createUser(request);

    // Assert
    assertNotNull(result);
    assertEquals("newuser", result.getUsername());
    assertEquals("New", result.getFirstName());
    verify(passwordEncoder).encode("password123");
  }

  @Test
  @DisplayName("createUser throws exception when username already exists")
  void createUser_WhenUsernameExists_ThrowsException() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("testuser");
    request.setPassword("password");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> userService.createUser(request));
    assertTrue(exception.getMessage().contains("already exists"));
  }

  // ==================== updateUser ====================

  @Test
  @DisplayName("updateUser updates user successfully")
  void updateUser_WithValidData_UpdatesUser() {
    // Arrange
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("updateduser");
    request.setFirstName("Updated");
    request.setLastName("Name");
    request.setRole(Role.MANAGER);
    request.setActive(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    UserDto result = userService.updateUser(1L, request);

    // Assert
    assertNotNull(result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("updateUser updates password when provided")
  void updateUser_WithNewPassword_UpdatesPassword() {
    // Arrange
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("testuser");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);
    request.setPassword("newPassword");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    userService.updateUser(1L, request);

    // Assert
    verify(passwordEncoder).encode("newPassword");
  }

  @Test
  @DisplayName("updateUser throws exception when user not found")
  void updateUser_WhenUserNotFound_ThrowsException() {
    // Arrange
    UpdateUserRequest request = new UpdateUserRequest();
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, request));
  }

  @Test
  @DisplayName("updateUser throws exception when new username conflicts with another user")
  void updateUser_WhenUsernameConflicts_ThrowsException() {
    // Arrange
    User otherUser = new User();
    otherUser.setId(2L);
    otherUser.setUsername("existinguser");

    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("existinguser");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(otherUser));

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, request));
    assertTrue(exception.getMessage().contains("already exists"));
  }

  // ==================== deleteUser ====================

  @Test
  @DisplayName("deleteUser deletes user successfully")
  void deleteUser_WhenUserExists_DeletesUser() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);

    // Act
    userService.deleteUser(1L);

    // Assert
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteUser throws exception when user not found")
  void deleteUser_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(userRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    verify(userRepository, never()).deleteById(any());
  }
}
