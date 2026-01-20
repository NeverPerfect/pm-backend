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

@Service
@Transactional
public class TeamService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;

  public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
  }

  public List<TeamDto> getAllTeams() {
    return teamRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  public TeamDto getTeamById(Long id) {
    Team team =
        teamRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    return toDto(team);
  }

  public TeamDto createTeam(CreateTeamRequest request) {
    Team team = new Team(request.getName(), request.getDescription());
    Team savedTeam = teamRepository.save(team);
    return toDto(savedTeam);
  }

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

  public void deleteTeam(Long id) {
    if (!teamRepository.existsById(id)) {
      throw new ResourceNotFoundException("Team not found with id: " + id);
    }
    teamRepository.deleteById(id);
  }

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

  private TeamDto toDto(Team team) {
    Set<Long> userIds = team.getUsers().stream().map(User::getId).collect(Collectors.toSet());
    return new TeamDto(team.getId(), team.getName(), team.getDescription(), userIds);
  }
}
