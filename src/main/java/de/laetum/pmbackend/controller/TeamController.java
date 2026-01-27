package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateTeamRequest;
import de.laetum.pmbackend.dto.TeamDto;
import de.laetum.pmbackend.dto.UpdateTeamRequest;
import de.laetum.pmbackend.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@Tag(
    name = "Teamverwaltung",
    description = "CRUD-Operationen für Teams und Benutzerzuweisung (MANAGER/ADMIN)")
public class TeamController {

  private final TeamService teamService;

  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @Operation(summary = "Alle Teams abrufen", description = "Gibt eine Liste aller Teams zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping
  public ResponseEntity<List<TeamDto>> getAllTeams() {
    return ResponseEntity.ok(teamService.getAllTeams());
  }

  @Operation(summary = "Team nach ID abrufen", description = "Gibt ein einzelnes Team zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team gefunden"),
    @ApiResponse(responseCode = "404", description = "Team nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping("/{id}")
  public ResponseEntity<TeamDto> getTeamById(
      @Parameter(description = "ID des Teams") @PathVariable Long id) {
    return ResponseEntity.ok(teamService.getTeamById(id));
  }

  @Operation(summary = "Neues Team erstellen", description = "Erstellt ein neues Team")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Team erstellt"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PostMapping
  public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody CreateTeamRequest request) {
    TeamDto created = teamService.createTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Team aktualisieren", description = "Aktualisiert ein bestehendes Team")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team aktualisiert"),
    @ApiResponse(responseCode = "404", description = "Team nicht gefunden"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PutMapping("/{id}")
  public ResponseEntity<TeamDto> updateTeam(
      @Parameter(description = "ID des Teams") @PathVariable Long id,
      @Valid @RequestBody UpdateTeamRequest request) {
    return ResponseEntity.ok(teamService.updateTeam(id, request));
  }

  @Operation(summary = "Team löschen", description = "Löscht ein Team")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Team gelöscht"),
    @ApiResponse(responseCode = "404", description = "Team nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTeam(
      @Parameter(description = "ID des Teams") @PathVariable Long id) {
    teamService.deleteTeam(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Benutzer zu Team hinzufügen",
      description = "Fügt einen Benutzer einem Team hinzu")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Benutzer hinzugefügt"),
    @ApiResponse(responseCode = "404", description = "Team oder Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PostMapping("/{teamId}/users/{userId}")
  public ResponseEntity<TeamDto> addUserToTeam(
      @Parameter(description = "ID des Teams") @PathVariable Long teamId,
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId) {
    return ResponseEntity.ok(teamService.addUserToTeam(teamId, userId));
  }

  @Operation(
      summary = "Benutzer aus Team entfernen",
      description = "Entfernt einen Benutzer aus einem Team")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Benutzer entfernt"),
    @ApiResponse(responseCode = "404", description = "Team oder Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @DeleteMapping("/{teamId}/users/{userId}")
  public ResponseEntity<TeamDto> removeUserFromTeam(
      @Parameter(description = "ID des Teams") @PathVariable Long teamId,
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId) {
    return ResponseEntity.ok(teamService.removeUserFromTeam(teamId, userId));
  }

  @Operation(
      summary = "Eigene Teams abrufen",
      description = "Gibt alle Teams zurück, in denen der eingeloggte User Mitglied ist")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Teams erfolgreich abgerufen")})
  @GetMapping("/my")
  public ResponseEntity<List<TeamDto>> getMyTeams(Authentication authentication) {
    String username = authentication.getName();
    List<TeamDto> teams = teamService.getTeamsByUsername(username);
    return ResponseEntity.ok(teams);
  }
}
