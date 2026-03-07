package de.laetum.pmbackend.exception;

/**
 * Thrown when a user attempts a forbidden self-modification action.
 */
public class SelfModificationException extends RuntimeException {

    public static final String SELF_DELETE = "Users cannot delete their own account";
    public static final String ADMIN_SELF_DEMOTE = "Admins cannot remove their own admin role";
    public static final String ADMIN_SELF_DEACTIVATE = "Admins cannot deactivate their own account";

    public SelfModificationException(String message) {
        super(message);
    }
}