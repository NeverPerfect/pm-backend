package de.laetum.pmbackend.controller.project;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private boolean active;
}