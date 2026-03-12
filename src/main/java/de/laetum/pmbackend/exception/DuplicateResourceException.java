package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to create or update a resource that would
 * cause a uniqueness conflict (e.g., duplicate username).
 * Results in HTTP 409 CONFLICT response.
 */
public class DuplicateResourceException extends RuntimeException {

    public static final String USERNAME_EXISTS = "Benutzername existiert bereits: %s";

    public DuplicateResourceException(String message) {
        super(message);
    }
}