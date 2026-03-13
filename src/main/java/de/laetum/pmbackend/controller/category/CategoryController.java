package de.laetum.pmbackend.controller.category;

import de.laetum.pmbackend.service.category.CategoryService;
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
 * REST controller for managing booking categories.
 * All endpoints require MANAGER or ADMIN role.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Buchungskategorien", description = "CRUD-Operationen für Buchungskategorien (MANAGER/ADMIN)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Alle Kategorien abrufen", description = "Gibt eine Liste aller Buchungskategorien zurück")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Kategorie nach ID abrufen", description = "Gibt eine einzelne Buchungskategorie zurück")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategorie gefunden"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID der Kategorie") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Neue Kategorie erstellen", description = "Erstellt eine neue Buchungskategorie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategorie erstellt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "409", description = "Name existiert bereits"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
    })
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Kategorie aktualisieren", description = "Aktualisiert eine bestehende Buchungskategorie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategorie aktualisiert"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "409", description = "Name existiert bereits"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID der Kategorie") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Kategorie löschen", description = "Löscht eine Buchungskategorie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategorie gelöscht"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            @ApiResponse(responseCode = "409", description = "Kategorie wird noch referenziert")
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID der Kategorie") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}