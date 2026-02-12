package de.laetum.pmbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Hours are required")
    @Positive(message = "Hours must be positive")
    private Double hours;

    private String description;

    @NotNull(message = "Project is required")
    private Long projectId;

    @NotNull(message = "Team is required")
    private Long teamId;
}