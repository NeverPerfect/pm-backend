package de.laetum.pmbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class CreateScheduleRequest {

  @NotNull(message = "Datum ist erforderlich")
  private LocalDate date;

  @NotNull(message = "Stunden sind erforderlich")
  @Positive(message = "Stunden müssen positiv sein")
  private Double hours;

  private String description;

  @NotNull(message = "Projekt ist erforderlich")
  private Long projectId;

  @NotNull(message = "Team ist erforderlich")
  private Long teamId;

  public CreateScheduleRequest() {}

  // Getter und Setter
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Double getHours() {
    return hours;
  }

  public void setHours(Double hours) {
    this.hours = hours;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }
}
