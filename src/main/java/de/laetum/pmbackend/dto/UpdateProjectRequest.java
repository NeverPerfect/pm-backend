package de.laetum.pmbackend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProjectRequest {

  @NotBlank(message = "Name is required")
  private String name;

  private String description;

  private boolean active;

  public UpdateProjectRequest() {}

  // Getters
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return active;
  }

  // Setters
  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
