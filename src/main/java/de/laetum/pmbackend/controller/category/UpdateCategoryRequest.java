package de.laetum.pmbackend.controller.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @NotBlank(message = "Name ist erforderlich.")
    private String name;

    private String description;

    @NotBlank(message = "Farbe ist erforderlich.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Farbe muss ein gültiger Hex-Code sein (z.B. #FF5733).")
    private String color;
}