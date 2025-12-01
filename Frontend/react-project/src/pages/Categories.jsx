import './Categories.css'
import { Link } from 'react-router-dom'

function Categories() {
  const cats = [
    { name: 'Odzież', slug: 'odziez', desc: 'Odkryj naszą kolekcję ubrań' },
    { name: 'Obuwie', slug: 'obuwie', desc: 'Znajdź idealne buty' },
    { name: 'Akcesoria', slug: 'akcesoria', desc: 'Uzupełnij swój styl' },
    { name: 'Elektronika', slug: 'elektronika', desc: 'Komputery, RTV i AGD, audio i więcej' },
    { name: 'Torby', slug: 'torby', desc: 'Praktyczne rozwiązania' },
    { name: 'Okulary', slug: 'okulary', desc: 'Ochrona i styl' }
  ]

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Kategorie produktów</h1>
        {/* <p className="subtitle">Przeglądaj nasze kategorie</p> */}

        <div className="categories-grid">
          {cats.map(c => (
            <Link key={c.slug} className="category-card" to={`/categories/${c.slug}`}>
              <h3>{c.name}</h3>
              <p>{c.desc}</p>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

export default Categories
