package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete a team that is still referenced
 * by projects or schedules.
 */
public class TeamInUseException extends RuntimeException {

    public static final String IN_PROJECTS = "Das Team ist noch einem oder mehreren Projekten zugewiesen.";
    public static final String HAS_SCHEDULES = "Das Team hat noch Zeitbuchungen.";

    public TeamInUseException(String message) {
        super(message);
    }
}