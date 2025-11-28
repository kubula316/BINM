package com.BINM.listing.attribute;

import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class AttributeSeeder {
    private final CategoryRepository categoryRepository;
    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeOptionRepository optionRepository;

    @Bean
    @Order(1)
    ApplicationRunner seedAttributesRunner() {
        return args -> seed();
    }

    @Transactional
    void seed() {
        // --- Krok 1: Stwórz uniwersalny atrybut "Stan" ---
        // Przypisujemy go do jednej z głównych kategorii, aby był dziedziczony.
        Optional<Category> rootCategoryForCondition = findByPath("Elektronika");
        if (rootCategoryForCondition.isEmpty()) {
            System.err.println("AttributeSeeder: Podstawowe kategorie nie istnieją. Przerywam seedowanie atrybutów.");
            return;
        }
        Map<String, AttributeDefinition> commonDefs = ensureDefinitions(rootCategoryForCondition.get(), new Object[][]{
                {"condition", "Stan", AttributeType.ENUM, true, null, 1}
        });
        ensureOptions(commonDefs.get("condition"), new String[][]{
                {"new", "Nowy"}, {"used", "Używany"}, {"damaged", "Uszkodzony"}
        });

        // --- Krok 2: Przypisz atrybuty do każdej kategorii ---

        // --- Elektronika ---
        findByPath("Elektronika", "Telefony i akcesoria").ifPresent(this::addPhoneAttributes);
        findByPath("Elektronika", "Komputery").ifPresent(this::addComputerAttributes);
        findByPath("Elektronika", "RTV i AGD").ifPresent(this::addHomeApplianceAttributes);
        findByPath("Elektronika", "Gaming").ifPresent(this::addGamingAttributes);

        // --- Moda ---
        findByPath("Moda", "Odzież damska").ifPresent(this::addClothingAttributes);
        findByPath("Moda", "Odzież męska").ifPresent(this::addClothingAttributes);
        findByPath("Moda", "Obuwie").ifPresent(this::addFootwearAttributes);
        findByPath("Moda", "Biżuteria i zegarki").ifPresent(this::addJewelryAttributes);

        // --- Dom i ogród ---
        findByPath("Dom i ogród", "Meble").ifPresent(this::addFurnitureAttributes);
        findByPath("Dom i ogród", "Narzędzia").ifPresent(this::addToolAttributes);

        // --- Supermarket ---
        findByPath("Supermarket", "Artykuły spożywcze").ifPresent(this::addFoodAttributes);
        findByPath("Supermarket", "Zwierzęta").ifPresent(this::addPetAttributes);

        // --- Dziecko ---
        findByPath("Dziecko", "Zabawki").ifPresent(this::addToyAttributes);
        findByPath("Dziecko", "Ubranka").ifPresent(this::addClothingAttributes); // Reużycie

        // --- Kultura i rozrywka ---
        findByPath("Kultura i rozrywka", "Książki").ifPresent(this::addBookAttributes);
        findByPath("Kultura i rozrywka", "Instrumenty", "Gitary").ifPresent(this::addGuitarAttributes);
        findByPath("Kultura i rozrywka", "Instrumenty", "Klawisze").ifPresent(this::addKeyboardInstrumentAttributes);

        // --- Sport i turystyka ---
        findByPath("Sport i turystyka", "Rowery").ifPresent(this::addBikeAttributes);

        // --- Motoryzacja ---
        findByPath("Motoryzacja", "Ogłoszenia motoryzacyjne").ifPresent(this::addCarAdAttributes);
        findByPath("Motoryzacja", "Części samochodowe").ifPresent(this::addCarPartAttributes);

        // --- Nieruchomości ---
        findByPath("Nieruchomości", "Mieszkania").ifPresent(this::addApartmentAttributes);
        findByPath("Nieruchomości", "Domy").ifPresent(this::addHouseAttributes);
    }

    // --- Metody pomocnicze dla poszczególnych grup kategorii ---

    private void addPhoneAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, true, null, 10},
                {"model", "Model", AttributeType.STRING, false, null, 20},
                {"memory", "Pamięć wbudowana", AttributeType.NUMBER, false, "GB", 30}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"apple", "Apple"}, {"samsung", "Samsung"}, {"xiaomi", "Xiaomi"}});
    }

    private void addComputerAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"type", "Typ komputera", AttributeType.ENUM, true, null, 5},
                {"brand", "Marka", AttributeType.ENUM, true, null, 10},
                {"processor", "Procesor", AttributeType.STRING, false, null, 20},
                {"ram", "Pamięć RAM", AttributeType.NUMBER, true, "GB", 30}
        });
        ensureOptions(defs.get("type"), new String[][]{{"laptop", "Laptop"}, {"desktop", "Stacjonarny"}});
        ensureOptions(defs.get("brand"), new String[][]{{"dell", "Dell"}, {"hp", "HP"}, {"lenovo", "Lenovo"}, {"apple", "Apple"}});
    }

    private void addHomeApplianceAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"type", "Rodzaj", AttributeType.ENUM, true, null, 10},
                {"brand", "Marka", AttributeType.ENUM, false, null, 20}
        });
        ensureOptions(defs.get("type"), new String[][]{{"fridge", "Lodówka"}, {"washing_machine", "Pralka"}, {"tv", "Telewizor"}});
        ensureOptions(defs.get("brand"), new String[][]{{"samsung", "Samsung"}, {"lg", "LG"}, {"bosch", "Bosch"}});
    }

    private void addGamingAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"platform", "Platforma", AttributeType.ENUM, true, null, 10}
        });
        ensureOptions(defs.get("platform"), new String[][]{{"pc", "PC"}, {"playstation", "PlayStation"}, {"xbox", "Xbox"}, {"nintendo", "Nintendo"}});
    }

    private void addClothingAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                {"size", "Rozmiar", AttributeType.STRING, true, null, 20},
                {"color", "Kolor", AttributeType.STRING, false, null, 30}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"zara", "Zara"}, {"h&m", "H&M"}, {"reserved", "Reserved"}});
    }

    private void addFootwearAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                {"size", "Rozmiar", AttributeType.STRING, true, null, 20}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"nike", "Nike"}, {"adidas", "Adidas"}, {"new-balance", "New Balance"}});
    }

    private void addJewelryAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"material", "Materiał", AttributeType.ENUM, true, null, 10}
        });
        ensureOptions(defs.get("material"), new String[][]{{"gold", "Złoto"}, {"silver", "Srebro"}, {"platinum", "Platyna"}});
    }

    private void addFurnitureAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"material", "Materiał", AttributeType.STRING, false, null, 10},
                {"color", "Kolor", AttributeType.STRING, false, null, 20}
        });
    }

    private void addToolAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"power_source", "Zasilanie", AttributeType.ENUM, true, null, 10}
        });
        ensureOptions(defs.get("power_source"), new String[][]{{"cordless", "Akumulatorowe"}, {"corded", "Sieciowe"}, {"manual", "Ręczne"}});
    }

    private void addFoodAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"expiry_date", "Data ważności", AttributeType.STRING, false, null, 10},
                {"weight", "Waga", AttributeType.NUMBER, false, "g", 20}
        });
    }

    private void addPetAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"animal_type", "Dla zwierzęcia", AttributeType.ENUM, true, null, 10}
        });
        ensureOptions(defs.get("animal_type"), new String[][]{{"dog", "Pies"}, {"cat", "Kot"}, {"rodent", "Gryzoń"}});
    }

    private void addToyAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"age_range", "Wiek", AttributeType.STRING, false, null, 10}
        });
    }

    private void addBookAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"author", "Autor", AttributeType.STRING, true, null, 10},
                {"cover_type", "Okładka", AttributeType.ENUM, false, null, 20}
        });
        ensureOptions(ensureDefinitions(cat, new Object[][]{}).get("cover_type"), new String[][]{{"hard", "Twarda"}, {"soft", "Miękka"}});
    }

    private void addGuitarAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                {"strings_count", "Liczba strun", AttributeType.NUMBER, false, null, 20}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"yamaha", "Yamaha"}, {"fender", "Fender"}, {"gibson", "Gibson"}});
    }

    private void addKeyboardInstrumentAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                {"keys_count", "Liczba klawiszy", AttributeType.NUMBER, true, null, 20}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"yamaha", "Yamaha"}, {"roland", "Roland"}, {"casio", "Casio"}});
    }

    private void addBikeAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                {"frame_size", "Rozmiar ramy", AttributeType.STRING, true, null, 20}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"kross", "Kross"}, {"trek", "Trek"}, {"giant", "Giant"}});
    }

    private void addCarAdAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"brand", "Marka", AttributeType.ENUM, true, null, 10},
                {"model", "Model", AttributeType.STRING, true, null, 20},
                {"year", "Rok produkcji", AttributeType.NUMBER, true, null, 30},
                {"mileage", "Przebieg", AttributeType.NUMBER, false, "km", 40},
                {"fuel", "Paliwo", AttributeType.ENUM, true, null, 50}
        });
        ensureOptions(defs.get("brand"), new String[][]{{"audi", "Audi"}, {"bmw", "BMW"}, {"mercedes-benz", "Mercedes-Benz"}});
        ensureOptions(defs.get("fuel"), new String[][]{{"diesel", "Diesel"}, {"petrol", "Benzyna"}, {"hybrid", "Hybryda"}});
    }

    private void addCarPartAttributes(Category cat) {
        Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                {"part_type", "Rodzaj części", AttributeType.ENUM, true, null, 10},
                {"fits_brand", "Pasuje do marki", AttributeType.ENUM, false, null, 20}
        });
        ensureOptions(defs.get("part_type"), new String[][]{{"engine", "Silnik"}, {"brakes", "Hamulce"}, {"suspension", "Zawieszenie"}});
        ensureOptions(defs.get("fits_brand"), new String[][]{{"audi", "Audi"}, {"bmw", "BMW"}, {"volkswagen", "Volkswagen"}});
    }

    private void addApartmentAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"area", "Powierzchnia", AttributeType.NUMBER, true, "m²", 10},
                {"rooms", "Liczba pokoi", AttributeType.NUMBER, true, null, 20},
                {"floor", "Piętro", AttributeType.NUMBER, false, null, 30}
        });
    }

    private void addHouseAttributes(Category cat) {
        ensureDefinitions(cat, new Object[][]{
                {"area", "Powierzchnia domu", AttributeType.NUMBER, true, "m²", 10},
                {"plot_area", "Powierzchnia działki", AttributeType.NUMBER, true, "m²", 20},
                {"rooms", "Liczba pokoi", AttributeType.NUMBER, true, null, 30}
        });
    }


    // --- Metody generyczne (bez zmian) ---

    private Optional<Category> findByPath(String... names) {
        if (names == null || names.length == 0) return Optional.empty();
        Optional<Category> current = categoryRepository.findByNameIgnoreCaseAndParentIsNull(names[0]);
        for (int i = 1; i < names.length && current.isPresent(); i++) {
            Long pid = current.get().getId();
            current = categoryRepository.findByNameIgnoreCaseAndParentId(names[i], pid);
        }
        return current;
    }

    private Map<String, AttributeDefinition> ensureDefinitions(Category category, Object[][] defs) {
        Map<String, AttributeDefinition> existing = new HashMap<>();
        for (AttributeDefinition d : definitionRepository.findByCategoryIdOrderBySortOrderAscIdAsc(category.getId())) {
            existing.put(d.getKey().toLowerCase(Locale.ROOT), d);
        }
        Map<String, AttributeDefinition> created = new LinkedHashMap<>();
        for (Object[] row : defs) {
            String key = (String) row[0];
            String label = (String) row[1];
            AttributeType type = (AttributeType) row[2];
            Boolean required = (Boolean) row[3];
            String unit = (String) row[4];
            Integer sortOrder = (Integer) row[5];
            AttributeDefinition def = existing.get(key.toLowerCase(Locale.ROOT));
            if (def == null) {
                def = AttributeDefinition.builder()
                        .category(category)
                        .key(key)
                        .label(label)
                        .type(type)
                        .required(required)
                        .unit(unit)
                        .sortOrder(sortOrder)
                        .active(true)
                        .build();
                def = definitionRepository.save(def);
            }
            created.put(key.toLowerCase(Locale.ROOT), def);
        }
        return created;
    }

    private void ensureOptions(AttributeDefinition def, String[][] options) {
        if (def == null || options == null) return;
        Map<String, AttributeOption> existing = new HashMap<>();
        for (AttributeOption o : optionRepository.findByAttributeIdOrderBySortOrderAscIdAsc(def.getId())) {
            existing.put(o.getValue().toLowerCase(Locale.ROOT), o);
        }
        int order = existing.size();
        for (String[] row : options) {
            String value = row[0];
            String label = row[1];
            if (!existing.containsKey(value.toLowerCase(Locale.ROOT))) {
                AttributeOption opt = AttributeOption.builder()
                        .attribute(def)
                        .value(value)
                        .label(label)
                        .sortOrder(order++)
                        .build();
                optionRepository.save(opt);
            }
        }
    }
}
