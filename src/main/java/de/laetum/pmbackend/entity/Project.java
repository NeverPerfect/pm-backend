package de.laetum.pmbackend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private boolean active = true;

  @ManyToMany
  @JoinTable(
      name = "project_teams",
      joinColumns = @JoinColumn(name = "project_id"),
      inverseJoinColumns = @JoinColumn(name = "team_id"))
  private Set<Team> teams = new HashSet<>();

  public Project() {}

  public Project(String name, String description, boolean active) {
    this.name = name;
    this.description = description;
    this.active = active;
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

  public Set<Team> getTeams() {
    return teams;
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

  public void setTeams(Set<Team> teams) {
    this.teams = teams;
  }

  // Helper methods
  public void addTeam(Team team) {
    this.teams.add(team);
  }

  public void removeTeam(Team team) {
    this.teams.remove(team);
  }
}
