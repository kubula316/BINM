import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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

  useEffect(() => {
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

      const prevStatus = statusById[editListing.publicId] || items.find((i) => i.publicId === editListing.publicId)?.status
      if (prevStatus === 'WAITING') {
        await fetch(`${API_BASE_URL}/user/listing/${editListing.publicId}/submit-for-approval`, {
          method: 'POST',
          credentials: 'include',
        })
      }

      await fetchMyListings()
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

  const getStatusBadge = (status) => {
    const styles = {
      ACTIVE: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      WAITING: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
      DRAFT: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
      REJECTED: 'bg-red-500/20 text-red-400 border-red-500/30',
      COMPLETED: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      EXPIRED: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
    }
    const labels = { ACTIVE: 'Aktywne', WAITING: 'Oczekujace', DRAFT: 'Robocze', REJECTED: 'Odrzucone', COMPLETED: 'Zakonczone', EXPIRED: 'Wygasle' }
    return (
      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${styles[status] || styles.DRAFT}`}>
        {labels[status] || status}
      </span>
    )
  }

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white">Moje ogloszenia</h1>
            <p className="text-slate-400 mt-1">Zarzadzaj swoimi ogloszeniami</p>
          </div>
          <Link to="/" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Ladowanie...</span>
          </div>
        )}
        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        {!loading && !error && items.length === 0 && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-12 text-center">
            <svg className="w-16 h-16 text-slate-600 mx-auto mb-4" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" /></svg>
            <p className="text-slate-400 mb-4">Nie masz jeszcze zadnych ogloszen.</p>
            <Link to="/add-listing" className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500">
              <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 4v16m8-8H4" /></svg>
              Dodaj pierwsze ogloszenie
            </Link>
          </div>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-1">
            {items.map((it) => {
              const status = statusById[it.publicId] || it.status
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

              return (
                <div key={it.publicId} className="flex flex-col gap-4">
                  <div className={`rounded-xl border bg-slate-800/50 p-4 transition-all ${isActive ? 'border-slate-700/50 hover:border-emerald-500/30' : 'border-slate-700/30 opacity-80'}`}>
                  {isActive ? (
                    <Link to={`/listing/${it.publicId}`} className="block">
                      <div className="flex gap-4">
                        {it.coverImageUrl ? (
                          <img src={it.coverImageUrl} alt={it.title} className="h-24 w-28 flex-none rounded-lg object-cover" />
                        ) : (
                          <div className="h-24 w-28 flex-none rounded-lg bg-slate-700/50 flex items-center justify-center">
                            <svg className="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                          </div>
                        )}
                        <div className="min-w-0 flex-1">
                          <div className="truncate font-medium text-white">{it.title}</div>
                          {status && <div className="mt-1">{getStatusBadge(status)}</div>}
                          {createdLabel && <div className="text-xs text-slate-500 mt-1">Dodano: {createdLabel}</div>}
                          {locationLabel && <div className="text-xs text-slate-500">{locationLabel}</div>}
                          <div className="mt-2 text-lg font-bold text-emerald-400">{priceLabel}</div>
                        </div>
                      </div>
                    </Link>
                  ) : (
                    <div className="flex gap-4">
                      {it.coverImageUrl ? (
                        <img src={it.coverImageUrl} alt={it.title} className="h-24 w-28 flex-none rounded-lg object-cover grayscale" />
                      ) : (
                        <div className="h-24 w-28 flex-none rounded-lg bg-slate-700/50 flex items-center justify-center">
                          <svg className="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                        </div>
                      )}
                      <div className="min-w-0 flex-1">
                        <div className="truncate font-medium text-slate-300">{it.title}</div>
                        {status && <div className="mt-1">{getStatusBadge(status)}</div>}
                        {createdLabel && <div className="text-xs text-slate-500 mt-1">Dodano: {createdLabel}</div>}
                        {locationLabel && <div className="text-xs text-slate-500">{locationLabel}</div>}
                        <div className="mt-2 text-lg font-bold text-slate-400">{priceLabel}</div>
                      </div>
                    </div>
                  )}
                  <div className="mt-3 pt-3 border-t border-slate-700/50 flex flex-wrap gap-2">
                    <button type="button" className="inline-flex items-center justify-center rounded-lg border border-slate-700/50 bg-slate-800/50 px-3 py-1.5 text-xs font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white" onClick={(e) => { e.preventDefault(); openEdit(it) }} disabled={updatingId === it.publicId}>
                      <svg className="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
                      Edytuj
                    </button>
                    {canSubmit && (
                      <button type="button" className="inline-flex items-center justify-center rounded-lg bg-emerald-600/20 border border-emerald-500/30 px-3 py-1.5 text-xs font-medium text-emerald-400 transition-all hover:bg-emerald-600/30" onClick={() => handleSubmitForApproval(it.publicId)} disabled={submittingId === it.publicId}>
                        {submittingId === it.publicId ? 'Wysylanie...' : 'Wyslij do akceptacji'}
                      </button>
                    )}
                    <button type="button" className="inline-flex items-center justify-center rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-1.5 text-xs font-medium text-red-400 transition-all hover:bg-red-500/20" onClick={() => handleDelete(it.publicId)} disabled={deletingId === it.publicId}>
                      <svg className="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                      {deletingId === it.publicId ? 'Usuwanie...' : 'Usun'}
                    </button>
                  </div>
                </div>

                  {editListing && editListing.publicId === it.publicId && (
                    <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
                      <h2 className="text-lg font-semibold text-white mb-6 flex items-center gap-2">
                        <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
                        Edytuj ogloszenie
                      </h2>
                      <form onSubmit={handleEditSave} className="space-y-5">
                        <div className="grid gap-5 sm:grid-cols-2">
                          <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Tytul <span className="text-xs text-slate-500">(Aktualnie: {editListing.original?.title || '-'})</span></label>
                            <input name="title" type="text" className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" value={editListing.title} onChange={handleEditChange} />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Cena (PLN) <span className="text-xs text-slate-500">(Aktualnie: {editListing.original?.priceAmount || '-'})</span></label>
                            <div className="flex items-center gap-4">
                              <input name="priceAmount" type="number" step="0.01" className="h-11 flex-1 rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" value={editListing.priceAmount} onChange={handleEditChange} />
                              <label className="relative inline-flex items-center gap-2 cursor-pointer select-none flex-none">
                                <input type="checkbox" name="negotiable" checked={!!editListing.negotiable} onChange={(e) => setEditListing((prev) => (prev ? { ...prev, negotiable: e.target.checked } : prev))} className="sr-only peer" />
                                <div className="w-11 h-6 bg-slate-700 rounded-full peer peer-checked:bg-emerald-600 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-5"></div>
                                <span className="text-sm text-slate-400">Do negocjacji</span>
                              </label>
                            </div>
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Miasto</label>
                            <input name="locationCity" type="text" className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" value={editListing.locationCity} onChange={handleEditChange} />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Wojewodztwo</label>
                            <input name="locationRegion" type="text" className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" value={editListing.locationRegion} onChange={handleEditChange} />
                          </div>
                          <div className="sm:col-span-2">
                            <label className="block text-sm font-medium text-slate-300 mb-2">Opis</label>
                            <textarea name="description" rows={3} className="w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 py-3 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20 resize-none" value={editListing.description} onChange={handleEditChange} />
                          </div>
                          {editAttributes.length > 0 && (
                            <div className="sm:col-span-2">
                              <label className="block text-sm font-medium text-slate-300 mb-3">Dodatkowe parametry</label>
                              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                                {editAttributes.slice().sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)).map((attr) => (
                                  <div key={attr.id || attr.key}>
                                    <label className="block text-xs text-slate-400 mb-1.5">{attr.label || attr.key} <span className="text-slate-500">(Aktualnie: {attr.currentValue || '-'})</span></label>
                                    {attr.type === 'ENUM' ? (
                                      <select className="h-10 w-full rounded-lg border border-slate-700/50 bg-slate-900/50 px-3 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50" value={editAttributeValues[attr.key] ?? ''} onChange={(e) => setEditAttributeValues((prev) => ({ ...prev, [attr.key]: e.target.value }))}>
                                        <option value="">Wybierz</option>
                                        {attr.options?.slice().sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)).map((opt) => (
                                          <option key={opt.id} value={opt.value}>{opt.label}</option>
                                        ))}
                                      </select>
                                    ) : (
                                      <input type="text" className="h-10 w-full rounded-lg border border-slate-700/50 bg-slate-900/50 px-3 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50" value={editAttributeValues[attr.key] ?? ''} onChange={(e) => setEditAttributeValues((prev) => ({ ...prev, [attr.key]: e.target.value }))} />
                                    )}
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                        <div className="flex gap-3">
                          <button type="submit" className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" disabled={updatingId === editListing.publicId}>{updatingId === editListing.publicId ? 'Zapisywanie...' : 'Zapisz zmiany'}</button>
                          <button type="button" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-6 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white" onClick={() => setEditListing(null)} disabled={updatingId === editListing.publicId}>Anuluj</button>
                        </div>
                      </form>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
