package com.BINM.listing.attribute;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, Long> {
    List<AttributeOption> findByAttributeIdIn(List<Long> attributeIds);
    List<AttributeOption> findByAttributeIdOrderBySortOrderAscIdAsc(Long attributeId);
    Optional<AttributeOption> findByAttributeIdAndValueIgnoreCase(Long attributeId, String value);
}
