import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function SellerProfile() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    const fetchAll = async () => {
      try {
        setLoading(true)
        setError('')

        const [profileRes, listingsRes] = await Promise.all([
          fetch(`${API_BASE_URL}/public/users/${encodeURIComponent(userId)}`),
          fetch(`${API_BASE_URL}/public/listings/user/${encodeURIComponent(userId)}?page=0&size=50`),
        ])

        if (!profileRes.ok) {
          setError('Nie udało się pobrać profilu użytkownika')
          return
        }
        if (!listingsRes.ok) {
          setError('Nie udało się pobrać ogłoszeń użytkownika')
          return
        }

        const p = await profileRes.json()
        const l = await listingsRes.json()

        if (cancelled) return
        setProfile(p)
        setItems(Array.isArray(l.content) ? l.content : [])
      } catch {
        if (!cancelled) setError('Brak połączenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    if (userId) fetchAll()
    return () => {
      cancelled = true
    }
  }, [userId])

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Profil sprzedawcy</h1>

        <section className="electronics-section">
          <Link to="/categories" className="item-image-link">Wróć</Link>

          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && profile && (
            <div className="item-card" style={{ marginTop: 16 }}>
              <div className="item-header">
                <div>
                  <div className="item-name">{profile.name || '—'}</div>
                  {profile.memberSince && (
                    <div className="item-meta">
                      Konto od: {new Date(profile.memberSince).toLocaleDateString('pl-PL')}
                    </div>
                  )}
                </div>
                {profile.profileImageUrl && (
                  <img
                    src={profile.profileImageUrl}
                    alt="avatar"
                    style={{ width: 84, height: 84, borderRadius: 12, objectFit: 'cover' }}
                  />
                )}
              </div>
            </div>
          )}

          {!loading && !error && (
            <>
              <h2 style={{ color: '#fff', marginTop: 20 }}>Ogłoszenia</h2>

              {items.length === 0 ? (
                <p style={{ color: '#fff' }}>Brak aktywnych ogłoszeń.</p>
              ) : (
                <div className="items-grid listings-grid">
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
            </>
          )}
        </section>
      </div>
    </div>
  )
}
