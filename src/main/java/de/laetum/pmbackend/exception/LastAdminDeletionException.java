package de.laetum.pmbackend.exception;

/**
 * Thrown when attempting to delete the last remaining active admin account.
 */
public class LastAdminDeletionException extends RuntimeException {

    public static final String MESSAGE = "Cannot delete the last active admin account";

    public LastAdminDeletionException() {
        super(MESSAGE);
    }
}