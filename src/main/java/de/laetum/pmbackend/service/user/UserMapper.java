package de.laetum.pmbackend.service.user;

import de.laetum.pmbackend.controller.user.UserDto;
import de.laetum.pmbackend.repository.user.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting User entities to DTOs.
 */
@Component
public class UserMapper {

    /**
     * Maps a User entity to a UserDto.
     * Excludes sensitive data like passwords.
     *
     * @param user the entity to map
     * @return UserDto without password
     */
    public UserDto map(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getRole(),
                null // generatedPassword - only set during user creation
        );
    }
}