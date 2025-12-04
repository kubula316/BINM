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
    <div className="categories-page">
      <div className="categories-container">
        <h1>{listing.title}</h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>Szczegóły ogłoszenia</p>

        <section className="electronics-section">
          <button
            type="button"
            className="item-image-link"
            onClick={() => navigate(-1)}
          >
            Wróć
          </button>

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
              </div>
              {mainImage && (
                <a className="item-image-link" href={mainImage} target="_blank" rel="noreferrer">
                  Zdjęcie
                </a>
              )}
            </div>

            <div className="item-body">
              <div className="item-price">
                {listing.priceAmount?.toLocaleString('pl-PL', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}{' '}
                {listing.currency || 'PLN'}{listing.negotiable ? ' (do negocjacji)' : ''}
              </div>
              <div className="item-location">
                Lokalizacja: {listing.locationCity || 'Brak danych'}{listing.locationRegion ? `, ${listing.locationRegion}` : ''}
              </div>
              <p className="item-desc">{listing.description}</p>

              {Array.isArray(listing.attributes) && listing.attributes.length > 0 && (
                <div className="item-attributes">
                  <h3 style={{ color: '#fff', marginTop: 12, marginBottom: 6 }}>Parametry</h3>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    {listing.attributes.map((attr) => {
                      const label = attr.label || attr.key
                      let valueText = attr.displayValue || attr.value

                      if (!valueText && attr.type === 'ENUM' && attr.optionLabel) {
                        valueText = attr.optionLabel
                      }

                      if (!valueText) return null

                      return (
                        <li key={attr.key} style={{ color: '#ddd', fontSize: 14 }}>
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
