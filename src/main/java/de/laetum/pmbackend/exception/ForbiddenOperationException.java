package de.laetum.pmbackend.exception;

/**
 * Thrown when a user attempts an operation they are not allowed to perform
 * based on business rules (not authentication).
 * Results in HTTP 403 FORBIDDEN response.
 */
public class ForbiddenOperationException extends RuntimeException {

    public static final String ONLY_ADMINS_MODIFY_ADMINS = "Nur Admins können andere Admins bearbeiten";
    public static final String SCHEDULE_NOT_OWNED = "Keine Berechtigung für diesen Schedule";
    public static final String ADMIN_NOT_VISIBLE = "Admin-Benutzer sind für Manager nicht sichtbar.";

    public ForbiddenOperationException(String message) {
        super(message);
    }
}