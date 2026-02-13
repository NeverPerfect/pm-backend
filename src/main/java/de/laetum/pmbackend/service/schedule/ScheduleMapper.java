package de.laetum.pmbackend.service.schedule;

import de.laetum.pmbackend.dto.ScheduleDto;
import de.laetum.pmbackend.entity.Schedule;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Schedule entities to DTOs.
 */
@Component
public class ScheduleMapper {

    /**
     * Maps a Schedule entity to a ScheduleDto.
     * Includes related entity names for display purposes.
     *
     * @param schedule the entity to map
     * @return ScheduleDto with user, project and team information
     */
    public ScheduleDto map(Schedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setDate(schedule.getDate());
        dto.setHours(schedule.getHours());
        dto.setDescription(schedule.getDescription());
        dto.setUserId(schedule.getUser().getId());
        dto.setUsername(schedule.getUser().getUsername());
        dto.setProjectId(schedule.getProject().getId());
        dto.setProjectName(schedule.getProject().getName());
        dto.setTeamId(schedule.getTeam().getId());
        dto.setTeamName(schedule.getTeam().getName());
        return dto;
    }
}