package com.BINM.listing.listing.service;

import com.BINM.listing.attribute.service.AttributeFacade;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import com.BINM.listing.category.service.CategoryFacade;
import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.SellerInfo;
import com.BINM.listing.listing.mapper.ListingMapper;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingStatus;
import com.BINM.listing.listing.repository.ListingAttributeRepository;
import com.BINM.listing.listing.repository.ListingMediaRepository;
import com.BINM.listing.listing.repository.ListingRepository;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.service.ProfileFacade;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ListingAttributeRepository listingAttributeRepository;
    @Mock
    private ListingMediaRepository listingMediaRepository;
    @Mock
    private AttributeFacade attributeService;
    @Mock
    private ProfileFacade profileFacade;
    @Mock
    private CategoryFacade categoryService;
    @Mock
    private ListingMapper listingMapper;
    @Mock
    private ListingValidator listingValidator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ListingService listingService;

    @Test
    void create_ShouldCreateDraftListing_WhenRequestIsValid() {
        // Arrange
        String sellerId = "user-1";
        ListingCreateRequest request = new ListingCreateRequest(
                1L, "Audi A4", "Opis", new BigDecimal("50000"), "PLN", true,
                "Gdańsk", "Pomorskie", 54.0, 18.0, "123456789",
                Collections.emptyList(), Collections.emptyList()
        );

        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        
        Listing listingEntity = new Listing();
        listingEntity.setId(100L);
        listingEntity.setStatus(ListingStatus.DRAFT);
        listingEntity.setSellerUserId(sellerId);

        when(listingMapper.toEntity(request, sellerId, category)).thenReturn(listingEntity);
        when(listingRepository.save(any(Listing.class))).thenReturn(listingEntity);
        
        when(attributeService.getEffectiveDefinitionsByKey(1L)).thenReturn(Collections.emptyMap());
        when(profileFacade.getProfile(sellerId)).thenReturn(new ProfileResponse(sellerId, "Jan", "jan@wp.pl", true, null));
        
        // Poprawna kolejność argumentów w ListingDto
        when(listingMapper.toDto(any(), any(), any(), any())).thenReturn(
                new ListingDto(
                        UUID.randomUUID(), // publicId
                        1L, // categoryId
                        new SellerInfo(sellerId, "Jan"), // seller
                        "Audi A4", // title
                        "Opis", // description
                        new BigDecimal("50000"), // priceAmount
                        "PLN", // currency
                        true, // negotiable
                        "Gdańsk", // locationCity
                        "Pomorskie", // locationRegion
                        54.0, // latitude
                        18.0, // longitude
                        ListingStatus.DRAFT.name(), // status (String)
                        null, // publishedAt
                        null, // expiresAt
                        null, // createdAt
                        null, // updatedAt
                        Collections.emptyList(), // attributes
                        Collections.emptyList() // media
                )
        );

        // Act
        ListingDto result = listingService.create(request, sellerId);

        // Assert
        assertNotNull(result);
        assertEquals(ListingStatus.DRAFT.name(), result.status());
        verify(listingValidator).validateCategoryIsLeaf(category);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void submitForApproval_ShouldChangeStatusToWaiting_WhenStatusIsDraft() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        String userId = "user-1";
        Listing listing = new Listing();
        listing.setPublicId(publicId);
        listing.setSellerUserId(userId);
        listing.setStatus(ListingStatus.DRAFT);

        when(listingRepository.findByPublicId(publicId)).thenReturn(Optional.of(listing));

        // Act
        listingService.submitForApproval(publicId, userId);

        // Assert
        assertEquals(ListingStatus.WAITING, listing.getStatus());
        verify(listingValidator).validateOwnership(listing, userId);
        verify(listingRepository).save(listing);
    }

    @Test
    void approveListing_ShouldChangeStatusToActiveAndSetDates() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        Listing listing = new Listing();
        listing.setStatus(ListingStatus.WAITING);

        when(listingRepository.findByPublicId(publicId)).thenReturn(Optional.of(listing));

        // Act
        listingService.approveListing(publicId);

        // Assert
        assertEquals(ListingStatus.ACTIVE, listing.getStatus());
        assertNotNull(listing.getPublishedAt());
        assertNotNull(listing.getExpiresAt());
        verify(listingRepository).save(listing);
    }

    @Test
    void get_ShouldReturnListing_WhenActive() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        Listing listing = new Listing();
        listing.setId(1L);
        listing.setPublicId(publicId);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setSellerUserId("user-1");

        when(listingRepository.findByPublicId(publicId)).thenReturn(Optional.of(listing));
        when(profileFacade.getProfile("user-1")).thenReturn(new ProfileResponse("user-1", "Jan", "email", true, null));
        when(listingMapper.toDto(any(), any(), any(), any())).thenReturn(mock(ListingDto.class));

        // Act
        ListingDto result = listingService.get(publicId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void get_ShouldThrowException_WhenNotActive() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        Listing listing = new Listing();
        listing.setStatus(ListingStatus.DRAFT);

        when(listingRepository.findByPublicId(publicId)).thenReturn(Optional.of(listing));

        // Act & Assert
        // Oczekujemy wyjątku z ListingException (który może być RuntimeException)
        assertThrows(RuntimeException.class, () -> listingService.get(publicId));
    }
}
