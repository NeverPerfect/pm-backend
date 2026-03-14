package de.laetum.pmbackend.controller.user;

import de.laetum.pmbackend.repository.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing user. Password is optional - only set if it
 * should be changed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "Benutzername ist erforderlich")
    private String username;
    @NotBlank(message = "Vorname ist erforderlich")
    private String firstName;
    @NotBlank(message = "Nachname ist erforderlich")
    private String lastName;
    @NotNull(message = "Rolle ist erforderlich")
    private Role role;
    @NotNull(message = "Aktiv-Status ist erforderlich")
    private Boolean active;
    private String password;
}