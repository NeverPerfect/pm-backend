package de.laetum.pmbackend.dto;

import java.util.Set;

public class TeamDto {

  private Long id;
  private String name;
  private String description;
  private Set<Long> userIds;

  public TeamDto() {}

  public TeamDto(Long id, String name, String description, Set<Long> userIds) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.userIds = userIds;
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

  public Set<Long> getUserIds() {
    return userIds;
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

  public void setUserIds(Set<Long> userIds) {
    this.userIds = userIds;
  }
}
