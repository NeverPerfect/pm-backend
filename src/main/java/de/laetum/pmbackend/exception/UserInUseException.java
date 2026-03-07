package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete a user that is still referenced
 * by teams or schedules.
 */
public class UserInUseException extends RuntimeException {

    public static final String IN_TEAMS = "User is still assigned to one or more teams";
    public static final String HAS_SCHEDULES = "User still has schedule entries";

    public UserInUseException(String message) {
        super(message);
    }
}