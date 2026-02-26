package de.laetum.pmbackend.exception;

/**
 * Thrown when an admin attempts to delete or demote their own account.
 */
public class AdminSelfModificationException extends RuntimeException {

    public static final String SELF_DELETE = "Admins cannot delete their own account";
    public static final String SELF_DEMOTE = "Admins cannot remove their own admin role";

    public AdminSelfModificationException(String message) {
        super(message);
    }
}