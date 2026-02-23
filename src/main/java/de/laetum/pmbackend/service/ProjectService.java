package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.CreateProjectRequest;
import de.laetum.pmbackend.dto.ProjectDto;
import de.laetum.pmbackend.dto.UpdateProjectRequest;
import de.laetum.pmbackend.entity.Project;
import de.laetum.pmbackend.entity.Team;
import de.laetum.pmbackend.repository.user.User;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.ProjectRepository;
import de.laetum.pmbackend.repository.TeamRepository;
import de.laetum.pmbackend.repository.user.UserRepository; 
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.laetum.pmbackend.service.project.ProjectMapper;

/**
 * Service for project management operations. Handles CRUD operations for projects and team
 * assignments.
 */
@Service
@Transactional
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final ProjectMapper projectMapper;

  public ProjectService(
      ProjectRepository projectRepository,
      TeamRepository teamRepository,
      UserRepository userRepository,
      ProjectMapper projectMapper) {
    this.projectRepository = projectRepository;
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
    this.projectMapper = projectMapper;
  }

  /**
   * Get all projects.
   *
   * @return List of all projects as DTOs
   */
  public List<ProjectDto> getAllProjects() {
    return projectRepository.findAll().stream().map(projectMapper::map).collect(Collectors.toList());
  }

  /**
   * Get a single project by ID.
   *
   * @param id Project ID
   * @return Project as DTO
   * @throws ResourceNotFoundException if project not found
   */
  public ProjectDto getProjectById(Long id) {
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    return projectMapper.map(project);
  }

  /**
   * Create a new project. New projects are active by default.
   *
   * @param request Project data (name, description)
   * @return Created project as DTO
   */
  public ProjectDto createProject(CreateProjectRequest request) {
    Project project = new Project(request.getName(), request.getDescription(), true);
    Project savedProject = projectRepository.save(project);
    return projectMapper.map(savedProject);
  }

  /**
   * Update an existing project.
   *
   * @param id Project ID
   * @param request Updated project data
   * @return Updated project as DTO
   * @throws ResourceNotFoundException if project not found
   */
  public ProjectDto updateProject(Long id, UpdateProjectRequest request) {
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setActive(request.isActive());

    Project savedProject = projectRepository.save(project);
    return projectMapper.map(savedProject);
  }

  /**
   * Delete a project.
   *
   * @param id Project ID
   * @throws ResourceNotFoundException if project not found
   */
  public void deleteProject(Long id) {
    if (!projectRepository.existsById(id)) {
      throw new ResourceNotFoundException("Project not found with id: " + id);
    }
    projectRepository.deleteById(id);
  }

  /**
   * Add a team to a project.
   *
   * @param projectId Project ID
   * @param teamId Team ID
   * @return Updated project as DTO
   * @throws ResourceNotFoundException if project or team not found
   */
  public ProjectDto addTeamToProject(Long projectId, Long teamId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

    project.addTeam(team);
    Project savedProject = projectRepository.save(project);
    return projectMapper.map(savedProject);
  }

  /**
   * Remove a team from a project.
   *
   * @param projectId Project ID
   * @param teamId Team ID
   * @return Updated project as DTO
   * @throws ResourceNotFoundException if project or team not found
   */
  public ProjectDto removeTeamFromProject(Long projectId, Long teamId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

    project.removeTeam(team);
    Project savedProject = projectRepository.save(project);
    return projectMapper.map(savedProject);
  }

  public List<ProjectDto> getProjectsByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden"));

    return projectRepository.findByActiveTrue().stream()
        .filter(
            project -> project.getTeams().stream().anyMatch(team -> team.getUsers().contains(user)))
        .map(projectMapper::map)
        .collect(Collectors.toList());
  }

}
