import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function Favorites() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [removingId, setRemovingId] = useState(null)

  const fetchFavorites = async () => {
    try {
      setLoading(true)
      setError('')

      const res = await fetch(`${API_BASE_URL}/user/interactions/favorites?page=0&size=50`, {
        credentials: 'include',
      })

      if (!res.ok) {
        if (res.status === 401) {
          setError('Musisz być zalogowany, aby zobaczyć obserwowane ogłoszenia.')
        } else {
          setError('Nie udało się pobrać obserwowanych ogłoszeń.')
        }
        return
      }

      const data = await res.json()
      setItems(Array.isArray(data.content) ? data.content : [])
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchFavorites()
  }, [])

  const removeFavorite = async (publicId) => {
    try {
      setRemovingId(publicId)
      const res = await fetch(`${API_BASE_URL}/user/interactions/favorites`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ entityId: String(publicId), entityType: 'LISTING' }),
      })

      if (!res.ok) {
        alert('Nie udało się usunąć z obserwowanych.')
        return
      }

      setItems((prev) => prev.filter((x) => x.publicId !== publicId))
    } catch {
      alert('Brak połączenia z serwerem')
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Obserwowane</h1>

        <section className="electronics-section">
          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && items.length === 0 && (
            <p style={{ color: '#fff' }}>Nie obserwujesz jeszcze żadnych ogłoszeń.</p>
          )}

          {!loading && !error && items.length > 0 && (
            <div className="items-grid">
              {items.map((it) => {
                const priceLabel = (() => {
                  if (it.priceAmount == null) return 'Brak ceny'
                  const raw = Number(it.priceAmount)
                  if (Number.isNaN(raw)) return 'Brak ceny'
                  return `${raw.toLocaleString('pl-PL', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })} PLN`
                })()

                return (
                  <Link
                    key={it.publicId}
                    to={`/listing/${it.publicId}`}
                    className="item-card item-card-link"
                    style={{ textDecoration: 'none' }}
                  >
                    <div className="item-header">
                      <div>
                        <div className="item-name">{it.title}</div>
                        {it.seller && it.seller.name && (
                          <div className="item-seller">Sprzedawca: {it.seller.name}</div>
                        )}
                        {it.locationCity && (
                          <div className="item-location">Lokalizacja: {it.locationCity}</div>
                        )}
                      </div>
                      {it.coverImageUrl && (
                        <span className="item-image-link">Zdjęcie</span>
                      )}
                    </div>

                    <div className="item-body">
                      <div className="item-price">{priceLabel}</div>
                      {it.negotiable && (
                        <div className="item-meta">Cena do negocjacji</div>
                      )}
                    </div>

                    <div style={{ marginTop: 8, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <button
                        type="button"
                        className="filters-button clear"
                        onClick={(e) => {
                          e.preventDefault()
                          e.stopPropagation()
                          removeFavorite(it.publicId)
                        }}
                        disabled={removingId === it.publicId}
                      >
                        {removingId === it.publicId ? 'Usuwanie...' : 'Usuń z obserwowanych'}
                      </button>
                    </div>
                  </Link>
                )
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
