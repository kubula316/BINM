import './Categories.css'

function Categories() {
  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Kategorie produktów</h1>
        {/* <p className="subtitle">Przeglądaj nasze kategorie</p> */}

        <div className="categories-grid">
          <div className="category-card">
            <h3>Odzież</h3>
            <p>Odkryj naszą kolekcję ubrań</p>
          </div>

          <div className="category-card">
            <h3>Obuwie</h3>
            <p>Znajdź idealne buty</p>
          </div>

          <div className="category-card">
            <h3>Akcesoria</h3>
            <p>Uzupełnij swój styl</p>
          </div>

          <div className="category-card">
            <h3>Zegarki</h3>
            <p>Elegancja na nadgarstku</p>
          </div>

          <div className="category-card">
            <h3>Torby</h3>
            <p>Praktyczne rozwiązania</p>
          </div>

          <div className="category-card">
            <h3>Okulary</h3>
            <p>Ochrona i styl</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Categories
