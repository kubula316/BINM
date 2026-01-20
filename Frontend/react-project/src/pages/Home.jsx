import { Link } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-8">
        <section className="relative overflow-hidden rounded-3xl border border-slate-800/50 bg-gradient-to-br from-slate-800/80 via-slate-800/50 to-slate-900/80 p-8 sm:p-12 text-center">
          <div className="absolute inset-0 bg-gradient-to-br from-emerald-500/5 via-transparent to-teal-500/5"></div>
          <div className="relative">
            <div className="inline-flex items-center gap-2 rounded-full bg-emerald-500/10 border border-emerald-500/20 px-4 py-1.5 text-emerald-400 text-sm font-medium mb-4">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              Platforma ogloszen
            </div>
            <h1 className="text-3xl sm:text-5xl font-bold text-white mb-3">Witaj w <span className="bg-gradient-to-r from-emerald-400 to-teal-400 bg-clip-text text-transparent">BINM</span></h1>
            <p className="text-slate-400 text-lg mb-8">Bylo i Nie Ma</p>

            <div className="flex flex-wrap justify-center gap-3">
              <Link className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-6 py-3 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white hover:border-slate-600" to="/categories">
                <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M4 6h16M4 10h16M4 14h16M4 18h16" /></svg>
                Przegladaj kategorie
              </Link>
              {isLoggedIn && (
                <Link className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40" to="/add-listing">
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 4v16m8-8H4" /></svg>
                  Dodaj ogloszenie
                </Link>
              )}
            </div>
          </div>
        </section>

        {suggestedCategories.length > 0 && (
          <section className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Kategorie</h2>
              <Link to="/categories" className="text-sm font-medium text-emerald-400 hover:text-emerald-300 transition-colors">Zobacz wszystkie</Link>
            </div>

            <div className="grid grid-cols-4 sm:grid-cols-8 gap-4">
              {suggestedCategories.map((cat) => (
                <Link key={cat.id} to={`/categories/${cat.id}`} className="group">
                  <div className="flex flex-col items-center gap-3">
                    <div className="flex h-14 w-14 sm:h-16 sm:w-16 items-center justify-center rounded-2xl border border-slate-700/50 bg-slate-800/50 transition-all group-hover:border-emerald-500/50 group-hover:bg-slate-700 group-hover:scale-105 group-hover:shadow-lg group-hover:shadow-emerald-500/10">
                      {cat.imageUrl ? (
                        <img src={cat.imageUrl} alt={cat.name} className="h-8 w-8 object-contain" />
                      ) : (
                        <span className="text-lg font-bold text-slate-400 group-hover:text-emerald-400 transition-colors">{String(cat.name || '?').slice(0, 1)}</span>
                      )}
                    </div>
                    <div className="w-full text-center text-xs text-slate-400 group-hover:text-slate-200 transition-colors truncate">{cat.name}</div>
                  </div>
                </Link>
              ))}
            </div>
          </section>
        )}

        {randomListings.length > 0 && (
          <section className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Proponowane ogloszenia</h2>
              <Link to="/categories" className="text-sm font-medium text-emerald-400 hover:text-emerald-300 transition-colors">Zobacz wiecej</Link>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
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
                    className="group rounded-xl border border-slate-700/50 bg-slate-800/50 p-4 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:shadow-lg hover:shadow-slate-950/50 hover:-translate-y-0.5"
                  >
                    <div className="flex gap-4">
                      {it.coverImageUrl ? (
                        <img
                          src={it.coverImageUrl}
                          alt={it.title}
                          className="h-24 w-28 flex-none rounded-lg object-cover"
                        />
                      ) : (
                        <div className="h-24 w-28 flex-none rounded-lg bg-slate-700/50 flex items-center justify-center">
                          <svg className="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                        </div>
                      )}
                      <div className="min-w-0 flex-1">
                        <div className="font-medium text-white line-clamp-2 break-words group-hover:text-emerald-400 transition-colors">{it.title}</div>
                        {it.seller?.name && <div className="truncate text-sm text-slate-400 mt-1">{it.seller.name}</div>}
                        {it.locationCity && (
                          <div className="flex items-center gap-1 truncate text-sm text-slate-500 mt-0.5">
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
          </section>
        )}
      </div>
    </div>
  )
}
