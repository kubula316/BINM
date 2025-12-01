package com.BINM.listing.listing.model;

import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "listing_attribute",
        uniqueConstraints = @UniqueConstraint(name = "uk_lattr_listing_attr", columnNames = {"listing_id", "attribute_id"}),
        indexes = {
                @Index(name = "idx_lattr_attr_text", columnList = "attribute_id, v_text"),
                @Index(name = "idx_lattr_attr_num", columnList = "attribute_id, v_number"),
                @Index(name = "idx_lattr_attr_bool", columnList = "attribute_id, v_boolean"),
                @Index(name = "idx_lattr_attr_option", columnList = "attribute_id, option_id"),
                @Index(name = "idx_lattr_listing", columnList = "listing_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeDefinition attribute;

    @Column(name = "v_text")
    private String vText;

    @Column(name = "v_number", precision = 18, scale = 4)
    private BigDecimal vNumber;

    @Column(name = "v_boolean")
    private Boolean vBoolean;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private AttributeOption option;
}
