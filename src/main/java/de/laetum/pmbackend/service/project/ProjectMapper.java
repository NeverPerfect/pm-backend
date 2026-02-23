package de.laetum.pmbackend.service.project;

import de.laetum.pmbackend.dto.ProjectDto;
import de.laetum.pmbackend.entity.Project;
import de.laetum.pmbackend.entity.Team;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting Project entities to DTOs.
 */
@Component
public class ProjectMapper {

    /**
     * Maps a Project entity to a ProjectDto.
     * Converts the team set to a set of team IDs.
     *
     * @param project the entity to map
     * @return ProjectDto with team IDs
     */
    public ProjectDto map(Project project) {
        Set<Long> teamIds = project.getTeams().stream()
            .map(Team::getId)
            .collect(Collectors.toSet());
        
        return new ProjectDto(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.isActive(),
            teamIds
        );
    }
}