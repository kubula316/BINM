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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" onClick={onClose}>
      <div className="relative w-full max-w-md rounded-2xl border border-zinc-700 bg-zinc-800 p-6 pt-12 shadow-xl" onClick={(e) => e.stopPropagation()}>
        <button className="absolute right-3 top-3 p-1 text-zinc-400 hover:text-zinc-200 z-10" onClick={onClose}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>

        <div className="mb-4 flex gap-2">
          <button className={`flex-1 rounded-lg py-2 text-sm font-medium transition ${mode === 'login' ? 'bg-emerald-600 text-white' : 'bg-zinc-700 text-zinc-300 hover:bg-zinc-600'}`} onClick={() => { setMode('login'); setError('') }}>Logowanie</button>
          <button className={`flex-1 rounded-lg py-2 text-sm font-medium transition ${mode === 'register' ? 'bg-emerald-600 text-white' : 'bg-zinc-700 text-zinc-300 hover:bg-zinc-600'}`} onClick={() => { setMode('register'); setError('') }}>Rejestracja</button>
        </div>

        {mode === 'login' ? (
          <form className="space-y-4" onSubmit={submitLogin}>
            <h2 className="text-xl font-semibold text-zinc-100">Logowanie</h2>
            {error && <div className="rounded bg-red-500/20 px-3 py-2 text-sm text-red-300">{error}</div>}
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Email</label>
              <input className="ui-input w-full" type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Wpisz email" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Haslo</label>
              <input className="ui-input w-full" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Wpisz haslo" />
            </div>
            <button type="submit" className="ui-btn-primary w-full" disabled={loading}>{loading ? 'Logowanie...' : 'Zaloguj sie'}</button>
            <button type="button" className="w-full text-center text-sm text-zinc-400 hover:text-emerald-400" onClick={() => { onClose(); navigate('/reset-password') }}>Nie pamietasz hasla?</button>
          </form>
        ) : (
          <form className="space-y-4" onSubmit={submitRegister}>
            <h2 className="text-xl font-semibold text-zinc-100">Rejestracja</h2>
            {error && <div className="rounded bg-red-500/20 px-3 py-2 text-sm text-red-300">{error}</div>}
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Nazwa uzytkownika</label>
              <input className="ui-input w-full" type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Wpisz nazwe" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Email</label>
              <input className="ui-input w-full" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Wpisz email" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Haslo</label>
              <input className="ui-input w-full" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Wpisz haslo" />
            </div>
            <div>
              <label className="block text-sm text-zinc-300 mb-1">Powtorz haslo</label>
              <input className="ui-input w-full" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Powtorz haslo" />
            </div>
            <button type="submit" className="ui-btn-primary w-full" disabled={loading}>{loading ? 'Rejestrowanie...' : 'Zarejestruj'}</button>
          </form>
        )}
      </div>
    </div>
  )
}

export default LoginForm
