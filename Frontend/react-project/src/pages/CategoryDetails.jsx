import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function CategoryDetails() {
  const { categoryId, subCategoryId } = useParams()
  const [categories, setCategories] = useState([])
  const [items, setItems] = useState([])
  const [attributes, setAttributes] = useState([])
  const [filters, setFilters] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const isListingView = Boolean(subCategoryId)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        setError('')

        if (!isListingView) {
          const response = await fetch(`${API_BASE_URL}/public/category/all`)
          if (!response.ok) {
            setError('Nie udalo sie pobrac kategorii')
            return
          }
          const data = await response.json()
          setCategories(data || [])
        } else {
          const parsedId = Number(subCategoryId)
          if (Number.isNaN(parsedId)) {
            setError('Niepoprawne ID podkategorii')
            return
          }

          const attrsResponse = await fetch(
            `${API_BASE_URL}/public/category/attributes?categoryId=${parsedId}`,
          )

          if (!attrsResponse.ok) {
            setError('Nie udalo sie pobrac atrybutow kategorii')
            return
          }

          const attrsData = await attrsResponse.json()
          setAttributes(Array.isArray(attrsData) ? attrsData : [])

          const response = await fetch(`${API_BASE_URL}/public/listings/search`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              categoryId: parsedId,
              sellerUserId: null,
              attributes: [],
              sort: [],
              page: 0,
              size: 20,
            }),
          })

          if (!response.ok) {
            setError('Nie udalo sie pobrac ogloszen dla tej podkategorii')
            return
          }

          const data = await response.json()
          setItems(Array.isArray(data.content) ? data.content : [])
        }
      } catch {
        setError('Brak polaczenia z serwerem')
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [categoryId, subCategoryId, isListingView])

  const handleFilterChange = (key, value, type) => {
    setFilters((prev) => ({
      ...prev,
      [key]: { value, type },
    }))
  }

  const applyFilters = async () => {
    if (!subCategoryId) return

    try {
      setLoading(true)
      setError('')
      const parsedId = Number(subCategoryId)
      if (Number.isNaN(parsedId)) {
        setError('Niepoprawne ID podkategorii')
        return
      }

      const attributesPayload = Object.entries(filters)
        .filter(([, v]) => v.value !== '' && v.value != null)
        .map(([key, v]) => ({
          key,
          type: v.type,
          op: 'eq',
          value: v.value,
        }))

      const response = await fetch(`${API_BASE_URL}/public/listings/search`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          categoryId: parsedId,
          sellerUserId: null,
          attributes: attributesPayload,
          sort: [],
          page: 0,
          size: 20,
        }),
      })

      if (!response.ok) {
        setError('Nie udalo sie pobrac ogloszen dla tej podkategorii')
        return
      }

      const data = await response.json()
      setItems(Array.isArray(data.content) ? data.content : [])
    } catch {
      setError('Brak polaczenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  const findCategoryById = (nodes, id) => {
    if (!Array.isArray(nodes)) return null
    for (const node of nodes) {
      if (node.id === id) return node
      if (Array.isArray(node.children) && node.children.length > 0) {
        const found = findCategoryById(node.children, id)
        if (found) return found
      }
    }
    return null
  }

  const numericCategoryId = Number(categoryId)
  const currentCategory = !Number.isNaN(numericCategoryId)
    ? findCategoryById(categories, numericCategoryId)
    : null

  const numericSubCategoryId = subCategoryId ? Number(subCategoryId) : null
  const currentSubCategory = numericSubCategoryId && currentCategory && Array.isArray(currentCategory.children)
    ? currentCategory.children.find((c) => c.id === numericSubCategoryId)
    : null

  const baseBackLink = '/categories'
  const backLink = isListingView ? `/categories/${categoryId}` : baseBackLink

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white">
              {isListingView
                ? currentSubCategory?.name || 'Ogloszenia'
                : currentCategory?.name || 'Podkategorie'}
            </h1>
            <p className="text-slate-400 mt-1">
              {isListingView ? 'Przegladaj dostepne ogloszenia' : 'Wybierz podkategorie'}
            </p>
          </div>
          <Link to={backLink} className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
          {loading && (
            <div className="flex items-center justify-center py-12">
              <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
              <span className="ml-3 text-slate-400">Ladowanie...</span>
            </div>
          )}
          {error && (
            <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>
          )}

          {!loading && !error && !isListingView && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {currentCategory && Array.isArray(currentCategory.children) && currentCategory.children.length > 0 ? (
                currentCategory.children
                  .slice()
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((child) => (
                    <Link
                      key={child.id}
                      to={`/categories/${currentCategory.id}/${child.id}`}
                      className="group flex items-center gap-4 rounded-xl border border-slate-700/50 bg-slate-800/50 p-4 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:shadow-lg"
                    >
                      <div className="flex h-12 w-12 flex-none items-center justify-center rounded-xl bg-slate-700/50 group-hover:bg-emerald-500/20 transition-colors">
                        {child.imageUrl ? (
                          <img src={child.imageUrl} alt={child.name} className="h-7 w-7 object-contain" />
                        ) : (
                          <span className="text-base font-bold text-slate-400 group-hover:text-emerald-400 transition-colors">{String(child.name || '?').slice(0, 1)}</span>
                        )}
                      </div>
                      <div className="truncate font-medium text-white group-hover:text-emerald-400 transition-colors">{child.name}</div>
                      <svg className="w-5 h-5 text-slate-500 group-hover:text-emerald-400 transition-colors ml-auto" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" /></svg>
                    </Link>
                  ))
              ) : (
                <p className="text-slate-400 col-span-full text-center py-8">Brak podkategorii.</p>
              )}
            </div>
          )}

          {!loading && !error && isListingView && attributes.length > 0 && (
            <div className="mb-6 rounded-xl border border-slate-700/50 bg-slate-800/50 p-5">
              <div className="flex items-center gap-2 mb-4">
                <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" /></svg>
                <span className="font-semibold text-white">Filtry</span>
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {attributes
                  .slice()
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((attr) => (
                    <div key={attr.id}>
                      <label className="mb-2 block text-sm font-medium text-slate-300">{attr.label}</label>
                      {attr.type === 'ENUM' ? (
                        <select
                          className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20"
                          value={filters[attr.key]?.value ?? ''}
                          onChange={(e) => handleFilterChange(attr.key, e.target.value, attr.type)}
                        >
                          <option value="">Wszystkie</option>
                          {attr.options
                            ?.slice()
                            .sort((a, b) => a.sortOrder - b.sortOrder)
                            .map((opt) => (
                              <option key={opt.id} value={opt.value}>
                                {opt.label}
                              </option>
                            ))}
                        </select>
                      ) : (
                        <input
                          className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20"
                          type="text"
                          value={filters[attr.key]?.value ?? ''}
                          onChange={(e) => handleFilterChange(attr.key, e.target.value, attr.type)}
                        />
                      )}
                    </div>
                  ))}
              </div>
              <div className="mt-5 flex flex-wrap gap-3">
                <button
                  type="button"
                  className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-5 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white"
                  onClick={() => {
                    setFilters({})
                    applyFilters()
                  }}
                >
                  Wyczysc
                </button>
                <button type="button" className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500" onClick={applyFilters}>
                  Zastosuj filtry
                </button>
              </div>
            </div>
          )}

          {!loading && !error && isListingView && items.length === 0 && <p className="text-slate-400 text-center py-8">Brak ogloszen w tej kategorii.</p>}

          {!loading && !error && isListingView && items.length > 0 && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {items.map((it) => {
                const priceLabel = (() => {
                  if (it.priceAmount == null) return 'Brak ceny'
                  const raw = Number(it.priceAmount)
                  if (Number.isNaN(raw)) return 'Brak ceny'
                  return `${raw.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN`
                })()

                const createdLabel = it.createdAt ? new Date(it.createdAt).toLocaleDateString('pl-PL') : null
                const locationLabel = [it.locationCity, it.locationRegion].filter(Boolean).join(', ')

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
                        {createdLabel && <div className="text-sm text-slate-500">Dodano: {createdLabel}</div>}
                        {locationLabel && (
                          <div className="flex items-center gap-1 truncate text-sm text-slate-500">
                            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                            {locationLabel}
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
      </div>
    </div>
  )
}
