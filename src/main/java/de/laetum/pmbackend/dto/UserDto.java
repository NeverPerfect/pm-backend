package de.laetum.pmbackend.dto;

import de.laetum.pmbackend.entity.Role;

/**
 * DTO for transferring user data without sensitive information. Used for API responses where
 * password must not be exposed.
 */
public class UserDto {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private boolean active;
  private Role role;

  // Default constructor (needed for JSON deserialization)
  public UserDto() {}

  // Constructor for easy conversion from Entity
  public UserDto(
      Long id, String username, String firstName, String lastName, boolean active, Role role) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.active = active;
    this.role = role;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
