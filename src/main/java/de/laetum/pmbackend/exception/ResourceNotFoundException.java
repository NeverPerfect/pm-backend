package de.laetum.pmbackend.exception;

/**
 * Thrown when a requested resource cannot be found. Results in HTTP 404
 * response.
 * Uses format patterns with static constants for consistent error messages.
 */
public class ResourceNotFoundException extends RuntimeException {

  // Format patterns for resource lookups by ID
  public static final String USER_NOT_FOUND = "Benutzer nicht gefunden mit ID: %d";
  public static final String TEAM_NOT_FOUND = "Team nicht gefunden mit ID: %d";
  public static final String PROJECT_NOT_FOUND = "Projekt nicht gefunden mit ID: %d";
  public static final String SCHEDULE_NOT_FOUND = "Zeitbuchung nicht gefunden mit ID: %d";

  // Format pattern for resource lookups by username
  public static final String USER_NOT_FOUND_BY_USERNAME = "Benutzer nicht gefunden: %s";

  public ResourceNotFoundException(String message) {
    super(message);
  }
}