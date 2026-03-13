package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete a category that is still referenced
 * by schedules.
 */
public class CategoryInUseException extends RuntimeException {

    public static final String HAS_SCHEDULES = "Die Kategorie hat noch Zeitbuchungen.";

    public CategoryInUseException(String message) {
        super(message);
    }
}