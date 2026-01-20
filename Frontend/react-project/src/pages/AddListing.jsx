import { useEffect, useRef, useState } from 'react'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-4xl px-4 sm:px-6 space-y-6">
        <div className="text-center">
          <h1 className="text-2xl sm:text-3xl font-bold text-white">Dodaj ogloszenie</h1>
          <p className="text-slate-400 mt-1">Wypelnij formularz i dodaj swoje ogloszenie</p>
        </div>

        <form onSubmit={onSubmit} className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8 space-y-6">
          <div className="grid gap-5 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Kategoria glowna</label>
              {loadingCategories ? (
                <div className="flex items-center gap-2 text-slate-400">
                  <div className="w-4 h-4 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
                  Ladowanie...
                </div>
              ) : categoriesError ? (
                <div className="text-red-400 text-sm">{categoriesError}</div>
              ) : (
                <select className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" name="parentCategoryId" value={form.parentCategoryId} onChange={handleParentCategoryChange}>
                  <option value="">Wybierz kategorie</option>
                  {topLevelCategories.map((cat) => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
                </select>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Podkategoria <span className="text-red-400">*</span></label>
              <select className={`h-11 w-full rounded-xl border bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:ring-2 focus:ring-emerald-500/20 ${errors.categoryId ? 'border-red-500' : 'border-slate-700/50 focus:border-emerald-500/50'}`} name="categoryId" value={form.categoryId} onChange={handleSubcategoryChange} disabled={!parentCategory || !subcategories.length}>
                <option value="">{parentCategory ? 'Wybierz podkategorie' : 'Najpierw wybierz kategorie'}</option>
                {subcategories.map((sub) => <option key={sub.id} value={sub.id}>{sub.name}</option>)}
              </select>
              {errors.categoryId && <p className="text-red-400 text-xs mt-1">Wybierz podkategorie</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Tytul <span className="text-red-400">*</span></label>
              <input className={`h-11 w-full rounded-xl border bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:ring-2 focus:ring-emerald-500/20 ${errors.title ? 'border-red-500' : 'border-slate-700/50 focus:border-emerald-500/50'}`} name="title" type="text" value={form.title} onChange={onChange} placeholder="np. Nowe Audi A4" />
              {errors.title && <p className="text-red-400 text-xs mt-1">Wpisz tytul</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Cena (PLN) <span className="text-red-400">*</span></label>
              <div className="flex items-center gap-4">
                <input className={`h-11 flex-1 rounded-xl border bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:ring-2 focus:ring-emerald-500/20 ${errors.priceAmount ? 'border-red-500' : 'border-slate-700/50 focus:border-emerald-500/50'}`} name="priceAmount" type="number" step="0.01" value={form.priceAmount} onChange={onChange} placeholder="np. 50000" />
                <label className="relative inline-flex items-center gap-2 cursor-pointer select-none flex-none">
                  <input type="checkbox" name="negotiable" checked={form.negotiable} onChange={onChange} className="sr-only peer" />
                  <div className="w-11 h-6 bg-slate-700 rounded-full peer peer-checked:bg-emerald-600 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-5"></div>
                  <span className="text-sm text-slate-400">Do negocjacji</span>
                </label>
              </div>
              {errors.priceAmount && <p className="text-red-400 text-xs mt-1">Wpisz cene</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Miasto</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" name="locationCity" type="text" value={form.locationCity} onChange={onChange} placeholder="np. Gdansk" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Wojewodztwo</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" name="locationRegion" type="text" value={form.locationRegion} onChange={onChange} placeholder="np. Pomorskie" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Numer telefonu</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" name="contactPhoneNumber" type="tel" value={form.contactPhoneNumber} onChange={onChange} placeholder="np. 500600700" />
            </div>
            <div className="sm:col-span-2">
              <label className="block text-sm font-medium text-slate-300 mb-2">Opis</label>
              <textarea className="w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20 resize-none" name="description" value={form.description} onChange={onChange} rows={4} placeholder="Opisz swoj przedmiot..." />
            </div>
            {attributes.length > 0 && (
              <div className="sm:col-span-2">
                <label className="block text-sm font-medium text-slate-300 mb-3">Dodatkowe parametry</label>
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {attributes.slice().sort((a, b) => a.sortOrder - b.sortOrder).map((attr) => (
                    <div key={attr.id}>
                      <label className="block text-xs text-slate-400 mb-1.5">{attr.label}</label>
                      {attr.type === 'ENUM' ? (
                        <select className="h-10 w-full rounded-lg border border-slate-700/50 bg-slate-900/50 px-3 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50" value={attributeValues[attr.key] ?? ''} onChange={(e) => handleAttributeChange(attr.key, e.target.value)}>
                          <option value="">Wybierz</option>
                          {attr.options?.slice().sort((a, b) => a.sortOrder - b.sortOrder).map((opt) => <option key={opt.id} value={opt.value}>{opt.label}</option>)}
                        </select>
                      ) : (
                        <input className="h-10 w-full rounded-lg border border-slate-700/50 bg-slate-900/50 px-3 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50" type="text" value={attributeValues[attr.key] ?? ''} onChange={(e) => handleAttributeChange(attr.key, e.target.value)} />
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="sm:col-span-2">
              <label className="block text-sm font-medium text-slate-300 mb-3">Zdjecia</label>
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
              <button type="button" onClick={() => fileInputRef.current?.click()} className="inline-flex items-center justify-center rounded-xl border-2 border-dashed border-slate-700 bg-slate-800/50 px-6 py-4 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:text-white" disabled={uploadingCount > 0}>
                <svg className="w-6 h-6 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                Dodaj zdjecia
              </button>
              {uploadError && <p className="text-red-400 text-sm mt-2">{uploadError}</p>}
              {uploadingCount > 0 && (
                <p className="text-slate-400 text-sm mt-2 flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
                  Wysylanie... ({uploadingCount})
                </p>
              )}
              {uploadedImageUrls.length > 0 && (
                <div className="mt-4">
                  <p className="text-slate-400 text-sm mb-3">Dodane zdjecia: {uploadedImageUrls.length}</p>
                  <div className="flex flex-wrap gap-3">
                    {uploadedImageUrls.map((url, idx) => (
                      <div key={idx} className="relative group">
                        <img src={url} alt="" className="h-24 w-24 rounded-xl object-cover ring-2 ring-slate-700" />
                        <button type="button" className="absolute -top-2 -right-2 w-6 h-6 rounded-full bg-red-500 text-white text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-lg" onClick={() => setUploadedImageUrls((prev) => prev.filter((_, i) => i !== idx))} disabled={uploadingCount > 0}>
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M6 18L18 6M6 6l12 12" /></svg>
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
          <button type="submit" className="w-full sm:w-auto inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-8 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40 disabled:opacity-50" disabled={submitting}>
            {submitting ? (
              <>
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                Zapisywanie...
              </>
            ) : (
              <>
                <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M5 13l4 4L19 7" /></svg>
                Opublikuj ogloszenie
              </>
            )}
          </button>
        </form>

        {submittedListing && (
          <div className="rounded-2xl border border-emerald-500/30 bg-emerald-500/10 p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center">
                <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M5 13l4 4L19 7" /></svg>
              </div>
              <h2 className="text-lg font-semibold text-white">Ogloszenie zostalo dodane!</h2>
            </div>
            <div className="space-y-1">
              <div className="font-medium text-white">{submittedListing.title}</div>
              <div className="text-lg font-bold text-emerald-400">{Number(submittedListing.priceAmount).toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} PLN</div>
              <div className="text-sm text-slate-400">{submittedListing.locationCity}{submittedListing.locationRegion ? `, ${submittedListing.locationRegion}` : ''}</div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
