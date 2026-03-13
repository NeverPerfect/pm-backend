package de.laetum.pmbackend.service.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.controller.schedule.CreateScheduleRequest;
import de.laetum.pmbackend.controller.schedule.ScheduleDto;
import de.laetum.pmbackend.controller.schedule.UpdateScheduleRequest;
import de.laetum.pmbackend.exception.ForbiddenOperationException;
import de.laetum.pmbackend.exception.ScheduleValidationException;
import de.laetum.pmbackend.repository.category.Category;
import de.laetum.pmbackend.repository.category.CategoryRepository;
import de.laetum.pmbackend.repository.project.Project;
import de.laetum.pmbackend.repository.user.Role;
import de.laetum.pmbackend.repository.schedule.Schedule;
import de.laetum.pmbackend.repository.team.Team;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.project.ProjectRepository;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;
import de.laetum.pmbackend.repository.team.TeamRepository;
import de.laetum.pmbackend.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private TeamRepository teamRepository;
  @Mock
  private ScheduleMapper scheduleMapper;
  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private ScheduleService scheduleService;

  private User testUser;
  private Team testTeam;
  private Project testProject;
  private Schedule testSchedule;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setRole(Role.EMPLOYEE);
    testUser.setActive(true);

    testTeam = new Team("Dev Team", "Development Team");
    testTeam.setId(1L);
    testTeam.addUser(testUser);

    testProject = new Project("Test Project", "A test project", true);
    testProject.setId(1L);
    testProject.addTeam(testTeam);

    testCategory = new Category("Entwicklung", "Softwareentwicklung", "#4CAF50");
    testCategory.setId(1L);

    testSchedule = new Schedule();
    testSchedule.setId(1L);
    testSchedule.setDate(LocalDate.of(2026, 1, 20));
    testSchedule.setHours(8.0);
    testSchedule.setDescription("Development work");
    testSchedule.setUser(testUser);
    testSchedule.setTeam(testTeam);
    testSchedule.setProject(testProject);
    testSchedule.setCategory(testCategory);

    when(scheduleMapper.map(any(Schedule.class))).thenAnswer(invocation -> {
      Schedule s = invocation.getArgument(0);
      ScheduleDto dto = new ScheduleDto();
      dto.setId(s.getId());
      dto.setDate(s.getDate());
      dto.setHours(s.getHours());
      dto.setDescription(s.getDescription());
      dto.setUserId(s.getUser().getId());
      dto.setUsername(s.getUser().getUsername());
      dto.setProjectId(s.getProject().getId());
      dto.setProjectName(s.getProject().getName());
      dto.setTeamId(s.getTeam().getId());
      dto.setTeamName(s.getTeam().getName());
      dto.setCategoryId(s.getCategory().getId());
      dto.setCategoryName(s.getCategory().getName());
      dto.setCategoryColor(s.getCategory().getColor());
      return dto;
    });
  }

  // ==================== getSchedulesByUserId ====================

  @Test
  @DisplayName("getSchedulesByUserId returns list of schedules")
  void getSchedulesByUserId_ReturnsSchedules() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);
    when(scheduleRepository.findByUserIdOrderByDateDesc(1L))
        .thenReturn(Arrays.asList(testSchedule));

    // Act
    List<ScheduleDto> result = scheduleService.getSchedulesByUserId(1L);

    // Assert
    assertEquals(1, result.size());
    assertEquals("Development work", result.get(0).getDescription());
  }

  @Test
  @DisplayName("getSchedulesByUserId throws exception when user not found")
  void getSchedulesByUserId_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(userRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> scheduleService.getSchedulesByUserId(99L));
  }

  // ==================== getScheduleById ====================

  @Test
  @DisplayName("getScheduleById returns schedule when found")
  void getScheduleById_WhenScheduleExists_ReturnsSchedule() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

    // Act
    ScheduleDto result = scheduleService.getScheduleById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(8.0, result.getHours());
    assertEquals("Development work", result.getDescription());
  }

  @Test
  @DisplayName("getScheduleById throws exception when schedule not found")
  void getScheduleById_WhenScheduleNotFound_ThrowsException() {
    // Arrange
    when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> scheduleService.getScheduleById(99L));
  }

  // ==================== createSchedule ====================

  @Test
  @DisplayName("createSchedule creates schedule successfully with valid data")
  void createSchedule_WithValidData_CreatesSchedule() {
    // Arrange
    CreateScheduleRequest request = new CreateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 21));
    request.setHours(6.0);
    request.setDescription("New work");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(scheduleRepository.save(any(Schedule.class)))
        .thenAnswer(
            invocation -> {
              Schedule saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

    // Act
    ScheduleDto result = scheduleService.createSchedule(1L, request);

    // Assert
    assertNotNull(result);
    assertEquals(6.0, result.getHours());
    assertEquals("New work", result.getDescription());
  }

  @Test
  @DisplayName("createSchedule throws exception when user not in team")
  void createSchedule_WhenUserNotInTeam_ThrowsException() {
    // Arrange
    User otherUser = new User();
    otherUser.setId(2L);
    otherUser.setUsername("otheruser");
    otherUser.setActive(true);

    CreateScheduleRequest request = new CreateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 21));
    request.setHours(6.0);
    request.setDescription("Work");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

    // Act & Assert
    ScheduleValidationException exception = assertThrows(
        ScheduleValidationException.class, () -> scheduleService.createSchedule(2L, request));
    assertEquals(ScheduleValidationException.USER_NOT_IN_TEAM, exception.getMessage());
  }

  @Test
  @DisplayName("createSchedule throws exception when project is inactive")
  void createSchedule_WhenProjectInactive_ThrowsException() {
    // Arrange
    Project inactiveProject = new Project("Inactive", "Inactive project", false);
    inactiveProject.setId(2L);
    inactiveProject.addTeam(testTeam);

    CreateScheduleRequest request = new CreateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 21));
    request.setHours(6.0);
    request.setDescription("Work");
    request.setTeamId(1L);
    request.setProjectId(2L);
    request.setCategoryId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.findById(2L)).thenReturn(Optional.of(inactiveProject));

    // Act & Assert
    ScheduleValidationException exception = assertThrows(
        ScheduleValidationException.class, () -> scheduleService.createSchedule(1L, request));
    assertEquals(ScheduleValidationException.PROJECT_NOT_ACTIVE, exception.getMessage());
  }

  @Test
  @DisplayName("createSchedule throws exception when team not assigned to project")
  void createSchedule_WhenTeamNotInProject_ThrowsException() {
    // Arrange
    Team otherTeam = new Team("Other Team", "Other");
    otherTeam.setId(2L);
    otherTeam.addUser(testUser);

    CreateScheduleRequest request = new CreateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 21));
    request.setHours(6.0);
    request.setDescription("Work");
    request.setTeamId(2L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.findById(2L)).thenReturn(Optional.of(otherTeam));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

    // Act & Assert
    ScheduleValidationException exception = assertThrows(
        ScheduleValidationException.class, () -> scheduleService.createSchedule(1L, request));
    assertEquals(ScheduleValidationException.TEAM_NOT_IN_PROJECT, exception.getMessage());
  }

  @Test
  @DisplayName("createSchedule throws exception when user is inactive")
  void createSchedule_WhenUserInactive_ThrowsException() {
    // Arrange
    testUser.setActive(false);

    CreateScheduleRequest request = new CreateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 21));
    request.setHours(6.0);
    request.setDescription("Work");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act & Assert
    ScheduleValidationException exception = assertThrows(
        ScheduleValidationException.class,
        () -> scheduleService.createSchedule(1L, request));
    assertEquals(ScheduleValidationException.USER_INACTIVE_CREATE, exception.getMessage());
    verify(scheduleRepository, never()).save(any());
  }

  // ==================== updateSchedule ====================

  @Test
  @DisplayName("updateSchedule updates schedule successfully")
  void updateSchedule_WithValidData_UpdatesSchedule() {
    // Arrange
    UpdateScheduleRequest request = new UpdateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 22));
    request.setHours(7.0);
    request.setDescription("Updated work");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

    // Act
    ScheduleDto result = scheduleService.updateSchedule(1L, 1L, request);

    // Assert
    assertNotNull(result);
    verify(scheduleRepository).save(any(Schedule.class));
  }

  @Test
  @DisplayName("updateSchedule throws exception when user doesn't own schedule")
  void updateSchedule_WhenNotOwner_ThrowsException() {
    // Arrange
    UpdateScheduleRequest request = new UpdateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 22));
    request.setHours(7.0);
    request.setDescription("Updated");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

    // Act & Assert
    ForbiddenOperationException exception = assertThrows(
        ForbiddenOperationException.class, () -> scheduleService.updateSchedule(1L, 99L, request));
    assertEquals(ForbiddenOperationException.SCHEDULE_NOT_OWNED, exception.getMessage());
  }

  @Test
  @DisplayName("updateSchedule throws exception when schedule not found")
  void updateSchedule_WhenScheduleNotFound_ThrowsException() {
    // Arrange
    UpdateScheduleRequest request = new UpdateScheduleRequest();
    when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> scheduleService.updateSchedule(99L, 1L, request));
  }

  @Test
  @DisplayName("updateSchedule throws exception when user is inactive")
  void updateSchedule_WhenUserInactive_ThrowsException() {
    // Arrange
    testUser.setActive(false);

    UpdateScheduleRequest request = new UpdateScheduleRequest();
    request.setDate(LocalDate.of(2026, 1, 22));
    request.setHours(7.0);
    request.setDescription("Updated");
    request.setTeamId(1L);
    request.setProjectId(1L);
    request.setCategoryId(1L);

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

    // Act & Assert
    ScheduleValidationException exception = assertThrows(
        ScheduleValidationException.class,
        () -> scheduleService.updateSchedule(1L, 1L, request));
    assertEquals(ScheduleValidationException.USER_INACTIVE_UPDATE, exception.getMessage());
    verify(scheduleRepository, never()).save(any());
  }

  // ==================== deleteSchedule ====================

  @Test
  @DisplayName("deleteSchedule deletes schedule successfully")
  void deleteSchedule_WhenOwner_DeletesSchedule() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

    // Act
    scheduleService.deleteSchedule(1L, 1L);

    // Assert
    verify(scheduleRepository).delete(testSchedule);
  }

  @Test
  @DisplayName("deleteSchedule throws exception when user doesn't own schedule")
  void deleteSchedule_WhenNotOwner_ThrowsException() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

    // Act & Assert
    ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
        () -> scheduleService.deleteSchedule(1L, 99L));
    assertEquals(ForbiddenOperationException.SCHEDULE_NOT_OWNED, exception.getMessage());
    verify(scheduleRepository, never()).delete(any());
  }

  @Test
  @DisplayName("deleteSchedule throws exception when schedule not found")
  void deleteSchedule_WhenScheduleNotFound_ThrowsException() {
    // Arrange
    when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> scheduleService.deleteSchedule(99L, 1L));
  }
}