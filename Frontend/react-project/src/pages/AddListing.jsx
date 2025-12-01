import { useState } from 'react'
import './Categories.css'

export default function AddListing({ username }) {
  const [form, setForm] = useState({
    name: '',
    price: '',
    currency: 'PLN',
    location: '',
    description: ''
  })
  const [submitted, setSubmitted] = useState(false)

  const onChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const onSubmit = (e) => {
    e.preventDefault()
    if (!form.name || !form.price || !form.location || !form.description) {
      alert('Uzupełnij wymagane pola: nazwa, cena, lokalizacja, opis')
      return
    }
    setSubmitted(true)
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Dodaj przedmiot</h1>
        <section className="electronics-section add-listing">
          <form onSubmit={onSubmit} className="item-card add-listing-form">
            <div className="items-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
              <div className="form-group">
                <label>Waluta</label>
                <input name="currency" type="text" value={form.currency} onChange={onChange} />
              </div>
              <div className="form-group">
                <label>Nazwa</label>
                <input name="name" type="text" value={form.name} onChange={onChange} placeholder="np. Laptop" />
              </div>
              <div className="form-group">
                <label>Cena</label>
                <input className="remove-arrows" name="price" type="number" step="0.01" value={form.price} onChange={onChange} placeholder="np. 1999.99" />
              </div>
              <div className="form-group">
                <label>Lokalizacja</label>
                <input name="location" type="text" value={form.location} onChange={onChange} placeholder="Miasto" />
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label>Opis</label>
                <textarea name="description" value={form.description} onChange={onChange} rows={4} placeholder="Krótki opis" />
              </div>
            </div>
            <button type="submit" className="login-button" style={{ marginTop: 16 }}>Zapisz</button>
            <button type="button" className="secondary-button add-photo-button">Dodaj zdjęcie </button>
          </form>

          {submitted && (
            <div className="item-card" style={{ marginTop: 16 }}>
              <div className="item-header">
                <div>
                  <div className="item-name">{form.name}</div>
                  <div className="item-seller">Sprzedawca: {username || 'Zalogowany użytkownik'}</div>
                </div>
              </div>
              <div className="item-body">
                <div className="item-price">{Number(form.price).toLocaleString('pl-PL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {form.currency}</div>
                <div className="item-location">Lokalizacja: {form.location}</div>
                <p className="item-desc">{form.description}</p>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
