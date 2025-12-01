import { useMemo } from 'react'
import { useParams, Link } from 'react-router-dom'
import './Categories.css'

const DATA = {
  elektronika: {
    name: 'Elektronika',
    subcategories: [
      {
        name: 'Komputery',
        items: [
          { id: 'komp-1', dateAdded: '2025-11-01', name: 'Laptop Dell XPS 13', price: 5499.0, currency: 'PLN', author: 'Jan Kowalski', location: 'Warszawa', imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop', description: 'Lekki ultrabook z procesorem i7, 16GB RAM i ekranem 13,4".' },
          { id: 'komp-2', dateAdded: '2025-11-03', name: 'Komputer stacjonarny do gier', price: 4299.99, currency: 'PLN', author: 'Anna Nowak', location: 'Kraków', imageUrl: '', description: 'Ryzen 5, RTX 4060, 16GB RAM, SSD 1TB. Idealny do gier.' },
          { id: 'komp-3', dateAdded: '2025-11-05', name: 'Monitor 27" 144Hz', price: 999.0, currency: 'PLN', author: 'Piotr Wiśniewski', location: 'Gdańsk', imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop', description: 'Szybki panel IPS 144Hz z FreeSync. Idealny do FPS.' }
        ]
      },
      {
        name: 'RTV i AGD',
        items: [
          { id: 'rtv-1', dateAdded: '2025-11-02', name: 'Telewizor 55" 4K', price: 2199.99, currency: 'PLN', author: 'Kasia Lis', location: 'Poznań', imageUrl: 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=800&auto=format&fit=crop', description: 'Matryca VA 120Hz, HDR10+, wbudowane aplikacje VOD.' },
          { id: 'rtv-2', dateAdded: '2025-11-04', name: 'Odkurzacz bezprzewodowy', price: 699.0, currency: 'PLN', author: 'Michał Baran', location: 'Łódź', imageUrl: '', description: 'Lekki, z zestawem końcówek. Do 40 min pracy.' },
          { id: 'rtv-3', dateAdded: '2025-11-06', name: 'Ekspres do kawy', price: 1299.0, currency: 'PLN', author: 'Ola Kaczmarek', location: 'Wrocław', imageUrl: 'https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?w=800&auto=format&fit=crop', description: 'Automatyczny z młynkiem, cappuccino jednym przyciskiem.' }
        ]
      },
      {
        name: 'Fotografia',
        items: [
          { id: 'foto-1', dateAdded: '2025-11-01', name: 'Aparat bezlusterkowy', price: 3499.0, currency: 'PLN', author: 'Tomasz Lewandowski', location: 'Szczecin', imageUrl: 'https://images.unsplash.com/photo-1519183071298-a2962be96f83?w=800&auto=format&fit=crop', description: 'Matryca APS-C, 4K30, obiektyw 15-45mm.' },
          { id: 'foto-2', dateAdded: '2025-11-03', name: 'Statyw fotograficzny', price: 199.99, currency: 'PLN', author: 'Ewa Jabłońska', location: 'Bydgoszcz', imageUrl: '', description: 'Aluminiowy, wys. do 160cm, głowica kulowa.' },
          { id: 'foto-3', dateAdded: '2025-11-06', name: 'Lampa LED do vlogów', price: 299.0, currency: 'PLN', author: 'Kamil Duda', location: 'Lublin', imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop', description: 'Regulacja temperatury barwowej, zasilanie USB-C.' }
        ]
      },
      {
        name: 'Audio',
        items: [
          { id: 'audio-1', dateAdded: '2025-11-02', name: 'Słuchawki bezprzewodowe', price: 599.0, currency: 'PLN', author: 'Marta Pawlak', location: 'Katowice', imageUrl: 'https://images.unsplash.com/photo-1518444213232-6f1c2f3cae0a?w=800&auto=format&fit=crop', description: 'ANC, Bluetooth 5.3, do 30h pracy.' },
          { id: 'audio-2', dateAdded: '2025-11-04', name: 'Głośnik przenośny', price: 349.99, currency: 'PLN', author: 'Adam Wójcik', location: 'Rzeszów', imageUrl: '', description: 'Wodoodporny IP67, basowy przetwornik pasywny.' },
          { id: 'audio-3', dateAdded: '2025-11-05', name: 'Soundbar 2.1', price: 899.0, currency: 'PLN', author: 'Natalia Król', location: 'Gdynia', imageUrl: 'https://images.unsplash.com/photo-1523441344349-8c5ae80d40c2?w=800&auto=format&fit=crop', description: 'Z subwooferem, HDMI ARC, tryb nocny.' }
        ]
      },
      {
        name: 'Smart home',
        items: [
          { id: 'sm-1', dateAdded: '2025-11-02', name: 'Inteligentna żarówka Wi‑Fi', price: 59.99, currency: 'PLN', author: 'Paweł Zieliński', location: 'Opole', imageUrl: '', description: 'Sterowanie z aplikacji, integracja z Asystentem Google.' },
          { id: 'sm-2', dateAdded: '2025-11-04', name: 'Kamera IP do domu', price: 229.0, currency: 'PLN', author: 'Joanna Bąk', location: 'Białystok', imageUrl: 'https://images.unsplash.com/photo-1582743158875-1b8b71bfa14f?w=800&auto=format&fit=crop', description: 'Detekcja ruchu, zapis w chmurze, tryb nocny.' },
          { id: 'sm-3', dateAdded: '2025-11-06', name: 'Inteligentne gniazdko', price: 79.0, currency: 'PLN', author: 'Krzysztof Borowski', location: 'Toruń', imageUrl: '', description: 'Pomiar zużycia energii, harmonogramy, zdalne sterowanie.' }
        ]
      },
      {
        name: 'Inne',
        items: [
          { id: 'inne-1', dateAdded: '2025-11-01', name: 'Czytnik e-booków', price: 499.0, currency: 'PLN', author: 'Agnieszka Sikora', location: 'Kielce', imageUrl: 'https://images.unsplash.com/photo-1513475382585-d06e58bcb0ea?w=800&auto=format&fit=crop', description: 'Ekran E‑Ink 6", podświetlenie, Wi‑Fi.' },
          { id: 'inne-2', dateAdded: '2025-11-03', name: 'Powerbank 20 000 mAh', price: 149.99, currency: 'PLN', author: 'Sebastian Mazur', location: 'Bielsko-Biała', imageUrl: '', description: 'Szybkie ładowanie PD 22,5W, 2x USB-A, 1x USB-C.' },
          { id: 'inne-3', dateAdded: '2025-11-05', name: 'Router Wi‑Fi 6', price: 399.0, currency: 'PLN', author: 'Dorota Piątek', location: 'Olsztyn', imageUrl: 'https://images.unsplash.com/photo-1586816879360-95459b9d8b00?w=800&auto=format&fit=crop', description: 'Dwupasmowy, OFDMA, MU‑MIMO, aplikacja mobilna.' }
        ]
      }
    ]
  }
}

export default function CategoryDetails() {
  const { slug } = useParams()
  const data = useMemo(() => DATA[slug], [slug])


  if (!data) {
    return (
      <div className="categories-page">
        <div className="categories-container">
          <h1>Brak danych dla tej kategorii</h1>
          <Link to="/categories" className="item-image-link">Wróć do kategorii</Link>
          <p style={{ color: '#fff' }}>Na razie przygotowaliśmy podstronę dla Elektroniki.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>{data.name}</h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>Podkategorie i przedmioty</p>

        <section className="electronics-section">
          <Link to="/categories" className="item-image-link">Wróć do kategorii</Link>
          {data.subcategories.map(sub => (
            <div key={sub.name} className="subcategory">
              <h3 className="subcategory-title">{sub.name}</h3>
              <div className="items-grid">
                {sub.items.map(it => (
                  <div key={it.id} className="item-card">
                    <div className="item-header">
                      <div>
                        <div className="item-name">{it.name}</div>
                        <div className="item-meta">Dodano: {it.dateAdded}</div>
                        <div className="item-seller">Sprzedawca: {it.author}</div>
                      </div>
                      {it.imageUrl && (
                        <a className="item-image-link" href={it.imageUrl} target="_blank" rel="noreferrer">Zdjęcie</a>
                      )}
                    </div>
                    <div className="item-body">
                      <div className="item-price">
                        {it.price.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {it.currency}
                      </div>
                      <div className="item-location">Lokalizacja: {it.location}</div>
                      <p className="item-desc">{it.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </section>

        
      </div>
    </div>
  )
}
