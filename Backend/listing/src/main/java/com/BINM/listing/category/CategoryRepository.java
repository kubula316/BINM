package com.BINM.listing.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullOrderBySortOrderAscNameAsc();

    List<Category> findByParentIdOrderBySortOrderAscNameAsc(Long parentId);

    boolean existsByParentAndNameIgnoreCase(Category parent, String name);

    boolean existsByParentIsNullAndNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCaseAndParentIsNull(String name);

    Optional<Category> findByNameIgnoreCaseAndParentId(String name, Long parentId);
}
