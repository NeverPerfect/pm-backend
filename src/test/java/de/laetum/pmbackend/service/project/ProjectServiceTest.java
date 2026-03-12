package de.laetum.pmbackend.service.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.controller.project.CreateProjectRequest;
import de.laetum.pmbackend.controller.project.ProjectDto;
import de.laetum.pmbackend.controller.project.UpdateProjectRequest;
import de.laetum.pmbackend.repository.project.Project;
import de.laetum.pmbackend.repository.team.Team;
import de.laetum.pmbackend.exception.ProjectInUseException;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.project.ProjectRepository;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;
import de.laetum.pmbackend.repository.team.TeamRepository;
import de.laetum.pmbackend.service.project.ProjectService;
import de.laetum.pmbackend.service.project.ProjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import java.util.Set;
import java.util.stream.Collectors;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private ProjectMapper projectMapper;

  @Mock
  private ScheduleRepository scheduleRepository;

  @InjectMocks
  private ProjectService projectService;

  private Project testProject;
  private Team testTeam;

  @BeforeEach
  void setUp() {
    testProject = new Project("Test Project", "A test project", true);
    testProject.setId(1L);

    testTeam = new Team("Dev Team", "Development Team");
    testTeam.setId(1L);
    when(projectMapper.map(any(Project.class))).thenAnswer(invocation -> {
      Project p = invocation.getArgument(0);
      Set<Long> teamIds = p.getTeams().stream().map(Team::getId).collect(Collectors.toSet());
      return new ProjectDto(p.getId(), p.getName(), p.getDescription(), p.isActive(), teamIds);
    });
  }

  // ==================== getAllProjects ====================

  @Test
  @DisplayName("getAllProjects returns list of all projects")
  void getAllProjects_ReturnsAllProjects() {
    // Arrange
    Project project2 = new Project("Project 2", "Second project", true);
    project2.setId(2L);

    when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject, project2));

    // Act
    List<ProjectDto> result = projectService.getAllProjects();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Test Project", result.get(0).getName());
    assertEquals("Project 2", result.get(1).getName());
  }

  @Test
  @DisplayName("getAllProjects returns empty list when no projects exist")
  void getAllProjects_WhenNoProjects_ReturnsEmptyList() {
    // Arrange
    when(projectRepository.findAll()).thenReturn(Arrays.asList());

    // Act
    List<ProjectDto> result = projectService.getAllProjects();

    // Assert
    assertTrue(result.isEmpty());
  }

  // ==================== getProjectById ====================

  @Test
  @DisplayName("getProjectById returns project when found")
  void getProjectById_WhenProjectExists_ReturnsProject() {
    // Arrange
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

    // Act
    ProjectDto result = projectService.getProjectById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("Test Project", result.getName());
    assertEquals("A test project", result.getDescription());
    assertTrue(result.isActive());
  }

  @Test
  @DisplayName("getProjectById throws exception when project not found")
  void getProjectById_WhenProjectNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
        () -> projectService.getProjectById(99L));
    assertTrue(exception.getMessage().contains("99"));
  }

  // ==================== createProject ====================

  @Test
  @DisplayName("createProject creates new project successfully")
  void createProject_WithValidData_CreatesProject() {
    // Arrange
    CreateProjectRequest request = new CreateProjectRequest();
    request.setName("New Project");
    request.setDescription("A new project");

    when(projectRepository.save(any(Project.class)))
        .thenAnswer(
            invocation -> {
              Project saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    // Act
    ProjectDto result = projectService.createProject(request);

    // Assert
    assertNotNull(result);
    assertEquals("New Project", result.getName());
    assertEquals("A new project", result.getDescription());
    assertTrue(result.isActive()); // New projects are active by default
  }

  // ==================== updateProject ====================

  @Test
  @DisplayName("updateProject updates project successfully")
  void updateProject_WithValidData_UpdatesProject() {
    // Arrange
    UpdateProjectRequest request = new UpdateProjectRequest();
    request.setName("Updated Project");
    request.setDescription("Updated description");
    request.setActive(false);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(projectRepository.save(any(Project.class))).thenReturn(testProject);

    // Act
    ProjectDto result = projectService.updateProject(1L, request);

    // Assert
    assertNotNull(result);
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  @DisplayName("updateProject throws exception when project not found")
  void updateProject_WhenProjectNotFound_ThrowsException() {
    // Arrange
    UpdateProjectRequest request = new UpdateProjectRequest();
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(99L, request));
  }

  // ==================== deleteProject ====================

  @Test
  @DisplayName("deleteProject deletes project successfully")
  void deleteProject_WhenProjectExists_DeletesProject() {
    // Arrange
    when(projectRepository.existsById(1L)).thenReturn(true);
    when(scheduleRepository.existsByProjectId(1L)).thenReturn(false);

    // Act
    projectService.deleteProject(1L);

    // Assert
    verify(projectRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteProject throws exception when project not found")
  void deleteProject_WhenProjectNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(99L));
    verify(projectRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteProject throws ProjectInUseException when project has schedules")
  void deleteProject_WhenProjectHasSchedules_ThrowsProjectInUseException() {
    // Arrange
    when(projectRepository.existsById(1L)).thenReturn(true);
    when(scheduleRepository.existsByProjectId(1L)).thenReturn(true);

    // Act & Assert
    ProjectInUseException exception = assertThrows(ProjectInUseException.class,
        () -> projectService.deleteProject(1L));
    assertEquals(ProjectInUseException.HAS_SCHEDULES, exception.getMessage());
    verify(projectRepository, never()).deleteById(any());
  }

  // ==================== addTeamToProject ====================

  @Test
  @DisplayName("addTeamToProject adds team successfully")
  void addTeamToProject_WithValidIds_AddsTeam() {
    // Arrange
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.save(any(Project.class))).thenReturn(testProject);

    // Act
    ProjectDto result = projectService.addTeamToProject(1L, 1L);

    // Assert
    assertNotNull(result);
    verify(projectRepository).save(testProject);
  }

  @Test
  @DisplayName("addTeamToProject throws exception when project not found")
  void addTeamToProject_WhenProjectNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> projectService.addTeamToProject(99L, 1L));
  }

  @Test
  @DisplayName("addTeamToProject throws exception when team not found")
  void addTeamToProject_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> projectService.addTeamToProject(1L, 99L));
  }

  // ==================== removeTeamFromProject ====================

  @Test
  @DisplayName("removeTeamFromProject removes team successfully")
  void removeTeamFromProject_WithValidIds_RemovesTeam() {
    // Arrange
    testProject.addTeam(testTeam);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(projectRepository.save(any(Project.class))).thenReturn(testProject);

    // Act
    ProjectDto result = projectService.removeTeamFromProject(1L, 1L);

    // Assert
    assertNotNull(result);
    verify(projectRepository).save(testProject);
  }

  @Test
  @DisplayName("removeTeamFromProject throws exception when project not found")
  void removeTeamFromProject_WhenProjectNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> projectService.removeTeamFromProject(99L, 1L));
  }

  @Test
  @DisplayName("removeTeamFromProject throws exception when team not found")
  void removeTeamFromProject_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> projectService.removeTeamFromProject(1L, 99L));
  }
}
