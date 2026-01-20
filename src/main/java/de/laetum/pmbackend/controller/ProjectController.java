package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateProjectRequest;
import de.laetum.pmbackend.dto.ProjectDto;
import de.laetum.pmbackend.dto.UpdateProjectRequest;
import de.laetum.pmbackend.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  public ResponseEntity<List<ProjectDto>> getAllProjects() {
    return ResponseEntity.ok(projectService.getAllProjects());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
    return ResponseEntity.ok(projectService.getProjectById(id));
  }

  @PostMapping
  public ResponseEntity<ProjectDto> createProject(
      @Valid @RequestBody CreateProjectRequest request) {
    ProjectDto created = projectService.createProject(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectDto> updateProject(
      @PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
    return ResponseEntity.ok(projectService.updateProject(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
    projectService.deleteProject(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{projectId}/teams/{teamId}")
  public ResponseEntity<ProjectDto> addTeamToProject(
      @PathVariable Long projectId, @PathVariable Long teamId) {
    return ResponseEntity.ok(projectService.addTeamToProject(projectId, teamId));
  }

  @DeleteMapping("/{projectId}/teams/{teamId}")
  public ResponseEntity<ProjectDto> removeTeamFromProject(
      @PathVariable Long projectId, @PathVariable Long teamId) {
    return ResponseEntity.ok(projectService.removeTeamFromProject(projectId, teamId));
  }
}
