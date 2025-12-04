package com.BINM.listing.listing.model;

public enum ListingStatus {
    ACTIVE,     // Ogłoszenie jest aktywne i widoczne dla wszystkich.
    SOLD,       // Ogłoszenie zostało sprzedane.
    SUSPENDED,  // Ogłoszenie zostało zawieszone przez użytkownika lub administratora.
    DRAFT       // Wersja robocza, niewidoczna publicznie (na przyszłość).
}
