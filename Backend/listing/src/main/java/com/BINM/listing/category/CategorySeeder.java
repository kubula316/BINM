package com.BINM.listing.category;

import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class CategorySeeder {

    private final CategoryRepository repository;

    @Bean
    @Order(0)
    ApplicationRunner seedCategoriesRunner() {
        return args -> seedIfEmpty();
    }

    @Transactional
    void seedIfEmpty() {
        if (repository.count() > 0) return;
        int rootOrder = 0;
        Category elektronika = create(null, "Elektronika", rootOrder++, "https://i.imgur.com/0Jk1z8B.png");
        Category moda = create(null, "Moda", rootOrder++, "https://i.imgur.com/7jZ5K5A.png");
        Category dom = create(null, "Dom i ogród", rootOrder++, "https://i.imgur.com/6Zj4K5A.png");
        Category supermarket = create(null, "Supermarket", rootOrder++, "https://i.imgur.com/5Zj4K5A.png");
        Category dziecko = create(null, "Dziecko", rootOrder++, "https://i.imgur.com/4Zj4K5A.png");
        Category uroda = create(null, "Uroda", rootOrder++, "https://i.imgur.com/3Zj4K5A.png");
        Category zdrowie = create(null, "Zdrowie", rootOrder++, "https://i.imgur.com/2Zj4K5A.png");
        Category kultura = create(null, "Kultura i rozrywka", rootOrder++, "https://i.imgur.com/1Zj4K5A.png");
        Category sport = create(null, "Sport i turystyka", rootOrder++, "https://i.imgur.com/0Zj4K5A.png");
        Category motoryzacja = create(null, "Motoryzacja", rootOrder++, "https://i.imgur.com/9Yj4K5A.png");
        Category nieruchomosci = create(null, "Nieruchomości", rootOrder++, "https://i.imgur.com/8Yj4K5A.png");
        Category kolekcje = create(null, "Kolekcje i sztuka", rootOrder++, "https://i.imgur.com/7Yj4K5A.png");
        Category firma = create(null, "Firma i usługi", rootOrder++, "https://i.imgur.com/6Yj4K5A.png");

        // Elektronika
        int o = 0;
        create(elektronika, "Telefony i akcesoria", o++, null);
        create(elektronika, "Komputery", o++, null);
        create(elektronika, "RTV i AGD", o++, null);
        create(elektronika, "Foto i kamery", o++, null);
        create(elektronika, "Gaming", o++, null);
        create(elektronika, "Smart home", o++, null);
        create(elektronika, "Audio", o++, null);
        create(elektronika, "Inne", o++, null);

        // Moda
        o = 0;
        create(moda, "Odzież damska", o++, null);
        create(moda, "Odzież męska", o++, null);
        create(moda, "Obuwie", o++, null);
        create(moda, "Torebki i akcesoria", o++, null);
        create(moda, "Biżuteria i zegarki", o++, null);
        create(moda, "Dziecięce", o++, null);
        create(moda, "Inne", o++, null);

        // Dom i ogród
        o = 0;
        create(dom, "Meble", o++, null);
        create(dom, "Wyposażenie wnętrz", o++, null);
        create(dom, "Oświetlenie", o++, null);
        create(dom, "Narzędzia", o++, null);
        create(dom, "Ogród", o++, null);
        create(dom, "Inne", o++, null);

        // Supermarket
        o = 0;
        create(supermarket, "Artykuły spożywcze", o++, null);
        create(supermarket, "Napoje", o++, null);
        create(supermarket, "Chemia domowa", o++, null);
        create(supermarket, "Zwierzęta", o++, null);
        create(supermarket, "Inne", o++, null);

        // Dziecko
        o = 0;
        create(dziecko, "Wózki i foteliki", o++, null);
        create(dziecko, "Zabawki", o++, null);
        create(dziecko, "Ubranka", o++, null);
        create(dziecko, "Karmienie i pielęgnacja", o++, null);
        create(dziecko, "Inne", o++, null);

        // Uroda
        o = 0;
        create(uroda, "On", o++, null);
        create(uroda, "Ona", o++, null);
        create(uroda, "Makijaż", o++, null);
        create(uroda, "Pielęgnacja", o++, null);
        create(uroda, "Inne", o++, null);

        // Zdrowie
        o = 0;
        create(zdrowie, "Suplementy", o++, null);
        create(zdrowie, "Leki i apteczka", o++, null);
        create(zdrowie, "Sprzęt medyczny", o++, null);
        create(zdrowie, "Inne", o++, null);

        // Kultura i rozrywka
        o = 0;
        create(kultura, "Książki", o++, null);
        create(kultura, "Muzyka", o++, null);
        create(kultura, "Filmy", o++, null);
        create(kultura, "Gry", o++, null);
        Category instrumenty = create(kultura, "Instrumenty", o++, null);
        int o2 = 0;
        create(instrumenty, "Gitary", o2++, null);
        create(instrumenty, "Klawisze", o2++, null);
        create(instrumenty, "Perkusja", o2++, null);
        create(instrumenty, "Dęte", o2++, null);
        create(kultura, "Inne", o++, null);

        // Sport i turystyka
        o = 0;
        create(sport, "Rowery", o++, null);
        create(sport, "Siłownia i fitness", o++, null);
        create(sport, "Sporty zimowe", o++, null);
        create(sport, "Turystyka i camping", o++, null);
        create(sport, "Inne", o++, null);

        // Motoryzacja
        o = 0;
        create(motoryzacja, "Części samochodowe", o++, null);
        create(motoryzacja, "Warsztat", o++, null);
        create(motoryzacja, "Chemia", o++, null);
        create(motoryzacja, "Ogłoszenia motoryzacyjne", o++, null);
        create(motoryzacja, "Inne", o++, null);

        // Nieruchomości
        o = 0;
        create(nieruchomosci, "Mieszkania", o++, null);
        create(nieruchomosci, "Domy", o++, null);
        create(nieruchomosci, "Działki", o++, null);
        create(nieruchomosci, "Biura i lokale", o++, null);
        create(nieruchomosci, "Inne", o++, null);

        // Kolekcje i sztuka
        o = 0;
        create(kolekcje, "Antyki", o++, null);
        create(kolekcje, "Rękodzieło", o++, null);
        create(kolekcje, "Sztuka współczesna", o++, null);
        create(kolekcje, "Numizmatyka", o++, null);
        create(kolekcje, "Inne", o++, null);

        // Firma i usługi
        o = 0;
        create(firma, "Usługi", o++, null);
        create(firma, "Wyposażenie biura", o++, null);
        create(firma, "Maszyny i narzędzia", o++, null);
        create(firma, "Materiały", o++, null);
        create(firma, "Inne", o++, null);
    }

    private Category create(Category parent, String name, int sortOrder, String imageUrl) {
        Integer depth = parent == null ? 0 : parent.getDepth() + 1;
        Category c = Category.builder()
                .parent(parent)
                .name(name)
                .sortOrder(sortOrder)
                .imageUrl(imageUrl)
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
