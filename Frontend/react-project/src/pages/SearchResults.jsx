import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

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
      if (!q) { setItems([]); setError(''); setLoading(false); return }
      try {
        setLoading(true)
        setError('')
        const res = await fetch(`${API_BASE_URL}/public/listings/search`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ query: q, categoryId: null, sellerUserId: null, attributes: [], sort: [], page: 0, size: 30 }),
        })
        if (!res.ok) { setError('Blad wyszukiwania'); return }
        const data = await res.json().catch(() => null)
        if (!cancelled) setItems(Array.isArray(data?.content) ? data.content : [])
      } catch {
        if (!cancelled) setError('Brak polaczenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    run()
    return () => { cancelled = true }
  }, [q])

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white">Wyszukiwanie</h1>
            <p className="text-slate-400 mt-1">{q ? `Wyniki dla: "${q}"` : 'Wpisz fraze w wyszukiwarce'}</p>
          </div>
          <Link to="/" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Szukam...</span>
          </div>
        )}
        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        {!loading && !error && q && items.length === 0 && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-12 text-center">
            <svg className="w-16 h-16 text-slate-600 mx-auto mb-4" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" /></svg>
            <p className="text-slate-400">Brak wynikow dla podanej frazy.</p>
          </div>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <div className="flex items-center gap-2 mb-5 text-sm text-slate-400">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
              Znaleziono {items.length} {items.length === 1 ? 'wynik' : 'wynikow'}
            </div>
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
                        {it.seller?.name && <div className="truncate text-sm text-slate-400">{it.seller.name}</div>}
                        {it.locationCity && (
                          <div className="flex items-center gap-1 truncate text-sm text-slate-500">
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
          </div>
        )}
      </div>
    </div>
  )
}
