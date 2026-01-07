import './Home.css'
import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'

const API_BASE_URL = 'http://localhost:8081'

function Home({ isLoggedIn }) {
  const [randomListings, setRandomListings] = useState([])
  const [categories, setCategories] = useState([])

  useEffect(() => {
    const fetchRandom = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/public/listings/random?page=0&size=12`)
        if (!response.ok) return
        const data = await response.json()
        setRandomListings(Array.isArray(data.content) ? data.content : [])
      } catch {
        // cicho ignorujemy błąd na stronie głównej
      }
    }

    fetchRandom()
  }, [])

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/public/category/all`)
        if (!res.ok) return
        const data = await res.json()
        setCategories(Array.isArray(data) ? data : [])
      } catch {
        // cicho ignorujemy błąd na stronie głównej
      }
    }

    fetchCategories()
  }, [])

  const suggestedCategories = Array.isArray(categories)
    ? categories.filter((c) => c && c.parentId === null).slice(0, 8)
    : []

  return (
    <div className="home-page">
      <div className="home-inner">
        <div className="title-section">
          <h1>BINM</h1>
          <p>Było I Nie Ma</p>
        </div>
        {isLoggedIn && (
          <div className="home-actions">
            <Link to="/add-listing" className="home-card home-card-primary">
              <div className="home-card-title">Dodaj przedmiot</div>
              <div className="home-card-desc">Szybko dodaj nowe ogłoszenie</div>
            </Link>
          </div>
        )}

        {suggestedCategories.length > 0 && (
          <div className="home-categories-section">
            <h2>Proponowane kategorie</h2>
            <div className="home-categories-grid">
              {suggestedCategories.map((cat) => (
                <Link
                  key={cat.id}
                  to={`/categories/${cat.id}`}
                  className="home-category"
                  aria-label={cat.name}
                  title={cat.name}
                >
                  {cat.imageUrl ? (
                    <img src={cat.imageUrl} alt={cat.name} className="home-category-icon" />
                  ) : (
                    <span className="home-category-fallback">{String(cat.name || '?').slice(0, 1)}</span>
                  )}
                </Link>
              ))}
            </div>
          </div>
        )}

        {randomListings.length > 0 && (
          <div className="home-random-section">
            <h2>Proponowane ogłoszenia</h2>
            <div className="home-random-grid">
              {randomListings.map((it) => (
                <Link
                  key={it.publicId}
                  to={`/listing/${it.publicId}`}
                  className="home-random-card"
                >
                  <div className="home-random-header">
                    <div>
                      <div className="home-random-title">{it.title}</div>
                      {it.seller && it.seller.name && (
                        <div className="home-random-seller">{it.seller.name}</div>
                      )}
                    </div>
                    {it.coverImageUrl && (
                      <img src={it.coverImageUrl} alt={it.title} className="home-random-thumb" />
                    )}
                  </div>
                  <div className="home-random-price">
                    {(() => {
                      if (it.priceAmount == null) return 'Brak ceny'
                      const raw = Number(it.priceAmount)
                      if (Number.isNaN(raw)) return 'Brak ceny'
                      return `${raw.toLocaleString('pl-PL', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })} PLN`
                    })()}
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default Home
