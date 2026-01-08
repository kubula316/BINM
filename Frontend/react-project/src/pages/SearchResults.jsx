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
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Wyszukiwanie</h1>
        <p className="ui-muted text-center">{q ? `Fraza: "${q}"` : 'Wpisz fraze w wyszukiwarce u gory'}</p>
        <div className="text-center">
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        {loading && <p className="ui-muted">Szukam...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && q && items.length === 0 && (
          <p className="ui-muted">Brak wynikow.</p>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
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
                      {it.seller?.name && <div className="truncate text-sm text-zinc-400">{it.seller.name}</div>}
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
    </div>
  )
}
