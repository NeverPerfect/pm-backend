package de.laetum.pmbackend.controller.auth; 

import de.laetum.pmbackend.controller.auth.LoginRequest;
import de.laetum.pmbackend.controller.auth.LoginResponse;
import de.laetum.pmbackend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentifizierung", description = "Login und Authentifizierung")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(
      summary = "Benutzer einloggen",
      description = "Authentifiziert einen Benutzer und gibt ein JWT-Token zurück")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login erfolgreich"),
    @ApiResponse(
        responseCode = "401",
        description = "Ungültige Anmeldedaten oder Benutzer inaktiv"),
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage (fehlende Felder)")
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }
}
