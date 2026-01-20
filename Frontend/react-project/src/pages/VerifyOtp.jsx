import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center py-8 px-4">
      <div className="w-full max-w-md">
        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-8 text-center">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center mx-auto mb-5">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </div>
          
          <h2 className="text-2xl font-bold text-white mb-2">Potwierdz email</h2>
          <p className="text-slate-400 mb-8">Wyslalismy kod weryfikacyjny na Twoj adres email. Wpisz go ponizej.</p>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Kod OTP</label>
              <input
                type="text"
                maxLength={6}
                className="h-14 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-xl text-center tracking-[0.5em] text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
                placeholder="000000"
              />
            </div>

            {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}
            {message && <div className="rounded-xl bg-emerald-500/10 border border-emerald-500/20 px-4 py-3 text-sm text-emerald-400">{message}</div>}

            <button type="submit" className="w-full h-11 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" disabled={loading}>
              {loading ? 'Weryfikowanie...' : 'Potwierdz kod'}
            </button>
          </form>

          <div className="mt-6 space-y-3">
            <button type="button" className="text-sm font-medium text-emerald-400 hover:text-emerald-300 transition-colors" onClick={handleResend} disabled={loading}>
              Wyslij kod ponownie
            </button>
            <div>
              <button type="button" className="text-sm text-slate-500 hover:text-slate-300 transition-colors" onClick={() => navigate('/')}>
                Wroc na strone glowna
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
