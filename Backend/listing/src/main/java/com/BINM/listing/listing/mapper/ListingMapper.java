package com.BINM.listing.listing.mapper;

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
