package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateScheduleRequest;
import de.laetum.pmbackend.dto.ScheduleDto;
import de.laetum.pmbackend.dto.UpdateScheduleRequest;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.repository.UserRepository;
import de.laetum.pmbackend.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@Tag(
    name = "Stundenbuchung",
    description = "Zeiterfassung für Mitarbeiter und Verwaltung durch Manager/Admin")
public class ScheduleController {

  private final ScheduleService scheduleService;
  private final UserRepository userRepository;

  public ScheduleController(ScheduleService scheduleService, UserRepository userRepository) {
    this.scheduleService = scheduleService;
    this.userRepository = userRepository;
  }

  // ==================== EIGENE SCHEDULES ====================

  @Operation(
      summary = "Eigene Buchungen abrufen",
      description = "Gibt alle Stundenbuchungen des eingeloggten Benutzers zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
    @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
  })
  @GetMapping
  public ResponseEntity<List<ScheduleDto>> getMySchedules(Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    List<ScheduleDto> schedules = scheduleService.getSchedulesByUserId(userId);
    return ResponseEntity.ok(schedules);
  }

  @Operation(
      summary = "Buchung nach ID abrufen",
      description = "Gibt eine einzelne Stundenbuchung zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Buchung gefunden"),
    @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden"),
    @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ScheduleDto> getScheduleById(
      @Parameter(description = "ID der Buchung") @PathVariable Long id) {
    ScheduleDto schedule = scheduleService.getScheduleById(id);
    return ResponseEntity.ok(schedule);
  }

  @Operation(
      summary = "Neue Buchung erstellen",
      description = "Erstellt eine neue Stundenbuchung für den eingeloggten Benutzer")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Buchung erstellt"),
    @ApiResponse(
        responseCode = "400",
        description = "Ungültige Anfrage (z.B. User nicht im Team, Projekt inaktiv)"),
    @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
  })
  @PostMapping
  public ResponseEntity<ScheduleDto> createSchedule(
      @Valid @RequestBody CreateScheduleRequest request, Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    ScheduleDto created = scheduleService.createSchedule(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
      summary = "Buchung aktualisieren",
      description = "Aktualisiert eine eigene Stundenbuchung")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Buchung aktualisiert"),
    @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ScheduleDto> updateSchedule(
      @Parameter(description = "ID der Buchung") @PathVariable Long id,
      @Valid @RequestBody UpdateScheduleRequest request,
      Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    ScheduleDto updated = scheduleService.updateSchedule(id, userId, request);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Buchung löschen", description = "Löscht eine eigene Stundenbuchung")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Buchung gelöscht"),
    @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden"),
    @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(
      @Parameter(description = "ID der Buchung") @PathVariable Long id,
      Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    scheduleService.deleteSchedule(id, userId);
    return ResponseEntity.noContent().build();
  }

  private Long getCurrentUserId(Authentication authentication) {
    String username = authentication.getName();
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User nicht gefunden"));
    return user.getId();
  }

  // ==================== ADMIN/MANAGER ENDPOINTS ====================

  @Operation(
      summary = "Buchungen eines Benutzers abrufen",
      description = "Gibt alle Stundenbuchungen eines bestimmten Benutzers zurück (MANAGER/ADMIN)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<ScheduleDto>> getSchedulesByUserId(
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId) {
    List<ScheduleDto> schedules = scheduleService.getSchedulesByUserId(userId);
    return ResponseEntity.ok(schedules);
  }

  @Operation(
      summary = "Buchung für Benutzer erstellen",
      description = "Erstellt eine Stundenbuchung für einen bestimmten Benutzer (MANAGER/ADMIN)")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Buchung erstellt"),
    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PostMapping("/user/{userId}")
  public ResponseEntity<ScheduleDto> createScheduleForUser(
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId,
      @Valid @RequestBody CreateScheduleRequest request) {
    ScheduleDto created = scheduleService.createSchedule(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(
      summary = "Buchung eines Benutzers aktualisieren",
      description = "Aktualisiert eine Stundenbuchung eines bestimmten Benutzers (MANAGER/ADMIN)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Buchung aktualisiert"),
    @ApiResponse(responseCode = "404", description = "Buchung oder Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @PutMapping("/user/{userId}/{scheduleId}")
  public ResponseEntity<ScheduleDto> updateScheduleForUser(
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId,
      @Parameter(description = "ID der Buchung") @PathVariable Long scheduleId,
      @Valid @RequestBody UpdateScheduleRequest request) {
    ScheduleDto updated = scheduleService.updateSchedule(scheduleId, userId, request);
    return ResponseEntity.ok(updated);
  }

  @Operation(
      summary = "Buchung eines Benutzers löschen",
      description = "Löscht eine Stundenbuchung eines bestimmten Benutzers (MANAGER/ADMIN)")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Buchung gelöscht"),
    @ApiResponse(responseCode = "404", description = "Buchung oder Benutzer nicht gefunden"),
    @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @DeleteMapping("/user/{userId}/{scheduleId}")
  public ResponseEntity<Void> deleteScheduleForUser(
      @Parameter(description = "ID des Benutzers") @PathVariable Long userId,
      @Parameter(description = "ID der Buchung") @PathVariable Long scheduleId) {
    scheduleService.deleteSchedule(scheduleId, userId);
    return ResponseEntity.noContent().build();
  }
}
