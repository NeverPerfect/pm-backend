package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateTeamRequest;
import de.laetum.pmbackend.dto.TeamDto;
import de.laetum.pmbackend.dto.UpdateTeamRequest;
import de.laetum.pmbackend.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  private final TeamService teamService;

  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @GetMapping
  public ResponseEntity<List<TeamDto>> getAllTeams() {
    return ResponseEntity.ok(teamService.getAllTeams());
  }

  @GetMapping("/{id}")
  public ResponseEntity<TeamDto> getTeamById(@PathVariable Long id) {
    return ResponseEntity.ok(teamService.getTeamById(id));
  }

  @PostMapping
  public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody CreateTeamRequest request) {
    TeamDto created = teamService.createTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TeamDto> updateTeam(
      @PathVariable Long id, @Valid @RequestBody UpdateTeamRequest request) {
    return ResponseEntity.ok(teamService.updateTeam(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
    teamService.deleteTeam(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{teamId}/users/{userId}")
  public ResponseEntity<TeamDto> addUserToTeam(
      @PathVariable Long teamId, @PathVariable Long userId) {
    return ResponseEntity.ok(teamService.addUserToTeam(teamId, userId));
  }

  @DeleteMapping("/{teamId}/users/{userId}")
  public ResponseEntity<TeamDto> removeUserFromTeam(
      @PathVariable Long teamId, @PathVariable Long userId) {
    return ResponseEntity.ok(teamService.removeUserFromTeam(teamId, userId));
  }
}
