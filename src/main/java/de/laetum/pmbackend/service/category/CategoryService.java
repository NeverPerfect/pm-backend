package de.laetum.pmbackend.service.category;

import de.laetum.pmbackend.controller.category.CategoryDto;
import de.laetum.pmbackend.controller.category.CreateCategoryRequest;
import de.laetum.pmbackend.controller.category.UpdateCategoryRequest;
import de.laetum.pmbackend.exception.DuplicateResourceException;
import de.laetum.pmbackend.exception.ResourceNotFoundException;
import de.laetum.pmbackend.repository.category.Category;
import de.laetum.pmbackend.repository.category.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for booking category management operations.
 * Handles CRUD operations for categories used to classify time entries.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Get all categories.
     *
     * @return List of all categories as DTOs
     */
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::map)
                .collect(Collectors.toList());
    }

    /**
     * Get a single category by ID.
     *
     * @param id Category ID
     * @return Category as DTO
     * @throws ResourceNotFoundException if category not found
     */
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ResourceNotFoundException.CATEGORY_NOT_FOUND, id)));
        return categoryMapper.map(category);
    }

    /**
     * Create a new category.
     *
     * @param request Category data (name, description, color)
     * @return Created category as DTO
     * @throws DuplicateResourceException if name already exists
     */
    public CategoryDto createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    String.format(DuplicateResourceException.CATEGORY_NAME_EXISTS, request.getName()));
        }

        Category category = new Category(
                request.getName(),
                request.getDescription(),
                request.getColor());
        Category saved = categoryRepository.save(category);
        return categoryMapper.map(saved);
    }

    /**
     * Update an existing category.
     *
     * @param id      Category ID
     * @param request Updated category data
     * @return Updated category as DTO
     * @throws ResourceNotFoundException  if category not found
     * @throws DuplicateResourceException if new name conflicts with existing
     *                                    category
     */
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ResourceNotFoundException.CATEGORY_NOT_FOUND, id)));

        // Check if new name conflicts with another category
        categoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                String.format(DuplicateResourceException.CATEGORY_NAME_EXISTS, request.getName()));
                    }
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());

        Category saved = categoryRepository.save(category);
        return categoryMapper.map(saved);
    }

    /**
     * Delete a category.
     *
     * @param id Category ID
     * @throws ResourceNotFoundException if category not found
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format(ResourceNotFoundException.CATEGORY_NOT_FOUND, id));
        }
        categoryRepository.deleteById(id);
    }
}