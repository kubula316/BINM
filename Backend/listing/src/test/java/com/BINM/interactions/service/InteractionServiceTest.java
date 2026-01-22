package com.BINM.interactions.service;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import com.BINM.interactions.repository.FavoriteRepository;
import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.service.ListingFacade;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private ListingFacade listingFacade;

    @InjectMocks
    private InteractionService interactionService;

    @Test
    void addFavorite_ShouldSaveFavorite_WhenListingExistsAndNotFavoritedYet() {
        // Arrange
        String userId = "user-1";
        String entityId = UUID.randomUUID().toString();
        EntityType entityType = EntityType.LISTING;

        when(listingFacade.existsById(UUID.fromString(entityId))).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType)).thenReturn(false);

        // Act
        interactionService.addFavorite(userId, entityId, entityType);

        // Assert
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void addFavorite_ShouldNotSave_WhenAlreadyFavorited() {
        // Arrange
        String userId = "user-1";
        String entityId = UUID.randomUUID().toString();
        EntityType entityType = EntityType.LISTING;

        when(listingFacade.existsById(UUID.fromString(entityId))).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType)).thenReturn(true);

        // Act
        interactionService.addFavorite(userId, entityId, entityType);

        // Assert
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void addFavorite_ShouldThrowException_WhenListingDoesNotExist() {
        // Arrange
        String userId = "user-1";
        String entityId = UUID.randomUUID().toString();
        EntityType entityType = EntityType.LISTING;

        when(listingFacade.existsById(UUID.fromString(entityId))).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            interactionService.addFavorite(userId, entityId, entityType)
        );
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void removeFavorite_ShouldDeleteFromRepository() {
        // Arrange
        String userId = "user-1";
        String entityId = "entity-1";
        EntityType entityType = EntityType.LISTING;

        // Act
        interactionService.removeFavorite(userId, entityId, entityType);

        // Assert
        verify(favoriteRepository).deleteByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
    }

    @Test
    void getFavouritesListings_ShouldReturnListings_WhenFavoritesExist() {
        // Arrange
        String userId = "user-1";
        String listingId1 = UUID.randomUUID().toString();
        String listingId2 = UUID.randomUUID().toString();
        List<String> favoriteIds = List.of(listingId1, listingId2);

        when(favoriteRepository.findEntityIdsByUserIdAndEntityType(userId, EntityType.LISTING))
                .thenReturn(favoriteIds);

        ListingCoverDto cover1 = mock(ListingCoverDto.class);
        ListingCoverDto cover2 = mock(ListingCoverDto.class);
        Page<ListingCoverDto> page = new PageImpl<>(List.of(cover1, cover2));

        when(listingFacade.getListingsByIds(anyList(), eq(0), eq(10))).thenReturn(page);

        // Act
        Page<ListingCoverDto> result = interactionService.getFavouritesListings(userId, 0, 10);

        // Assert
        assertEquals(2, result.getTotalElements());
        verify(listingFacade).getListingsByIds(anyList(), eq(0), eq(10));
    }

    @Test
    void getFavouritesListings_ShouldReturnEmptyPage_WhenNoFavorites() {
        // Arrange
        String userId = "user-1";
        when(favoriteRepository.findEntityIdsByUserIdAndEntityType(userId, EntityType.LISTING))
                .thenReturn(Collections.emptyList());

        // Act
        Page<ListingCoverDto> result = interactionService.getFavouritesListings(userId, 0, 10);

        // Assert
        assertTrue(result.isEmpty());
        verify(listingFacade, never()).getListingsByIds(anyList(), anyInt(), anyInt());
    }
}
