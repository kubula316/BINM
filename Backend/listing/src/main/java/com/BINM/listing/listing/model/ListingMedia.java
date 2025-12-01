package com.BINM.listing.listing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "listing_media",
        indexes = {
                @Index(name = "idx_media_listing_pos", columnList = "listing_id, position")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // image | video

    @Column(name = "position", nullable = false)
    private Integer position;
}
