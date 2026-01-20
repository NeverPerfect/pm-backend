package de.laetum.pmbackend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectRequest {

  @NotBlank(message = "Name is required")
  private String name;

  private String description;

  public CreateProjectRequest() {}

  public CreateProjectRequest(String name, String description) {
    this.name = name;
    this.description = description;
  }

  // Getters
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  // Setters
  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
