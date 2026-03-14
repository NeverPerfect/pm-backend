package de.laetum.pmbackend.service.schedule;

import de.laetum.pmbackend.controller.schedule.CreateScheduleRequest;
import de.laetum.pmbackend.controller.schedule.ScheduleDto;
import de.laetum.pmbackend.controller.schedule.UpdateScheduleRequest;
import de.laetum.pmbackend.exception.ForbiddenOperationException;
import de.laetum.pmbackend.exception.ScheduleValidationException;
import de.laetum.pmbackend.repository.category.Category;
import de.laetum.pmbackend.repository.category.CategoryRepository;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service for schedule management operations. Handles CRUD operations for time
 * bookings with validation of user-team-project-category relationships.
 */
@Service
@Transactional
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final TeamRepository teamRepository;
  private final CategoryRepository categoryRepository;
  private final ScheduleMapper scheduleMapper;

  public ScheduleService(
      ScheduleRepository scheduleRepository,
      UserRepository userRepository,
      ProjectRepository projectRepository,
      TeamRepository teamRepository,
      CategoryRepository categoryRepository,
      ScheduleMapper scheduleMapper) {
    this.scheduleRepository = scheduleRepository;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.teamRepository = teamRepository;
    this.categoryRepository = categoryRepository;
    this.scheduleMapper = scheduleMapper;
  }

  /**
   * Get all schedules for a user, ordered by date descending.
   *
   * @param userId User ID
   * @return List of schedule DTOs
   * @throws ResourceNotFoundException if user not found
   */
  public List<ScheduleDto> getSchedulesByUserId(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException(
          String.format(ResourceNotFoundException.USER_NOT_FOUND, userId));
    }

    return scheduleRepository.findByUserIdOrderByDateDesc(userId).stream()
        .map(scheduleMapper::map)
        .collect(Collectors.toList());
  }

  /**
   * Get a single schedule by ID.
   *
   * @param id Schedule ID
   * @return Schedule as DTO
   * @throws ResourceNotFoundException if schedule not found
   */
  public ScheduleDto getScheduleById(Long id) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.SCHEDULE_NOT_FOUND, id)));
    return scheduleMapper.map(schedule);
  }

  /**
   * Create a new schedule entry. Validates that the user is active, belongs to
   * the team, the project is active, the team is assigned to the project,
   * and the category exists.
   *
   * @param userId  User ID
   * @param request Schedule data
   * @return Created schedule as DTO
   * @throws ResourceNotFoundException   if user, team, project or category not
   *                                     found
   * @throws ScheduleValidationException if validation fails
   */
  public ScheduleDto createSchedule(Long userId, CreateScheduleRequest request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.USER_NOT_FOUND, userId)));

    // Prevent time bookings for inactive users
    if (!user.isActive()) {
      throw new ScheduleValidationException(ScheduleValidationException.USER_INACTIVE_CREATE);
    }

    // Prevent bookings for future dates
    if (request.getDate().isAfter(LocalDate.now())) {
      throw new ScheduleValidationException(ScheduleValidationException.DATE_IN_FUTURE);
    }

    Team team = teamRepository
        .findById(request.getTeamId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.TEAM_NOT_FOUND, request.getTeamId())));

    Project project = projectRepository
        .findById(request.getProjectId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.PROJECT_NOT_FOUND, request.getProjectId())));

    // Validation: User must be in the team
    if (!team.getUsers().contains(user)) {
      throw new ScheduleValidationException(ScheduleValidationException.USER_NOT_IN_TEAM);
    }

    // Validation: Project must be active
    if (!project.isActive()) {
      throw new ScheduleValidationException(ScheduleValidationException.PROJECT_NOT_ACTIVE);
    }

    // Validation: Team must be assigned to the project
    if (!project.getTeams().contains(team)) {
      throw new ScheduleValidationException(ScheduleValidationException.TEAM_NOT_IN_PROJECT);
    }

    // Load the selected category
    Category category = categoryRepository
        .findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.CATEGORY_NOT_FOUND, request.getCategoryId())));

    double hours = calculateHours(request.getStartTime(), request.getEndTime());

    Schedule schedule = new Schedule();
    schedule.setDate(request.getDate());
    schedule.setStartTime(request.getStartTime());
    schedule.setEndTime(request.getEndTime());
    schedule.setHours(hours);
    schedule.setDescription(request.getDescription());
    schedule.setUser(user);
    schedule.setProject(project);
    schedule.setTeam(team);
    schedule.setCategory(category);

    Schedule saved = scheduleRepository.save(schedule);
    return scheduleMapper.map(saved);
  }

  /**
   * Update an existing schedule entry. Validates ownership and the same
   * constraints as creation.
   *
   * @param id      Schedule ID
   * @param userId  User ID (must match schedule owner)
   * @param request Updated schedule data
   * @return Updated schedule as DTO
   * @throws ResourceNotFoundException   if schedule, team, project or category
   *                                     not found
   * @throws ForbiddenOperationException if user doesn't own the schedule
   * @throws ScheduleValidationException if validation fails
   */
  public ScheduleDto updateSchedule(Long id, Long userId, UpdateScheduleRequest request) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.SCHEDULE_NOT_FOUND, id)));

    // Security: Schedule must belong to the user
    if (!schedule.getUser().getId().equals(userId)) {
      throw new ForbiddenOperationException(ForbiddenOperationException.SCHEDULE_NOT_OWNED);
    }

    User user = schedule.getUser();

    // Prevent time bookings for inactive users
    if (!user.isActive()) {
      throw new ScheduleValidationException(ScheduleValidationException.USER_INACTIVE_UPDATE);
    }

    // Prevent bookings for future dates
    if (request.getDate().isAfter(LocalDate.now())) {
      throw new ScheduleValidationException(ScheduleValidationException.DATE_IN_FUTURE);
    }

    Team team = teamRepository
        .findById(request.getTeamId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.TEAM_NOT_FOUND, request.getTeamId())));

    Project project = projectRepository
        .findById(request.getProjectId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.PROJECT_NOT_FOUND, request.getProjectId())));

    // Same validations as for creation
    if (!team.getUsers().contains(user)) {
      throw new ScheduleValidationException(ScheduleValidationException.USER_NOT_IN_TEAM);
    }

    if (!project.isActive()) {
      throw new ScheduleValidationException(ScheduleValidationException.PROJECT_NOT_ACTIVE);
    }

    if (!project.getTeams().contains(team)) {
      throw new ScheduleValidationException(ScheduleValidationException.TEAM_NOT_IN_PROJECT);
    }

    // Load the selected category
    Category category = categoryRepository
        .findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.CATEGORY_NOT_FOUND, request.getCategoryId())));

    double hours = calculateHours(request.getStartTime(), request.getEndTime());

    schedule.setDate(request.getDate());
    schedule.setStartTime(request.getStartTime());
    schedule.setEndTime(request.getEndTime());
    schedule.setHours(hours);
    schedule.setDescription(request.getDescription());
    schedule.setProject(project);
    schedule.setTeam(team);
    schedule.setCategory(category);

    Schedule saved = scheduleRepository.save(schedule);
    return scheduleMapper.map(saved);
  }

  /**
   * Delete a schedule entry. Only the owning user may delete it.
   *
   * @param id     Schedule ID
   * @param userId User ID (must match schedule owner)
   * @throws ResourceNotFoundException   if schedule not found
   * @throws ForbiddenOperationException if user doesn't own the schedule
   */
  public void deleteSchedule(Long id, Long userId) {
    Schedule schedule = scheduleRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format(ResourceNotFoundException.SCHEDULE_NOT_FOUND, id)));

    if (!schedule.getUser().getId().equals(userId)) {
      throw new ForbiddenOperationException(ForbiddenOperationException.SCHEDULE_NOT_OWNED);
    }

    scheduleRepository.delete(schedule);
  }

  /**
   * Calculates the duration in hours between start and end time.
   * Supports overnight shifts (e.g. 22:00-02:00 = 4h).
   * Validates that start != end and duration <= 24h.
   *
   * @param startTime shift start
   * @param endTime   shift end
   * @return duration in hours as double, rounded to two decimal places
   * @throws ScheduleValidationException if times are equal or duration exceeds
   *                                     24h
   */
  private double calculateHours(LocalTime startTime, LocalTime endTime) {
    if (startTime.equals(endTime)) {
      throw new ScheduleValidationException(ScheduleValidationException.START_EQUALS_END);
    }

    long minutes;
    if (endTime.isAfter(startTime)) {
      // Normal: e.g. 09:00 - 17:00
      minutes = Duration.between(startTime, endTime).toMinutes();
    } else {
      // Overnight: e.g. 22:00 - 02:00 → total minutes in a day minus the gap
      minutes = 1440 + Duration.between(startTime, endTime).toMinutes();
    }

    double hours = Math.round(minutes / 60.0 * 100.0) / 100.0;

    if (hours > 24.0) {
      throw new ScheduleValidationException(ScheduleValidationException.DURATION_EXCEEDS_24H);
    }

    return hours;
  }

}