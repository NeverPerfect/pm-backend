package de.laetum.pmbackend.service.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.laetum.pmbackend.controller.user.CreateUserRequest;
import de.laetum.pmbackend.controller.user.UpdateUserRequest;
import de.laetum.pmbackend.controller.user.UserDto;
import de.laetum.pmbackend.exception.DuplicateResourceException;
import de.laetum.pmbackend.exception.ForbiddenOperationException;
import de.laetum.pmbackend.exception.LastAdminDeletionException;
import de.laetum.pmbackend.exception.SelfModificationException;
import de.laetum.pmbackend.exception.UserInUseException;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;
import de.laetum.pmbackend.repository.team.TeamRepository;
import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.repository.user.UserRepository;

/**
 * Service for user management operations. Handles CRUD operations and
 * conversion between Entity and DTO.
 */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final TeamRepository teamRepository;
  private final ScheduleRepository scheduleRepository;
  private final PasswordGenerator passwordGenerator;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      UserMapper userMapper, TeamRepository teamRepository,
      ScheduleRepository scheduleRepository, PasswordGenerator passwordGenerator) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
    this.teamRepository = teamRepository;
    this.scheduleRepository = scheduleRepository;
    this.passwordGenerator = passwordGenerator;
  }

  /**
   * Get all users.
   *
   * @return List of all users as DTOs (without passwords)
   */
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(userMapper::map).collect(Collectors.toList());
  }

  /**
   * Get a single user by ID.
   *
   * @param id User ID
   * @return User as DTO
   * @throws ResourceNotFoundException if user not found
   */
  public UserDto getUserById(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND, id)));
    return userMapper.map(user);
  }

  /**
   * Retrieves the currently authenticated user from the database.
   *
   * @return the authenticated User entity
   * @throws ResourceNotFoundException if the authenticated user is not found in
   *                                   the database
   */
  private User getAuthenticatedUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND_BY_USERNAME, username)));
  }

  /**
   * Create a new user. If no password is provided, a secure random
   * password is generated and included in the response.
   *
   * @param request User data, password optional
   * @return Created user as DTO, with generatedPassword set if auto-generated
   * @throws DuplicateResourceException if username already exists
   */
  public UserDto createUser(CreateUserRequest request) {
    // Check if username already exists
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new DuplicateResourceException(
          String.format(DuplicateResourceException.USERNAME_EXISTS, request.getUsername()));
    }

    // Generate password if not provided
    String rawPassword;
    boolean wasGenerated;
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      rawPassword = request.getPassword();
      wasGenerated = false;
    } else {
      rawPassword = passwordGenerator.generate();
      wasGenerated = true;
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRole(request.getRole());
    user.setActive(request.isActive());

    User savedUser = userRepository.save(user);
    UserDto dto = userMapper.map(savedUser);

    // Only include generated password in response (shown once)
    if (wasGenerated) {
      dto.setGeneratedPassword(rawPassword);
    }

    return dto;
  }

  /**
   * Update an existing user.
   *
   * @param id      User ID
   * @param request Updated user data
   * @return Updated user as DTO
   * @throws ResourceNotFoundException   if user not found
   * @throws SelfModificationException   if admin tries to demote or deactivate
   *                                     themselves
   * @throws ForbiddenOperationException if non-admin tries to modify an admin
   * @throws DuplicateResourceException  if new username conflicts with existing
   *                                     user
   */
  public UserDto updateUser(Long id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND, id)));

    // Prevent admins from removing their own admin role
    User currentUser = getAuthenticatedUser();
    if (currentUser.getId().equals(id)
        && user.getRole() == Role.ADMIN
        && request.getRole() != Role.ADMIN) {
      throw new SelfModificationException(
          SelfModificationException.ADMIN_SELF_DEMOTE);
    }

    // Prevent admins from deactivating their own account
    if (currentUser.getId().equals(id)
        && user.getRole() == Role.ADMIN
        && !request.getActive()) {
      throw new SelfModificationException(
          SelfModificationException.ADMIN_SELF_DEACTIVATE);
    }

    // Only admins can modify other admin users
    if (user.getRole() == Role.ADMIN && currentUser.getRole() != Role.ADMIN) {
      throw new ForbiddenOperationException(
          ForbiddenOperationException.ONLY_ADMINS_MODIFY_ADMINS);
    }

    // Check if new username conflicts with another user
    userRepository.findByUsername(request.getUsername())
        .ifPresent(existingUser -> {
          if (!existingUser.getId().equals(id)) {
            throw new DuplicateResourceException(
                String.format(DuplicateResourceException.USERNAME_EXISTS, request.getUsername()));
          }
        });

    user.setUsername(request.getUsername());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRole(request.getRole());
    user.setActive(request.getActive());

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    User savedUser = userRepository.save(user);
    return userMapper.map(savedUser);
  }

  /**
   * Resets a user's password to a new auto-generated password.
   *
   * @param id User ID
   * @return UserDto with the generated password set
   * @throws ResourceNotFoundException if user does not exist
   */
  public UserDto resetPassword(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND, id)));

    String newPassword = passwordGenerator.generate();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    UserDto dto = userMapper.map(user);
    dto.setGeneratedPassword(newPassword);
    return dto;
  }

  /**
   * Delete a user.
   *
   * @param id User ID
   * @throws ResourceNotFoundException  if user does not exist
   * @throws SelfModificationException  if user attempts to delete themselves
   * @throws LastAdminDeletionException if this would delete the last active admin
   * @throws UserInUseException         if user is still assigned to teams or has
   *                                    schedules
   */
  public void deleteUser(Long id) {
    User userToDelete = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND, id)));

    // Prevent self-deletion
    User currentUser = getAuthenticatedUser();
    if (currentUser.getId().equals(id)) {
      throw new SelfModificationException(SelfModificationException.SELF_DELETE);
    }

    // Prevent deletion of the last active admin
    if (userToDelete.getRole() == Role.ADMIN && userToDelete.isActive()) {
      long activeAdminCount = userRepository.countByRoleAndActiveTrue(Role.ADMIN);
      if (activeAdminCount <= 1) {
        throw new LastAdminDeletionException();
      }
    }

    // Prevent deletion of users still assigned to teams
    if (teamRepository.existsByUsersId(id)) {
      throw new UserInUseException(UserInUseException.IN_TEAMS);
    }

    // Prevent deletion of users with schedule entries
    if (scheduleRepository.existsByUserId(id)) {
      throw new UserInUseException(UserInUseException.HAS_SCHEDULES);
    }

    userRepository.deleteById(id);
  }
}