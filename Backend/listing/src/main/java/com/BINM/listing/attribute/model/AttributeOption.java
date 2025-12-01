package com.BINM.listing.attribute.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attribute_option",
        uniqueConstraints = @UniqueConstraint(name = "uk_attropt_attr_value", columnNames = {"attribute_id", "value"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeDefinition attribute;

    @Column(nullable = false)
    private String value; // np. 'audi'

    @Column(nullable = false)
    private String label; // np. 'Audi'

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
