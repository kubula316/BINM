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
  const [activeImageIndex, setActiveImageIndex] = useState(0)

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
    setActiveImageIndex(0)
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

  const images = Array.isArray(listing.media)
    ? listing.media.map((m) => m && m.url).filter(Boolean)
    : []

  const mainImage = images.length > 0 ? images[Math.min(activeImageIndex, images.length - 1)] : null

  return (
    <div className="categories-page listing-details-page">
      <div className="categories-container">
        <h1>{listing.title}</h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>Szczegóły ogłoszenia</p>

        <section className="electronics-section">
          <div className="listing-details-actions">
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

          <div className="listing-details-top">
            <div className="listing-details-gallery item-card">
              {mainImage ? (
                <>
                  <div className="listing-details-galleryMain">
                    {images.length > 1 && (
                      <div className="listing-details-galleryNav">
                        <button
                          type="button"
                          className="filters-button clear"
                          onClick={() => setActiveImageIndex((i) => (i - 1 + images.length) % images.length)}
                          style={{ padding: '6px 10px' }}
                        >
                          ‹
                        </button>
                        <div style={{ color: '#ddd', fontSize: 12 }}>
                          {activeImageIndex + 1}/{images.length}
                        </div>
                        <button
                          type="button"
                          className="filters-button clear"
                          onClick={() => setActiveImageIndex((i) => (i + 1) % images.length)}
                          style={{ padding: '6px 10px' }}
                        >
                          ›
                        </button>
                      </div>
                    )}
                    <img
                      src={mainImage}
                      alt={listing.title}
                      className="listing-details-image"
                    />
                  </div>

                  {images.length > 1 && (
                    <div className="listing-details-thumbs">
                      {images.map((url, idx) => (
                        <button
                          key={`${url}-${idx}`}
                          type="button"
                          onClick={() => setActiveImageIndex(idx)}
                          className={`listing-details-thumbBtn ${idx === activeImageIndex ? 'active' : ''}`}
                          aria-label={`Pokaż zdjęcie ${idx + 1}`}
                          title={`Zdjęcie ${idx + 1}`}
                        >
                          <img
                            src={url}
                            alt={`miniatura-${idx + 1}`}
                            className="listing-details-thumbImg"
                          />
                        </button>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <p style={{ color: '#fff', margin: 0 }}>Brak zdjęć</p>
              )}
            </div>

            <div className="listing-details-info item-card">
              <div className="item-name">{listing.title}</div>
              <div className="item-meta">
                Dodano: {listing.createdAt ? new Date(listing.createdAt).toLocaleDateString('pl-PL') : '–'}
              </div>
              {listing.seller && (
                  <div className="item-seller">
                    Sprzedawca:{' '}
                    <Link className="seller-link" to={`/users/${listing.seller.id}`}>{listing.seller.name}</Link>
                  </div>
              )}

              {listing.seller?.id && (
                <Link
                  to={`/messages/new/${publicId}/${listing.seller.id}`}
                  className="filters-button apply"
                  style={{ width: 'fit-content', marginTop: 10, textDecoration: 'none' }}
                >
                  Napisz do sprzedawcy
                </Link>
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
          </div>

          <div className="listing-details-bottom">
            <div className="item-card">
              <div className="listing-details-sectionTitle">Opis</div>
              <p className="item-desc" style={{ marginTop: 10 }}>
                {listing.description || 'Brak opisu'}
              </p>
            </div>

            {Array.isArray(listing.attributes) && listing.attributes.length > 0 && (
              <div className="item-card" style={{ marginTop: 16 }}>
                <div className="listing-details-sectionTitle">Parametry</div>
                <ul style={{ listStyle: 'none', padding: 0, margin: '10px 0 0 0' }}>
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
                      <li key={attr.key || index} style={{ color: '#ddd', fontSize: 14, marginBottom: 4 }}>
                        <strong>{label}:</strong> {valueText}
                      </li>
                    )
                  })}
                </ul>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}

export default ListingDetails
