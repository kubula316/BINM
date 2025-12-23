package com.BINM.interactions.service;

import com.BINM.interactions.model.EntityType;
import com.BINM.listing.listing.dto.ListingCoverDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InteractionFacade {

    void addFavorite(String userId, String entityId, EntityType entityType);

    void removeFavorite(String userId, String entityId, EntityType entityType);

    boolean isFavorite(String userId, String entityId, EntityType entityType);

    Page<ListingCoverDto> getFavouritesListings(String userId, int page, int size);

    void removeAllFavoritesForEntity(String entityId, EntityType entityType);
}
