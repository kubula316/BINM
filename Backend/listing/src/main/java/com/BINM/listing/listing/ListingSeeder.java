package com.BINM.listing.listing;

import com.BINM.listing.attribute.AttributeDefinition;
import com.BINM.listing.attribute.AttributeOption;
import com.BINM.listing.attribute.AttributeOptionRepository;
import com.BINM.listing.attribute.AttributeService;
import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ListingSeeder {
    private final ListingRepository listingRepository;
    private final ListingAttributeRepository listingAttributeRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final AttributeService attributeService;
    private final AttributeOptionRepository optionRepository;
    private final CategoryRepository categoryRepository;

    private static final String DEMO_SELLER_ID = "69aee8bc-c451-4da2-823e-a5c23df7d39c";

    @Bean
    @Order(2)
    ApplicationRunner seedListingsRunner() {
        return args -> seedIfEmpty();
    }

    @Transactional
    void seedIfEmpty() {
        boolean fresh = listingRepository.count() == 0;
        // Najpierw przykłady kuratorowane dla wybranych ścieżek (tylko gdy brak danych)
        if (fresh) {
            // Kultura i rozrywka -> Instrumenty -> Gitary
            Optional<Category> gitary = findByPath("Kultura i rozrywka", "Instrumenty", "Gitary");
            gitary.ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Gitara klasyczna Yamaha C40",
                        "Stan bardzo dobry, komplet strun, pokrowiec w zestawie.",
                        new BigDecimal("499.99"),
                        "Kraków", "Małopolskie",
                        "used");
                addMedia(l, "https://example.com/images/gitary/yamaha-c40.jpg");
                attachAttributes(cat, l, Map.of(
                        "brand", "yamaha",
                        "strings_count", "6",
                        "acoustic", "true",
                        "condition", "used"
                ));
            });

            // Elektronika -> Telefony i akcesoria
            findByPath("Elektronika", "Telefony i akcesoria").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Apple iPhone 12 128GB czarny",
                        "Bateria 89%, bez blokady, komplet akcesoriów.",
                        new BigDecimal("1999.00"),
                        "Warszawa", "Mazowieckie",
                        "used");
                addMedia(l, "https://example.com/images/phones/iphone12.jpg");
                attachAttributes(cat, l, Map.of(
                        "brand", "apple",
                        "model", "iPhone 12",
                        "condition", "used",
                        "memory", "128"
                ));
            });

            // Elektronika -> Komputery
            findByPath("Elektronika", "Komputery").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Laptop Dell Inspiron 15",
                        "i5, 16GB RAM, 512GB SSD, stan bardzo dobry.",
                        new BigDecimal("2499.00"),
                        "Gdańsk", "Pomorskie",
                        "used");
                addMedia(l, "https://example.com/images/laptops/dell-inspiron15.jpg");
            });

            // Dom i ogród -> Meble
            findByPath("Dom i ogród", "Meble").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Sofa 3-osobowa szara",
                        "Nowoczesna, rozkładana, prawie nieużywana.",
                        new BigDecimal("1200.00"),
                        "Wrocław", "Dolnośląskie",
                        "used");
                addMedia(l, "https://example.com/images/furniture/sofa.jpg");
            });

            // Sport i turystyka -> Rowery
            findByPath("Sport i turystyka", "Rowery").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Rower górski MTB 29",
                        "Aluminiowa rama, hamulce tarczowe, 21 biegów.",
                        new BigDecimal("1599.00"),
                        "Poznań", "Wielkopolskie",
                        "used");
                addMedia(l, "https://example.com/images/bikes/mtb29.jpg");
            });

            // Motoryzacja -> Ogłoszenia motoryzacyjne
            findByPath("Motoryzacja", "Ogłoszenia motoryzacyjne").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Audi A4 B8 2.0 TDI 2009",
                        "Serwisowany, bezwypadkowy, przebieg 210k km.",
                        new BigDecimal("26999.00"),
                        "Łódź", "Łódzkie",
                        "used");
                addMedia(l, "https://example.com/images/cars/audi-a4-b8.jpg");
                attachAttributes(cat, l, Map.of(
                        "brand", "audi",
                        "model", "A4",
                        "year", "2009",
                        "mileage", "210000",
                        "fuel", "diesel",
                        "gearbox", "manual",
                        "body_type", "sedan",
                        "engine_capacity", "1968",
                        "power", "143",
                        "vin", "WAUZZZ8K09A000000"
                ));
            });

            // Nieruchomości -> Mieszkania
            findByPath("Nieruchomości", "Mieszkania").ifPresent(cat -> {
                Listing l = createListing(cat,
                        "Mieszkanie 2 pokoje 42m2",
                        "Parter, balkon, piwnica, blisko komunikacji.",
                        new BigDecimal("399000.00"),
                        "Katowice", "Śląskie",
                        "used");
                addMedia(l, "https://example.com/images/flats/2-pokoje.jpg");
            });
        }

        // Następnie uzupełnij brakujące: po jednym ogłoszeniu dla KAŻDEJ kategorii‑liścia, jeśli pusta
        categoryRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsLeaf()))
                .forEach(c -> {
                    if (listingRepository.countByCategoryId(c.getId()) == 0) {
                        Listing l = createListing(
                                c,
                                "Przykładowe ogłoszenie: " + c.getName(),
                                "Wpis testowy dla kategorii '" + c.getName() + "'.",
                                new BigDecimal("99.00"),
                                "Warszawa",
                                "Mazowieckie",
                                "used"
                        );
                        addMedia(l, "https://example.com/images/placeholder/" + c.getId() + ".jpg");
                        // brak atrybutów – fallback
                    }
                });
    }

    private Optional<Category> findByPath(String... names) {
        if (names == null || names.length == 0) return Optional.empty();
        Optional<Category> current = categoryRepository.findByNameIgnoreCaseAndParentIsNull(names[0]);
        for (int i = 1; i < names.length && current.isPresent(); i++) {
            Long pid = current.get().getId();
            current = categoryRepository.findByNameIgnoreCaseAndParentId(names[i], pid);
        }
        return current;
    }

    private Listing createListing(Category category,
                                  String title,
                                  String description,
                                  BigDecimal price,
                                  String city,
                                  String region,
                                  String condition) {
        if (!Boolean.TRUE.equals(category.getIsLeaf())) return null; // safety
        Listing l = Listing.builder()
                .sellerUserId(DEMO_SELLER_ID)
                .category(category)
                .title(title)
                .description(description)
                .priceAmount(price)
                .currency("PLN")
                .negotiable(false)
                .conditionLabel(condition)
                .locationCity(city)
                .locationRegion(region)
                .build();
        return listingRepository.save(l);
    }

    private void addMedia(Listing listing, String url) {
        if (listing == null || url == null || url.isBlank()) return;
        ListingMedia m = ListingMedia.builder()
                .listing(listing)
                .mediaUrl(url)
                .mediaType("image")
                .position(0)
                .build();
        listingMediaRepository.save(m);
    }

    private void attachAttributes(Category category, Listing listing, Map<String, String> attrs) {
        if (listing == null || attrs == null || attrs.isEmpty()) return;
        Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(category.getId());
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (key == null) continue;
            AttributeDefinition def = defs.get(key.toLowerCase(Locale.ROOT));
            if (def == null) continue;
            ListingAttribute la = new ListingAttribute();
            la.setListing(listing);
            la.setAttribute(def);
            switch (def.getType()) {
                case STRING -> la.setVText(value);
                case NUMBER -> {
                    try {
                        la.setVNumber(value != null ? new java.math.BigDecimal(value) : null);
                    } catch (NumberFormatException ignored) {
                    }
                }
                case BOOLEAN -> la.setVBoolean(value != null && ("true".equalsIgnoreCase(value) || "1".equals(value)));
                case ENUM -> {
                    if (value != null) {
                        Optional<AttributeOption> opt = optionRepository.findByAttributeIdAndValueIgnoreCase(def.getId(), value);
                        opt.ifPresent(la::setOption);
                    }
                }
            }
            listingAttributeRepository.save(la);
        }
    }
}
