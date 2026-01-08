import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function MyListings() {
  const [items, setItems] = useState([])
  const [statusById, setStatusById] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [submittingId, setSubmittingId] = useState(null)
  const [editListing, setEditListing] = useState(null)
  const [editAttributes, setEditAttributes] = useState([])
  const [editAttributeValues, setEditAttributeValues] = useState({})

  useEffect(() => {
    const fetchMyListings = async () => {
      try {
        setLoading(true)
        setError('')
        const response = await fetch(`${API_BASE_URL}/user/listing/my?page=0&size=20`, { credentials: 'include' })

        if (!response.ok) {
          setError(response.status === 401 ? 'Musisz byc zalogowany.' : 'Nie udalo sie pobrac ogloszen.')
          return
        }

        const data = await response.json()
        const list = Array.isArray(data.content) ? data.content : []
        setItems(list)

        const ids = new Set(list.map((x) => x.publicId))
        const STATUSES = ['ACTIVE', 'WAITING', 'DRAFT', 'REJECTED', 'COMPLETED', 'EXPIRED']

        const results = await Promise.allSettled(
          STATUSES.map(async (status) => {
            const res = await fetch(`${API_BASE_URL}/user/listing/my?page=0&size=200&status=${status}`, { credentials: 'include' })
            if (!res.ok) return { status, ids: [] }
            const page = await res.json()
            const content = Array.isArray(page.content) ? page.content : []
            return { status, ids: content.map((it) => it.publicId).filter((id) => ids.has(id)) }
          }),
        )

        const map = {}
        results.forEach((r) => {
          if (r.status !== 'fulfilled') return
          r.value.ids.forEach((id) => { map[id] = r.value.status })
        })
        setStatusById(map)
      } catch {
        setError('Brak polaczenia z serwerem')
      } finally {
        setLoading(false)
      }
    }
    fetchMyListings()
  }, [])

  const openEdit = async (listing) => {
    try {
      setUpdatingId(listing.publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${listing.publicId}/edit-data`, { credentials: 'include' })
      if (!response.ok) { alert('Nie udalo sie pobrac danych do edycji.'); return }
      const data = await response.json()

      const currentAttrMap = new Map()
      if (Array.isArray(data.attributes)) {
        data.attributes.forEach((a) => currentAttrMap.set(a.key, a))
      }

      let mergedAttributes = []
      if (data.categoryId) {
        try {
          const attrDefResp = await fetch(`${API_BASE_URL}/public/category/attributes?categoryId=${data.categoryId}`)
          if (attrDefResp.ok) {
            const defs = await attrDefResp.json()
            if (Array.isArray(defs)) {
              mergedAttributes = defs.map((def) => {
                const current = currentAttrMap.get(def.key)
                let currentValue = '', selectValue = ''
                if (current) {
                  if (current.type === 'ENUM') { currentValue = current.enumLabel || current.enumValue || ''; selectValue = current.enumValue || '' }
                  else if (current.type === 'STRING') { currentValue = current.stringValue || ''; selectValue = currentValue }
                  else if (current.type === 'NUMBER') { currentValue = current.numberValue != null ? String(current.numberValue) : ''; selectValue = currentValue }
                  else if (current.type === 'BOOLEAN') { currentValue = current.booleanValue == null ? '' : current.booleanValue ? 'Tak' : 'Nie'; selectValue = current.booleanValue == null ? '' : String(current.booleanValue) }
                }
                return { ...def, currentValue, selectValue }
              })
            }
          }
        } catch { /* ignore */ }
      }

      if (!mergedAttributes.length && Array.isArray(data.attributes)) {
        mergedAttributes = data.attributes.map((a) => {
          let currentValue = '', selectValue = ''
          if (a.type === 'ENUM') { currentValue = a.enumLabel || a.enumValue || ''; selectValue = a.enumValue || '' }
          else if (a.type === 'STRING') { currentValue = a.stringValue || ''; selectValue = currentValue }
          else if (a.type === 'NUMBER') { currentValue = a.numberValue != null ? String(a.numberValue) : ''; selectValue = currentValue }
          else if (a.type === 'BOOLEAN') { currentValue = a.booleanValue == null ? '' : a.booleanValue ? 'Tak' : 'Nie'; selectValue = a.booleanValue == null ? '' : String(a.booleanValue) }
          return { ...a, currentValue, selectValue }
        })
      }

      setEditAttributes(mergedAttributes)
      const initialAttrValues = {}
      mergedAttributes.forEach((attr) => { initialAttrValues[attr.key] = attr.type === 'ENUM' ? attr.selectValue || '' : attr.currentValue || '' })
      setEditAttributeValues(initialAttrValues)

      setEditListing({
        publicId: listing.publicId,
        original: data,
        title: data.title || '',
        priceAmount: typeof data.priceAmount === 'number' ? String(data.priceAmount) : String(data.priceAmount?.parsedValue ?? data.priceAmount?.source ?? ''),
        locationCity: data.locationCity || '',
        locationRegion: data.locationRegion || '',
        description: data.description || '',
        negotiable: !!data.negotiable,
      })
    } catch {
      alert('Blad polaczenia')
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
    const titleStr = String(editListing.title || '').trim()
    if (titleStr !== '') body.title = titleStr
    const priceStr = String(editListing.priceAmount || '').trim()
    if (priceStr !== '') {
      const num = Number(priceStr.replace(',', '.'))
      if (Number.isNaN(num) || num <= 0) { alert('Niepoprawna cena'); return }
      body.priceAmount = num
    }
    body.locationCity = String(editListing.locationCity || '').trim()
    body.locationRegion = String(editListing.locationRegion || '').trim()
    body.description = String(editListing.description || '').trim()
    body.negotiable = !!editListing.negotiable

    const attributesPayload = editAttributes
      .map((attr) => ({ key: attr.key, value: editAttributeValues[attr.key] ?? '' }))
      .filter((a) => a.value !== '')
    if (attributesPayload.length > 0) body.attributes = attributesPayload

    try {
      setUpdatingId(editListing.publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${editListing.publicId}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body),
      })
      if (!response.ok) {
        const errText = await response.text().catch(() => '')
        console.error('Update error:', response.status, errText)
        alert(`Nie udalo sie zaktualizowac ogloszenia. ${errText || ''}`)
        return
      }

      // Jeśli status był WAITING, automatycznie wyślij do akceptacji
      const prevStatus = statusById[editListing.publicId]
      if (prevStatus === 'WAITING') {
        await fetch(`${API_BASE_URL}/user/listing/${editListing.publicId}/submit-for-approval`, {
          method: 'POST',
          credentials: 'include',
        })
      }

      setItems((prev) => prev.map((it) => it.publicId === editListing.publicId ? { ...it, title: body.title || it.title, priceAmount: body.priceAmount || it.priceAmount } : it))
      setEditListing(null)
    } catch {
      alert('Blad polaczenia')
    } finally {
      setUpdatingId(null)
    }
  }

  const handleDelete = async (publicId) => {
    if (!window.confirm('Na pewno chcesz usunac to ogloszenie?')) return
    try {
      setDeletingId(publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${publicId}/delete`, { method: 'DELETE', credentials: 'include' })
      if (!response.ok) { alert('Nie udalo sie usunac ogloszenia.'); return }
      setItems((prev) => prev.filter((it) => it.publicId !== publicId))
    } catch {
      alert('Blad polaczenia')
    } finally {
      setDeletingId(null)
    }
  }

  const handleSubmitForApproval = async (publicId) => {
    try {
      setSubmittingId(publicId)
      const response = await fetch(`${API_BASE_URL}/user/listing/${publicId}/submit-for-approval`, { method: 'POST', credentials: 'include' })
      if (!response.ok) { alert('Nie udalo sie wyslac do akceptacji.'); return }
      setStatusById((prev) => ({ ...prev, [publicId]: 'WAITING' }))
    } catch {
      alert('Blad polaczenia')
    } finally {
      setSubmittingId(null)
    }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Moje ogloszenia</h1>
        <div className="text-center">
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        {loading && <p className="ui-muted">Ladowanie...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && items.length === 0 && (
          <p className="ui-muted">Nie masz jeszcze zadnych ogloszen.</p>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((it) => {
              const status = statusById[it.publicId]
              const statusLabel = (() => {
                if (!status) return null
                if (status === 'ACTIVE') return 'Aktywne'
                if (status === 'WAITING') return 'Oczekujace'
                if (status === 'DRAFT') return 'Robocze'
                if (status === 'REJECTED') return 'Odrzucone'
                if (status === 'COMPLETED') return 'Zakonczone'
                if (status === 'EXPIRED') return 'Wygasle'
                return status
              })()

              const priceLabel = (() => {
                if (!it.priceAmount) return 'Brak ceny'
                const raw = typeof it.priceAmount === 'number' ? it.priceAmount : it.priceAmount.parsedValue ?? Number(it.priceAmount.source)
                if (Number.isNaN(raw) || raw == null) return 'Brak ceny'
                return `${raw.toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN`
              })()

              const createdLabel = it.createdAt ? new Date(it.createdAt).toLocaleDateString('pl-PL') : null
              const locationLabel = [it.locationCity, it.locationRegion].filter(Boolean).join(', ')
              const canSubmit = status === 'DRAFT' || status === 'REJECTED'
              const isActive = status === 'ACTIVE'

              const cardContent = (
                <div className="flex gap-3">
                  {it.coverImageUrl && (
                    <img src={it.coverImageUrl} alt={it.title} className="h-20 w-24 flex-none rounded-lg object-cover" />
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-medium text-zinc-100">{it.title}</div>
                    {statusLabel && <div className="text-xs text-zinc-400">Status: {statusLabel}</div>}
                    {createdLabel && <div className="text-xs text-zinc-500">Dodano: {createdLabel}</div>}
                    {locationLabel && <div className="text-xs text-zinc-500">{locationLabel}</div>}
                    <div className="mt-2 font-semibold text-emerald-400">{priceLabel}</div>
                  </div>
                </div>
              )

              return (
                <div key={it.publicId} className={`rounded-xl border bg-zinc-800 p-3 transition-all ${isActive ? 'border-zinc-700 hover:border-emerald-500/50 hover:bg-zinc-750' : 'border-zinc-700/50 opacity-75'}`}>
                  {isActive ? (
                    <Link to={`/listing/${it.publicId}`} className="block">{cardContent}</Link>
                  ) : (
                    <div className="cursor-not-allowed">{cardContent}</div>
                  )}
                  <div className="mt-2 flex flex-wrap gap-2">
                    <button type="button" className="ui-btn text-xs" onClick={(e) => { e.preventDefault(); openEdit(it) }} disabled={updatingId === it.publicId}>Edytuj</button>
                    {canSubmit && (
                      <button type="button" className="ui-btn-primary text-xs" onClick={() => handleSubmitForApproval(it.publicId)} disabled={submittingId === it.publicId}>
                        {submittingId === it.publicId ? 'Wysylanie...' : 'Wyslij do akceptacji'}
                      </button>
                    )}
                    <button type="button" className="ui-btn text-xs text-red-400 border-red-400/30 hover:bg-red-500/10" onClick={() => handleDelete(it.publicId)} disabled={deletingId === it.publicId}>
                      {deletingId === it.publicId ? 'Usuwanie...' : 'Usun'}
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}

        {editListing && (
          <div className="ui-section">
            <h2 className="ui-h2 mb-4">Edytuj ogloszenie</h2>
            <form onSubmit={handleEditSave} className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Tytul <span className="text-xs text-zinc-500">(Aktualnie: {editListing.original?.title || '-'})</span></label>
                  <input name="title" type="text" className="ui-input w-full" value={editListing.title} onChange={handleEditChange} />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Cena (PLN) <span className="text-xs text-zinc-500">(Aktualnie: {editListing.original?.priceAmount || '-'})</span></label>
                  <div className="flex items-center gap-4">
                    <input name="priceAmount" type="number" step="0.01" className="ui-input flex-1" value={editListing.priceAmount} onChange={handleEditChange} />
                    <label className="relative inline-flex items-center gap-2 cursor-pointer select-none flex-none">
                      <input type="checkbox" name="negotiable" checked={!!editListing.negotiable} onChange={(e) => setEditListing((prev) => (prev ? { ...prev, negotiable: e.target.checked } : prev))} className="sr-only peer" />
                      <div className="w-10 h-5 bg-zinc-700 rounded-full peer peer-checked:bg-emerald-600 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:after:translate-x-5"></div>
                      <span className="text-xs text-zinc-400">Do negocjacji</span>
                    </label>
                  </div>
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Miasto <span className="text-xs text-zinc-500">(Aktualnie: {editListing.original?.locationCity || '-'})</span></label>
                  <input name="locationCity" type="text" className="ui-input w-full" value={editListing.locationCity} onChange={handleEditChange} />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Wojewodztwo <span className="text-xs text-zinc-500">(Aktualnie: {editListing.original?.locationRegion || '-'})</span></label>
                  <input name="locationRegion" type="text" className="ui-input w-full" value={editListing.locationRegion} onChange={handleEditChange} />
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-sm text-zinc-300 mb-1">Opis</label>
                  <textarea name="description" rows={3} className="ui-input w-full" value={editListing.description} onChange={handleEditChange} />
                </div>
                {editAttributes.length > 0 && (
                  <div className="sm:col-span-2">
                    <label className="block text-sm text-zinc-300 mb-2">Dodatkowe parametry</label>
                    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                      {editAttributes.slice().sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)).map((attr) => (
                        <div key={attr.id || attr.key}>
                          <label className="block text-xs text-zinc-400 mb-1">{attr.label || attr.key} <span className="text-zinc-500">(Aktualnie: {attr.currentValue || '-'})</span></label>
                          {attr.type === 'ENUM' ? (
                            <select className="ui-input w-full" value={editAttributeValues[attr.key] ?? ''} onChange={(e) => setEditAttributeValues((prev) => ({ ...prev, [attr.key]: e.target.value }))}>
                              <option value="">Wybierz</option>
                              {attr.options?.slice().sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)).map((opt) => (
                                <option key={opt.id} value={opt.value}>{opt.label}</option>
                              ))}
                            </select>
                          ) : (
                            <input type="text" className="ui-input w-full" value={editAttributeValues[attr.key] ?? ''} onChange={(e) => setEditAttributeValues((prev) => ({ ...prev, [attr.key]: e.target.value }))} />
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
              <div className="flex gap-2">
                <button type="submit" className="ui-btn-primary" disabled={updatingId === editListing.publicId}>{updatingId === editListing.publicId ? 'Zapisywanie...' : 'Zapisz zmiany'}</button>
                <button type="button" className="ui-btn" onClick={() => setEditListing(null)} disabled={updatingId === editListing.publicId}>Anuluj</button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
