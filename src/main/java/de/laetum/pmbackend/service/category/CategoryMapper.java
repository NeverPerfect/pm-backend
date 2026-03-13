package de.laetum.pmbackend.service.category;

import de.laetum.pmbackend.controller.category.CategoryDto;
import de.laetum.pmbackend.repository.category.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Category entities to DTOs.
 */
@Component
public class CategoryMapper {

    /**
     * Maps a Category entity to a CategoryDto.
     *
     * @param category the entity to map
     * @return CategoryDto
     */
    public CategoryDto map(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getColor());
    }
}