package de.laetum.pmbackend.controller.schedule;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {
    @NotNull(message = "Datum ist erforderlich.")
    private LocalDate date;

    @NotNull(message = "Startzeit ist erforderlich.")
    private LocalTime startTime;

    @NotNull(message = "Endzeit ist erforderlich.")
    private LocalTime endTime;

    private String description;

    @NotNull(message = "Projekt ist erforderlich.")
    private Long projectId;

    @NotNull(message = "Team ist erforderlich.")
    private Long teamId;

    @NotNull(message = "Kategorie ist erforderlich.")
    private Long categoryId;
}