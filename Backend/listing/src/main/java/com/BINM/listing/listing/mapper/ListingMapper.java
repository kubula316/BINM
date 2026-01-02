package com.BINM.listing.listing.mapper;

import com.BINM.listing.category.model.Category;
import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.user.io.ProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currency", expression = "java(req.currency() != null ? req.currency() : \"PLN\")")
    @Mapping(target = "negotiable", expression = "java(req.negotiable() != null && req.negotiable())")
    @Mapping(target = "sellerUserId", source = "sellerUserId")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "title", source = "req.title")
    @Mapping(target = "description", source = "req.description")
    @Mapping(target = "priceAmount", source = "req.priceAmount")
    @Mapping(target = "locationCity", source = "req.locationCity")
    @Mapping(target = "locationRegion", source = "req.locationRegion")
    @Mapping(target = "latitude", source = "req.latitude")
    @Mapping(target = "longitude", source = "req.longitude")
    @Mapping(target = "contactPhoneNumber", source = "req.contactPhoneNumber")
    Listing toEntity(ListingCreateRequest req, String sellerUserId, Category category);

    @Mapping(target = "categoryId", source = "listing.category.id")
    @Mapping(target = "seller", source = "sellerProfile")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "media", source = "media")
    @Mapping(target = "seller.id", source = "sellerProfile.userId")
    @Mapping(target = "seller.name", source = "sellerProfile.name")
    ListingDto toDto(Listing listing, ProfileResponse sellerProfile, List<ListingAttribute> attributes, List<ListingMedia> media);

    @Mapping(target = "categoryId", source = "listing.category.id")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "media", source = "media")
    ListingEditDto toEditDto(Listing listing, List<ListingAttribute> attributes, List<ListingMedia> media);

    @Mapping(target = "seller.id", source = "sellerProfile.userId")
    @Mapping(target = "seller.name", source = "sellerProfile.name")
    @Mapping(target = "coverImageUrl", source = "coverImage")
    @Mapping(target = "locationCity", source = "listing.locationCity")
    ListingCoverDto toCoverDto(Listing listing, ProfileResponse sellerProfile, String coverImage);

    @Mapping(target = "key", source = "attribute.key")
    @Mapping(target = "label", source = "attribute.label")
    @Mapping(target = "type", source = "attribute.type")
    @Mapping(target = "stringValue", source = "VText")
    @Mapping(target = "numberValue", source = "VNumber")
    @Mapping(target = "booleanValue", source = "VBoolean")
    @Mapping(target = "enumValue", source = "option.value")
    @Mapping(target = "enumLabel", source = "option.label")
    ListingAttributeDto toAttributeDto(ListingAttribute attribute);

    @Mapping(target = "url", source = "mediaUrl")
    ListingMediaDto toMediaDto(ListingMedia media);
}
