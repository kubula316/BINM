package com.BINM.listing.listing.scheduler;

import com.BINM.listing.listing.service.ListingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ListingScheduler {

    private final ListingFacade listingFacade;

    // Co godzinę (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    public void expireListings() {
        log.info("Running scheduled task: expireListings");

        listingFacade.expireOverdueListings();
    }
}
