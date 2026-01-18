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
            setError('Nie udało się pobrać kategorii')
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

          // Pobierz atrybuty dla tej podkategorii
          const attrsResponse = await fetch(
            `${API_BASE_URL}/public/category/attributes?categoryId=${parsedId}`,
          )

          if (!attrsResponse.ok) {
            setError('Nie udało się pobrać atrybutów kategorii')
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
            setError('Nie udało się pobrać ogłoszeń dla tej podkategorii')
            return
          }

          const data = await response.json()
          setItems(Array.isArray(data.content) ? data.content : [])
        }
      } catch {
        setError('Brak połączenia z serwerem')
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
        setError('Nie udało się pobrać ogłoszeń dla tej podkategorii')
        return
      }

      const data = await response.json()
      setItems(Array.isArray(data.content) ? data.content : [])
    } catch {
      setError('Brak połączenia z serwerem')
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
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">
          {isListingView
            ? currentSubCategory?.name || 'Ogloszenia'
            : currentCategory?.name || 'Podkategorie'}
        </h1>
        <div className="text-center">
          <Link to={backLink} className="ui-btn">Wroc</Link>
        </div>

        <section className="ui-section">
          {loading && <p className="ui-muted">Ladowanie...</p>}
          {error && <p className="text-sm text-red-400">{error}</p>}

          {!loading && !error && !isListingView && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {currentCategory && Array.isArray(currentCategory.children) && currentCategory.children.length > 0 ? (
                currentCategory.children
                  .slice()
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((child) => (
                    <Link
                      key={child.id}
                      to={`/categories/${currentCategory.id}/${child.id}`}
                      className="flex items-center gap-3 rounded-lg border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-700"
                    >
                      <div className="flex h-10 w-10 flex-none items-center justify-center rounded-lg bg-zinc-700">
                        {child.imageUrl ? (
                          <img src={child.imageUrl} alt={child.name} className="h-6 w-6 object-contain" />
                        ) : (
                          <span className="text-sm font-semibold text-zinc-300">{String(child.name || '?').slice(0, 1)}</span>
                        )}
                      </div>
                      <div className="truncate font-medium text-zinc-100">{child.name}</div>
                    </Link>
                  ))
              ) : (
                <p className="ui-muted">Brak podkategorii.</p>
              )}
            </div>
          )}

          {!loading && !error && isListingView && attributes.length > 0 && (
            <div className="mb-4 rounded-lg border border-zinc-700 bg-zinc-800/50 p-4">
              <div className="mb-3 font-medium text-zinc-100">Filtry</div>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {attributes
                  .slice()
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((attr) => (
                    <div key={attr.id}>
                      <label className="mb-1 block text-sm text-zinc-400">{attr.label}</label>
                      {attr.type === 'ENUM' ? (
                        <select
                          className="ui-input"
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
                          className="ui-input"
                          type="text"
                          value={filters[attr.key]?.value ?? ''}
                          onChange={(e) => handleFilterChange(attr.key, e.target.value, attr.type)}
                        />
                      )}
                    </div>
                  ))}
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                <button
                  type="button"
                  className="ui-btn"
                  onClick={() => {
                    setFilters({})
                    applyFilters()
                  }}
                >
                  Wyczysc
                </button>
                <button type="button" className="ui-btn-primary" onClick={applyFilters}>
                  Zastosuj
                </button>
              </div>
            </div>
          )}

          {!loading && !error && isListingView && items.length === 0 && <p className="ui-muted">Brak ogloszen.</p>}

          {!loading && !error && isListingView && items.length > 0 && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
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
                    className="rounded-lg border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-700"
                  >
                    <div className="flex gap-3">
                      {it.coverImageUrl && (
                        <img src={it.coverImageUrl} alt={it.title} className="h-20 w-24 flex-none rounded-lg object-cover" />
                      )}
                      <div className="min-w-0 flex-1">
                        <div className="truncate font-medium text-zinc-100">{it.title}</div>
                        {it.seller?.name && <div className="truncate text-sm text-zinc-400">{it.seller.name}</div>}
                        {createdLabel && <div className="text-sm text-zinc-500">Dodano: {createdLabel}</div>}
                        {locationLabel && <div className="truncate text-sm text-zinc-500">{locationLabel}</div>}
                        <div className="mt-2 font-semibold text-emerald-400">{priceLabel}</div>
                      </div>
                    </div>
                  </Link>
                )
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
