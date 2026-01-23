package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateProjectRequest;
import de.laetum.pmbackend.dto.ProjectDto;
import de.laetum.pmbackend.dto.UpdateProjectRequest;
import de.laetum.pmbackend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@Tag(
    name = "Projektverwaltung",
    description = "CRUD-Operationen für Projekte und Teamzuweisung (MANAGER/ADMIN)")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Operation(
      summary = "Alle Projekte abrufen",
      description = "Gibt eine Liste aller Projekte zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping
  public ResponseEntity<List<ProjectDto>> getAllProjects() {
    return ResponseEntity.ok(projectService.getAllProjects());
  }

  @Operation(summary = "Projekt nach ID abrufen", description = "Gibt ein einzelnes Projekt zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Projekt gefunden"),
    @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ProjectDto> getProjectById(
      @Parameter(description = "ID des Projekts") @PathVariable Long id) {
    return ResponseEntity.ok(projectService.getProjectById(id));
  }

  @Operation(summary = "Neues Projekt erstellen", description = "Erstellt ein neues Projekt")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Projekt erstellt"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PostMapping
  public ResponseEntity<ProjectDto> createProject(
      @Valid @RequestBody CreateProjectRequest request) {
    ProjectDto created = projectService.createProject(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
      summary = "Projekt aktualisieren",
      description = "Aktualisiert ein bestehendes Projekt")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Projekt aktualisiert"),
    @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ProjectDto> updateProject(
      @Parameter(description = "ID des Projekts") @PathVariable Long id,
      @Valid @RequestBody UpdateProjectRequest request) {
    return ResponseEntity.ok(projectService.updateProject(id, request));
  }

  @Operation(summary = "Projekt löschen", description = "Löscht ein Projekt")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Projekt gelöscht"),
    @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProject(
      @Parameter(description = "ID des Projekts") @PathVariable Long id) {
    projectService.deleteProject(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Team zu Projekt hinzufügen",
      description = "Weist ein Team einem Projekt zu")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team hinzugefügt"),
    @ApiResponse(responseCode = "404", description = "Projekt oder Team nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PostMapping("/{projectId}/teams/{teamId}")
  public ResponseEntity<ProjectDto> addTeamToProject(
      @Parameter(description = "ID des Projekts") @PathVariable Long projectId,
      @Parameter(description = "ID des Teams") @PathVariable Long teamId) {
    return ResponseEntity.ok(projectService.addTeamToProject(projectId, teamId));
  }

  @Operation(
      summary = "Team aus Projekt entfernen",
      description = "Entfernt ein Team aus einem Projekt")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team entfernt"),
    @ApiResponse(responseCode = "404", description = "Projekt oder Team nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @DeleteMapping("/{projectId}/teams/{teamId}")
  public ResponseEntity<ProjectDto> removeTeamFromProject(
      @Parameter(description = "ID des Projekts") @PathVariable Long projectId,
      @Parameter(description = "ID des Teams") @PathVariable Long teamId) {
    return ResponseEntity.ok(projectService.removeTeamFromProject(projectId, teamId));
  }
}
