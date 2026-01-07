import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function ResetPassword() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1) // 1: email, 2: otp+new password
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const sendOtp = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')

    if (!email.trim()) {
      setError('Podaj email')
      return
    }

    try {
      setLoading(true)
      const url = `${API_BASE_URL}/public/send-reset-otp?email=${encodeURIComponent(email.trim())}`
      const res = await fetch(url, { method: 'POST' })
      if (!res.ok) {
        setError('Nie udało się wysłać kodu resetującego.')
        return
      }

      setMessage('Kod OTP został wysłany na podany email.')
      setStep(2)
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  const resetPassword = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')

    if (!otp.trim()) {
      setError('Wpisz kod OTP')
      return
    }
    if (!newPassword) {
      setError('Wpisz nowe hasło')
      return
    }
    if (newPassword.length < 6) {
      setError('Hasło musi mieć min. 6 znaków')
      return
    }
    if (newPassword !== confirm) {
      setError('Hasła nie są takie same')
      return
    }

    try {
      setLoading(true)
      const res = await fetch(`${API_BASE_URL}/public/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email.trim(),
          otp: otp.trim(),
          newPassword,
        }),
      })

      if (!res.ok) {
        setError('Nie udało się zresetować hasła. Sprawdź OTP i spróbuj ponownie.')
        return
      }

      setMessage('Hasło zostało zmienione. Możesz się zalogować.')
      setTimeout(() => navigate('/'), 1200)
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Reset hasła</h1>

        <section className="electronics-section add-listing">
          <div className="item-card" style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12 }}>
              <p style={{ color: '#fff', margin: 0 }}>
                {step === 1 ? 'Krok 1/2: podaj email' : 'Krok 2/2: podaj OTP i nowe hasło'}
              </p>
              <Link to="/" className="item-image-link">Wróć</Link>
            </div>

            {error && <p style={{ color: '#ff6b6b', marginTop: 12 }}>{error}</p>}
            {message && <p style={{ color: '#9ae6b4', marginTop: 12 }}>{message}</p>}

            {step === 1 ? (
              <form onSubmit={sendOtp} className="add-listing-form" style={{ marginTop: 12 }}>
                <div className="form-group">
                  <label>Email</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Wpisz email"
                  />
                </div>

                <button type="submit" className="login-button" disabled={loading} style={{ marginTop: 10 }}>
                  {loading ? 'Wysyłanie...' : 'Wyślij kod OTP'}
                </button>
              </form>
            ) : (
              <form onSubmit={resetPassword} className="add-listing-form" style={{ marginTop: 12 }}>
                <div className="items-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                  <div className="form-group">
                    <label>Email</label>
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label>Kod OTP</label>
                    <input
                      type="text"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
                      placeholder="6 cyfr"
                      maxLength={6}
                    />
                  </div>
                  <div className="form-group">
                    <label>Nowe hasło</label>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="Min. 6 znaków"
                    />
                  </div>
                  <div className="form-group">
                    <label>Powtórz hasło</label>
                    <input
                      type="password"
                      value={confirm}
                      onChange={(e) => setConfirm(e.target.value)}
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
                  <button type="submit" className="login-button" disabled={loading}>
                    {loading ? 'Zapisywanie...' : 'Zmień hasło'}
                  </button>
                  <button
                    type="button"
                    className="secondary-button"
                    style={{ width: 'auto', padding: '10px 14px' }}
                    onClick={() => {
                      setStep(1)
                      setOtp('')
                      setNewPassword('')
                      setConfirm('')
                      setError('')
                      setMessage('')
                    }}
                    disabled={loading}
                  >
                    Cofnij do kroku 1
                  </button>
                  <button
                    type="button"
                    className="secondary-button"
                    style={{ width: 'auto', padding: '10px 14px' }}
                    onClick={() => {
                      setOtp('')
                      setError('')
                      setMessage('')
                      // ponów wysyłkę
                      sendOtp({ preventDefault() {} })
                    }}
                    disabled={loading}
                  >
                    Wyślij OTP ponownie
                  </button>
                </div>
              </form>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}
