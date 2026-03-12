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
  public ResponseEntity<ErrorResponse> handleUserInUseException(UserInUseException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handles uniqueness constraint violations (e.g., duplicate username).
   */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handles business rule violations where the user is not allowed
   * to perform the requested operation.
   */
  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenOperationException(
      ForbiddenOperationException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles schedule-specific validation failures
   * (e.g., user not in team, inactive project).
   */
  @ExceptionHandler(ScheduleValidationException.class)
  public ResponseEntity<ErrorResponse> handleScheduleValidationException(
      ScheduleValidationException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles attempts to delete a team that is still referenced
   * by other entities (projects, schedules).
   */
  @ExceptionHandler(TeamInUseException.class)
  public ResponseEntity<ErrorResponse> handleTeamInUseException(TeamInUseException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handles attempts to delete a project that is still referenced
   * by schedules.
   */
  @ExceptionHandler(ProjectInUseException.class)
  public ResponseEntity<ErrorResponse> handleProjectInUseException(ProjectInUseException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

}