package de.laetum.pmbackend.service.schedule;

import de.laetum.pmbackend.controller.schedule.CreateScheduleRequest;
import de.laetum.pmbackend.controller.schedule.ScheduleDto;
import de.laetum.pmbackend.controller.schedule.UpdateScheduleRequest;
import de.laetum.pmbackend.repository.project.Project;
import de.laetum.pmbackend.repository.schedule.Schedule;
import de.laetum.pmbackend.repository.team.Team;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.project.ProjectRepository;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;
import de.laetum.pmbackend.repository.team.TeamRepository;
import de.laetum.pmbackend.repository.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final TeamRepository teamRepository;
  private final ScheduleMapper scheduleMapper;

  public ScheduleService(
      ScheduleRepository scheduleRepository,
      UserRepository userRepository,
      ProjectRepository projectRepository,
      TeamRepository teamRepository,
      ScheduleMapper scheduleMapper) {
    this.scheduleRepository = scheduleRepository;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.teamRepository = teamRepository;
    this.scheduleMapper = scheduleMapper;
  }

  /** Gets all schedules of a user. */
  public List<ScheduleDto> getSchedulesByUserId(Long userId) {
    // Check if user exists
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User nicht gefunden mit ID: " + userId);
    }

    return scheduleRepository.findByUserIdOrderByDateDesc(userId).stream()
        .map(scheduleMapper::map)
        .collect(Collectors.toList());
  }

  /** Gets a schedule by ID. */
  public ScheduleDto getScheduleById(Long id) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));
    return scheduleMapper.map(schedule);
  }

  /**
   * Creates a new schedule. Validates: user is in the team, project is active,
   * team belongs to the project.
   */
  public ScheduleDto createSchedule(Long userId, CreateScheduleRequest request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden"));
    // Prevent time bookings for inactive users
    if (!user.isActive()) {
      throw new IllegalArgumentException("Die Planung kann für einen inaktiven Benutzer nicht erstellt werden");
    }

    Team team = teamRepository
        .findById(request.getTeamId())
        .orElseThrow(() -> new ResourceNotFoundException("Team nicht gefunden"));

    Project project = projectRepository
        .findById(request.getProjectId())
        .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden"));

    // Validation: User must be in the team
    if (!team.getUsers().contains(user)) {
      throw new IllegalArgumentException("User ist nicht Mitglied dieses Teams");
    }

    // Validation: Project must be active
    if (!project.isActive()) {
      throw new IllegalArgumentException("Projekt ist nicht aktiv");
    }

    // Validation: Team must be assigned to the project
    if (!project.getTeams().contains(team)) {
      throw new IllegalArgumentException("Team ist diesem Projekt nicht zugewiesen");
    }

    Schedule schedule = new Schedule();
    schedule.setDate(request.getDate());
    schedule.setHours(request.getHours());
    schedule.setDescription(request.getDescription());
    schedule.setUser(user);
    schedule.setProject(project);
    schedule.setTeam(team);

    Schedule saved = scheduleRepository.save(schedule);
    return scheduleMapper.map(saved);
  }

  /** Updates a schedule. Checks if the schedule belongs to the user. */
  public ScheduleDto updateSchedule(Long id, Long userId, UpdateScheduleRequest request) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));

    // Security: Schedule must belong to the user
    if (!schedule.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Keine Berechtigung für diesen Schedule");
    }

    User user = schedule.getUser();

    // Prevent time bookings for inactive users
    if (!user.isActive()) {
      throw new IllegalArgumentException("Der Schedule kann für einen inaktiven Benutzer nicht aktualisiert werden");
    }

    Team team = teamRepository
        .findById(request.getTeamId())
        .orElseThrow(() -> new ResourceNotFoundException("Team nicht gefunden"));

    Project project = projectRepository
        .findById(request.getProjectId())
        .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden"));

    // Same validations as for creation
    if (!team.getUsers().contains(user)) {
      throw new IllegalArgumentException("User ist nicht Mitglied dieses Teams");
    }

    if (!project.isActive()) {
      throw new IllegalArgumentException("Projekt ist nicht aktiv");
    }

    if (!project.getTeams().contains(team)) {
      throw new IllegalArgumentException("Team ist diesem Projekt nicht zugewiesen");
    }

    schedule.setDate(request.getDate());
    schedule.setHours(request.getHours());
    schedule.setDescription(request.getDescription());
    schedule.setProject(project);
    schedule.setTeam(team);

    Schedule saved = scheduleRepository.save(schedule);
    return scheduleMapper.map(saved);
  }

  /** Deletes a schedule. Checks if the schedule belongs to the user. */
  public void deleteSchedule(Long id, Long userId) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));

    if (!schedule.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Keine Berechtigung für diesen Schedule");
    }

    scheduleRepository.delete(schedule);
  }
}