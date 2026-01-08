import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function Favorites() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [removingId, setRemovingId] = useState(null)

  const fetchFavorites = async () => {
    try {
      setLoading(true)
      setError('')
      const res = await fetch(`${API_BASE_URL}/user/interactions/favorites?page=0&size=50`, {
        credentials: 'include',
      })
      if (!res.ok) {
        setError(res.status === 401 ? 'Musisz byc zalogowany.' : 'Blad pobierania.')
        return
      }
      const data = await res.json()
      setItems(Array.isArray(data.content) ? data.content : [])
    } catch {
      setError('Brak polaczenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchFavorites() }, [])

  const removeFavorite = async (publicId) => {
    try {
      setRemovingId(publicId)
      const res = await fetch(`${API_BASE_URL}/user/interactions/favorites`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ entityId: String(publicId), entityType: 'LISTING' }),
      })
      if (!res.ok) { alert('Blad usuwania.'); return }
      setItems((prev) => prev.filter((x) => x.publicId !== publicId))
    } catch {
      alert('Brak polaczenia')
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Obserwowane</h1>
        <div className="text-center">
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        {loading && <p className="ui-muted">Ladowanie...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && items.length === 0 && (
          <p className="ui-muted">Nie obserwujesz jeszcze zadnych ogloszen.</p>
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
                  <div className="mt-2">
                    <button
                      type="button"
                      className="ui-btn text-xs"
                      onClick={(e) => { e.preventDefault(); e.stopPropagation(); removeFavorite(it.publicId) }}
                      disabled={removingId === it.publicId}
                    >
                      {removingId === it.publicId ? 'Usuwanie...' : 'Usun z obserwowanych'}
                    </button>
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
