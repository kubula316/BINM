import './Home.css'
import { Link } from 'react-router-dom'

function Home({ isLoggedIn }) {
  return (
    <div className="home-page">
      <div>
        <div className="title-section">
          <h1>BINM</h1>
          <p>Było I Nie Ma</p>
        </div>
        {isLoggedIn && (
          <div className="home-actions">
            <Link to="/add-listing" className="home-card">
              <div className="home-card-title">Dodaj przedmiot</div>
              <div className="home-card-desc">Szybko dodaj nowe ogłoszenie</div>
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}

export default Home
