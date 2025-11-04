package com.BINM.listing.attribute;

import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
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
        // Motoryzacja -> Ogłoszenia motoryzacyjne
        findByPath("Motoryzacja", "Ogłoszenia motoryzacyjne").ifPresent(cat -> {
            Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                    {"brand", "Marka", AttributeType.ENUM, true, null, 10},
                    {"model", "Model", AttributeType.STRING, true, null, 20},
                    {"year", "Rok produkcji", AttributeType.NUMBER, true, null, 30},
                    {"mileage", "Przebieg", AttributeType.NUMBER, false, "km", 40},
                    {"fuel", "Paliwo", AttributeType.ENUM, true, null, 50},
                    {"gearbox", "Skrzynia biegów", AttributeType.ENUM, false, null, 60},
                    {"body_type", "Nadwozie", AttributeType.ENUM, false, null, 70},
                    {"engine_capacity", "Pojemność silnika", AttributeType.NUMBER, false, "cm3", 80},
                    {"power", "Moc", AttributeType.NUMBER, false, "KM", 90},
                    {"vin", "VIN", AttributeType.STRING, false, null, 100}
            });
            ensureOptions(defs.get("brand"), new String[][]{
                    {"audi", "Audi"}, {"bmw", "BMW"}, {"mercedes", "Mercedes-Benz"}, {"volkswagen", "Volkswagen"}, {"toyota", "Toyota"}
            });
            ensureOptions(defs.get("fuel"), new String[][]{
                    {"diesel", "Diesel"}, {"petrol", "Benzyna"}, {"hybrid", "Hybryda"}, {"electric", "Elektryczny"}
            });
            ensureOptions(defs.get("gearbox"), new String[][]{
                    {"manual", "Manualna"}, {"automatic", "Automatyczna"}
            });
            ensureOptions(defs.get("body_type"), new String[][]{
                    {"sedan", "Sedan"}, {"hatchback", "Hatchback"}, {"combi", "Kombi"}, {"suv", "SUV"}, {"coupe", "Coupé"}
            });
        });

        // Elektronika -> Telefony i akcesoria
        findByPath("Elektronika", "Telefony i akcesoria").ifPresent(cat -> {
            Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                    {"brand", "Marka", AttributeType.ENUM, true, null, 10},
                    {"model", "Model", AttributeType.STRING, true, null, 20},
                    {"condition", "Stan", AttributeType.ENUM, true, null, 30},
                    {"memory", "Pamięć", AttributeType.NUMBER, false, "GB", 40}
            });
            ensureOptions(defs.get("brand"), new String[][]{
                    {"apple", "Apple"}, {"samsung", "Samsung"}, {"xiaomi", "Xiaomi"}, {"huawei", "Huawei"}
            });
            ensureOptions(defs.get("condition"), new String[][]{
                    {"new", "Nowy"}, {"used", "Używany"}, {"refurbished", "Odnowiony"}
            });
        });

        // Kultura i rozrywka -> Instrumenty -> Gitary
        findByPath("Kultura i rozrywka", "Instrumenty", "Gitary").ifPresent(cat -> {
            Map<String, AttributeDefinition> defs = ensureDefinitions(cat, new Object[][]{
                    {"brand", "Marka", AttributeType.ENUM, false, null, 10},
                    {"strings_count", "Liczba strun", AttributeType.NUMBER, false, null, 20},
                    {"acoustic", "Akustyczna", AttributeType.BOOLEAN, false, null, 30},
                    {"condition", "Stan", AttributeType.ENUM, false, null, 40}
            });
            ensureOptions(defs.get("brand"), new String[][]{
                    {"yamaha", "Yamaha"}, {"fender", "Fender"}, {"gibson", "Gibson"}, {"ibanez", "Ibanez"}
            });
            ensureOptions(defs.get("condition"), new String[][]{
                    {"new", "Nowy"}, {"used", "Używany"}
            });
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
