package de.laetum.pmbackend.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.controller.user.CreateUserRequest;
import de.laetum.pmbackend.controller.user.UpdateUserRequest;
import de.laetum.pmbackend.controller.user.UserDto;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;
import de.laetum.pmbackend.repository.team.Team;
import de.laetum.pmbackend.repository.team.TeamRepository;
import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.service.user.UserService;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.user.UserRepository;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.DuplicateResourceException;
import de.laetum.pmbackend.exception.ForbiddenOperationException;
import de.laetum.pmbackend.exception.LastAdminDeletionException;
import de.laetum.pmbackend.exception.PasswordPolicyException;
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
import de.laetum.pmbackend.exception.UserInUseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private PasswordGenerator passwordGenerator;

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
      return new UserDto(u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.isActive(), u.getRole(),
          null);
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

  // ==================== Admin Visibility Restriction ====================

  @Test
  @DisplayName("getAllUsers as manager excludes admin users")
  void getAllUsers_AsManager_ExcludesAdmins() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setFirstName("Admin");
    adminUser.setLastName("User");
    adminUser.setRole(Role.ADMIN);
    adminUser.setActive(true);

    User managerUser = new User();
    managerUser.setId(3L);
    managerUser.setUsername("manageruser");
    managerUser.setFirstName("Manager");
    managerUser.setLastName("User");
    managerUser.setRole(Role.MANAGER);
    managerUser.setActive(true);

    when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, adminUser, managerUser));
    when(userRepository.findByUsername("manageruser")).thenReturn(Optional.of(managerUser));
    mockAuthenticatedUser("manageruser");

    // Act
    List<UserDto> result = userService.getAllUsers();

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().noneMatch(u -> u.getRole() == Role.ADMIN));
  }

  @Test
  @DisplayName("getAllUsers as admin includes all users")
  void getAllUsers_AsAdmin_IncludesAdmins() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setFirstName("Admin");
    adminUser.setLastName("User");
    adminUser.setRole(Role.ADMIN);
    adminUser.setActive(true);

    when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, adminUser));
    when(userRepository.findByUsername("currentadmin")).thenReturn(Optional.of(adminUser));
    mockAuthenticatedUser("currentadmin");

    // Act
    List<UserDto> result = userService.getAllUsers();

    // Assert
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("getUserById as manager throws exception for admin user")
  void getUserById_AsManager_ThrowsForAdminUser() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setRole(Role.ADMIN);

    when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

    // Act & Assert
    ForbiddenOperationException exception = assertThrows(
        ForbiddenOperationException.class,
        () -> userService.getUserById(2L));
    assertEquals(ForbiddenOperationException.ADMIN_NOT_VISIBLE, exception.getMessage());
  }

  @Test
  @DisplayName("getUserById as admin returns admin user")
  void getUserById_AsAdmin_ReturnsAdminUser() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setRole(Role.ADMIN);

    User currentAdmin = new User();
    currentAdmin.setId(3L);
    currentAdmin.setUsername("currentadmin");
    currentAdmin.setRole(Role.ADMIN);

    when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
    when(userRepository.findByUsername("currentadmin")).thenReturn(Optional.of(currentAdmin));
    mockAuthenticatedUser("currentadmin");

    // Act
    UserDto result = userService.getUserById(2L);

    // Assert
    assertNotNull(result);
    assertEquals("adminuser", result.getUsername());
  }

  // ==================== createUser ====================

  @Test
  @DisplayName("createUser generates password when none provided")
  void createUser_WhenNoPassword_GeneratesPassword() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword(null);
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    when(passwordGenerator.generate()).thenReturn("GeneratedPass123!");
    when(passwordEncoder.encode("GeneratedPass123!")).thenReturn("encodedGenerated");
    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> {
          User saved = invocation.getArgument(0);
          saved.setId(2L);
          return saved;
        });

    // Act
    UserDto result = userService.createUser(request);

    // Assert
    assertNotNull(result);
    assertEquals("GeneratedPass123!", result.getGeneratedPassword());
    verify(passwordGenerator).generate();
    verify(passwordEncoder).encode("GeneratedPass123!");
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
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
        () -> userService.createUser(request));
    assertTrue(exception.getMessage().contains("existiert bereits"));
  }

  // ==================== Password Policy ====================

  @Test
  @DisplayName("createUser throws exception when manual password is too short")
  void createUser_WhenPasswordTooShort_ThrowsException() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword("Short1!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    // Act & Assert
    PasswordPolicyException exception = assertThrows(PasswordPolicyException.class,
        () -> userService.createUser(request));
    assertEquals(PasswordPolicyException.TOO_SHORT, exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("createUser throws exception when password missing uppercase")
  void createUser_WhenPasswordMissingUppercase_ThrowsException() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword("lowercase1!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    // Act & Assert
    PasswordPolicyException exception = assertThrows(PasswordPolicyException.class,
        () -> userService.createUser(request));
    assertEquals(PasswordPolicyException.MISSING_UPPERCASE, exception.getMessage());
  }

  @Test
  @DisplayName("createUser throws exception when password missing special character")
  void createUser_WhenPasswordMissingSpecial_ThrowsException() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword("NoSpecial1Aa");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    // Act & Assert
    PasswordPolicyException exception = assertThrows(PasswordPolicyException.class,
        () -> userService.createUser(request));
    assertEquals(PasswordPolicyException.MISSING_SPECIAL, exception.getMessage());
  }

  @Test
  @DisplayName("createUser accepts valid manual password")
  void createUser_WhenPasswordValid_Succeeds() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword("ValidPass1!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("ValidPass1!")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User saved = invocation.getArgument(0);
      saved.setId(2L);
      return saved;
    });

    // Act
    UserDto result = userService.createUser(request);

    // Assert
    assertNotNull(result);
    assertNull(result.getGeneratedPassword());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("createUser skips validation for auto-generated passwords")
  void createUser_WhenNoPassword_SkipsValidation() {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser");
    request.setPassword(null);
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);

    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    when(passwordGenerator.generate()).thenReturn("GeneratedPass123!");
    when(passwordEncoder.encode("GeneratedPass123!")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User saved = invocation.getArgument(0);
      saved.setId(2L);
      return saved;
    });

    // Act
    UserDto result = userService.createUser(request);

    // Assert
    assertNotNull(result);
    assertEquals("GeneratedPass123!", result.getGeneratedPassword());
  }

  @Test
  @DisplayName("updateUser throws exception when new password violates policy")
  void updateUser_WhenNewPasswordInvalid_ThrowsException() {
    // Arrange
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUsername("testuser");
    request.setFirstName("Test");
    request.setLastName("User");
    request.setRole(Role.EMPLOYEE);
    request.setActive(true);
    request.setPassword("weak");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act & Assert
    PasswordPolicyException exception = assertThrows(PasswordPolicyException.class,
        () -> userService.updateUser(1L, request));
    assertEquals(PasswordPolicyException.TOO_SHORT, exception.getMessage());
    verify(userRepository, never()).save(any());
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
    request.setPassword("NewPassword1!");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode("NewPassword1!")).thenReturn("newEncodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    userService.updateUser(1L, request);

    // Assert
    verify(passwordEncoder).encode("NewPassword1!");
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
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
        () -> userService.updateUser(1L, request));
    assertTrue(exception.getMessage().contains("existiert bereits"));
  }

  // ==================== resetPassword ====================

  @Test
  @DisplayName("resetPassword generates new password and returns it")
  void resetPassword_WhenUserExists_ReturnsGeneratedPassword() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(passwordGenerator.generate()).thenReturn("NewSecurePass123!");
    when(passwordEncoder.encode("NewSecurePass123!")).thenReturn("encodedNewPass");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    UserDto result = userService.resetPassword(1L);

    // Assert
    assertNotNull(result);
    assertEquals("NewSecurePass123!", result.getGeneratedPassword());
    verify(passwordEncoder).encode("NewSecurePass123!");
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("resetPassword throws exception when user not found")
  void resetPassword_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class,
        () -> userService.resetPassword(99L));
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

  // ==================== User In Use Protection ====================
  @Test
  @DisplayName("deleteUser throws exception when user is assigned to teams")
  void deleteUser_WhenUserInTeams_ThrowsException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.existsByUsersId(1L)).thenReturn(true);

    // Act & Assert
    UserInUseException exception = assertThrows(
        UserInUseException.class,
        () -> userService.deleteUser(1L));
    assertEquals(UserInUseException.IN_TEAMS, exception.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteUser throws exception when user has schedule entries")
  void deleteUser_WhenUserHasSchedules_ThrowsException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.existsByUsersId(1L)).thenReturn(false);
    when(scheduleRepository.existsByUserId(1L)).thenReturn(true);

    // Act & Assert
    UserInUseException exception = assertThrows(
        UserInUseException.class,
        () -> userService.deleteUser(1L));
    assertEquals(UserInUseException.HAS_SCHEDULES, exception.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteUser checks teams before schedules")
  void deleteUser_WhenUserInTeamsAndHasSchedules_ThrowsTeamException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.existsByUsersId(1L)).thenReturn(true);

    // Act & Assert
    UserInUseException exception = assertThrows(
        UserInUseException.class,
        () -> userService.deleteUser(1L));
    assertEquals(UserInUseException.IN_TEAMS, exception.getMessage());
    // Schedule check should not be reached when team check already fails
    verify(scheduleRepository, never()).existsByUserId(any());
  }

  // ==================== Force Delete ====================

  @Test
  @DisplayName("deleteUserForced removes user from teams and deletes")
  void deleteUserForced_WhenUserInTeams_RemovesAndDeletes() {
    // Arrange
    Team team1 = new Team("Team A", "Desc A");
    team1.setId(10L);
    team1.addUser(testUser);

    Team team2 = new Team("Team B", "Desc B");
    team2.setId(11L);
    team2.addUser(testUser);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.findAllByUsersId(1L)).thenReturn(Arrays.asList(team1, team2));
    when(scheduleRepository.existsByUserId(1L)).thenReturn(false);

    // Act
    userService.deleteUserForced(1L);

    // Assert
    assertFalse(team1.getUsers().contains(testUser));
    assertFalse(team2.getUsers().contains(testUser));
    verify(teamRepository).save(team1);
    verify(teamRepository).save(team2);
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteUserForced still blocks when user has schedules")
  void deleteUserForced_WhenUserHasSchedules_ThrowsException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.findAllByUsersId(1L)).thenReturn(Arrays.asList());
    when(scheduleRepository.existsByUserId(1L)).thenReturn(true);

    // Act & Assert
    UserInUseException exception = assertThrows(
        UserInUseException.class,
        () -> userService.deleteUserForced(1L));
    assertEquals(UserInUseException.HAS_SCHEDULES, exception.getMessage());
    verify(userRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteUserForced prevents self-deletion")
  void deleteUserForced_WhenSelfDelete_ThrowsException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    mockAuthenticatedUser("testuser");

    // Act & Assert
    SelfModificationException exception = assertThrows(
        SelfModificationException.class,
        () -> userService.deleteUserForced(1L));
    assertEquals(SelfModificationException.SELF_DELETE, exception.getMessage());
  }

}
