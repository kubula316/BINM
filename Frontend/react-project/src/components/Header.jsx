import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'

function Header({ isLoggedIn, username, userAvatar, onLoginClick, onLogout }) {
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
    <header className="sticky top-0 z-50 border-b border-slate-800/50 bg-slate-900/80 backdrop-blur-xl shadow-lg shadow-slate-950/20">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 flex h-16 items-center justify-between gap-4">
        <Link to="/" className="flex items-center gap-2 text-xl font-bold bg-gradient-to-r from-emerald-400 to-teal-400 bg-clip-text text-transparent hover:from-emerald-300 hover:to-teal-300 transition-all">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
            <span className="text-white font-bold text-sm">B</span>
          </div>
          BINM
        </Link>

        <div className="flex items-center gap-3">
          <form onSubmit={handleSearch} className="hidden sm:block">
            <div className="relative">
              <svg className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
              </svg>
              <input 
                type="text" 
                placeholder="Szukaj ogloszen..." 
                className="h-10 w-64 rounded-xl border border-slate-700/50 bg-slate-800/50 pl-10 pr-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:bg-slate-800 focus:ring-2 focus:ring-emerald-500/20" 
                value={query} 
                onChange={(e) => setQuery(e.target.value)} 
              />
            </div>
          </form>

          <div className="relative sm:hidden" ref={searchRef}>
            <button type="button" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 p-2.5 text-slate-300 transition-all hover:bg-slate-700 hover:text-white" onClick={() => setSearchOpen((v) => !v)}>
              <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
              </svg>
            </button>
            {searchOpen && (
              <form onSubmit={handleSearch} className="absolute right-0 mt-2 w-72 rounded-xl border border-slate-700/50 bg-slate-800 p-3 shadow-xl shadow-slate-950/50">
                <input type="text" placeholder="Szukaj..." className="h-10 w-full rounded-lg border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition focus:border-emerald-500/50" value={query} onChange={(e) => setQuery(e.target.value)} autoFocus />
              </form>
            )}
          </div>

          <Link to="/categories" className={`hidden sm:inline-flex items-center justify-center rounded-xl border px-4 py-2.5 text-sm font-medium transition-all ${isActive('/categories') ? 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400' : 'border-slate-700/50 bg-slate-800/50 text-slate-300 hover:bg-slate-700 hover:text-white'}`}>Kategorie</Link>
          <Link to="/help" className={`hidden sm:inline-flex items-center justify-center rounded-xl border px-4 py-2.5 text-sm font-medium transition-all ${isActive('/help') ? 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400' : 'border-slate-700/50 bg-slate-800/50 text-slate-300 hover:bg-slate-700 hover:text-white'}`}>Pomoc</Link>
          {isLoggedIn && <Link to="/add-listing" className="hidden sm:inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40">+ Dodaj</Link>}

          {isLoggedIn ? (
            <div className="relative" ref={menuRef}>
              <button type="button" className={`inline-flex items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-medium transition-all ${isActive('/my-listings') || isActive('/favorites') || isActive('/messages') || isActive('/profile') ? 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400' : 'border-slate-700/50 bg-slate-800/50 text-slate-300 hover:bg-slate-700 hover:text-white'}`} onClick={() => setMenuOpen((v) => !v)}>
                {userAvatar ? (
                  <img src={userAvatar} alt={username} className="w-7 h-7 rounded-full object-cover ring-2 ring-emerald-500/50" />
                ) : (
                  <div className="w-7 h-7 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white text-xs font-bold">
                    {username ? username.charAt(0).toUpperCase() : 'U'}
                  </div>
                )}
                <span className="max-w-[100px] truncate hidden sm:block">{username}</span>
                <svg className={`h-4 w-4 transition-transform ${menuOpen ? 'rotate-180' : ''}`} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>
              {menuOpen && (
                <div className="absolute right-0 mt-2 w-56 rounded-xl border border-slate-700/50 bg-slate-800 p-2 shadow-xl shadow-slate-950/50">
                  <Link to="/my-listings" className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-all ${isActive('/my-listings') ? 'text-emerald-400 bg-emerald-500/10' : 'text-slate-300 hover:bg-slate-700 hover:text-white'}`}>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
                    Moje ogloszenia
                  </Link>
                  <Link to="/favorites" className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-all ${isActive('/favorites') ? 'text-emerald-400 bg-emerald-500/10' : 'text-slate-300 hover:bg-slate-700 hover:text-white'}`}>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>
                    Obserwowane
                  </Link>
                  <Link to="/messages" className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-all ${isActive('/messages') ? 'text-emerald-400 bg-emerald-500/10' : 'text-slate-300 hover:bg-slate-700 hover:text-white'}`}>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
                    Wiadomosci
                  </Link>
                  <Link to="/profile" className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-all ${isActive('/profile') ? 'text-emerald-400 bg-emerald-500/10' : 'text-slate-300 hover:bg-slate-700 hover:text-white'}`}>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                    Konto
                  </Link>
                  <hr className="my-2 border-slate-700/50" />
                  <button type="button" onClick={onLogout} className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-red-400 hover:bg-red-500/10 transition-all">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
                    Wyloguj
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button type="button" onClick={onLoginClick} className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40">Zaloguj sie</button>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
