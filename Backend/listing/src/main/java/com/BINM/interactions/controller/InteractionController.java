package com.BINM.interactions.controller;

import com.BINM.interactions.dto.FavoriteRequest;
import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.service.InteractionFacade;
import com.BINM.listing.listing.dto.ListingCoverDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionFacade interactionFacade;

    @PostMapping("/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @Valid @RequestBody FavoriteRequest request) {
        interactionFacade.addFavorite(userId, request.entityId(), request.entityType());
    }

    @DeleteMapping("/favorites")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @Valid @RequestBody FavoriteRequest request) {
        interactionFacade.removeFavorite(userId, request.entityId(), request.entityType());
    }

    @GetMapping("/favorites/status")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam String entityId,
            @RequestParam EntityType entityType) {
        boolean isFavorite = interactionFacade.isFavorite(userId, entityId, entityType);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<ListingCoverDto>> getFavouritesListings(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(interactionFacade.getFavouritesListings(userId, page, size));
    }
}
