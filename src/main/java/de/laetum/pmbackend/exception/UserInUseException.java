package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete a user that is still referenced
 * by teams or schedules.
 */
public class UserInUseException extends RuntimeException {

    public static final String IN_TEAMS = "Der Benutzer ist noch einem oder mehreren Teams zugewiesen.";
    public static final String HAS_SCHEDULES = "Der Benutzer hat noch Zeitbuchungen.";

    public UserInUseException(String message) {
        super(message);
    }
}