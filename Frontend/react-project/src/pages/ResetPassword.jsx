import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function ResetPassword() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
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
    if (!email.trim()) { setError('Podaj email'); return }
    try {
      setLoading(true)
      const res = await fetch(`${API_BASE_URL}/public/send-reset-otp?email=${encodeURIComponent(email.trim())}`, { method: 'POST' })
      if (!res.ok) { setError('Blad wysylania OTP.'); return }
      setMessage('Kod OTP wyslany na email.')
      setStep(2)
    } catch { setError('Brak polaczenia z serwerem') }
    finally { setLoading(false) }
  }

  const resetPassword = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    if (!otp.trim()) { setError('Wpisz kod OTP'); return }
    if (!newPassword) { setError('Wpisz nowe haslo'); return }
    if (newPassword.length < 6) { setError('Haslo min. 6 znakow'); return }
    if (newPassword !== confirm) { setError('Hasla sie nie zgadzaja'); return }
    try {
      setLoading(true)
      const res = await fetch(`${API_BASE_URL}/public/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), otp: otp.trim(), newPassword }),
      })
      if (!res.ok) { setError('Blad resetu. Sprawdz OTP.'); return }
      setMessage('Haslo zmienione. Mozesz sie zalogowac.')
      setTimeout(() => navigate('/'), 1200)
    } catch { setError('Brak polaczenia z serwerem') }
    finally { setLoading(false) }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <div className="flex items-center justify-between gap-3">
          <h1 className="ui-h1">Reset hasla</h1>
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        <div className="ui-section">
          <p className="text-zinc-300 mb-4">{step === 1 ? 'Krok 1/2: podaj email' : 'Krok 2/2: OTP i nowe haslo'}</p>

          {error && <p className="text-red-400 mb-3">{error}</p>}
          {message && <p className="text-emerald-400 mb-3">{message}</p>}

          {step === 1 ? (
            <form onSubmit={sendOtp} className="space-y-4">
              <div>
                <label className="block text-sm text-zinc-300 mb-1">Email</label>
                <input className="ui-input w-full max-w-sm" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Wpisz email" />
              </div>
              <button type="submit" className="ui-btn-primary" disabled={loading}>{loading ? 'Wysylanie...' : 'Wyslij kod OTP'}</button>
            </form>
          ) : (
            <form onSubmit={resetPassword} className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Email</label>
                  <input className="ui-input w-full" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Kod OTP</label>
                  <input className="ui-input w-full" type="text" value={otp} onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))} placeholder="6 cyfr" maxLength={6} />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Nowe haslo</label>
                  <input className="ui-input w-full" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="Min. 6 znakow" />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Powtorz haslo</label>
                  <input className="ui-input w-full" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                <button type="submit" className="ui-btn-primary" disabled={loading}>{loading ? 'Zapisywanie...' : 'Zmien haslo'}</button>
                <button type="button" className="ui-btn" onClick={() => { setStep(1); setOtp(''); setNewPassword(''); setConfirm(''); setError(''); setMessage('') }} disabled={loading}>Cofnij</button>
                <button type="button" className="ui-btn" onClick={() => { setOtp(''); setError(''); setMessage(''); sendOtp({ preventDefault() {} }) }} disabled={loading}>Wyslij OTP ponownie</button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
