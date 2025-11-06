import { Link } from 'react-router-dom'
import './Header.css'

function Header({ isLoggedIn, username, onLoginClick, onLogout }) {
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
            <input 
              type="text" 
              placeholder="Szukaj" 
              className="search-input"
            />
          </div>

          <Link to="/categories" className="nav-btn">Kategorie</Link>
          <Link to="/help" className="nav-btn">Pomoc</Link>

          {isLoggedIn ? (
            <div className="user-menu">
              <span className="username">{username}</span>
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
