package de.laetum.pmbackend.exception;

/**
 * Thrown when a manually provided password does not meet the required policy
 * rules.
 * Maps to 400 BAD REQUEST.
 */
public class PasswordPolicyException extends RuntimeException {

    public static final String TOO_SHORT = "Das Passwort muss mindestens 8 Zeichen lang sein.";
    public static final String MISSING_UPPERCASE = "Das Passwort muss mindestens einen Großbuchstaben enthalten.";
    public static final String MISSING_LOWERCASE = "Das Passwort muss mindestens einen Kleinbuchstaben enthalten.";
    public static final String MISSING_DIGIT = "Das Passwort muss mindestens eine Ziffer enthalten.";
    public static final String MISSING_SPECIAL = "Das Passwort muss mindestens ein Sonderzeichen enthalten (!@#$%&*).";

    public PasswordPolicyException(String message) {
        super(message);
    }
}