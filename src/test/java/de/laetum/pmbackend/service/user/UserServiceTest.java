package de.laetum.pmbackend.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.controller.user.CreateUserRequest;
import de.laetum.pmbackend.controller.user.UpdateUserRequest;
import de.laetum.pmbackend.controller.user.UserDto;
import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.service.user.UserService;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.user.UserRepository;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.LastAdminDeletionException;
import de.laetum.pmbackend.exception.SelfModificationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import de.laetum.pmbackend.service.user.UserMapper;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

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
    when(userMapper.map(any(User.class))).thenAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.isActive(), u.getRole());
    });

    // Default: authenticate as a non-admin user that won't interfere with tests
    mockAuthenticatedUser("otheruser");
    User otherUser = new User();
    otherUser.setId(99L);
    otherUser.setUsername("otheruser");
    otherUser.setRole(Role.MANAGER);
    when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
  }

  /**
   * Sets up a mocked SecurityContext so that getAuthenticatedUser()
   * resolves to the user with the given username.
   */
  private void mockAuthenticatedUser(String username) {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(username);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
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
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
        () -> userService.getUserById(99L));
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
    RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
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
    RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(1L, request));
    assertTrue(exception.getMessage().contains("already exists"));
  }

  // ==================== deleteUser ====================

  @Test
  @DisplayName("deleteUser deletes user successfully")
  void deleteUser_WhenUserExists_DeletesUser() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act
    userService.deleteUser(1L);

    // Assert
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteUser throws exception when user not found")
  void deleteUser_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    verify(userRepository, never()).deleteById(any());
  }

  // ==================== Admin Self-Protection ====================

  @Test
  @DisplayName("deleteUser throws exception when user tries to delete themselves")
  void deleteUser_WhenUserDeletesSelf_ThrowsException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    mockAuthenticatedUser("testuser");

    // Act & Assert
    SelfModificationException exception = assertThrows(
        SelfModificationException.class,
        () -> userService.deleteUser(1L));
    assertEquals(SelfModificationException.SELF_DELETE, exception.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteUser allows admin to delete a different user")
  void deleteUser_WhenAdminDeletesOther_Succeeds() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("admin");
    adminUser.setRole(Role.ADMIN);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
    mockAuthenticatedUser("admin");

    // Act
    userService.deleteUser(1L);

    // Assert
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("updateUser throws exception when admin demotes themselves")
  void updateUser_WhenAdminDemotesSelf_ThrowsException() {
    // Arrange
    testUser.setRole(Role.ADMIN);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("testuser");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    mockAuthenticatedUser("testuser");

    // Act & Assert
    SelfModificationException exception = assertThrows(
        SelfModificationException.class,
        () -> userService.updateUser(1L, request));
    assertEquals(SelfModificationException.ADMIN_SELF_DEMOTE, exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser allows admin to update themselves without role change")
  void updateUser_WhenAdminUpdatesSelfKeepingRole_Succeeds() {
    // Arrange
    testUser.setRole(Role.ADMIN);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("testuser");
    request.setFirstName("Updated");
    request.setLastName("Admin");
    request.setRole(Role.ADMIN);
    request.setActive(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    mockAuthenticatedUser("testuser");

    // Act
    UserDto result = userService.updateUser(1L, request);

    // Assert
    assertNotNull(result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("updateUser throws exception when admin deactivates themselves")
  void updateUser_WhenAdminDeactivatesSelf_ThrowsException() {
    // Arrange
    testUser.setRole(Role.ADMIN);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("testuser");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.ADMIN);
    request.setActive(false); // <-- deactivating self

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    mockAuthenticatedUser("testuser");

    // Act & Assert
    SelfModificationException exception = assertThrows(
        SelfModificationException.class,
        () -> userService.updateUser(1L, request));
    assertEquals(SelfModificationException.ADMIN_SELF_DEACTIVATE, exception.getMessage());
    verify(userRepository, never()).save(any());
  }
  // ==================== Last Admin Protection ====================

  @Test
  @DisplayName("deleteUser throws exception when deleting last active admin")
  void deleteUser_WhenDeletingLastActiveAdmin_ThrowsException() {
    // Arrange
    User lastAdmin = new User();
    lastAdmin.setId(3L);
    lastAdmin.setUsername("lastadmin");
    lastAdmin.setRole(Role.ADMIN);
    lastAdmin.setActive(true);

    User currentAdmin = new User();
    currentAdmin.setId(4L);
    currentAdmin.setUsername("currentadmin");
    currentAdmin.setRole(Role.ADMIN);

    when(userRepository.findById(3L)).thenReturn(Optional.of(lastAdmin));
    when(userRepository.findByUsername("currentadmin")).thenReturn(Optional.of(currentAdmin));
    when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);
    mockAuthenticatedUser("currentadmin");

    // Act & Assert
    LastAdminDeletionException exception = assertThrows(
        LastAdminDeletionException.class,
        () -> userService.deleteUser(3L));
    assertEquals(LastAdminDeletionException.MESSAGE, exception.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteUser allows deletion of admin when other active admins exist")
  void deleteUser_WhenOtherActiveAdminsExist_Succeeds() {
    // Arrange
    User adminToDelete = new User();
    adminToDelete.setId(3L);
    adminToDelete.setUsername("admintodelete");
    adminToDelete.setRole(Role.ADMIN);
    adminToDelete.setActive(true);

    User currentAdmin = new User();
    currentAdmin.setId(4L);
    currentAdmin.setUsername("currentadmin");
    currentAdmin.setRole(Role.ADMIN);

    when(userRepository.findById(3L)).thenReturn(Optional.of(adminToDelete));
    when(userRepository.findByUsername("currentadmin")).thenReturn(Optional.of(currentAdmin));
    when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(2L);
    mockAuthenticatedUser("currentadmin");

    // Act
    userService.deleteUser(3L);

    // Assert
    verify(userRepository).deleteById(3L);
  }

  @Test
  @DisplayName("deleteUser allows deletion of inactive admin even if last")
  void deleteUser_WhenDeletingInactiveAdmin_Succeeds() {
    // Arrange
    User inactiveAdmin = new User();
    inactiveAdmin.setId(3L);
    inactiveAdmin.setUsername("inactiveadmin");
    inactiveAdmin.setRole(Role.ADMIN);
    inactiveAdmin.setActive(false);

    User currentAdmin = new User();
    currentAdmin.setId(4L);
    currentAdmin.setUsername("currentadmin");
    currentAdmin.setRole(Role.ADMIN);

    when(userRepository.findById(3L)).thenReturn(Optional.of(inactiveAdmin));
    when(userRepository.findByUsername("currentadmin")).thenReturn(Optional.of(currentAdmin));
    mockAuthenticatedUser("currentadmin");

    // Act
    userService.deleteUser(3L);

    // Assert
    verify(userRepository).deleteById(3L);
    // Note: countByRoleAndActiveTrue is NOT called because the admin is inactive
    verify(userRepository, never()).countByRoleAndActiveTrue(any());
  }
}
