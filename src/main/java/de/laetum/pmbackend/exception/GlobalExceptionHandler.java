package de.laetum.pmbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling for all REST controllers.
 * Translates exceptions into consistent HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles failed authentication attempts.
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles requests for resources that do not exist.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles invalid method arguments.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErrorResponse error = new ErrorResponse(400, ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles attempts by users to perform forbidden self-modifications
   * (e.g., self-deletion or admin self-demotion).
   */
  @ExceptionHandler(SelfModificationException.class)
  public ResponseEntity<ErrorResponse> handleSelfModificationException(
      SelfModificationException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles attempts to delete the last remaining active admin account.
   */
  @ExceptionHandler(LastAdminDeletionException.class)
  public ResponseEntity<ErrorResponse> handleLastAdminDeletionException(
      LastAdminDeletionException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handles attempts to delete a user that is still referenced
   * by other entities (teams, schedules).
   */
  @ExceptionHandler(UserInUseException.class)
  public ResponseEntity<ErrorResponse> handleUserInUseException(
      UserInUseException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }
}
