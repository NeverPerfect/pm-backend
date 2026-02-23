package de.laetum.pmbackend.service.team;

import de.laetum.pmbackend.controller.team.TeamDto; 
import de.laetum.pmbackend.repository.team.Team;   
import de.laetum.pmbackend.repository.user.User; 
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting Team entities to DTOs.
 */
@Component
public class TeamMapper {

    /**
     * Maps a Team entity to a TeamDto.
     * Converts the user set to a set of user IDs.
     *
     * @param team the entity to map
     * @return TeamDto with user IDs
     */
    public TeamDto map(Team team) {
        Set<Long> userIds = team.getUsers().stream()
            .map(User::getId)
            .collect(Collectors.toSet());
        
        return new TeamDto(
            team.getId(),
            team.getName(),
            team.getDescription(),
            userIds
        );
    }
}