package de.laetum.pmbackend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import de.laetum.pmbackend.repository.user.User;   

@Entity
@Table(name = "teams")
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @ManyToMany
  @JoinTable(
      name = "team_users",
      joinColumns = @JoinColumn(name = "team_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<User> users = new HashSet<>();

  public Team() {}

  public Team(String name, String description) {
    this.name = name;
    this.description = description;
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

  public Set<User> getUsers() {
    return users;
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

  public void setUsers(Set<User> users) {
    this.users = users;
  }

  // Helper methods
  public void addUser(User user) {
    this.users.add(user);
  }

  public void removeUser(User user) {
    this.users.remove(user);
  }
}
