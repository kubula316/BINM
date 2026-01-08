import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'

function Header({ isLoggedIn, username, onLoginClick, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)
  const menuRef = useRef(null)
  const searchRef = useRef(null)
  const [query, setQuery] = useState('')
  const navigate = useNavigate()
  const location = useLocation()

  const isActive = (path) => location.pathname === path || location.pathname.startsWith(path + '/')

  useEffect(() => {
    const onDocClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false)
      if (searchRef.current && !searchRef.current.contains(e.target)) setSearchOpen(false)
    }
    const onKey = (e) => {
      if (e.key === 'Escape') { setMenuOpen(false); setSearchOpen(false) }
    }
    document.addEventListener('click', onDocClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('click', onDocClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [])

  const handleSearch = (e) => {
    e.preventDefault()
    const q = query.trim()
    if (!q) return
    navigate(`/search?q=${encodeURIComponent(q)}`)
    setSearchOpen(false)
  }

  return (
    <header className="sticky top-0 z-50 border-b border-zinc-800 bg-zinc-900">
      <div className="ui-container flex h-14 items-center justify-between gap-3">
        <Link to="/" className="text-lg font-bold text-zinc-100 hover:text-zinc-100">BINM</Link>

        <div className="flex items-center gap-2">
          {/* Desktop search */}
          <form onSubmit={handleSearch} className="hidden sm:block">
            <input type="text" placeholder="Szukaj..." className="ui-input w-56" value={query} onChange={(e) => setQuery(e.target.value)} />
          </form>

          {/* Mobile search toggle */}
          <div className="relative sm:hidden" ref={searchRef}>
            <button type="button" className="ui-btn p-2" onClick={() => setSearchOpen((v) => !v)}>
              <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
              </svg>
            </button>
            {searchOpen && (
              <form onSubmit={handleSearch} className="absolute right-0 mt-2 w-64 rounded-lg border border-zinc-700 bg-zinc-800 p-2 shadow-lg">
                <input type="text" placeholder="Szukaj..." className="ui-input w-full" value={query} onChange={(e) => setQuery(e.target.value)} autoFocus />
              </form>
            )}
          </div>

          <Link to="/categories" className={`ui-btn hidden sm:inline-flex ${isActive('/categories') ? 'border-emerald-500 text-emerald-400' : ''}`}>Kategorie</Link>
          <Link to="/help" className={`ui-btn hidden sm:inline-flex ${isActive('/help') ? 'border-emerald-500 text-emerald-400' : ''}`}>Pomoc</Link>
          {isLoggedIn && <Link to="/add-listing" className="ui-btn-primary hidden sm:inline-flex">Dodaj</Link>}

          {isLoggedIn ? (
            <div className="relative" ref={menuRef}>
              <button type="button" className={`ui-btn ${isActive('/my-listings') || isActive('/favorites') || isActive('/messages') || isActive('/profile') ? 'border-emerald-500 text-emerald-400' : ''}`} onClick={() => setMenuOpen((v) => !v)}>
                <span className="max-w-[120px] truncate">{username}</span>
                <svg className={`ml-1 h-4 w-4 transition-transform ${menuOpen ? 'rotate-180' : ''}`} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>
              {menuOpen && (
                <div className="absolute right-0 mt-2 w-48 rounded-lg border border-zinc-700 bg-zinc-800 p-1 shadow-lg">
                  <Link to="/my-listings" className={`block rounded-md px-3 py-2 text-sm ${isActive('/my-listings') ? 'text-emerald-400 bg-zinc-700' : 'text-zinc-200 hover:bg-zinc-700'}`}>Moje ogloszenia</Link>
                  <Link to="/favorites" className={`block rounded-md px-3 py-2 text-sm ${isActive('/favorites') ? 'text-emerald-400 bg-zinc-700' : 'text-zinc-200 hover:bg-zinc-700'}`}>Obserwowane</Link>
                  <Link to="/messages" className={`block rounded-md px-3 py-2 text-sm ${isActive('/messages') ? 'text-emerald-400 bg-zinc-700' : 'text-zinc-200 hover:bg-zinc-700'}`}>Wiadomosci</Link>
                  <Link to="/profile" className={`block rounded-md px-3 py-2 text-sm ${isActive('/profile') ? 'text-emerald-400 bg-zinc-700' : 'text-zinc-200 hover:bg-zinc-700'}`}>Konto</Link>
                  <hr className="my-1 border-zinc-700" />
                  <button type="button" onClick={onLogout} className="w-full rounded-md px-3 py-2 text-left text-sm text-zinc-200 hover:bg-zinc-700">Wyloguj</button>
                </div>
              )}
            </div>
          ) : (
            <button type="button" onClick={onLoginClick} className="ui-btn-primary">Logowanie</button>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
