package com.BINM.listing.listing.mapper;

import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.user.io.ProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(target = "categoryId", source = "listing.category.id")
    @Mapping(target = "seller", source = "sellerProfile")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "media", source = "media")
    ListingDto toDto(Listing listing, ProfileResponse sellerProfile, List<ListingAttribute> attributes, List<ListingMedia> media);

    @Mapping(target = "categoryId", source = "listing.category.id")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "media", source = "media")
    ListingEditDto toEditDto(Listing listing, List<ListingAttribute> attributes, List<ListingMedia> media);

    @Mapping(target = "seller", source = "sellerProfile")
    @Mapping(target = "coverImageUrl", source = "coverImage")
    ListingCoverDto toCoverDto(Listing listing, ProfileResponse sellerProfile, String coverImage);


}
