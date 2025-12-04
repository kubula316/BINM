import './Home.css'
import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'

const API_BASE_URL = 'http://localhost:8081'

function Home({ isLoggedIn }) {
  const [randomListings, setRandomListings] = useState([])

  useEffect(() => {
    const fetchRandom = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/public/listings/random?page=0&size=6`)
        if (!response.ok) return
        const data = await response.json()
        setRandomListings(Array.isArray(data.content) ? data.content : [])
      } catch {
        // cicho ignorujemy błąd na stronie głównej
      }
    }

    fetchRandom()
  }, [])

  return (
    <div className="home-page">
      <div className="home-inner">
        <div className="title-section">
          <h1>BINM</h1>
          <p>Było I Nie Ma</p>
        </div>
        {isLoggedIn && (
          <div className="home-actions">
            <Link to="/add-listing" className="home-card">
              <div className="home-card-title">Dodaj przedmiot</div>
              <div className="home-card-desc">Szybko dodaj nowe ogłoszenie</div>
            </Link>
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
                    <div className="home-random-title">{it.title}</div>
                    {it.seller && it.seller.name && (
                      <div className="home-random-seller">{it.seller.name}</div>
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
