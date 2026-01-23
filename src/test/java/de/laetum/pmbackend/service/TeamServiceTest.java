package de.laetum.pmbackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.dto.CreateTeamRequest;
import de.laetum.pmbackend.dto.TeamDto;
import de.laetum.pmbackend.dto.UpdateTeamRequest;
import de.laetum.pmbackend.entity.Role;
import de.laetum.pmbackend.entity.Team;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.TeamRepository;
import de.laetum.pmbackend.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock private TeamRepository teamRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private TeamService teamService;

  private Team testTeam;
  private User testUser;

  @BeforeEach
  void setUp() {
    testTeam = new Team("Dev Team", "Development Team");
    testTeam.setId(1L);

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setRole(Role.EMPLOYEE);
    testUser.setActive(true);
  }

  // ==================== getAllTeams ====================

  @Test
  @DisplayName("getAllTeams returns list of all teams")
  void getAllTeams_ReturnsAllTeams() {
    // Arrange
    Team team2 = new Team("QA Team", "Quality Assurance");
    team2.setId(2L);

    when(teamRepository.findAll()).thenReturn(Arrays.asList(testTeam, team2));

    // Act
    List<TeamDto> result = teamService.getAllTeams();

    // Assert
    assertEquals(2, result.size());
    assertEquals("Dev Team", result.get(0).getName());
    assertEquals("QA Team", result.get(1).getName());
  }

  @Test
  @DisplayName("getAllTeams returns empty list when no teams exist")
  void getAllTeams_WhenNoTeams_ReturnsEmptyList() {
    // Arrange
    when(teamRepository.findAll()).thenReturn(Arrays.asList());

    // Act
    List<TeamDto> result = teamService.getAllTeams();

    // Assert
    assertTrue(result.isEmpty());
  }

  // ==================== getTeamById ====================

  @Test
  @DisplayName("getTeamById returns team when found")
  void getTeamById_WhenTeamExists_ReturnsTeam() {
    // Arrange
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));

    // Act
    TeamDto result = teamService.getTeamById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("Dev Team", result.getName());
    assertEquals("Development Team", result.getDescription());
  }

  @Test
  @DisplayName("getTeamById throws exception when team not found")
  void getTeamById_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamById(99L));
    assertTrue(exception.getMessage().contains("99"));
  }

  // ==================== createTeam ====================

  @Test
  @DisplayName("createTeam creates new team successfully")
  void createTeam_WithValidData_CreatesTeam() {
    // Arrange
    CreateTeamRequest request = new CreateTeamRequest();
    request.setName("New Team");
    request.setDescription("A new team");

    when(teamRepository.save(any(Team.class)))
        .thenAnswer(
            invocation -> {
              Team saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    // Act
    TeamDto result = teamService.createTeam(request);

    // Assert
    assertNotNull(result);
    assertEquals("New Team", result.getName());
    assertEquals("A new team", result.getDescription());
  }

  // ==================== updateTeam ====================

  @Test
  @DisplayName("updateTeam updates team successfully")
  void updateTeam_WithValidData_UpdatesTeam() {
    // Arrange
    UpdateTeamRequest request = new UpdateTeamRequest();
    request.setName("Updated Team");
    request.setDescription("Updated description");

    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

    // Act
    TeamDto result = teamService.updateTeam(1L, request);

    // Assert
    assertNotNull(result);
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  @DisplayName("updateTeam throws exception when team not found")
  void updateTeam_WhenTeamNotFound_ThrowsException() {
    // Arrange
    UpdateTeamRequest request = new UpdateTeamRequest();
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.updateTeam(99L, request));
  }

  // ==================== deleteTeam ====================

  @Test
  @DisplayName("deleteTeam deletes team successfully")
  void deleteTeam_WhenTeamExists_DeletesTeam() {
    // Arrange
    when(teamRepository.existsById(1L)).thenReturn(true);

    // Act
    teamService.deleteTeam(1L);

    // Assert
    verify(teamRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteTeam throws exception when team not found")
  void deleteTeam_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.deleteTeam(99L));
    verify(teamRepository, never()).deleteById(any());
  }

  // ==================== addUserToTeam ====================

  @Test
  @DisplayName("addUserToTeam adds user successfully")
  void addUserToTeam_WithValidIds_AddsUser() {
    // Arrange
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

    // Act
    TeamDto result = teamService.addUserToTeam(1L, 1L);

    // Assert
    assertNotNull(result);
    verify(teamRepository).save(testTeam);
  }

  @Test
  @DisplayName("addUserToTeam throws exception when team not found")
  void addUserToTeam_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.addUserToTeam(99L, 1L));
  }

  @Test
  @DisplayName("addUserToTeam throws exception when user not found")
  void addUserToTeam_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.addUserToTeam(1L, 99L));
  }

  // ==================== removeUserFromTeam ====================

  @Test
  @DisplayName("removeUserFromTeam removes user successfully")
  void removeUserFromTeam_WithValidIds_RemovesUser() {
    // Arrange
    testTeam.addUser(testUser);
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

    // Act
    TeamDto result = teamService.removeUserFromTeam(1L, 1L);

    // Assert
    assertNotNull(result);
    verify(teamRepository).save(testTeam);
  }

  @Test
  @DisplayName("removeUserFromTeam throws exception when team not found")
  void removeUserFromTeam_WhenTeamNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.removeUserFromTeam(99L, 1L));
  }

  @Test
  @DisplayName("removeUserFromTeam throws exception when user not found")
  void removeUserFromTeam_WhenUserNotFound_ThrowsException() {
    // Arrange
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> teamService.removeUserFromTeam(1L, 99L));
  }
}
