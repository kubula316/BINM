import { useState } from 'react'
import './LoginForm.css'

function LoginForm({ onLogin, onClose }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    
    if (!username || !password) {
      setError('Wypełnij wszystkie pola')
      return
    }

    if (username === 'admin' && password === 'admin') {
      setError('')
      onLogin(username)
    } else {
      setError('Nieprawidłowa nazwa użytkownika lub hasło')
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
        
        <form className="login-form" onSubmit={handleSubmit}>
          <h2>Logowanie</h2>
          
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="username">Nazwa użytkownika:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Wpisz nazwę użytkownika"
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

          <button type="submit" className="login-button">
            Zaloguj się
          </button>

          <div className="login-hint">
            <small>DEMO: Login: admin, Hasło: admin</small>
          </div>
        </form>
      </div>
    </div>
  )
}

export default LoginForm
