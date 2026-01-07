import { Link, useNavigate } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'
import './Header.css'

function Header({ isLoggedIn, username, onLoginClick, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)
  const [query, setQuery] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    function onDocClick(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false)
      }
    }
    function onKey(e) {
      if (e.key === 'Escape') setMenuOpen(false)
    }
    document.addEventListener('click', onDocClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('click', onDocClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [])

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-left">
          <Link to="/" className="logo">
            <span>BINM</span>
          </Link>
        </div>

        <div className="header-right">
          <div className="search-container">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                const q = query.trim()
                if (!q) return
                navigate(`/search?q=${encodeURIComponent(q)}`)
              }}
            >
              <input
                type="text"
                placeholder="Szukaj"
                className="search-input"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </form>
          </div>

          <Link to="/categories" className="nav-btn">Kategorie</Link>
          <Link to="/help" className="nav-btn">Pomoc</Link>
          {isLoggedIn && (
            <Link to="/add-listing" className="nav-btn">Dodaj przedmiot</Link>
          )}

          {isLoggedIn ? (
            <div className="user-menu" ref={menuRef}>
              <button className="username-btn" onClick={() => setMenuOpen((v) => !v)}>
                {username}
                <svg className={`chevron ${menuOpen ? 'open' : ''}`} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>
              {menuOpen && (
                <div className="user-dropdown">
                  <Link to="/my-listings" className="dropdown-item">Moje ogłoszenia</Link>
                  <Link to="/favorites" className="dropdown-item">Obserwowane</Link>
                  <Link to="/messages" className="dropdown-item">Wiadomości</Link>
                  <Link to="/profile" className="dropdown-item">Konto</Link>
                </div>
              )}
              <button onClick={onLogout} className="logout-btn">Wyloguj</button>
            </div>
          ) : (
            <button onClick={onLoginClick} className="login-btn">
              Logowanie
            </button>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
