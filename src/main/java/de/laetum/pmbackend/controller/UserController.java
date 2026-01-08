package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateUserRequest;
import de.laetum.pmbackend.dto.UpdateUserRequest;
import de.laetum.pmbackend.dto.UserDto;
import de.laetum.pmbackend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for user management. Only accessible by MANAGER and ADMIN roles. */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /** Get all users. Accessible by: MANAGER, ADMIN */
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  /** Get a single user by ID. Accessible by: MANAGER, ADMIN */
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  /** Create a new user. Accessible by: ADMIN only */
  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserDto createdUser = userService.createUser(request);
    return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
  }

  /** Update an existing user. Accessible by: MANAGER, ADMIN */
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.updateUser(id, request));
  }

  /** Delete a user. Accessible by: ADMIN only */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
