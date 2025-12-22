import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import './Categories.css'

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
    <div className="categories-page">
      <div className="categories-container">
        <h1>
          {isListingView
            ? currentSubCategory?.name || 'Ogłoszenia w podkategorii'
            : currentCategory?.name || 'Podkategorie'}
        </h1>
        <p className="subtitle" style={{ color: '#fff', textAlign: 'center' }}>
          {isListingView ? 'Lista dostępnych ogłoszeń' : 'Wybierz podkategorię'}
        </p>

        <section className="electronics-section">
          <Link to={backLink} className="item-image-link">
            Wróć
          </Link>

          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && !isListingView && (
            <div className="categories-grid">
              {currentCategory && Array.isArray(currentCategory.children) && currentCategory.children.length > 0 ? (
                currentCategory.children
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((child) => (
                    <Link
                      key={child.id}
                      className="category-card"
                      to={`/categories/${currentCategory.id}/${child.id}`}
                    >
                      <h3>{child.name}</h3>
                    </Link>
                  ))
              ) : (
                <p style={{ color: '#fff' }}>Brak podkategorii.</p>
              )}
            </div>
          )}

          {!loading && !error && isListingView && attributes.length > 0 && (
            <div className="filters-panel">
              <h3 style={{ color: '#fff' }}>Filtry</h3>
              <div className="filters-grid">
                {attributes
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((attr) => (
                    <div key={attr.id} className="filter-item">
                      <label style={{ color: '#fff', display: 'block', marginBottom: 4 }}>
                        {attr.label}
                      </label>
                      {attr.type === 'ENUM' ? (
                        <select
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
                          type="text"
                          value={filters[attr.key]?.value ?? ''}
                          onChange={(e) => handleFilterChange(attr.key, e.target.value, attr.type)}
                        />
                      )}
                    </div>
                  ))}
              </div>
              <div className="filters-actions">
                <button
                  type="button"
                  className="filters-button clear"
                  onClick={() => {
                    setFilters({})
                    applyFilters()
                  }}
                >
                  Wyczyść
                </button>
                <button
                  type="button"
                  className="filters-button apply"
                  onClick={applyFilters}
                >
                  Zastosuj filtry
                </button>
              </div>
            </div>
          )}

          {!loading && !error && isListingView && items.length === 0 && (
            <p style={{ color: '#fff' }}>Brak ogłoszeń w tej podkategorii.</p>
          )}

          {!loading && !error && isListingView && (
            <div className="items-grid">
              {items.map((it) => {
                const priceLabel = (() => {
                  if (!it.priceAmount) return 'Brak ceny'
                  const raw =
                    typeof it.priceAmount === 'number'
                      ? it.priceAmount
                      : it.priceAmount.parsedValue ?? Number(it.priceAmount.source)
                  if (Number.isNaN(raw) || raw == null) return 'Brak ceny'
                  return `${raw.toLocaleString('pl-PL', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })} PLN`
                })()

                const createdLabel = it.createdAt
                  ? new Date(it.createdAt).toLocaleDateString('pl-PL')
                  : null

                const locationLabel = [it.locationCity, it.locationRegion]
                  .filter(Boolean)
                  .join(', ')

                const previewAttrs = Array.isArray(it.attributes)
                  ? it.attributes.slice(0, 2)
                  : []

                return (
                  <Link
                    key={it.publicId}
                    to={`/listing/${it.publicId}`}
                    className="item-card"
                    style={{ textDecoration: 'none' }}
                  >
                    <div className="item-header">
                      <div>
                        <div className="item-name">{it.title}</div>
                        {it.seller && it.seller.name && (
                          <div className="item-seller">Sprzedawca: {it.seller.name}</div>
                        )}
                        {createdLabel && (
                          <div className="item-meta">Dodano: {createdLabel}</div>
                        )}
                        {locationLabel && (
                          <div className="item-location">Lokalizacja: {locationLabel}</div>
                        )}
                      </div>
                      {it.coverImageUrl && (
                        <span className="item-image-link">Zdjęcie</span>
                      )}
                    </div>
                    <div className="item-body">
                      <div className="item-price">{priceLabel}</div>
                      {previewAttrs.length > 0 && (
                        <ul style={{ listStyle: 'none', padding: 0, margin: '4px 0 0' }}>
                          {previewAttrs.map((attr) => (
                            <li key={attr.key} style={{ color: '#ddd', fontSize: 14 }}>
                              <strong>{attr.label || attr.key}:</strong>{' '}
                              {attr.displayValue || attr.value || attr.optionLabel}
                            </li>
                          ))}
                        </ul>
                      )}
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
