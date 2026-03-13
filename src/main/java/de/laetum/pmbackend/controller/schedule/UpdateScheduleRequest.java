package de.laetum.pmbackend.controller.schedule;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {
    @NotNull(message = "Datum ist erforderlich.")
    private LocalDate date;

    @NotNull(message = "Stunden sind erforderlich.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Stunden dürfen nicht negativ sein.")
    @DecimalMax(value = "24.0", inclusive = true, message = "Stunden dürfen 24 nicht überschreiten.")
    private Double hours;

    private String description;

    @NotNull(message = "Projekt ist erforderlich.")
    private Long projectId;

    @NotNull(message = "Team ist erforderlich.")
    private Long teamId;
}