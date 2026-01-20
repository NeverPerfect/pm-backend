package de.laetum.pmbackend.dto;

import java.util.Set;

public class ProjectDto {

  private Long id;
  private String name;
  private String description;
  private boolean active;
  private Set<Long> teamIds;

  public ProjectDto() {}

  public ProjectDto(Long id, String name, String description, boolean active, Set<Long> teamIds) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.active = active;
    this.teamIds = teamIds;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return active;
  }

  public Set<Long> getTeamIds() {
    return teamIds;
  }

  // Setters
  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setTeamIds(Set<Long> teamIds) {
    this.teamIds = teamIds;
  }
}
