package de.laetum.pmbackend.controller.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double hours;
    private String description;
    private Long userId;
    private String username;
    private Long projectId;
    private String projectName;
    private Long teamId;
    private String teamName;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
}