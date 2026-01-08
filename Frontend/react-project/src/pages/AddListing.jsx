import { useEffect, useRef, useState } from 'react'

const API_BASE_URL = 'http://localhost:8081'

export default function AddListing({ username }) {
  const [form, setForm] = useState({
    parentCategoryId: '',
    categoryId: '',
    title: '',
    priceAmount: '',
    negotiable: false,
    locationCity: '',
    locationRegion: '',
    contactPhoneNumber: '',
    description: '',
  })
  const [categories, setCategories] = useState([])
  const [loadingCategories, setLoadingCategories] = useState(true)
  const [categoriesError, setCategoriesError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submittedListing, setSubmittedListing] = useState(null)
  const [attributes, setAttributes] = useState([])
  const [attributeValues, setAttributeValues] = useState({})
  const [uploadedImageUrls, setUploadedImageUrls] = useState([])
  const [uploadingCount, setUploadingCount] = useState(0)
  const [uploadError, setUploadError] = useState('')
  const [errors, setErrors] = useState({})
  const fileInputRef = useRef(null)

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoadingCategories(true)
        setCategoriesError('')
        const response = await fetch(`${API_BASE_URL}/public/category/all`)
        if (!response.ok) { setCategoriesError('Blad pobierania kategorii'); return }
        const data = await response.json()
        setCategories(Array.isArray(data) ? data : [])
      } catch { setCategoriesError('Brak polaczenia z serwerem') }
      finally { setLoadingCategories(false) }
    }
    fetchCategories()
  }, [])

  const onChange = (e) => {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  const topLevelCategories = categories.filter((c) => c.parentId === null)
  const parentCategory = form.parentCategoryId ? categories.find((c) => c.id === Number(form.parentCategoryId)) : null
  const subcategories = parentCategory?.children || []

  const handleParentCategoryChange = (e) => {
    setForm((prev) => ({ ...prev, parentCategoryId: e.target.value, categoryId: '' }))
    setAttributes([])
    setAttributeValues({})
  }

  const handleSubcategoryChange = async (e) => {
    const subId = e.target.value
    setForm((prev) => ({ ...prev, categoryId: subId }))
    setAttributes([])
    setAttributeValues({})
    const numericId = Number(subId)
    if (!subId || Number.isNaN(numericId)) return
    try {
      const response = await fetch(`${API_BASE_URL}/public/category/attributes?categoryId=${numericId}`)
      if (!response.ok) return
      const data = await response.json()
      const attrs = Array.isArray(data) ? data : []
      setAttributes(attrs)
      const initialValues = {}
      attrs.forEach((attr) => { initialValues[attr.key] = '' })
      setAttributeValues(initialValues)
    } catch { /* ignore */ }
  }

  const handleAttributeChange = (key, value) => setAttributeValues((prev) => ({ ...prev, [key]: value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    const newErrors = {}
    if (!form.categoryId) newErrors.categoryId = true
    if (!form.title.trim()) newErrors.title = true
    if (!form.priceAmount) newErrors.priceAmount = true
    setErrors(newErrors)
    if (Object.keys(newErrors).length > 0) return
    setUploadError('')
    if (uploadingCount > 0) { alert('Poczekaj az zdjecia zostana wyslane.'); return }

    const attributesPayload = Object.entries(attributeValues).filter(([, v]) => v !== '').map(([key, value]) => ({ key, value }))
    const body = {
      categoryId: Number(form.categoryId),
      title: form.title,
      description: form.description || undefined,
      priceAmount: Number(form.priceAmount),
      negotiable: form.negotiable,
      locationCity: form.locationCity || undefined,
      locationRegion: form.locationRegion || undefined,
      contactPhoneNumber: form.contactPhoneNumber || undefined,
      mediaUrls: uploadedImageUrls,
      attributes: attributesPayload,
    }

    try {
      setSubmitting(true)
      const response = await fetch(`${API_BASE_URL}/user/listing/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body),
      })
      if (!response.ok) { alert('Blad tworzenia ogloszenia.'); return }
      const data = await response.json().catch(() => null)
      setSubmittedListing({ title: form.title, priceAmount: form.priceAmount, locationCity: form.locationCity, locationRegion: form.locationRegion, description: form.description, sellerName: username || 'Uzytkownik', ...(data || {}) })
    } catch { alert('Brak polaczenia z serwerem') }
    finally { setSubmitting(false) }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1">Dodaj przedmiot</h1>

        <form onSubmit={onSubmit} className="ui-section space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Kategoria glowna</label>
              {loadingCategories ? <p className="ui-muted">Ladowanie...</p> : categoriesError ? <p className="text-red-400">{categoriesError}</p> : (
                <select className="ui-input w-full" name="parentCategoryId" value={form.parentCategoryId} onChange={handleParentCategoryChange}>
                  <option value="">Wybierz kategorie</option>
                  {topLevelCategories.map((cat) => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
                </select>
              )}
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Podkategoria <span className="text-red-400">*</span></label>
              <select className={`ui-input w-full ${errors.categoryId ? 'border-red-500' : ''}`} name="categoryId" value={form.categoryId} onChange={handleSubcategoryChange} disabled={!parentCategory || !subcategories.length}>
                <option value="">{parentCategory ? 'Wybierz podkategorie' : 'Najpierw wybierz kategorie'}</option>
                {subcategories.map((sub) => <option key={sub.id} value={sub.id}>{sub.name}</option>)}
              </select>
              {errors.categoryId && <p className="text-red-400 text-xs mt-1">Wybierz podkategorie</p>}
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Tytul <span className="text-red-400">*</span></label>
              <input className={`ui-input w-full ${errors.title ? 'border-red-500' : ''}`} name="title" type="text" value={form.title} onChange={onChange} placeholder="np. Nowe Audi A4" />
              {errors.title && <p className="text-red-400 text-xs mt-1">Wpisz tytul</p>}
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Cena (PLN) <span className="text-red-400">*</span></label>
              <div className="flex items-center gap-4">
                <input className={`ui-input flex-1 ${errors.priceAmount ? 'border-red-500' : ''}`} name="priceAmount" type="number" step="0.01" value={form.priceAmount} onChange={onChange} placeholder="np. 50000" />
                <label className="relative inline-flex items-center gap-2 cursor-pointer select-none flex-none">
                  <input type="checkbox" name="negotiable" checked={form.negotiable} onChange={onChange} className="sr-only peer" />
                  <div className="w-10 h-5 bg-zinc-700 rounded-full peer peer-checked:bg-emerald-600 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:after:translate-x-5"></div>
                  <span className="text-xs text-zinc-400">Do negocjacji</span>
                </label>
              </div>
              {errors.priceAmount && <p className="text-red-400 text-xs mt-1">Wpisz cene</p>}
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Miasto</label>
              <input className="ui-input w-full" name="locationCity" type="text" value={form.locationCity} onChange={onChange} placeholder="np. Gdansk" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Wojewodztwo</label>
              <input className="ui-input w-full" name="locationRegion" type="text" value={form.locationRegion} onChange={onChange} placeholder="np. Pomorskie" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Numer telefonu</label>
              <input className="ui-input w-full" name="contactPhoneNumber" type="tel" value={form.contactPhoneNumber} onChange={onChange} placeholder="np. 500600700" />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-sm text-zinc-300 mb-1">Opis</label>
              <textarea className="ui-input w-full" name="description" value={form.description} onChange={onChange} rows={4} placeholder="Krotki opis" />
            </div>
            {attributes.length > 0 && (
              <div className="sm:col-span-2">
                <label className="block text-sm text-zinc-300 mb-2">Dodatkowe parametry</label>
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                  {attributes.slice().sort((a, b) => a.sortOrder - b.sortOrder).map((attr) => (
                    <div key={attr.id}>
                      <label className="block text-xs text-zinc-400 mb-1">{attr.label}</label>
                      {attr.type === 'ENUM' ? (
                        <select className="ui-input w-full" value={attributeValues[attr.key] ?? ''} onChange={(e) => handleAttributeChange(attr.key, e.target.value)}>
                          <option value="">Wybierz</option>
                          {attr.options?.slice().sort((a, b) => a.sortOrder - b.sortOrder).map((opt) => <option key={opt.id} value={opt.value}>{opt.label}</option>)}
                        </select>
                      ) : (
                        <input className="ui-input w-full" type="text" value={attributeValues[attr.key] ?? ''} onChange={(e) => handleAttributeChange(attr.key, e.target.value)} />
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="sm:col-span-2">
              <label className="block text-sm text-zinc-300 mb-1">Zdjecia</label>
              <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden" onChange={(e) => {
                setUploadError('')
                const files = e.target.files ? Array.from(e.target.files) : []
                if (!files.length) return
                ;(async () => {
                  setUploadingCount((c) => c + files.length)
                  try {
                    for (const file of files) {
                      const formData = new FormData()
                      formData.append('file', file)
                      const uploadResponse = await fetch(`${API_BASE_URL}/user/upload/media-image`, { method: 'POST', body: formData, credentials: 'include' })
                      if (!uploadResponse.ok) { setUploadError('Blad uploadu zdjecia.'); continue }
                      const url = await uploadResponse.text()
                      if (url) setUploadedImageUrls((prev) => [...prev, url])
                    }
                  } catch { setUploadError('Blad polaczenia.') }
                  finally { setUploadingCount((c) => Math.max(0, c - files.length)); e.target.value = '' }
                })()
              }} />
              <button type="button" onClick={() => fileInputRef.current?.click()} className="ui-btn inline-flex items-center gap-2" disabled={uploadingCount > 0}>
                <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M12 4v16m8-8H4" />
                </svg>
                Dodaj zdjecia
              </button>
              {uploadError && <p className="text-red-400 text-sm mt-1">{uploadError}</p>}
              {uploadingCount > 0 && <p className="text-zinc-400 text-sm mt-1">Wysylanie... ({uploadingCount})</p>}
              {uploadedImageUrls.length > 0 && (
                <div className="mt-2">
                  <p className="text-zinc-400 text-sm mb-2">Dodane zdjecia: {uploadedImageUrls.length}</p>
                  <div className="flex flex-wrap gap-2">
                    {uploadedImageUrls.map((url, idx) => (
                      <div key={idx} className="relative">
                        <img src={url} alt="" className="h-20 w-20 rounded-lg object-cover" />
                        <button type="button" className="absolute top-1 right-1 rounded bg-zinc-800/80 px-1.5 py-0.5 text-xs text-red-400" onClick={() => setUploadedImageUrls((prev) => prev.filter((_, i) => i !== idx))} disabled={uploadingCount > 0}>X</button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
          <button type="submit" className="ui-btn-primary" disabled={submitting}>{submitting ? 'Zapisywanie...' : 'Zapisz ogloszenie'}</button>
        </form>

        {submittedListing && (
          <div className="ui-section">
            <h2 className="ui-h2 mb-2">Utworzono ogloszenie</h2>
            <div className="text-zinc-100 font-medium">{submittedListing.title}</div>
            <div className="text-emerald-400 font-semibold">{Number(submittedListing.priceAmount).toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN</div>
            <div className="text-sm text-zinc-400">{submittedListing.locationCity}{submittedListing.locationRegion ? `, ${submittedListing.locationRegion}` : ''}</div>
          </div>
        )}
      </div>
    </div>
  )
}
