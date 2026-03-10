package de.laetum.pmbackend.service.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    @DisplayName("generate creates password with default length")
    void generate_ReturnsPasswordWithDefaultLength() {
        String password = passwordGenerator.generate();
        assertEquals(16, password.length());
    }

    @Test
    @DisplayName("generate creates password with specified length")
    void generate_WithCustomLength_ReturnsCorrectLength() {
        String password = passwordGenerator.generate(20);
        assertEquals(20, password.length());
    }

    @Test
    @DisplayName("generate creates password containing required character types")
    void generate_ContainsAllRequiredCharacterTypes() {
        String password = passwordGenerator.generate();
        assertTrue(password.chars().anyMatch(Character::isUpperCase), "Must contain uppercase");
        assertTrue(password.chars().anyMatch(Character::isLowerCase), "Must contain lowercase");
        assertTrue(password.chars().anyMatch(Character::isDigit), "Must contain digit");
        assertTrue(password.chars().anyMatch(c -> "!@#$%&*".indexOf(c) >= 0), "Must contain special char");
    }

    @Test
    @DisplayName("generate throws exception for length below minimum")
    void generate_WithTooShortLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> passwordGenerator.generate(7));
    }

    @Test
    @DisplayName("generate creates unique passwords")
    void generate_CreatesUniquePasswords() {
        String password1 = passwordGenerator.generate();
        String password2 = passwordGenerator.generate();
        assertNotEquals(password1, password2);
    }
}