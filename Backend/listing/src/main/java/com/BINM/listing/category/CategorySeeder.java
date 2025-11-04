package com.BINM.listing.category;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CategorySeeder {

    private final CategoryRepository repository;

    @Bean
    ApplicationRunner seedCategoriesRunner() {
        return args -> seedIfEmpty();
    }

    @Transactional
    void seedIfEmpty() {
        if (repository.count() > 0) return;
        int rootOrder = 0;
        Category elektronika = create(null, "Elektronika", rootOrder++);
        Category moda = create(null, "Moda", rootOrder++);
        Category dom = create(null, "Dom i ogród", rootOrder++);
        Category supermarket = create(null, "Supermarket", rootOrder++);
        Category dziecko = create(null, "Dziecko", rootOrder++);
        Category uroda = create(null, "Uroda", rootOrder++);
        Category zdrowie = create(null, "Zdrowie", rootOrder++);
        Category kultura = create(null, "Kultura i rozrywka", rootOrder++);
        Category sport = create(null, "Sport i turystyka", rootOrder++);
        Category motoryzacja = create(null, "Motoryzacja", rootOrder++);
        Category nieruchomosci = create(null, "Nieruchomości", rootOrder++);
        Category kolekcje = create(null, "Kolekcje i sztuka", rootOrder++);
        Category firma = create(null, "Firma i usługi", rootOrder++);

        // Elektronika
        int o = 0;
        create(elektronika, "Telefony i akcesoria", o++);
        create(elektronika, "Komputery", o++);
        create(elektronika, "RTV i AGD", o++);
        create(elektronika, "Foto i kamery", o++);
        create(elektronika, "Gaming", o++);
        create(elektronika, "Smart home", o++);
        create(elektronika, "Audio", o++);
        create(elektronika, "Inne", o++);

        // Moda
        o = 0;
        create(moda, "Odzież damska", o++);
        create(moda, "Odzież męska", o++);
        create(moda, "Obuwie", o++);
        create(moda, "Torebki i akcesoria", o++);
        create(moda, "Biżuteria i zegarki", o++);
        create(moda, "Dziecięce", o++);
        create(moda, "Inne", o++);

        // Dom i ogród
        o = 0;
        create(dom, "Meble", o++);
        create(dom, "Wyposażenie wnętrz", o++);
        create(dom, "Oświetlenie", o++);
        create(dom, "Narzędzia", o++);
        create(dom, "Ogród", o++);
        create(dom, "Inne", o++);

        // Supermarket
        o = 0;
        create(supermarket, "Artykuły spożywcze", o++);
        create(supermarket, "Napoje", o++);
        create(supermarket, "Chemia domowa", o++);
        create(supermarket, "Zwierzęta", o++);
        create(supermarket, "Inne", o++);

        // Dziecko
        o = 0;
        create(dziecko, "Wózki i foteliki", o++);
        create(dziecko, "Zabawki", o++);
        create(dziecko, "Ubranka", o++);
        create(dziecko, "Karmienie i pielęgnacja", o++);
        create(dziecko, "Inne", o++);

        // Uroda
        o = 0;
        create(uroda, "On", o++);
        create(uroda, "Ona", o++);
        create(uroda, "Makijaż", o++);
        create(uroda, "Pielęgnacja", o++);
        create(uroda, "Inne", o++);

        // Zdrowie
        o = 0;
        create(zdrowie, "Suplementy", o++);
        create(zdrowie, "Leki i apteczka", o++);
        create(zdrowie, "Sprzęt medyczny", o++);
        create(zdrowie, "Inne", o++);

        // Kultura i rozrywka
        o = 0;
        create(kultura, "Książki", o++);
        create(kultura, "Muzyka", o++);
        create(kultura, "Filmy", o++);
        create(kultura, "Gry", o++);
        Category instrumenty = create(kultura, "Instrumenty", o++);
        int o2 = 0;
        create(instrumenty, "Gitary", o2++);
        create(instrumenty, "Klawisze", o2++);
        create(instrumenty, "Perkusja", o2++);
        create(instrumenty, "Dęte", o2++);
        create(kultura, "Inne", o++);

        // Sport i turystyka
        o = 0;
        create(sport, "Rowery", o++);
        create(sport, "Siłownia i fitness", o++);
        create(sport, "Sporty zimowe", o++);
        create(sport, "Turystyka i camping", o++);
        create(sport, "Inne", o++);

        // Motoryzacja
        o = 0;
        create(motoryzacja, "Części samochodowe", o++);
        create(motoryzacja, "Warsztat", o++);
        create(motoryzacja, "Chemia", o++);
        create(motoryzacja, "Ogłoszenia motoryzacyjne", o++);
        create(motoryzacja, "Inne", o++);

        // Nieruchomości
        o = 0;
        create(nieruchomosci, "Mieszkania", o++);
        create(nieruchomosci, "Domy", o++);
        create(nieruchomosci, "Działki", o++);
        create(nieruchomosci, "Biura i lokale", o++);
        create(nieruchomosci, "Inne", o++);

        // Kolekcje i sztuka
        o = 0;
        create(kolekcje, "Antyki", o++);
        create(kolekcje, "Rękodzieło", o++);
        create(kolekcje, "Sztuka współczesna", o++);
        create(kolekcje, "Numizmatyka", o++);
        create(kolekcje, "Inne", o++);

        // Firma i usługi
        o = 0;
        create(firma, "Usługi", o++);
        create(firma, "Wyposażenie biura", o++);
        create(firma, "Maszyny i narzędzia", o++);
        create(firma, "Materiały", o++);
        create(firma, "Inne", o++);
    }

    private Category create(Category parent, String name, int sortOrder) {
        Integer depth = parent == null ? 0 : parent.getDepth() + 1;
        Category c = Category.builder()
                .parent(parent)
                .name(name)
                .sortOrder(sortOrder)
                .depth(depth)
                .isLeaf(true)
                .build();
        Category saved = repository.save(c);
        if (parent != null && Boolean.TRUE.equals(parent.getIsLeaf())) {
            parent.setIsLeaf(false);
            repository.save(parent);
        }
        return saved;
    }
}
