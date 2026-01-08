import { Link } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'

const API_BASE_URL = 'http://localhost:8081'

export default function Home({ isLoggedIn }) {
  const [randomListings, setRandomListings] = useState([])
  const [categories, setCategories] = useState([])

  useEffect(() => {
    const fetchRandom = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/public/listings/random?page=0&size=12`)
        if (!response.ok) return
        const data = await response.json()
        setRandomListings(Array.isArray(data.content) ? data.content : [])
      } catch {
        // cicho
      }
    }
    fetchRandom()
  }, [])

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/public/category/all`)
        if (!res.ok) return
        const data = await res.json()
        setCategories(Array.isArray(data) ? data : [])
      } catch {
        // cicho
      }
    }
    fetchCategories()
  }, [])

  const suggestedCategories = useMemo(() => {
    if (!Array.isArray(categories)) return []
    return categories.filter((c) => c && c.parentId === null).slice(0, 8)
  }, [categories])

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-5">
        <section className="ui-section text-center">
          <h1 className="ui-h1">Witaj w BINM</h1>
          <p className="ui-muted mt-1">Było i Nie Ma</p>

          <div className="mt-4 flex flex-wrap justify-center gap-2">
            <Link className="ui-btn" to="/categories">Przegladaj kategorie</Link>
            {isLoggedIn && <Link className="ui-btn-primary" to="/add-listing">Dodaj ogloszenie</Link>}
          </div>
        </section>

        {suggestedCategories.length > 0 && (
          <section className="ui-section">
            <h2 className="ui-h2 text-center">Kategorie</h2>

            <div className="mt-4 flex flex-wrap justify-center gap-4">
              {suggestedCategories.map((cat) => (
                <Link key={cat.id} to={`/categories/${cat.id}`} className="group">
                  <div className="flex w-20 flex-col items-center gap-2">
                    <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-zinc-700 bg-zinc-800 transition group-hover:border-emerald-500/50 group-hover:bg-zinc-700">
                      {cat.imageUrl ? (
                        <img src={cat.imageUrl} alt={cat.name} className="h-9 w-9 object-contain" />
                      ) : (
                        <span className="text-base font-semibold text-zinc-300">{String(cat.name || '?').slice(0, 1)}</span>
                      )}
                    </div>
                    <div className="w-full truncate text-center text-xs text-zinc-400 group-hover:text-zinc-300">{cat.name}</div>
                  </div>
                </Link>
              ))}
            </div>

            <div className="mt-4 text-center">
              <Link to="/categories" className="ui-link text-sm">Zobacz wszystkie kategorie</Link>
            </div>
          </section>
        )}

        {randomListings.length > 0 && (
          <section className="ui-section">
            <div className="flex items-center justify-between gap-3">
              <h2 className="ui-h2">Proponowane ogloszenia</h2>
              <Link to="/categories" className="ui-link">Przegladaj</Link>
            </div>

            <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {randomListings.map((it) => {
                const priceLabel = (() => {
                  if (it.priceAmount == null) return 'Brak ceny'
                  const raw = Number(it.priceAmount)
                  if (Number.isNaN(raw)) return 'Brak ceny'
                  return `${raw.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN`
                })()

                return (
                  <Link
                    key={it.publicId}
                    to={`/listing/${it.publicId}`}
                    className="rounded-xl border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-750 hover:border-zinc-600"
                  >
                    <div className="flex gap-3">
                      {it.coverImageUrl && (
                        <img
                          src={it.coverImageUrl}
                          alt={it.title}
                          className="h-20 w-24 flex-none rounded-lg object-cover"
                        />
                      )}
                      <div className="min-w-0 flex-1">
                        <div className="font-medium text-zinc-100 line-clamp-2 break-words">{it.title}</div>
                        {it.seller?.name && <div className="truncate text-sm text-zinc-400">{it.seller.name}</div>}
                        {it.locationCity && <div className="truncate text-sm text-zinc-500">{it.locationCity}</div>}
                        <div className="mt-1 font-semibold text-emerald-400">{priceLabel}</div>
                      </div>
                    </div>
                  </Link>
                )
              })}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}
