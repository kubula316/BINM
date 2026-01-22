package com.BINM.listing.attribute.service;

import com.BINM.listing.attribute.dto.AttributeCreateRequest;
import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.attribute.Mapper.AttributeMapper;
import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import com.BINM.listing.attribute.model.AttributeType;
import com.BINM.listing.attribute.repostiory.AttributeDefinitionRepository;
import com.BINM.listing.attribute.repostiory.AttributeOptionRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {

    @Mock
    private AttributeDefinitionRepository definitionRepository;
    @Mock
    private AttributeOptionRepository optionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AttributeMapper attributeMapper;

    @InjectMocks
    private AttributeService attributeService;

    @Test
    void createAttribute_ShouldSaveDefinitionAndOptions_WhenTypeIsEnum() {
        // Arrange
        AttributeCreateRequest request = new AttributeCreateRequest(
                1L, "color", "Kolor", AttributeType.ENUM, null, 1, List.of("Red", "Blue")
        );

        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(definitionRepository.save(any(AttributeDefinition.class))).thenAnswer(i -> {
            AttributeDefinition def = i.getArgument(0);
            def.setId(100L);
            return def;
        });

        // Mockujemy mapper dla wyniku
        when(attributeMapper.toDto(any(AttributeDefinition.class), anyList())).thenReturn(mock(AttributeDefinitionDto.class));

        // Act
        attributeService.createAttribute(request);

        // Assert
        verify(definitionRepository).save(any(AttributeDefinition.class));
        verify(optionRepository, times(2)).save(any(AttributeOption.class)); // 2 opcje
    }

    @Test
    void createAttribute_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        AttributeCreateRequest request = new AttributeCreateRequest(
                999L, "key", "Label", AttributeType.STRING, null, 1, null
        );
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> attributeService.createAttribute(request));
        verify(definitionRepository, never()).save(any());
    }
}
