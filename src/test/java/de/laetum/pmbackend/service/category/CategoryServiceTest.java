package de.laetum.pmbackend.service.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.laetum.pmbackend.controller.category.CategoryDto;
import de.laetum.pmbackend.controller.category.CreateCategoryRequest;
import de.laetum.pmbackend.controller.category.UpdateCategoryRequest;
import de.laetum.pmbackend.exception.CategoryInUseException;
import de.laetum.pmbackend.exception.DuplicateResourceException;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.category.Category;
import de.laetum.pmbackend.repository.category.CategoryRepository;
import de.laetum.pmbackend.repository.schedule.ScheduleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Entwicklung", "Softwareentwicklung", "#4CAF50");
        testCategory.setId(1L);

        when(categoryMapper.map(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            return new CategoryDto(c.getId(), c.getName(), c.getDescription(), c.getColor());
        });
    }

    // ==================== getAllCategories ====================

    @Test
    @DisplayName("getAllCategories returns list of all categories")
    void getAllCategories_ReturnsAllCategories() {
        Category cat2 = new Category("Meeting", "Meetings und Abstimmungen", "#2196F3");
        cat2.setId(2L);

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, cat2));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Entwicklung", result.get(0).getName());
        assertEquals("Meeting", result.get(1).getName());
    }

    @Test
    @DisplayName("getAllCategories returns empty list when no categories exist")
    void getAllCategories_WhenEmpty_ReturnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        List<CategoryDto> result = categoryService.getAllCategories();

        assertTrue(result.isEmpty());
    }

    // ==================== getCategoryById ====================

    @Test
    @DisplayName("getCategoryById returns category when found")
    void getCategoryById_WhenExists_ReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals("Entwicklung", result.getName());
        assertEquals("#4CAF50", result.getColor());
    }

    @Test
    @DisplayName("getCategoryById throws exception when not found")
    void getCategoryById_WhenNotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    // ==================== createCategory ====================

    @Test
    @DisplayName("createCategory creates category successfully")
    void createCategory_WithValidData_CreatesCategory() {
        CreateCategoryRequest request = new CreateCategoryRequest("Testing", "QA Tests", "#FF9800");

        when(categoryRepository.existsByName("Testing")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        CategoryDto result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("Testing", result.getName());
        assertEquals("#FF9800", result.getColor());
    }

    @Test
    @DisplayName("createCategory throws exception when name already exists")
    void createCategory_WhenNameExists_ThrowsDuplicateException() {
        CreateCategoryRequest request = new CreateCategoryRequest("Entwicklung", "Duplicate", "#000000");

        when(categoryRepository.existsByName("Entwicklung")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    // ==================== updateCategory ====================

    @Test
    @DisplayName("updateCategory updates category successfully")
    void updateCategory_WithValidData_UpdatesCategory() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Dev", "Updated description", "#00BCD4");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName("Dev")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory allows keeping the same name")
    void updateCategory_WithSameName_UpdatesSuccessfully() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Entwicklung", "New desc", "#000000");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName("Entwicklung")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory throws exception when name conflicts with another category")
    void updateCategory_WhenNameConflicts_ThrowsDuplicateException() {
        Category otherCategory = new Category("Meeting", "Meetings", "#2196F3");
        otherCategory.setId(2L);

        UpdateCategoryRequest request = new UpdateCategoryRequest("Meeting", "Trying to steal name", "#000000");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName("Meeting")).thenReturn(Optional.of(otherCategory));

        assertThrows(DuplicateResourceException.class, () -> categoryService.updateCategory(1L, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCategory throws exception when category not found")
    void updateCategory_WhenNotFound_ThrowsException() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Whatever", "Desc", "#000000");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(99L, request));
    }

    // ==================== deleteCategory ====================

    @Test
    @DisplayName("deleteCategory deletes category successfully")
    void deleteCategory_WhenExists_DeletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteCategory throws exception when not found")
    void deleteCategory_WhenNotFound_ThrowsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteCategory throws CategoryInUseException when category has schedules")
    void deleteCategory_WhenHasSchedules_ThrowsCategoryInUseException() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByCategoryId(1L)).thenReturn(true);

        CategoryInUseException exception = assertThrows(CategoryInUseException.class,
                () -> categoryService.deleteCategory(1L));
        assertEquals(CategoryInUseException.HAS_SCHEDULES, exception.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }
}