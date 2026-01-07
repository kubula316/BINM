import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

function ListingDetails() {
  const { publicId } = useParams()
  const navigate = useNavigate()
  const [listing, setListing] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [favoriteLoading, setFavoriteLoading] = useState(false)
  const [isFavorite, setIsFavorite] = useState(false)
  const [favoriteError, setFavoriteError] = useState('')
  const [contactLoading, setContactLoading] = useState(false)
  const [contactPhone, setContactPhone] = useState('')
  const [contactError, setContactError] = useState('')

  useEffect(() => {
    const fetchListing = async () => {
      try {
        setLoading(true)
        setError('')

        const response = await fetch(`${API_BASE_URL}/public/listings/get/${publicId}`)
        if (!response.ok) {
          setError('Nie udało się pobrać ogłoszenia')
          return
        }

        const data = await response.json()
        setListing(data)
      } catch {
        setError('Brak połączenia z serwerem')
      } finally {
        setLoading(false)
      }
    }

    fetchListing()
  }, [publicId])

  useEffect(() => {
    const fetchFavoriteStatus = async () => {
      try {
        setFavoriteError('')
        const res = await fetch(
          `${API_BASE_URL}/user/interactions/favorites/status?entityId=${encodeURIComponent(publicId)}&entityType=LISTING`,
          { credentials: 'include' },
        )

        if (!res.ok) {
          // dla niezalogowanych po prostu ukrywamy status (serduszko nadal pokażemy, ale klik zwróci komunikat)
          return
        }

        const data = await res.json().catch(() => null)
        setIsFavorite(Boolean(data && data.isFavorite))
      } catch {
        // cicho
      }
    }

    if (publicId) fetchFavoriteStatus()
  }, [publicId])

  if (loading) {
    return (
      <div className="categories-page">
        <div className="categories-container">
          <p style={{ color: '#fff' }}>Ładowanie ogłoszenia...</p>
        </div>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="categories-page">
        <div className="categories-container">
          <p style={{ color: '#ff6b6b' }}>{error || 'Ogłoszenie nie zostało znalezione'}</p>
          <Link to="/categories" className="item-image-link">Wróć do kategorii</Link>
        </div>
      </div>
    )
  }

  const mainImage = Array.isArray(listing.media) && listing.media.length > 0
    ? listing.media[0].url
    : null

  return (
    <div className="categories-page listing-details-page">
      <div className="categories-container">
        <h1>{listing.title}</h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>Szczegóły ogłoszenia</p>

        <section className="electronics-section">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
            <button
              type="button"
              className="item-image-link"
              onClick={() => navigate(-1)}
            >
              Wróć
            </button>

            <button
              type="button"
              className="item-image-link"
              disabled={contactLoading}
              onClick={async () => {
                setContactError('')

                if (contactPhone) return

                try {
                  setContactLoading(true)
                  const res = await fetch(`${API_BASE_URL}/user/listing/${publicId}/contact`, {
                    credentials: 'include',
                  })

                  if (!res.ok) {
                    if (res.status === 401) {
                      setContactError('Zaloguj się, aby zobaczyć numer telefonu.')
                    } else {
                      setContactError('Nie udało się pobrać numeru telefonu.')
                    }
                    return
                  }

                  const data = await res.json().catch(() => null)
                  const phone = data && data.phoneNumber ? String(data.phoneNumber) : ''
                  if (!phone) {
                    setContactError('Brak numeru telefonu dla tego ogłoszenia.')
                    return
                  }

                  setContactPhone(phone)
                } catch {
                  setContactError('Brak połączenia z serwerem')
                } finally {
                  setContactLoading(false)
                }
              }}
            >
              {contactLoading ? 'Ładowanie...' : contactPhone ? `Tel: ${contactPhone}` : 'Pokaż numer'}
            </button>

            <button
              type="button"
              className={`favorite-btn ${isFavorite ? 'active' : ''}`}
              disabled={favoriteLoading}
              onClick={async () => {
                setFavoriteError('')

                try {
                  setFavoriteLoading(true)
                  const body = { entityId: String(publicId), entityType: 'LISTING' }
                  const res = await fetch(`${API_BASE_URL}/user/interactions/favorites`, {
                    method: isFavorite ? 'DELETE' : 'POST',
                    headers: {
                      'Content-Type': 'application/json',
                    },
                    credentials: 'include',
                    body: JSON.stringify(body),
                  })

                  if (!res.ok) {
                    if (res.status === 401) {
                      setFavoriteError('Zaloguj się, aby dodać ogłoszenie do obserwowanych.')
                    } else {
                      setFavoriteError('Nie udało się zmienić statusu obserwowania.')
                    }
                    return
                  }

                  setIsFavorite((v) => !v)
                } catch {
                  setFavoriteError('Brak połączenia z serwerem')
                } finally {
                  setFavoriteLoading(false)
                }
              }}
              aria-label={isFavorite ? 'Usuń z obserwowanych' : 'Dodaj do obserwowanych'}
              title={isFavorite ? 'Usuń z obserwowanych' : 'Dodaj do obserwowanych'}
            >
              {favoriteLoading ? '...' : isFavorite ? '♥ Obserwowane' : '♡ Obserwuj'}
            </button>
          </div>

          {favoriteError && (
            <p style={{ color: '#ff6b6b', marginTop: 10 }}>{favoriteError}</p>
          )}

          {contactError && (
            <p style={{ color: '#ff6b6b', marginTop: 10 }}>{contactError}</p>
          )}

          <div className="item-card" style={{ marginTop: 16 }}>
            <div className="item-header">
              <div>
                <div className="item-name">{listing.title}</div>
                <div className="item-meta">
                  Dodano: {listing.createdAt ? new Date(listing.createdAt).toLocaleDateString('pl-PL') : '–'}
                </div>
                {listing.seller && (
                  <div className="item-seller">Sprzedawca: {listing.seller.name}</div>
                )}
                <div className="item-price" style={{ marginTop: 8 }}>
                  {listing.priceAmount?.toLocaleString('pl-PL', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}{' '}
                  {listing.currency || 'PLN'}{listing.negotiable ? ' (do negocjacji)' : ''}
                </div>
                <div className="item-location">
                  Lokalizacja: {listing.locationCity || 'Brak danych'}{listing.locationRegion ? `, ${listing.locationRegion}` : ''}
                </div>
              </div>
              {mainImage && (
                <div style={{ marginLeft: 16 }}>
                  <img
                    src={mainImage}
                    alt={listing.title}
                    style={{ maxWidth: 220, maxHeight: 220, objectFit: 'cover', borderRadius: 8 }}
                  />
                </div>
              )}
            </div>

            <div className="item-body">
              <p className="item-desc">{listing.description}</p>

              {Array.isArray(listing.attributes) && listing.attributes.length > 0 && (
                <div className="item-attributes">
                  <h3 style={{ color: '#fff', marginTop: 12, marginBottom: 6 }}>Parametry</h3>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    {listing.attributes.map((attr, index) => {
                      const label = attr.label || attr.key

                      let valueText = null
                      if (attr.type === 'ENUM') {
                        valueText = attr.enumLabel || attr.enumValue
                      } else if (attr.type === 'NUMBER') {
                        if (attr.numberValue !== null && attr.numberValue !== undefined) {
                          valueText = attr.numberValue
                        }
                      } else if (attr.type === 'BOOLEAN') {
                        if (attr.booleanValue === true) valueText = 'Tak'
                        else if (attr.booleanValue === false) valueText = 'Nie'
                      } else {
                        valueText = attr.stringValue
                      }

                      if (valueText === null || valueText === undefined || valueText === '') return null

                      return (
                        <li key={attr.key || index} style={{ color: '#ddd', fontSize: 14 }}>
                          <strong>{label}:</strong> {valueText}
                        </li>
                      )
                    })}
                  </ul>
                </div>
              )}
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}

export default ListingDetails
