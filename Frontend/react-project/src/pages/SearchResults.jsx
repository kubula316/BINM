import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function SearchResults() {
  const [params] = useSearchParams()
  const qRaw = params.get('q') || ''
  const q = useMemo(() => qRaw.trim(), [qRaw])

  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    const run = async () => {
      if (!q) {
        setItems([])
        setError('')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError('')

        const res = await fetch(`${API_BASE_URL}/public/listings/search`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            query: q,
            categoryId: null,
            sellerUserId: null,
            attributes: [],
            sort: [],
            page: 0,
            size: 30,
          }),
        })

        if (!res.ok) {
          setError('Nie udało się wyszukać ogłoszeń')
          return
        }

        const data = await res.json().catch(() => null)
        const list = Array.isArray(data?.content) ? data.content : []
        if (!cancelled) setItems(list)
      } catch {
        if (!cancelled) setError('Brak połączenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    run()
    return () => {
      cancelled = true
    }
  }, [q])

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Wyszukiwanie</h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>
          {q ? `Fraza: "${q}"` : 'Wpisz frazę w wyszukiwarce u góry'}
        </p>

        <section className="electronics-section">
          <Link to="/" className="item-image-link">Wróć</Link>

          {loading && <p style={{ color: '#fff', marginTop: 12 }}>Szukam...</p>}
          {error && <p style={{ color: '#ff6b6b', marginTop: 12 }}>{error}</p>}

          {!loading && !error && q && items.length === 0 && (
            <p style={{ color: '#fff', marginTop: 12 }}>Brak wyników.</p>
          )}

          {!loading && !error && items.length > 0 && (
            <div className="items-grid listings-grid" style={{ marginTop: 16 }}>
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
                        {it.seller?.name && (
                          <div className="item-seller">Sprzedawca: {it.seller.name}</div>
                        )}
                        {it.locationCity && (
                          <div className="item-location">Lokalizacja: {it.locationCity}</div>
                        )}
                      </div>
                      {it.coverImageUrl && (
                        <img src={it.coverImageUrl} alt={it.title} className="listing-thumb" />
                      )}
                    </div>
                    <div className="item-body">
                      <div className="item-price">{priceLabel}</div>
                      {it.negotiable && <div className="item-meta">Cena do negocjacji</div>}
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
