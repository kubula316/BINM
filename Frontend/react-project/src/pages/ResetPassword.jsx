import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-lg px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-2xl sm:text-3xl font-bold text-white">Reset hasla</h1>
          <Link to="/" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${step >= 1 ? 'bg-emerald-500 text-white' : 'bg-slate-700 text-slate-400'}`}>1</div>
              <div className={`w-12 h-1 rounded ${step >= 2 ? 'bg-emerald-500' : 'bg-slate-700'}`}></div>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${step >= 2 ? 'bg-emerald-500 text-white' : 'bg-slate-700 text-slate-400'}`}>2</div>
            </div>
            <span className="text-sm text-slate-400 ml-3">{step === 1 ? 'Podaj email' : 'Ustaw nowe haslo'}</span>
          </div>

          {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400 mb-4">{error}</div>}
          {message && <div className="rounded-xl bg-emerald-500/10 border border-emerald-500/20 px-4 py-3 text-sm text-emerald-400 mb-4">{message}</div>}

          {step === 1 ? (
            <form onSubmit={sendOtp} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Adres email</label>
                <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="twoj@email.com" />
              </div>
              <button type="submit" className="w-full h-11 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" disabled={loading}>
                {loading ? 'Wysylanie...' : 'Wyslij kod OTP'}
              </button>
            </form>
          ) : (
            <form onSubmit={resetPassword} className="space-y-5">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Email</label>
                  <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Kod OTP</label>
                  <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 tracking-widest text-center outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="text" value={otp} onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))} placeholder="000000" maxLength={6} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Nowe haslo</label>
                  <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="Min. 6 znakow" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Powtorz haslo</label>
                  <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Powtorz haslo" />
                </div>
              </div>
              <div className="flex flex-wrap gap-3">
                <button type="submit" className="flex-1 h-11 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" disabled={loading}>{loading ? 'Zapisywanie...' : 'Zmien haslo'}</button>
                <button type="button" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white" onClick={() => { setStep(1); setOtp(''); setNewPassword(''); setConfirm(''); setError(''); setMessage('') }} disabled={loading}>Cofnij</button>
                <button type="button" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white" onClick={() => { setOtp(''); setError(''); setMessage(''); sendOtp({ preventDefault() {} }) }} disabled={loading}>Wyslij ponownie</button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
