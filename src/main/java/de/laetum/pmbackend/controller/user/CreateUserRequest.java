package de.laetum.pmbackend.controller.user;

import de.laetum.pmbackend.repository.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new user. Contains all required fields.
 * Password is optional — if not provided, a secure password is generated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Benutzername ist erforderlich")
    private String username;

    private String password; // Optional - generated if not provided

    @NotBlank(message = "Vorname ist erforderlich")
    private String firstName;

    @NotBlank(message = "Nachname ist erforderlich")
    private String lastName;

    @NotNull(message = "Rolle ist erforderlich")
    private Role role;

    private boolean active = true;
}