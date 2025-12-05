package com.BINM.listing.category;

import com.BINM.listing.category.service.CategoryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class CategoryCacheWarmup {
    private final CategoryFacade categoryService;

    @Bean
    @Order(3)
    ApplicationRunner warmupCategoryTreeCache() {
        return args -> categoryService.getAllTree();
    }
}
