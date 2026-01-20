import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-2xl sm:text-3xl font-bold text-white">Profil sprzedawcy</h1>
          <button type="button" onClick={() => navigate(-1)} className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </button>
        </div>

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Ladowanie...</span>
          </div>
        )}
        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        {!loading && !error && profile && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <div className="flex items-center gap-5">
              {profile.profileImageUrl ? (
                <img src={profile.profileImageUrl} alt="avatar" className="h-20 w-20 rounded-2xl object-cover ring-4 ring-slate-700/50" />
              ) : (
                <div className="h-20 w-20 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
                  <span className="text-2xl font-bold text-white">{profile.name?.charAt(0)?.toUpperCase() || 'U'}</span>
                </div>
              )}
              <div>
                <div className="text-xl font-semibold text-white">{profile.name || '-'}</div>
                {profile.memberSince && (
                  <div className="flex items-center gap-2 text-sm text-slate-400 mt-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                    Konto od: {new Date(profile.memberSince).toLocaleDateString('pl-PL')}
                  </div>
                )}
                <div className="flex items-center gap-2 text-sm text-slate-400 mt-1">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
                  {items.length} {items.length === 1 ? 'ogloszenie' : 'ogloszen'}
                </div>
              </div>
            </div>
          </div>
        )}

        {!loading && !error && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <h2 className="text-lg font-semibold text-white mb-5 flex items-center gap-2">
              <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
              Ogloszenia sprzedawcy
            </h2>
            {items.length === 0 ? (
              <p className="text-slate-400 text-center py-8">Brak aktywnych ogloszen.</p>
            ) : (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {items.map((it) => {
                  const priceLabel = it.priceAmount != null && !Number.isNaN(Number(it.priceAmount))
                    ? `${Number(it.priceAmount).toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN`
                    : 'Brak ceny'
                  return (
                    <Link
                      key={it.publicId}
                      to={`/listing/${it.publicId}`}
                      className="group rounded-xl border border-slate-700/50 bg-slate-800/50 p-4 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:shadow-lg hover:-translate-y-0.5"
                    >
                      <div className="flex gap-4">
                        {it.coverImageUrl ? (
                          <img src={it.coverImageUrl} alt={it.title} className="h-24 w-28 flex-none rounded-lg object-cover" />
                        ) : (
                          <div className="h-24 w-28 flex-none rounded-lg bg-slate-700/50 flex items-center justify-center">
                            <svg className="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                          </div>
                        )}
                        <div className="min-w-0 flex-1">
                          <div className="truncate font-medium text-white group-hover:text-emerald-400 transition-colors">{it.title}</div>
                          {it.locationCity && (
                            <div className="flex items-center gap-1 truncate text-sm text-slate-500 mt-1">
                              <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                              {it.locationCity}
                            </div>
                          )}
                          <div className="mt-2 text-lg font-bold text-emerald-400">{priceLabel}</div>
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
