import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

function LoginForm({ onLogin, onClose }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const submitLogin = async (e) => {
    e.preventDefault()
    if (!username || !password) { setError('Wypelnij wszystkie pola'); return }
    try {
      setLoading(true)
      setError('')
      const response = await fetch(`${API_BASE_URL}/public/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email: username, password }),
      })
      if (!response.ok) { setError(response.status === 401 ? 'Nieprawidlowy email lub haslo' : 'Blad logowania'); return }
      const data = await response.json().catch(() => null)
      if (data?.token) localStorage.setItem('jwtToken', data.token)
      localStorage.removeItem('forceLoggedOut')
      onLogin(username)
      onClose()
      window.location.reload()
    } catch { setError('Brak polaczenia z serwerem') }
    finally { setLoading(false) }
  }

  const submitRegister = async (e) => {
    e.preventDefault()
    if (!username || !email || !password || !confirm) { setError('Wypelnij wszystkie pola'); return }
    if (password !== confirm) { setError('Hasla nie sa takie same'); return }
    try {
      setLoading(true)
      setError('')
      const response = await fetch(`${API_BASE_URL}/public/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name: username, email, password }),
      })
      if (!response.ok) { setError(response.status === 409 ? 'Uzytkownik juz istnieje' : 'Blad rejestracji'); return }
      const loginResponse = await fetch(`${API_BASE_URL}/public/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email, password }),
      })
      if (!loginResponse.ok) { setError('Rejestracja udana, zaloguj sie recznie.'); return }
      const loginData = await loginResponse.json().catch(() => null)
      if (loginData?.token) localStorage.setItem('jwtToken', loginData.token)
      localStorage.removeItem('forceLoggedOut')
      navigate('/verify-otp')
      onClose()
      window.location.reload()
    } catch { setError('Brak polaczenia z serwerem') }
    finally { setLoading(false) }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="relative w-full max-w-md rounded-2xl border border-slate-700/50 bg-gradient-to-b from-slate-800 to-slate-900 p-8 shadow-2xl shadow-slate-950/50" onClick={(e) => e.stopPropagation()}>
        <button className="absolute right-4 top-4 p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700/50 transition-all" onClick={onClose}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>

        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center mx-auto mb-3">
            <span className="text-white font-bold text-lg">B</span>
          </div>
          <h2 className="text-2xl font-bold text-white">Witaj w BINM</h2>
          <p className="text-slate-400 text-sm mt-1">Zaloguj sie lub zaloz konto</p>
        </div>

        <div className="flex gap-2 p-1 bg-slate-800/50 rounded-xl mb-6">
          <button className={`flex-1 rounded-lg py-2.5 text-sm font-medium transition-all ${mode === 'login' ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`} onClick={() => { setMode('login'); setError('') }}>Logowanie</button>
          <button className={`flex-1 rounded-lg py-2.5 text-sm font-medium transition-all ${mode === 'register' ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`} onClick={() => { setMode('register'); setError('') }}>Rejestracja</button>
        </div>

        {mode === 'login' ? (
          <form className="space-y-4" onSubmit={submitLogin}>
            {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Email</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="twoj@email.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Haslo</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Wprowadz haslo" />
            </div>
            <button type="submit" className="w-full h-11 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40 disabled:opacity-50" disabled={loading}>{loading ? 'Logowanie...' : 'Zaloguj sie'}</button>
            <button type="button" className="w-full text-center text-sm text-slate-400 hover:text-emerald-400 transition-colors" onClick={() => { onClose(); navigate('/reset-password') }}>Nie pamietasz hasla?</button>
          </form>
        ) : (
          <form className="space-y-4" onSubmit={submitRegister}>
            {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Nazwa uzytkownika</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Twoja nazwa" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Email</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="twoj@email.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Haslo</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Min. 6 znakow" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Powtorz haslo</label>
              <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Powtorz haslo" />
            </div>
            <button type="submit" className="w-full h-11 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40 disabled:opacity-50" disabled={loading}>{loading ? 'Rejestrowanie...' : 'Zaloz konto'}</button>
          </form>
        )}
      </div>
    </div>
  )
}

export default LoginForm
