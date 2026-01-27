package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.CreateTeamRequest;
import de.laetum.pmbackend.dto.TeamDto;
import de.laetum.pmbackend.dto.UpdateTeamRequest;
import de.laetum.pmbackend.entity.Team;
import de.laetum.pmbackend.entity.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.TeamRepository;
import de.laetum.pmbackend.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for team management operations. Handles CRUD operations for teams and user assignments.
 */
@Service
@Transactional
public class TeamService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;

  public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
  }

  /**
   * Get all teams.
   *
   * @return List of all teams as DTOs
   */
  public List<TeamDto> getAllTeams() {
    return teamRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  /**
   * Get a single team by ID.
   *
   * @param id Team ID
   * @return Team as DTO
   * @throws ResourceNotFoundException if team not found
   */
  public TeamDto getTeamById(Long id) {
    Team team =
        teamRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    return toDto(team);
  }

  /**
   * Create a new team.
   *
   * @param request Team data (name, description)
   * @return Created team as DTO
   */
  public TeamDto createTeam(CreateTeamRequest request) {
    Team team = new Team(request.getName(), request.getDescription());
    Team savedTeam = teamRepository.save(team);
    return toDto(savedTeam);
  }

  /**
   * Update an existing team.
   *
   * @param id Team ID
   * @param request Updated team data
   * @return Updated team as DTO
   * @throws ResourceNotFoundException if team not found
   */
  public TeamDto updateTeam(Long id, UpdateTeamRequest request) {
    Team team =
        teamRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

    team.setName(request.getName());
    team.setDescription(request.getDescription());

    Team savedTeam = teamRepository.save(team);
    return toDto(savedTeam);
  }

  /**
   * Delete a team.
   *
   * @param id Team ID
   * @throws ResourceNotFoundException if team not found
   */
  public void deleteTeam(Long id) {
    if (!teamRepository.existsById(id)) {
      throw new ResourceNotFoundException("Team not found with id: " + id);
    }
    teamRepository.deleteById(id);
  }

  /**
   * Add a user to a team.
   *
   * @param teamId Team ID
   * @param userId User ID
   * @return Updated team as DTO
   * @throws ResourceNotFoundException if team or user not found
   */
  public TeamDto addUserToTeam(Long teamId, Long userId) {
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    team.addUser(user);
    Team savedTeam = teamRepository.save(team);
    return toDto(savedTeam);
  }

  /**
   * Remove a user from a team.
   *
   * @param teamId Team ID
   * @param userId User ID
   * @return Updated team as DTO
   * @throws ResourceNotFoundException if team or user not found
   */
  public TeamDto removeUserFromTeam(Long teamId, Long userId) {
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    team.removeUser(user);
    Team savedTeam = teamRepository.save(team);
    return toDto(savedTeam);
  }

  public List<TeamDto> getTeamsByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden"));

    return teamRepository.findAll().stream()
        .filter(team -> team.getUsers().contains(user))
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Convert Team entity to TeamDto.
   *
   * @param team Team entity
   * @return TeamDto with user IDs
   */
  private TeamDto toDto(Team team) {
    Set<Long> userIds = team.getUsers().stream().map(User::getId).collect(Collectors.toSet());
    return new TeamDto(team.getId(), team.getName(), team.getDescription(), userIds);
  }
}
