import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function MyListings() {
  const [items, setItems] = useState([])
  const [statusById, setStatusById] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [editListing, setEditListing] = useState(null)
  const [editAttributes, setEditAttributes] = useState([])
  const [editAttributeValues, setEditAttributeValues] = useState({})

  useEffect(() => {
    const fetchMyListings = async () => {
      try {
        setLoading(true)
        setError('')
        const response = await fetch(`${API_BASE_URL}/user/listing/my?page=0&size=20`, {
          credentials: 'include',
        })

        if (!response.ok) {
          if (response.status === 401) {
            setError('Musisz być zalogowany, aby zobaczyć swoje ogłoszenia.')
          } else {
            setError('Nie udało się pobrać Twoich ogłoszeń.')
          }
          return
        }

        const data = await response.json()
        const list = Array.isArray(data.content) ? data.content : []
        setItems(list)

        // Backend /user/listing/my zwraca ListingCoverDto bez statusu.
        // Dociągamy statusy przez filtrowanie po statusie na endpointcie user (działa też dla niepublicznych).
        const ids = new Set(list.map((x) => x.publicId))
        const STATUSES = ['ACTIVE', 'WAITING', 'DRAFT', 'REJECTED', 'COMPLETED', 'EXPIRED']

        const results = await Promise.allSettled(
          STATUSES.map(async (status) => {
            const res = await fetch(
              `${API_BASE_URL}/user/listing/my?page=0&size=200&status=${status}`,
              { credentials: 'include' },
            )
            if (!res.ok) return { status, ids: [] }
            const page = await res.json()
            const content = Array.isArray(page.content) ? page.content : []
            return {
              status,
              ids: content.map((it) => it.publicId).filter((id) => ids.has(id)),
            }
          }),
        )

        const map = {}
        results.forEach((r) => {
          if (r.status !== 'fulfilled') return
          r.value.ids.forEach((id) => {
            map[id] = r.value.status
          })
        })
        setStatusById(map)
      } catch {
        setError('Brak połączenia z serwerem')
      } finally {
        setLoading(false)
      }
    }

    fetchMyListings()
  }, [])

  const openEdit = async (listing) => {
    try {
      setUpdatingId(listing.publicId)

      const response = await fetch(`${API_BASE_URL}/user/listing/${listing.publicId}/edit-data`, {
        credentials: 'include',
      })

      if (!response.ok) {
        alert('Nie udało się pobrać danych ogłoszenia do edycji.')
        return
      }

      const data = await response.json()

      // Zbuduj mapę aktualnych wartości atrybutów z edit-data
      const currentAttrMap = new Map()
      if (Array.isArray(data.attributes)) {
        data.attributes.forEach((a) => currentAttrMap.set(a.key, a))
      }

      // Pobierz pełne definicje atrybutów dla kategorii, żeby pokazać także puste
      let mergedAttributes = []
      if (data.categoryId) {
        try {
          const attrDefResp = await fetch(
            `${API_BASE_URL}/public/category/attributes?categoryId=${data.categoryId}`,
          )
          if (attrDefResp.ok) {
            const defs = await attrDefResp.json()
            if (Array.isArray(defs)) {
              mergedAttributes = defs.map((def) => {
                const current = currentAttrMap.get(def.key)
                let currentValue = ''
                let selectValue = ''

                if (current) {
                  if (current.type === 'ENUM') {
                    currentValue = current.enumLabel || current.enumValue || ''
                    selectValue = current.enumValue || ''
                  } else if (current.type === 'STRING') {
                    currentValue = current.stringValue || ''
                    selectValue = currentValue
                  } else if (current.type === 'NUMBER') {
                    currentValue =
                      current.numberValue != null ? String(current.numberValue) : ''
                    selectValue = currentValue
                  } else if (current.type === 'BOOLEAN') {
                    currentValue =
                      current.booleanValue == null
                        ? ''
                        : current.booleanValue
                          ? 'Tak'
                          : 'Nie'
                    selectValue =
                      current.booleanValue == null ? '' : String(current.booleanValue)
                  }
                }

                return {
                  ...def,
                  currentValue,
                  selectValue,
                }
              })
            }
          }
        } catch {
          // jeśli pobranie definicji się nie uda, użyj tego co zwrócił edit-data
        }
      }

      if (!mergedAttributes.length && Array.isArray(data.attributes)) {
        mergedAttributes = data.attributes.map((a) => {
          let currentValue = ''
          let selectValue = ''
          if (a.type === 'ENUM') {
            currentValue = a.enumLabel || a.enumValue || ''
            selectValue = a.enumValue || ''
          } else if (a.type === 'STRING') {
            currentValue = a.stringValue || ''
            selectValue = currentValue
          } else if (a.type === 'NUMBER') {
            currentValue = a.numberValue != null ? String(a.numberValue) : ''
            selectValue = currentValue
          } else if (a.type === 'BOOLEAN') {
            currentValue =
              a.booleanValue == null ? '' : a.booleanValue ? 'Tak' : 'Nie'
            selectValue = a.booleanValue == null ? '' : String(a.booleanValue)
          }
          return {
            ...a,
            currentValue,
            selectValue,
          }
        })
      }

      setEditAttributes(mergedAttributes)
      const initialAttrValues = {}
      mergedAttributes.forEach((attr) => {
        initialAttrValues[attr.key] =
          attr.type === 'ENUM' ? attr.selectValue || '' : attr.currentValue || ''
      })
      setEditAttributeValues(initialAttrValues)

      setEditListing({
        publicId: listing.publicId,
        original: data,
        title: data.title || '',
        priceAmount:
          typeof data.priceAmount === 'number'
            ? String(data.priceAmount)
            : String(data.priceAmount?.parsedValue ?? data.priceAmount?.source ?? ''),
        locationCity: data.locationCity || '',
        locationRegion: data.locationRegion || '',
        description: data.description || '',
        negotiable: !!data.negotiable,
      })
    } catch {
      alert('Błąd połączenia podczas pobierania danych do edycji')
    } finally {
      setUpdatingId(null)
    }
  }

  const handleEditChange = (e) => {
    const { name, value } = e.target
    setEditListing((prev) => (prev ? { ...prev, [name]: value } : prev))
  }

  const handleEditSave = async (e) => {
    e.preventDefault()
    if (!editListing) return

    const body = {}
    if (editListing.title.trim() !== '') body.title = editListing.title.trim()
    if (editListing.priceAmount.trim() !== '') {
      const num = Number(editListing.priceAmount.replace(',', '.'))
      if (Number.isNaN(num) || num <= 0) {
        alert('Niepoprawna cena')
        return
      }
      body.priceAmount = num
    }
    body.locationCity = editListing.locationCity.trim()
    body.locationRegion = editListing.locationRegion.trim()
    body.description = editListing.description.trim()
    body.negotiable = !!editListing.negotiable

    const attributesPayload = editAttributes.map((attr) => ({
      key: attr.key,
      value: editAttributeValues[attr.key] ?? '',
    }))

    if (attributesPayload.length > 0) {
      body.attributes = attributesPayload
    }

    if (Object.keys(body).length === 0) {
      alert('Brak zmian do zapisania')
      return
    }

    try {
      setUpdatingId(editListing.publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${editListing.publicId}/update`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(body),
      })

      if (!response.ok) {
        alert('Nie udało się zaktualizować ogłoszenia.')
        return
      }

      setItems((prev) =>
        prev.map((it) =>
          it.publicId === editListing.publicId
            ? {
                ...it,
                ...('title' in body ? { title: body.title } : {}),
                ...('priceAmount' in body ? { priceAmount: body.priceAmount } : {}),
                ...('locationCity' in body ? { locationCity: body.locationCity } : {}),
                ...('locationRegion' in body ? { locationRegion: body.locationRegion } : {}),
                ...('description' in body ? { description: body.description } : {}),
                ...('negotiable' in body ? { negotiable: body.negotiable } : {}),
              }
            : it,
        ),
      )
      setEditListing(null)
    } catch {
      alert('Błąd połączenia podczas aktualizacji ogłoszenia')
    } finally {
      setUpdatingId(null)
    }
  }

  const handleDelete = async (publicId) => {
    if (!window.confirm('Na pewno chcesz usunąć to ogłoszenie?')) return

    try {
      setDeletingId(publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${publicId}/delete`, {
        method: 'DELETE',
        credentials: 'include',
      })

      if (!response.ok) {
        alert('Nie udało się usunąć ogłoszenia.')
        return
      }

      setItems((prev) => prev.filter((it) => it.publicId !== publicId))
    } catch {
      alert('Błąd połączenia podczas usuwania ogłoszenia')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Moje ogłoszenia</h1>

        <section className="electronics-section">
          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && items.length === 0 && (
            <p style={{ color: '#fff' }}>Nie masz jeszcze żadnych ogłoszeń.</p>
          )}

          {!loading && !error && items.length > 0 && (
            <div className="items-grid listings-grid">
              {items.map((it) => {
                const statusLabel = (() => {
                  const s = statusById[it.publicId]
                  if (!s) return null
                  if (s === 'ACTIVE') return 'Aktywne'
                  if (s === 'WAITING') return 'Oczekujące'
                  if (s === 'DRAFT') return 'Robocze'
                  if (s === 'REJECTED') return 'Odrzucone'
                  if (s === 'COMPLETED') return 'Zakończone'
                  if (s === 'EXPIRED') return 'Wygasłe'
                  return s
                })()

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

                const shortDesc = it.description
                  ? it.description.length > 120
                    ? `${it.description.slice(0, 117)}...`
                    : it.description
                  : ''

                return (
                  <Link
                    key={it.publicId}
                    to={`/listing/${it.publicId}`}
                    className="item-card item-card-link"
                    style={{ textDecoration: 'none' }}
                  >
                    <div className="item-header">
                      <div>
                        <div className="item-name">{it.title}</div>
                        {statusLabel && (
                          <div className="item-meta">Status: {statusLabel}</div>
                        )}
                        {createdLabel && (
                          <div className="item-meta">Dodano: {createdLabel}</div>
                        )}
                        {locationLabel && (
                          <div className="item-location">Lokalizacja: {locationLabel}</div>
                        )}
                        {shortDesc && (
                          <p className="item-desc" style={{ marginTop: 6 }}>
                            {shortDesc}
                          </p>
                        )}
                      </div>
                      {it.coverImageUrl && (
                        <img src={it.coverImageUrl} alt={it.title} className="listing-thumb" />
                      )}
                    </div>
                    <div className="item-body">
                      <div className="item-price">{priceLabel}</div>
                      {it.negotiable && (
                        <div className="item-meta">Cena do negocjacji</div>
                      )}
                    </div>
                    <div style={{ marginTop: 8, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <button
                        type="button"
                        className="filters-button apply"
                        onClick={(e) => {
                          e.preventDefault()
                          e.stopPropagation()
                          openEdit(it)
                        }}
                        disabled={updatingId === it.publicId || deletingId === it.publicId}
                      >
                        Edytuj
                      </button>
                      <button
                        type="button"
                        className="filters-button clear"
                        onClick={(e) => {
                          e.preventDefault()
                          e.stopPropagation()
                          handleDelete(it.publicId)
                        }}
                        disabled={deletingId === it.publicId || updatingId === it.publicId}
                      >
                        {deletingId === it.publicId ? 'Usuwanie...' : 'Usuń'}
                      </button>
                    </div>
                  </Link>
                )
              })}
            </div>
          )}
          {editListing && (
            <div className="item-card" style={{ marginTop: 24 }}>
              <h2 style={{ color: '#fff', marginBottom: 12 }}>Edytuj ogłoszenie</h2>
              <form onSubmit={handleEditSave} className="add-listing-form">
                <div className="items-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                  <div className="form-group">
                    <label>
                      Tytuł
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie: {editListing.original?.title || '—'}
                      </span>
                    </label>
                    <input
                      name="title"
                      type="text"
                      value={editListing.title}
                      onChange={handleEditChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>
                      Cena (PLN)
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie:{' '}
                        {(() => {
                          const raw =
                            typeof editListing.original?.priceAmount === 'number'
                              ? editListing.original.priceAmount
                              : editListing.original?.priceAmount?.parsedValue
                                ?? Number(editListing.original?.priceAmount?.source)
                          return Number.isNaN(raw) || raw == null
                            ? '—'
                            : `${raw.toLocaleString('pl-PL', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                              })} PLN`
                        })()}
                      </span>
                    </label>
                    <input
                      name="priceAmount"
                      type="number"
                      step="0.01"
                      className="remove-arrows"
                      value={editListing.priceAmount}
                      onChange={handleEditChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>
                      Miasto
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie: {editListing.original?.locationCity || '—'}
                      </span>
                    </label>
                    <input
                      name="locationCity"
                      type="text"
                      value={editListing.locationCity}
                      onChange={handleEditChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>
                      Województwo
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie: {editListing.original?.locationRegion || '—'}
                      </span>
                    </label>
                    <input
                      name="locationRegion"
                      type="text"
                      value={editListing.locationRegion}
                      onChange={handleEditChange}
                    />
                  </div>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>
                      Cena do negocjacji
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie: {editListing.original?.negotiable ? 'Tak' : 'Nie'}
                      </span>
                    </label>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <input
                        id="edit-negotiable"
                        type="checkbox"
                        name="negotiable"
                        checked={!!editListing.negotiable}
                        onChange={(e) =>
                          setEditListing((prev) => (prev ? { ...prev, negotiable: e.target.checked } : prev))
                        }
                      />
                      <label htmlFor="edit-negotiable" style={{ margin: 0 }}>Zaznacz, jeśli cena jest do negocjacji</label>
                    </div>
                  </div>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>
                      Opis
                      <span style={{ display: 'block', fontSize: 12, color: '#6b7280' }}>
                        Aktualnie: {editListing.original?.description || '—'}
                      </span>
                    </label>
                    <textarea
                      name="description"
                      rows={3}
                      value={editListing.description}
                      onChange={handleEditChange}
                    />
                  </div>
                  {editAttributes.length > 0 && (
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                      <label style={{ display: 'block', marginBottom: 8, color: '#e5e7eb' }}>
                        Dodatkowe parametry
                      </label>
                      <div className="filters-grid">
                        {editAttributes
                          .slice()
                          .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
                          .map((attr) => (
                            <div key={attr.id || attr.key} className="filter-item">
                              <label style={{ color: '#fff', display: 'block', marginBottom: 2 }}>
                                {attr.label || attr.key}
                              </label>
                              <span
                                style={{ display: 'block', fontSize: 11, color: '#9ca3af', marginBottom: 4 }}
                              >
                                Aktualnie: {attr.currentValue || '—'}
                              </span>
                              {attr.type === 'ENUM' ? (
                                <select
                                  value={editAttributeValues[attr.key] ?? ''}
                                  onChange={(e) =>
                                    setEditAttributeValues((prev) => ({
                                      ...prev,
                                      [attr.key]: e.target.value,
                                    }))
                                  }
                                >
                                  <option value="">Wybierz</option>
                                  {attr.options
                                    ?.slice()
                                    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
                                    .map((opt) => (
                                      <option key={opt.id} value={opt.value}>
                                        {opt.label}
                                      </option>
                                    ))}
                                </select>
                              ) : (
                                <input
                                  type="text"
                                  value={editAttributeValues[attr.key] ?? ''}
                                  onChange={(e) =>
                                    setEditAttributeValues((prev) => ({
                                      ...prev,
                                      [attr.key]: e.target.value,
                                    }))
                                  }
                                />
                              )}
                            </div>
                          ))}
                      </div>
                    </div>
                  )}
                </div>
                <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                  <button
                    type="submit"
                    className="filters-button apply"
                    disabled={updatingId === editListing.publicId}
                  >
                    {updatingId === editListing.publicId ? 'Zapisywanie...' : 'Zapisz zmiany'}
                  </button>
                  <button
                    type="button"
                    className="filters-button clear"
                    onClick={() => setEditListing(null)}
                    disabled={updatingId === editListing.publicId}
                  >
                    Anuluj
                  </button>
                </div>
              </form>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
