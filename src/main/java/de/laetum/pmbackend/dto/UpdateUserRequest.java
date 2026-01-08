package de.laetum.pmbackend.dto;

import de.laetum.pmbackend.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO for updating an existing user. Password is optional - only set if it should be changed. */
public class UpdateUserRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotNull(message = "Role is required")
  private Role role;

  @NotNull(message = "Active status is required")
  private Boolean active;

  // Optional - only if password should be changed
  private String password;

  // Default constructor
  public UpdateUserRequest() {}

  // Getters and Setters
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
