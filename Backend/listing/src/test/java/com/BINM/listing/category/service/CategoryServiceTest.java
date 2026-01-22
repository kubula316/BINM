package com.BINM.listing.category.service;

import com.BINM.listing.category.dto.CategoryCreateRequest;
import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.mapper.CategoryMapper;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldCreateRootCategory_WhenParentIdIsNull() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest("Elektronika", null, null, null);
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        when(categoryMapper.toDto(any(Category.class))).thenReturn(new CategoryDto(1L, null, "Elektronika", null, 0, 0, true));

        // Act
        CategoryDto result = categoryService.createCategory(request);

        // Assert
        assertNotNull(result);
        assertEquals("Elektronika", result.name());
        assertNull(result.parentId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldCreateSubCategory_WhenParentIdIsProvided() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest("Laptopy", 1L, null, null);
        Category parent = new Category();
        parent.setId(1L);
        parent.setName("Elektronika");
        parent.setDepth(0);
        parent.setIsLeaf(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category c = i.getArgument(0);
            c.setId(2L);
            return c;
        });

        when(categoryMapper.toDto(any(Category.class))).thenReturn(new CategoryDto(2L, 1L, "Laptopy", null, 0, 1, true));

        // Act
        CategoryDto result = categoryService.createCategory(request);

        // Assert
        assertNotNull(result);
        assertEquals("Laptopy", result.name());
        assertEquals(1L, result.parentId());
        verify(categoryRepository, times(2)).save(any(Category.class)); // Raz parent (update isLeaf), raz child
    }

    @Test
    void createCategory_ShouldThrowException_WhenParentCategoryNotFound() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest("Laptopy", 999L, null, null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getPath_ShouldReturnPathFromRootToLeaf() {
        // Arrange
        Category root = new Category(); root.setId(1L); root.setName("Root");
        Category middle = new Category(); middle.setId(2L); middle.setName("Middle"); middle.setParent(root);
        Category leaf = new Category(); leaf.setId(3L); leaf.setName("Leaf"); leaf.setParent(middle);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(leaf));

        when(categoryMapper.toDto(root)).thenReturn(new CategoryDto(1L, null, "Root", null, 0, 0, false));
        when(categoryMapper.toDto(middle)).thenReturn(new CategoryDto(2L, 1L, "Middle", null, 0, 1, false));
        when(categoryMapper.toDto(leaf)).thenReturn(new CategoryDto(3L, 2L, "Leaf", null, 0, 2, true));

        // Act
        List<CategoryDto> path = categoryService.getPath(3L);

        // Assert
        assertEquals(3, path.size());
        assertEquals("Root", path.get(0).name());
        assertEquals("Middle", path.get(1).name());
        assertEquals("Leaf", path.get(2).name());
    }

    @Test
    void collectDescendantIds_ShouldReturnAllSubCategoryIds() {
        // Arrange
        Category root = new Category(); root.setId(1L);
        Category child1 = new Category(); child1.setId(2L);
        Category child2 = new Category(); child2.setId(3L);
        Category grandChild = new Category(); grandChild.setId(4L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        
        // Mockowanie pobierania dzieci
        when(categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(1L)).thenReturn(List.of(child1, child2));
        when(categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(2L)).thenReturn(List.of(grandChild));
        when(categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(3L)).thenReturn(Collections.emptyList());
        when(categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(4L)).thenReturn(Collections.emptyList());

        // Act
        List<Long> ids = categoryService.collectDescendantIds(1L);

        // Assert
        assertEquals(4, ids.size()); // 1, 2, 3, 4
        assertTrue(ids.containsAll(List.of(1L, 2L, 3L, 4L)));
    }
}
