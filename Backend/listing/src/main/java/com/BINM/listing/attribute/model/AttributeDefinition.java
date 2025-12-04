package com.BINM.listing.attribute.model;

import com.BINM.listing.category.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attribute_definition",
        uniqueConstraints = @UniqueConstraint(name = "uk_attrdef_category_key", columnNames = {"category_id", "attr_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "attr_key", nullable = false)
    private String key; // np. brand, model, year

    @Column(nullable = false)
    private String label; // np. Marka

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeType type;

    @Column(name = "unit")
    private String unit; //  cm3, KM, PLN

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
