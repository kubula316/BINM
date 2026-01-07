import { useEffect, useState } from 'react'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function AddListing({ username }) {
  const [form, setForm] = useState({
    parentCategoryId: '',
    categoryId: '', // ID wybranej podkategorii wysyłane do backendu
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
  const [selectedFile, setSelectedFile] = useState(null)
  const [uploadError, setUploadError] = useState('')

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoadingCategories(true)
        setCategoriesError('')
        const response = await fetch(`${API_BASE_URL}/public/category/all`)
        if (!response.ok) {
          setCategoriesError('Nie udało się pobrać kategorii')
          return
        }
        const data = await response.json()
        setCategories(Array.isArray(data) ? data : [])
      } catch {
        setCategoriesError('Brak połączenia z serwerem przy pobieraniu kategorii')
      } finally {
        setLoadingCategories(false)
      }
    }

    fetchCategories()
  }, [])

  const onChange = (e) => {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const topLevelCategories = categories.filter((c) => c.parentId === null)

  const parentCategory =
    form.parentCategoryId && Array.isArray(categories)
      ? categories.find((c) => c.id === Number(form.parentCategoryId))
      : null

  const subcategories = parentCategory?.children || []

  const handleParentCategoryChange = (e) => {
    const parentId = e.target.value
    setForm((prev) => ({
      ...prev,
      parentCategoryId: parentId,
      categoryId: '',
    }))
    setAttributes([])
    setAttributeValues({})
  }

  const handleSubcategoryChange = async (e) => {
    const subId = e.target.value
    setForm((prev) => ({
      ...prev,
      categoryId: subId,
    }))

    setAttributes([])
    setAttributeValues({})

    const numericId = Number(subId)
    if (!subId || Number.isNaN(numericId)) return

    try {
      const response = await fetch(`${API_BASE_URL}/public/category/attributes?categoryId=${numericId}`)
      if (!response.ok) {
        console.error('Nie udało się pobrać atrybutów kategorii')
        return
      }
      const data = await response.json()
      const attrs = Array.isArray(data) ? data : []
      setAttributes(attrs)

      const initialValues = {}
      attrs.forEach((attr) => {
        initialValues[attr.key] = ''
      })
      setAttributeValues(initialValues)
    } catch {
      console.error('Błąd połączenia przy pobieraniu atrybutów')
    }
  }

  const handleAttributeChange = (key, value) => {
    setAttributeValues((prev) => ({
      ...prev,
      [key]: value,
    }))
  }

  const onSubmit = async (e) => {
    e.preventDefault()

    if (!form.categoryId || !form.title || !form.priceAmount) {
      alert('Uzupełnij wymagane pola: podkategoria, tytuł, cena')
      return
    }

    setUploadError('')

    let uploadedImageUrl = null

    if (selectedFile) {
      const formData = new FormData()
      formData.append('file', selectedFile)

      try {
        const uploadResponse = await fetch(`${API_BASE_URL}/user/upload/media-image`, {
          method: 'POST',
          body: formData,
          credentials: 'include',
        })

        if (!uploadResponse.ok) {
          setUploadError('Nie udało się wysłać zdjęcia. Spróbuj ponownie lub wybierz inny plik.')
          return
        }

        uploadedImageUrl = await uploadResponse.text()
      } catch {
        setUploadError('Błąd połączenia przy wysyłaniu zdjęcia.')
        return
      }
    }

    const attributesPayload = Object.entries(attributeValues)
      .filter(([, value]) => value !== '' && value != null)
      .map(([key, value]) => ({
        key,
        value,
      }))

    const body = {
      categoryId: Number(form.categoryId),
      title: form.title,
      description: form.description || undefined,
      priceAmount: Number(form.priceAmount),
      negotiable: form.negotiable,
      locationCity: form.locationCity || undefined,
      locationRegion: form.locationRegion || undefined,
      contactPhoneNumber: form.contactPhoneNumber || undefined,
      mediaUrls: uploadedImageUrl ? [uploadedImageUrl] : [],
      attributes: attributesPayload,
    }

    try {
      setSubmitting(true)
      const response = await fetch(`${API_BASE_URL}/user/listing/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(body),
      })

      if (!response.ok) {
        alert('Nie udało się utworzyć ogłoszenia. Upewnij się, że jesteś zalogowany i spróbuj ponownie.')
        return
      }

      const data = await response.json().catch(() => null)
      setSubmittedListing({
        title: form.title,
        priceAmount: form.priceAmount,
        locationCity: form.locationCity,
        locationRegion: form.locationRegion,
        description: form.description,
        sellerName: username || 'Zalogowany użytkownik',
        ...(data || {}),
      })
    } catch {
      alert('Brak połączenia z serwerem podczas tworzenia ogłoszenia')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Dodaj przedmiot</h1>
        <section className="electronics-section add-listing">
          <form onSubmit={onSubmit} className="item-card add-listing-form">
            <div className="items-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
              <div className="form-group">
                <label>Kategoria główna</label>
                {loadingCategories ? (
                  <p style={{ color: '#fff' }}>Ładowanie kategorii...</p>
                ) : categoriesError ? (
                  <p style={{ color: '#ff6b6b' }}>{categoriesError}</p>
                ) : (
                  <select
                    name="parentCategoryId"
                    value={form.parentCategoryId}
                    onChange={handleParentCategoryChange}
                  >
                    <option value="">Wybierz kategorię</option>
                    {topLevelCategories.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                )}
              </div>
              <div className="form-group">
                <label>Podkategoria</label>
                <select
                  name="categoryId"
                  value={form.categoryId}
                  onChange={handleSubcategoryChange}
                  disabled={!parentCategory || !subcategories.length}
                >
                  <option value="">
                    {parentCategory ? 'Wybierz podkategorię' : 'Najpierw wybierz kategorię'}
                  </option>
                  {subcategories.map((sub) => (
                    <option key={sub.id} value={sub.id}>
                      {sub.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Tytuł</label>
                <input
                  name="title"
                  type="text"
                  value={form.title}
                  onChange={onChange}
                  placeholder="np. Nowe Audi A4"
                />
              </div>
              <div className="form-group">
                <label>Cena</label>
                <input
                  className="remove-arrows"
                  name="priceAmount"
                  type="number"
                  step="0.01"
                  value={form.priceAmount}
                  onChange={onChange}
                  placeholder="np. 50000.00"
                />
              </div>
              <div className="form-group">
                <label>Miasto</label>
                <input
                  name="locationCity"
                  type="text"
                  value={form.locationCity}
                  onChange={onChange}
                  placeholder="np. Gdańsk"
                />
              </div>
              <div className="form-group">
                <label>Województwo</label>
                <input
                  name="locationRegion"
                  type="text"
                  value={form.locationRegion}
                  onChange={onChange}
                  placeholder="np. Pomorskie"
                />
              </div>
              <div className="form-group">
                <label>Numer telefonu</label>
                <input
                  name="contactPhoneNumber"
                  type="tel"
                  value={form.contactPhoneNumber}
                  onChange={onChange}
                  placeholder="np. 500600700"
                />
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label>
                  <input
                    type="checkbox"
                    name="negotiable"
                    checked={form.negotiable}
                    onChange={onChange}
                    style={{ marginRight: 8 }}
                  />
                  Cena do negocjacji
                </label>
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label>Opis</label>
                <textarea
                  name="description"
                  value={form.description}
                  onChange={onChange}
                  rows={4}
                  placeholder="Krótki opis"
                />
              </div>
              {attributes.length > 0 && (
                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>Dodatkowe parametry</label>
                  <div className="filters-grid">
                    {attributes
                      .slice()
                      .sort((a, b) => a.sortOrder - b.sortOrder)
                      .map((attr) => (
                        <div key={attr.id} className="filter-item">
                          <label style={{ color: '#fff', display: 'block', marginBottom: 4 }}>
                            {attr.label}
                          </label>
                          {attr.type === 'ENUM' ? (
                            <select
                              value={attributeValues[attr.key] ?? ''}
                              onChange={(e) => handleAttributeChange(attr.key, e.target.value)}
                            >
                              <option value="">Wybierz</option>
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
                              value={attributeValues[attr.key] ?? ''}
                              onChange={(e) => handleAttributeChange(attr.key, e.target.value)}
                            />
                          )}
                        </div>
                      ))}
                  </div>
                </div>
              )}
            </div>
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label>Zdjęcie przedmiotu</label>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  setUploadError('')
                  const file = e.target.files && e.target.files[0]
                  setSelectedFile(file || null)
                }}
              />
              {uploadError && (
                <p style={{ color: '#ff6b6b', marginTop: 4 }}>{uploadError}</p>
              )}
              {selectedFile && !uploadError && (
                <p style={{ color: '#fff', marginTop: 4, fontSize: 12 }}>
                  Wybrany plik: {selectedFile.name}
                </p>
              )}
            </div>
            <button type="submit" className="login-button" style={{ marginTop: 16 }} disabled={submitting}>
              {submitting ? 'Zapisywanie...' : 'Zapisz ogłoszenie'}
            </button>
          </form>

          {submittedListing && (
            <div className="item-card" style={{ marginTop: 16 }}>
              <div className="item-header">
                <div>
                  <div className="item-name">{submittedListing.title}</div>
                  <div className="item-seller">Sprzedawca: {submittedListing.sellerName}</div>
                </div>
              </div>
              <div className="item-body">
                <div className="item-price">
                  {Number(submittedListing.priceAmount).toLocaleString('pl-PL', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}{' '}
                  PLN
                  {submittedListing.negotiable ? ' (do negocjacji)' : ''}
                </div>
                <div className="item-location">
                  Lokalizacja: {submittedListing.locationCity || '-'}{submittedListing.locationRegion ? `, ${submittedListing.locationRegion}` : ''}
                </div>
                <p className="item-desc">{submittedListing.description}</p>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
