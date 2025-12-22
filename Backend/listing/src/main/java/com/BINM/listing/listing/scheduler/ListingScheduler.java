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

    // Uruchamia się co godzinę (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    public void expireListings() {
        log.info("Running scheduled task: expireListings");
        // Uwaga: Musimy dodać metodę expireOverdueListings do fasady, 
        // albo wstrzyknąć tutaj ListingService bezpośrednio (jeśli jest w tym samym module).
        // Ponieważ ListingScheduler jest w tym samym module co ListingService, 
        // ale w innym pakiecie, a metoda w serwisie jest publiczna, 
        // to najczystszym rozwiązaniem jest dodanie jej do fasady, 
        // ale tylko do użytku wewnętrznego (lub stworzenie osobnego interfejsu ListingInternalFacade).
        
        // Na potrzeby tego zadania, dodam metodę do ListingFacade, 
        // chociaż nie będzie ona wystawiona w kontrolerze.
        listingFacade.expireOverdueListings();
    }
}
