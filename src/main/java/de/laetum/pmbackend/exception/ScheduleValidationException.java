package de.laetum.pmbackend.exception;

/**
 * Thrown when schedule creation or update fails validation rules
 * (e.g., user not in team, project inactive).
 * Results in HTTP 400 BAD REQUEST response.
 */
public class ScheduleValidationException extends RuntimeException {

    public static final String USER_NOT_IN_TEAM = "User ist nicht Mitglied dieses Teams";
    public static final String PROJECT_NOT_ACTIVE = "Projekt ist nicht aktiv";
    public static final String TEAM_NOT_IN_PROJECT = "Team ist diesem Projekt nicht zugewiesen";
    public static final String USER_INACTIVE_CREATE = "Die Planung kann für einen inaktiven Benutzer nicht erstellt werden";
    public static final String USER_INACTIVE_UPDATE = "Der Schedule kann für einen inaktiven Benutzer nicht aktualisiert werden";

    public ScheduleValidationException(String message) {
        super(message);
    }
}