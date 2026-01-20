package de.laetum.pmbackend.service;

import de.laetum.pmbackend.dto.CreateProjectRequest;
import de.laetum.pmbackend.dto.ProjectDto;
import de.laetum.pmbackend.dto.UpdateProjectRequest;
import de.laetum.pmbackend.entity.Project;
import de.laetum.pmbackend.entity.Team;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.ProjectRepository;
import de.laetum.pmbackend.repository.TeamRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final TeamRepository teamRepository;

  public ProjectService(ProjectRepository projectRepository, TeamRepository teamRepository) {
    this.projectRepository = projectRepository;
    this.teamRepository = teamRepository;
  }

  public List<ProjectDto> getAllProjects() {
    return projectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  public ProjectDto getProjectById(Long id) {
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    return toDto(project);
  }

  public ProjectDto createProject(CreateProjectRequest request) {
    Project project = new Project(request.getName(), request.getDescription(), true);
    Project savedProject = projectRepository.save(project);
    return toDto(savedProject);
  }

  public ProjectDto updateProject(Long id, UpdateProjectRequest request) {
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setActive(request.isActive());

    Project savedProject = projectRepository.save(project);
    return toDto(savedProject);
  }

  public void deleteProject(Long id) {
    if (!projectRepository.existsById(id)) {
      throw new ResourceNotFoundException("Project not found with id: " + id);
    }
    projectRepository.deleteById(id);
  }

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
    return toDto(savedProject);
  }

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
    return toDto(savedProject);
  }

  private ProjectDto toDto(Project project) {
    Set<Long> teamIds = project.getTeams().stream().map(Team::getId).collect(Collectors.toSet());
    return new ProjectDto(
        project.getId(), project.getName(), project.getDescription(), project.isActive(), teamIds);
  }
}
