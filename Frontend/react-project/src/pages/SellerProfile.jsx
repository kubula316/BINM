import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function SellerProfile() {
  const { userId } = useParams()
  const navigate = useNavigate()
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
        if (!profileRes.ok) { setError('Nie udalo sie pobrac profilu'); return }
        if (!listingsRes.ok) { setError('Nie udalo sie pobrac ogloszen'); return }
        const p = await profileRes.json()
        const l = await listingsRes.json()
        if (cancelled) return
        setProfile(p)
        setItems(Array.isArray(l.content) ? l.content : [])
      } catch {
        if (!cancelled) setError('Brak polaczenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    if (userId) fetchAll()
    return () => { cancelled = true }
  }, [userId])

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <div className="flex items-center justify-between gap-3">
          <h1 className="ui-h1">Profil sprzedawcy</h1>
          <button type="button" onClick={() => navigate(-1)} className="ui-btn">Wroc</button>
        </div>

        {loading && <p className="ui-muted">Ladowanie...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && profile && (
          <div className="ui-section">
            <div className="flex items-center gap-4">
              {profile.profileImageUrl && (
                <img src={profile.profileImageUrl} alt="avatar" className="h-20 w-20 rounded-xl object-cover" />
              )}
              <div>
                <div className="text-xl font-medium text-zinc-100">{profile.name || '-'}</div>
                {profile.memberSince && (
                  <div className="text-sm text-zinc-400">Konto od: {new Date(profile.memberSince).toLocaleDateString('pl-PL')}</div>
                )}
              </div>
            </div>
          </div>
        )}

        {!loading && !error && (
          <div className="ui-section">
            <h2 className="ui-h2">Ogloszenia</h2>
            {items.length === 0 ? (
              <p className="mt-2 text-zinc-400">Brak aktywnych ogloszen.</p>
            ) : (
              <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {items.map((it) => {
                  const priceLabel = it.priceAmount != null && !Number.isNaN(Number(it.priceAmount))
                    ? `${Number(it.priceAmount).toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN`
                    : 'Brak ceny'
                  return (
                    <Link
                      key={it.publicId}
                      to={`/listing/${it.publicId}`}
                      className="rounded-xl border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-750 hover:border-zinc-600"
                    >
                      <div className="flex gap-3">
                        {it.coverImageUrl && (
                          <img src={it.coverImageUrl} alt={it.title} className="h-20 w-24 flex-none rounded-lg object-cover" />
                        )}
                        <div className="min-w-0 flex-1">
                          <div className="truncate font-medium text-zinc-100">{it.title}</div>
                          {it.locationCity && <div className="truncate text-sm text-zinc-500">{it.locationCity}</div>}
                          <div className="mt-2 font-semibold text-emerald-400">{priceLabel}</div>
                        </div>
                      </div>
                    </Link>
                  )
                })}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
