package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.CreateUserRequest;
import de.laetum.pmbackend.dto.UpdateUserRequest;
import de.laetum.pmbackend.dto.UserDto;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for user management operations. Handles CRUD operations and conversion between Entity and
 * DTO.
 */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Get all users.
   *
   * @return List of all users as DTOs (without passwords)
   */
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
  }

  /**
   * Get a single user by ID.
   *
   * @param id User ID
   * @return User as DTO
   * @throws RuntimeException if user not found
   */
  public UserDto getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return convertToDto(user);
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
    return convertToDto(savedUser);
  }

  /**
   * Update an existing user.
   *
   * @param id User ID
   * @param request Updated user data
   * @return Updated user as DTO
   */
  public UserDto updateUser(Long id, UpdateUserRequest request) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    // Prüfen ob Ziel-User ein Admin ist
    if (user.getRole() == Role.ADMIN) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAdmin =
          auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (!isAdmin) {
        throw new RuntimeException("Only admins can modify admin users");
      }
    }

    // Check if new username conflicts with another user
    userRepository
        .findByUsername(request.getUsername())
        .ifPresent(
            existingUser -> {
              if (!existingUser.getId().equals(id)) {
                throw new RuntimeException("Username already exists: " + request.getUsername());
              }
            });

    user.setUsername(request.getUsername());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRole(request.getRole());
    user.setActive(request.getActive());

    // Only update password if provided
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    User savedUser = userRepository.save(user);
    return convertToDto(savedUser);
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
    userRepository.deleteById(id);
  }

  /** Convert User entity to UserDto. This ensures passwords are never exposed. */
  private UserDto convertToDto(User user) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.isActive(),
        user.getRole());
  }
}
