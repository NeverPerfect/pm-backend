package de.laetum.pmbackend.controller.user;

import de.laetum.pmbackend.controller.user.CreateUserRequest;
import de.laetum.pmbackend.controller.user.UpdateUserRequest;
import de.laetum.pmbackend.controller.user.UserDto;
import de.laetum.pmbackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management. Only accessible by MANAGER and ADMIN
 * roles.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Benutzerverwaltung", description = "CRUD-Operationen für Benutzer (MANAGER/ADMIN)")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "Alle Benutzer abrufen", description = "Gibt eine Liste aller Benutzer zurück")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @Operation(summary = "Benutzer nach ID abrufen", description = "Gibt einen einzelnen Benutzer zurück")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Benutzer gefunden"),
      @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(
      @Parameter(description = "ID des Benutzers") @PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @Operation(summary = "Neuen Benutzer erstellen", description = "Erstellt einen neuen Benutzer (nur ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Benutzer erstellt"),
      @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nur ADMIN)"),
      @ApiResponse(responseCode = "409", description = "Benutzername existiert bereits")
  })
  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserDto createdUser = userService.createUser(request);
    return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
  }

  @Operation(summary = "Benutzer aktualisieren", description = "Aktualisiert einen bestehenden Benutzer")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Benutzer aktualisiert"),
      @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
      @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
      @ApiResponse(responseCode = "409", description = "Benutzername existiert bereits")
  })

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @Parameter(description = "ID des Benutzers") @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.updateUser(id, request));
  }

  @Operation(summary = "Passwort zurücksetzen", description = "Setzt das Passwort eines Benutzers auf ein generiertes Passwort zurück (nur ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Passwort zurückgesetzt"),
      @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nur ADMIN)")
  })
  @PostMapping("/{id}/reset-password")
  public ResponseEntity<UserDto> resetPassword(
      @Parameter(description = "ID des Benutzers") @PathVariable Long id) {
    return ResponseEntity.ok(userService.resetPassword(id));
  }

  @Operation(summary = "Benutzer löschen", description = "Löscht einen Benutzer (nur ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Benutzer gelöscht"),
      @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
      @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nur ADMIN)")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "ID des Benutzers") @PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
