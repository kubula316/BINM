package com.BINM.listing.listing.dto;

public record ListingMediaDto(
        String url,
        String type, // image|video
        Integer position
) {}
