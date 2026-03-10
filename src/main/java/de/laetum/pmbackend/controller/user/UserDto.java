package de.laetum.pmbackend.controller.user;

import de.laetum.pmbackend.repository.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring user data without sensitive information.
 * The generatedPassword field is only set once during user creation
 * when a password was auto-generated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private Role role;
    private String generatedPassword; // Only set on creation with auto-generated password
}