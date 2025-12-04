import { useState } from 'react'
import './LoginForm.css'

const API_BASE_URL = 'http://localhost:8081'

function LoginForm({ onLogin, onClose }) {
  const [mode, setMode] = useState('login') // 'login' | 'register'
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submitLogin = async (e) => {
    e.preventDefault()
    if (!username || !password) {
      setError('Wypełnij wszystkie pola')
      return
    }
    try {
      setLoading(true)
      setError('')
      const response = await fetch(`${API_BASE_URL}/public/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          email: username,
          password,
        }),
      })

      if (!response.ok) {
        if (response.status === 401) {
          setError('Nieprawidłowy email lub hasło')
        } else {
          setError('Wystąpił błąd podczas logowania')
        }
        return
      }

      onLogin(username)
      onClose()
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  const submitRegister = async (e) => {
    e.preventDefault()
    if (!username || !email || !password || !confirm) {
      setError('Wypełnij wszystkie pola')
      return
    }
    if (password !== confirm) {
      setError('Hasła nie są takie same')
      return
    }

    try {
      setLoading(true)
      setError('')
      const response = await fetch(`${API_BASE_URL}/public/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: username,
          email,
          password,
        }),
      })

      if (!response.ok) {
        if (response.status === 409) {
          setError('Użytkownik z takim adresem email już istnieje')
        } else {
          setError('Wystąpił błąd podczas rejestracji')
        }
        return
      }

      onLogin(username)
      onClose()
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-modal-overlay" onClick={onClose}>
      <div className="login-modal" onClick={(e) => e.stopPropagation()}>
        <button className="close-button" onClick={onClose}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        
        <div className="auth-tabs">
          <button className={`auth-tab ${mode === 'login' ? 'active' : ''}`} onClick={() => { setMode('login'); setError('') }}>Logowanie</button>
          <button className={`auth-tab ${mode === 'register' ? 'active' : ''}`} onClick={() => { setMode('register'); setError('') }}>Rejestracja</button>
        </div>

        {mode === 'login' ? (
        <form className="login-form" onSubmit={submitLogin}>
          <h2>Logowanie</h2>
          
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="username"> Email:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Wpisz email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Hasło:</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Wpisz hasło"
            />
          </div>

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Logowanie...' : 'Zaloguj się'}
          </button>
        </form>
        ) : (
        <form className="login-form" onSubmit={submitRegister}>
          <h2>Rejestracja</h2>

          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="reg-username">Nazwa użytkownika:</label>
            <input
              type="text"
              id="reg-username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Wpisz nazwę użytkownika"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-email">Email:</label>
            <input
              type="email"
              id="reg-email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Wpisz email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-password">Hasło:</label>
            <input
              type="password"
              id="reg-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Wpisz hasło"
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-confirm">Powtórz hasło:</label>
            <input
              type="password"
              id="reg-confirm"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="Powtórz hasło"
            />
          </div>

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Rejestrowanie...' : 'Zarejestruj'}
          </button>
        </form>
        )}
      </div>
    </div>
  )
}

export default LoginForm
