package de.laetum.pmbackend.controller;

import de.laetum.pmbackend.dto.CreateScheduleRequest;
import de.laetum.pmbackend.dto.ScheduleDto;
import de.laetum.pmbackend.dto.UpdateScheduleRequest;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.repository.UserRepository;
import de.laetum.pmbackend.service.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

  private final ScheduleService scheduleService;
  private final UserRepository userRepository;

  public ScheduleController(ScheduleService scheduleService, UserRepository userRepository) {
    this.scheduleService = scheduleService;
    this.userRepository = userRepository;
  }

  /** Holt alle Schedules des eingeloggten Users. */
  @GetMapping
  public ResponseEntity<List<ScheduleDto>> getMySchedules(Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    List<ScheduleDto> schedules = scheduleService.getSchedulesByUserId(userId);
    return ResponseEntity.ok(schedules);
  }

  /** Holt einen einzelnen Schedule. */
  @GetMapping("/{id}")
  public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
    ScheduleDto schedule = scheduleService.getScheduleById(id);
    return ResponseEntity.ok(schedule);
  }

  /** Erstellt einen neuen Schedule für den eingeloggten User. */
  @PostMapping
  public ResponseEntity<ScheduleDto> createSchedule(
      @Valid @RequestBody CreateScheduleRequest request, Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    ScheduleDto created = scheduleService.createSchedule(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Aktualisiert einen Schedule des eingeloggten Users. */
  @PutMapping("/{id}")
  public ResponseEntity<ScheduleDto> updateSchedule(
      @PathVariable Long id,
      @Valid @RequestBody UpdateScheduleRequest request,
      Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    ScheduleDto updated = scheduleService.updateSchedule(id, userId, request);
    return ResponseEntity.ok(updated);
  }

  /** Löscht einen Schedule des eingeloggten Users. */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable Long id, Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    scheduleService.deleteSchedule(id, userId);
    return ResponseEntity.noContent().build();
  }

  /** Hilfsmethode: Holt die User-ID aus dem Authentication-Objekt. */
  private Long getCurrentUserId(Authentication authentication) {
    String username = authentication.getName();
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User nicht gefunden"));
    return user.getId();
  }

  // ==================== ADMIN/MANAGER ENDPOINTS ====================

  /** Holt alle Schedules eines bestimmten Users (für Manager/Admin). */
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<ScheduleDto>> getSchedulesByUserId(@PathVariable Long userId) {
    List<ScheduleDto> schedules = scheduleService.getSchedulesByUserId(userId);
    return ResponseEntity.ok(schedules);
  }

  /** Erstellt einen Schedule für einen bestimmten User (für Manager/Admin). */
  @PostMapping("/user/{userId}")
  public ResponseEntity<ScheduleDto> createScheduleForUser(
      @PathVariable Long userId, @Valid @RequestBody CreateScheduleRequest request) {
    ScheduleDto created = scheduleService.createSchedule(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Aktualisiert einen Schedule eines bestimmten Users (für Manager/Admin). */
  @PutMapping("/user/{userId}/{scheduleId}")
  public ResponseEntity<ScheduleDto> updateScheduleForUser(
      @PathVariable Long userId,
      @PathVariable Long scheduleId,
      @Valid @RequestBody UpdateScheduleRequest request) {
    ScheduleDto updated = scheduleService.updateSchedule(scheduleId, userId, request);
    return ResponseEntity.ok(updated);
  }

  /** Löscht einen Schedule eines bestimmten Users (für Manager/Admin). */
  @DeleteMapping("/user/{userId}/{scheduleId}")
  public ResponseEntity<Void> deleteScheduleForUser(
      @PathVariable Long userId, @PathVariable Long scheduleId) {
    scheduleService.deleteSchedule(scheduleId, userId);
    return ResponseEntity.noContent().build();
  }
}
