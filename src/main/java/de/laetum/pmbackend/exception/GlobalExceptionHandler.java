package de.laetum.pmbackend.exception;

import de.laetum.pmbackend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }
}
