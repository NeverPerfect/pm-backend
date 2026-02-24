package de.laetum.pmbackend.repository.project;

import de.laetum.pmbackend.repository.team.Team;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
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
  @JoinTable(name = "project_teams", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
  private Set<Team> teams = new HashSet<>();

  public Project(String name, String description, boolean active) {
    this.name = name;
    this.description = description;
    this.active = active;
  }

  public void addTeam(Team team) {
    this.teams.add(team);
  }

  public void removeTeam(Team team) {
    this.teams.remove(team);
  }
}