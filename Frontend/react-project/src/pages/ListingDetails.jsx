import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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
      if (localStorage.getItem('forceLoggedOut') === '1') return

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
      <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
        <div className="mx-auto w-full max-w-6xl px-4 sm:px-6">
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Ladowanie ogloszenia...</span>
          </div>
        </div>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
        <div className="mx-auto w-full max-w-6xl px-4 sm:px-6">
          <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400 mb-4">{error || 'Ogloszenie nie zostalo znalezione'}</div>
          <Link to="/categories" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">Wroc do kategorii</Link>
        </div>
      </div>
    )
  }

  const handleFavorite = async () => {
    if (!meId) {
      alert('Musisz byc zalogowany, aby dodac do obserwowanych.')
      return
    }
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
    if (!meId) {
      alert('Musisz byc zalogowany, aby zobaczyc numer.')
      return
    }
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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-2xl sm:text-3xl font-bold text-white break-words">{listing.title}</h1>
          <button type="button" onClick={() => navigate(-1)} className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white flex-none">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </button>
        </div>

        {listing.status && listing.status !== 'ACTIVE' && (
          <div className="rounded-xl bg-amber-500/10 border border-amber-500/20 px-4 py-3 text-sm text-amber-400 flex items-center gap-2">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
            To ogloszenie oczekuje na akceptacje.
          </div>
        )}

        <div className="grid gap-6 lg:grid-cols-2">
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-5 sm:p-6">
            {mainImage ? (
              <div className="space-y-4">
                <div className="relative rounded-xl overflow-hidden bg-slate-900">
                  <img src={mainImage} alt={listing.title} className="w-full object-contain max-h-96" />
                  {images.length > 1 && (
                    <div className="absolute inset-x-0 bottom-4 flex items-center justify-center gap-3">
                      <button type="button" className="h-10 w-10 rounded-full bg-slate-900/90 text-white hover:bg-slate-800 transition-colors flex items-center justify-center" onClick={() => setActiveImageIndex((i) => (i - 1 + images.length) % images.length)}>
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7" /></svg>
                      </button>
                      <span className="bg-slate-900/90 px-3 py-1.5 rounded-full text-sm text-white">{activeImageIndex + 1}/{images.length}</span>
                      <button type="button" className="h-10 w-10 rounded-full bg-slate-900/90 text-white hover:bg-slate-800 transition-colors flex items-center justify-center" onClick={() => setActiveImageIndex((i) => (i + 1) % images.length)}>
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" /></svg>
                      </button>
                    </div>
                  )}
                </div>
                {images.length > 1 && (
                  <div className="flex justify-center gap-2 overflow-x-auto pb-2">
                    {images.map((url, idx) => (
                      <button key={idx} type="button" onClick={() => setActiveImageIndex(idx)} className={`h-16 w-16 flex-none rounded-lg overflow-hidden transition-all ${idx === activeImageIndex ? 'ring-2 ring-emerald-500 ring-offset-2 ring-offset-slate-800' : 'opacity-60 hover:opacity-100'}`}>
                        <img src={url} alt="" className="h-full w-full object-cover" />
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center justify-center h-64 bg-slate-700/30 rounded-xl">
                <div className="text-center">
                  <svg className="w-12 h-12 text-slate-500 mx-auto mb-2" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                  <p className="text-slate-400">Brak zdjec</p>
                </div>
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-5 sm:p-6 space-y-5">
            <div>
              <div className="text-3xl font-bold bg-gradient-to-r from-emerald-400 to-teal-400 bg-clip-text text-transparent">
                {listing.priceAmount?.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {listing.currency || 'PLN'}
              </div>
              {listing.negotiable && <span className="inline-flex items-center gap-1 mt-2 text-sm text-slate-400"><svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>Do negocjacji</span>}
            </div>
            
            <div className="space-y-3 text-sm">
              <div className="flex items-center gap-3 text-slate-400">
                <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                Dodano: {listing.createdAt ? new Date(listing.createdAt).toLocaleDateString('pl-PL') : '-'}
              </div>
              {listing.seller && (
                <div className="flex items-center gap-3 text-slate-400">
                  <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                  Sprzedawca: <Link to={`/users/${listing.seller.id}`} className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors">{listing.seller.name}</Link>
                </div>
              )}
              <div className="flex items-center gap-3 text-slate-400">
                <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                {listing.locationCity || 'Brak'}{listing.locationRegion ? `, ${listing.locationRegion}` : ''}
              </div>
            </div>

            <div className="flex flex-wrap gap-3 pt-3">
              <button type="button" onClick={handleContact} disabled={contactLoading} className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-5 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white disabled:opacity-50">
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" /></svg>
                {contactLoading ? '...' : contactPhone ? `${contactPhone}` : 'Pokaz numer'}
              </button>
              <button type="button" onClick={handleFavorite} disabled={favoriteLoading} className={`inline-flex items-center justify-center rounded-xl border px-5 py-2.5 text-sm font-medium transition-all disabled:opacity-50 ${isFavorite ? 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400' : 'border-slate-700/50 bg-slate-800/50 text-slate-300 hover:bg-slate-700 hover:text-white'}`}>
                {isFavorite ? (
                  <svg className="w-4 h-4 mr-2 fill-current" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>
                ) : (
                  <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>
                )}
                {favoriteLoading ? '...' : isFavorite ? 'Obserwowane' : 'Obserwuj'}
              </button>
              {listing.seller?.id && meId && meId !== listing.seller.id && (
                <Link to={`/messages/new/${publicId}/${listing.seller.id}`} className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500">
                  <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
                  Napisz wiadomosc
                </Link>
              )}
            </div>
            {(favoriteError || contactError) && <p className="text-sm text-red-400 mt-2">{favoriteError || contactError}</p>}
          </div>
        </div>

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-5 sm:p-6">
          <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
            Opis
          </h2>
          <p className="text-slate-300 whitespace-pre-wrap break-words leading-relaxed">{listing.description || 'Brak opisu'}</p>
        </div>

        {Array.isArray(listing.attributes) && listing.attributes.length > 0 && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-5 sm:p-6">
            <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" /></svg>
              Parametry
            </h2>
            <div className="grid gap-3 sm:grid-cols-2">
              {listing.attributes.map((attr, idx) => {
                const label = attr.label || attr.key
                let val = null
                if (attr.type === 'ENUM') val = attr.enumLabel || attr.enumValue
                else if (attr.type === 'NUMBER') val = attr.numberValue
                else if (attr.type === 'BOOLEAN') val = attr.booleanValue === true ? 'Tak' : attr.booleanValue === false ? 'Nie' : null
                else val = attr.stringValue
                if (val == null || val === '') return null
                return (
                  <div key={attr.key || idx} className="flex items-center justify-between p-3 rounded-lg bg-slate-700/30">
                    <span className="text-sm text-slate-400">{label}</span>
                    <span className="text-sm font-medium text-white">{val}</span>
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
