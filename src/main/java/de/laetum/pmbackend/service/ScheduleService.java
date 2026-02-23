package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.CreateScheduleRequest;
import de.laetum.pmbackend.dto.ScheduleDto;
import de.laetum.pmbackend.dto.UpdateScheduleRequest;
import de.laetum.pmbackend.repository.project.Project;   
import de.laetum.pmbackend.entity.Schedule;
import de.laetum.pmbackend.repository.team.Team;   
import de.laetum.pmbackend.repository.user.User; 
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.project.ProjectRepository;
import de.laetum.pmbackend.repository.ScheduleRepository;
import de.laetum.pmbackend.repository.team.TeamRepository;  
import de.laetum.pmbackend.repository.user.UserRepository; 
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.laetum.pmbackend.service.schedule.ScheduleMapper;

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

  /** Holt alle Schedules eines Users. */
  public List<ScheduleDto> getSchedulesByUserId(Long userId) {
    // Prüfen ob User existiert
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User nicht gefunden mit ID: " + userId);
    }

    return scheduleRepository.findByUserIdOrderByDateDesc(userId).stream()
        .map(scheduleMapper::map)
        .collect(Collectors.toList());
  }

  /** Holt einen Schedule anhand der ID. */
  public ScheduleDto getScheduleById(Long id) {
    Schedule schedule =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));
    return scheduleMapper.map(schedule);
  }

  /**
   * Erstellt einen neuen Schedule. Validiert: User ist im Team, Projekt ist aktiv, Team gehört zum
   * Projekt.
   */
  public ScheduleDto createSchedule(Long userId, CreateScheduleRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden"));

    Team team =
        teamRepository
            .findById(request.getTeamId())
            .orElseThrow(() -> new ResourceNotFoundException("Team nicht gefunden"));

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden"));

    // Validierung: User muss im Team sein
    if (!team.getUsers().contains(user)) {
      throw new IllegalArgumentException("User ist nicht Mitglied dieses Teams");
    }

    // Validierung: Projekt muss aktiv sein
    if (!project.isActive()) {
      throw new IllegalArgumentException("Projekt ist nicht aktiv");
    }

    // Validierung: Team muss dem Projekt zugewiesen sein
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

  /** Aktualisiert einen Schedule. Prüft, ob der Schedule dem User gehört. */
  public ScheduleDto updateSchedule(Long id, Long userId, UpdateScheduleRequest request) {
    Schedule schedule =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));

    // Sicherheit: Schedule muss dem User gehören
    if (!schedule.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Keine Berechtigung für diesen Schedule");
    }

    Team team =
        teamRepository
            .findById(request.getTeamId())
            .orElseThrow(() -> new ResourceNotFoundException("Team nicht gefunden"));

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden"));

    User user = schedule.getUser();

    // Dieselben Validierungen wie beim Erstellen
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

  /** Löscht einen Schedule. Prüft, ob der Schedule dem User gehört. */
  public void deleteSchedule(Long id, Long userId) {
    Schedule schedule =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Schedule nicht gefunden mit ID: " + id));

    if (!schedule.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Keine Berechtigung für diesen Schedule");
    }

    scheduleRepository.delete(schedule);
  }
}
