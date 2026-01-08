import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function VerifyOtp() {
  const [otp, setOtp] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    if (!otp.trim()) { setError('Wpisz kod OTP.'); return }
    try {
      setLoading(true)
      const response = await fetch(`${API_BASE_URL}/user/verify-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ otp }),
      })
      if (!response.ok) { setError('Nieprawidlowy lub wygasly kod OTP.'); return }
      setMessage('Kod zweryfikowany. Mozesz sie teraz zalogowac.')
      setTimeout(() => navigate('/'), 2000)
    } catch { setError('Brak polaczenia z serwerem.') }
    finally { setLoading(false) }
  }

  const handleResend = async () => {
    setError('')
    setMessage('')
    try {
      setLoading(true)
      const response = await fetch(`${API_BASE_URL}/user/send-otp`, { method: 'POST', credentials: 'include' })
      if (!response.ok) { setError('Blad wysylania OTP.'); return }
      setMessage('Nowy kod OTP wyslany na email.')
    } catch { setError('Brak polaczenia z serwerem.') }
    finally { setLoading(false) }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6 flex items-center justify-center">
      <div className="ui-section max-w-md w-full text-center">
        <h2 className="ui-h1 mb-2">Potwierdz swoj email</h2>
        <p className="ui-muted mb-6">Wyslalismy kod weryfikacyjny na adres powiazany z Twoim kontem.</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-zinc-300 mb-1">Kod OTP</label>
            <input
              type="text"
              maxLength={6}
              className="ui-input w-full text-center text-lg tracking-widest"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
              placeholder="000000"
            />
          </div>

          {error && <p className="text-red-400 text-sm">{error}</p>}
          {message && <p className="text-emerald-400 text-sm">{message}</p>}

          <button type="submit" className="ui-btn-primary w-full" disabled={loading}>
            {loading ? 'Weryfikowanie...' : 'Potwierdz kod'}
          </button>
        </form>

        <div className="mt-4 space-y-2">
          <button type="button" className="ui-link text-sm" onClick={handleResend} disabled={loading}>
            Wyslij kod ponownie
          </button>
          <button type="button" className="block mx-auto text-sm text-zinc-500 hover:text-zinc-300" onClick={() => navigate('/')}>
            Wroc na strone glowna
          </button>
        </div>
      </div>
    </div>
  )
}
