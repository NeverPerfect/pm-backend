package de.laetum.pmbackend.service.user;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Generates cryptographically secure random passwords.
 */
@Component
public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final int DEFAULT_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a random password with at least one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     *
     * @return a secure random password
     */
    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random password of the specified length.
     *
     * @param length password length (minimum 8)
     * @return a secure random password
     */
    public String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Passwort muss mindestes 8 Zeichen lang sein");
        }

        StringBuilder password = new StringBuilder(length);

        // Guarantee at least one character from each category
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            password.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        // Shuffle to avoid predictable positions
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}