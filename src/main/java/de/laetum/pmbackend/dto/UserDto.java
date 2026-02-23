package de.laetum.pmbackend.dto;

import de.laetum.pmbackend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring user data without sensitive information.
 * Used for API responses where password must not be exposed.
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
}