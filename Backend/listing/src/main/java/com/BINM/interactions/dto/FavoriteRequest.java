package com.BINM.interactions.dto;

import com.BINM.interactions.model.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(
        @NotBlank
        String entityId,

        @NotNull
        EntityType entityType
) {
}
