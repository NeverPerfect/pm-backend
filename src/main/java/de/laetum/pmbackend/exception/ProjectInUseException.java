package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete a project that is still referenced
 * by schedules.
 */
public class ProjectInUseException extends RuntimeException {

    public static final String HAS_SCHEDULES = "Das Projekt hat noch Zeitbuchungen.";

    public ProjectInUseException(String message) {
        super(message);
    }
}