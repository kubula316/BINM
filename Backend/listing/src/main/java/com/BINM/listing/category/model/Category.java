package com.BINM.listing.category.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_parent_name", columnNames = {"parent_id", "name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer depth;

    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf;
}
