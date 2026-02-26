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
import de.laetum.pmbackend.exception.AdminSelfModificationException;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.repository.user.UserRepository;

/**
 * Service for user management operations. Handles CRUD operations and
 * conversion between Entity and
 * DTO.
 */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
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
   * @throws RuntimeException if user not found
   */
  public UserDto getUserById(Long id) {
    User user = userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
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
            "Authenticated user not found: " + username));
  }

  /**
   * Create a new user.
   *
   * @param request User data including password
   * @return Created user as DTO
   */
  public UserDto createUser(CreateUserRequest request) {
    // Check if username already exists
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new RuntimeException("Username already exists: " + request.getUsername());
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRole(request.getRole());
    user.setActive(request.isActive());

    User savedUser = userRepository.save(user);
    return userMapper.map(savedUser);
  }

  /**
   * Update an existing user.
   *
   * @param id      User ID
   * @param request Updated user data
   * @return Updated user as DTO
   */
  public UserDto updateUser(Long id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    // Prevent admins from removing their own admin role
    User currentUser = getAuthenticatedUser();
    if (currentUser.getId().equals(id)
        && user.getRole() == Role.ADMIN
        && request.getRole() != Role.ADMIN) {
      throw new AdminSelfModificationException(
          AdminSelfModificationException.SELF_DEMOTE);
    }

    // Only admins can modify other admin users
    if (user.getRole() == Role.ADMIN && currentUser.getRole() != Role.ADMIN) {
      throw new RuntimeException("Only admins can modify admin users");
    }

    // Check if new username conflicts with another user
    userRepository.findByUsername(request.getUsername())
        .ifPresent(existingUser -> {
          if (!existingUser.getId().equals(id)) {
            throw new RuntimeException(
                "Username already exists: " + request.getUsername());
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
   * Delete a user.
   *
   * @param id User ID
   */
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }

    User currentUser = getAuthenticatedUser();
    if (currentUser.getId().equals(id)) {
      throw new AdminSelfModificationException(
          AdminSelfModificationException.SELF_DELETE);
    }

    userRepository.deleteById(id);
  }
}
