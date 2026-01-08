import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'

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
  const [meId, setMeId] = useState(null)

  useEffect(() => {
    const fetchListing = async () => {
      try {
        setLoading(true)
        setError('')
        const response = await fetch(`${API_BASE_URL}/public/listings/get/${publicId}`)
        if (!response.ok) { setError('Nie udalo sie pobrac ogloszenia'); return }
        const data = await response.json()
        setListing(data)
      } catch {
        setError('Brak polaczenia z serwerem')
      } finally {
        setLoading(false)
      }
    }
    fetchListing()
  }, [publicId])

  useEffect(() => { setActiveImageIndex(0) }, [publicId])

  useEffect(() => {
    const fetchUserAndFavorite = async () => {
      try {
        const [profileRes, favRes] = await Promise.all([
          fetch(`${API_BASE_URL}/user/profile`, { credentials: 'include' }),
          fetch(`${API_BASE_URL}/user/interactions/favorites/status?entityId=${encodeURIComponent(publicId)}&entityType=LISTING`, { credentials: 'include' }),
        ])
        if (profileRes.ok) {
          const profile = await profileRes.json().catch(() => null)
          setMeId(profile?.userId || null)
        }
        if (favRes.ok) {
          const data = await favRes.json().catch(() => null)
          setIsFavorite(Boolean(data?.isFavorite))
        }
      } catch { /* cicho */ }
    }
    if (publicId) fetchUserAndFavorite()
  }, [publicId])

  const images = listing && Array.isArray(listing.media) ? listing.media.map((m) => m && m.url).filter(Boolean) : []
  const mainImage = images.length > 0 ? images[Math.min(activeImageIndex, images.length - 1)] : null

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
        <div className="ui-container"><p className="ui-muted">Ladowanie ogloszenia...</p></div>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
        <div className="ui-container">
          <p className="text-red-400">{error || 'Ogloszenie nie zostalo znalezione'}</p>
          <Link to="/categories" className="ui-btn mt-2 inline-block">Wroc do kategorii</Link>
        </div>
      </div>
    )
  }

  const handleFavorite = async () => {
    setFavoriteError('')
    try {
      setFavoriteLoading(true)
      const body = { entityId: String(publicId), entityType: 'LISTING' }
      const res = await fetch(`${API_BASE_URL}/user/interactions/favorites`, {
        method: isFavorite ? 'DELETE' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body),
      })
      if (!res.ok) { setFavoriteError(res.status === 401 ? 'Zaloguj sie.' : 'Blad.'); return }
      setIsFavorite((v) => !v)
    } catch {
      setFavoriteError('Brak polaczenia')
    } finally {
      setFavoriteLoading(false)
    }
  }

  const handleContact = async () => {
    if (contactPhone) return
    setContactError('')
    try {
      setContactLoading(true)
      const res = await fetch(`${API_BASE_URL}/user/listing/${publicId}/contact`, { credentials: 'include' })
      if (!res.ok) { setContactError(res.status === 401 ? 'Zaloguj sie.' : 'Blad.'); return }
      const data = await res.json().catch(() => null)
      setContactPhone(data?.phoneNumber || '')
      if (!data?.phoneNumber) setContactError('Brak numeru.')
    } catch {
      setContactError('Brak polaczenia')
    } finally {
      setContactLoading(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <div className="flex items-center justify-between gap-3">
          <h1 className="ui-h1 break-words">{listing.title}</h1>
          <button type="button" onClick={() => navigate(-1)} className="ui-btn flex-none">Wroc</button>
        </div>

        {listing.status && listing.status !== 'ACTIVE' && (
          <div className="rounded-lg bg-yellow-500/20 border border-yellow-500/30 px-4 py-2 text-sm text-yellow-300">
            To ogloszenie oczekuje na akceptacje.
          </div>
        )}

        <div className="grid gap-4 lg:grid-cols-2">
          <div className="ui-section">
            {mainImage ? (
              <div className="space-y-3">
                <div className="relative">
                  <img src={mainImage} alt={listing.title} className="w-full rounded-lg object-contain max-h-80" />
                  {images.length > 1 && (
                    <div className="absolute inset-x-0 bottom-2 flex items-center justify-center gap-2">
                      <button type="button" className="h-8 w-8 rounded-full bg-zinc-800/80 text-zinc-200" onClick={() => setActiveImageIndex((i) => (i - 1 + images.length) % images.length)}>‹</button>
                      <span className="text-xs text-zinc-300">{activeImageIndex + 1}/{images.length}</span>
                      <button type="button" className="h-8 w-8 rounded-full bg-zinc-800/80 text-zinc-200" onClick={() => setActiveImageIndex((i) => (i + 1) % images.length)}>›</button>
                    </div>
                  )}
                </div>
                {images.length > 1 && (
                  <div className="flex justify-center gap-2 overflow-x-auto">
                    {images.map((url, idx) => (
                      <button key={idx} type="button" onClick={() => setActiveImageIndex(idx)} className={`h-14 w-14 flex-none rounded-lg border-2 overflow-hidden ${idx === activeImageIndex ? 'border-emerald-500' : 'border-zinc-700'}`}>
                        <img src={url} alt="" className="h-full w-full object-cover" />
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <p className="ui-muted">Brak zdjec</p>
            )}
          </div>

          <div className="ui-section space-y-3">
            <div className="text-2xl font-bold text-emerald-400">
              {listing.priceAmount?.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {listing.currency || 'PLN'}
              {listing.negotiable && <span className="ml-2 text-sm text-zinc-400">(do negocjacji)</span>}
            </div>
            <div className="text-sm text-zinc-400">Dodano: {listing.createdAt ? new Date(listing.createdAt).toLocaleDateString('pl-PL') : '-'}</div>
            {listing.seller && (
              <div className="text-sm text-zinc-400">Sprzedawca: <Link to={`/users/${listing.seller.id}`} className="text-emerald-400 hover:underline">{listing.seller.name}</Link></div>
            )}
            <div className="text-sm text-zinc-400">Lokalizacja: {listing.locationCity || 'Brak'}{listing.locationRegion ? `, ${listing.locationRegion}` : ''}</div>
            <div className="flex flex-wrap gap-2 pt-2">
              <button type="button" onClick={handleContact} disabled={contactLoading} className="ui-btn">
                {contactLoading ? '...' : contactPhone ? `Tel: ${contactPhone}` : 'Pokaz numer'}
              </button>
              <button type="button" onClick={handleFavorite} disabled={favoriteLoading} className={`ui-btn ${isFavorite ? 'border-emerald-500 text-emerald-400' : ''}`}>
                {favoriteLoading ? '...' : isFavorite ? '♥ Obserwowane' : '♡ Obserwuj'}
              </button>
              {listing.seller?.id && meId !== listing.seller.id && (
                <Link to={`/messages/new/${publicId}/${listing.seller.id}`} className="ui-btn-primary">Napisz</Link>
              )}
            </div>
            {(favoriteError || contactError) && <p className="text-sm text-red-400">{favoriteError || contactError}</p>}
          </div>
        </div>

        <div className="ui-section overflow-hidden">
          <h2 className="ui-h2">Opis</h2>
          <p className="mt-2 text-zinc-300 whitespace-pre-wrap break-words overflow-wrap-anywhere">{listing.description || 'Brak opisu'}</p>
        </div>

        {Array.isArray(listing.attributes) && listing.attributes.length > 0 && (
          <div className="ui-section">
            <h2 className="ui-h2">Parametry</h2>
            <div className="mt-2 grid gap-2 sm:grid-cols-2">
              {listing.attributes.map((attr, idx) => {
                const label = attr.label || attr.key
                let val = null
                if (attr.type === 'ENUM') val = attr.enumLabel || attr.enumValue
                else if (attr.type === 'NUMBER') val = attr.numberValue
                else if (attr.type === 'BOOLEAN') val = attr.booleanValue === true ? 'Tak' : attr.booleanValue === false ? 'Nie' : null
                else val = attr.stringValue
                if (val == null || val === '') return null
                return (
                  <div key={attr.key || idx} className="text-sm">
                    <span className="text-zinc-400">{label}:</span> <span className="text-zinc-200">{val}</span>
                  </div>
                )
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default ListingDetails
