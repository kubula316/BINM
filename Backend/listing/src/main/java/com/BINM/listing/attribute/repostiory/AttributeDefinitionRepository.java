package com.BINM.listing.attribute.repostiory;

import com.BINM.listing.attribute.model.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    List<AttributeDefinition> findByCategoryIdOrderBySortOrderAscIdAsc(Long categoryId);
}
