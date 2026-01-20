import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { API_BASE_URL } from '../config'

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [name, setName] = useState('')
  const [selectedFile, setSelectedFile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const fileInputRef = useRef(null)

  useEffect(() => {
    let cancelled = false
    const fetchProfile = async () => {
      try {
        setLoading(true)
        setError('')
        const res = await fetch(`${API_BASE_URL}/user/profile`, { credentials: 'include' })
        if (!res.ok) { setError(res.status === 401 ? 'Musisz byc zalogowany.' : 'Blad pobierania.'); return }
        const data = await res.json()
        if (!cancelled) { setProfile(data); setName(data?.name || '') }
      } catch {
        if (!cancelled) setError('Brak polaczenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    fetchProfile()
    return () => { cancelled = true }
  }, [])

  const handleSave = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    if (!profile) return
    try {
      setSaving(true)
      let uploadedUrl = null
      if (selectedFile) {
        const formData = new FormData()
        formData.append('file', selectedFile)
        setUploading(true)
        const uploadRes = await fetch(`${API_BASE_URL}/user/upload/profile-image`, { method: 'POST', body: formData, credentials: 'include' })
        if (!uploadRes.ok) { setError('Blad uploadu zdjecia.'); return }
        uploadedUrl = await uploadRes.text()
        setUploading(false)
      }
      const trimmedName = name.trim()
      const updateBody = {}
      if (trimmedName && trimmedName !== (profile.name || '')) updateBody.name = trimmedName
      if (uploadedUrl) updateBody.profileImageUrl = uploadedUrl
      if (Object.keys(updateBody).length === 0) { setMessage('Brak zmian.'); return }
      const patchRes = await fetch(`${API_BASE_URL}/user/profile`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(updateBody),
      })
      if (!patchRes.ok) { setError('Blad zapisu.'); return }
      const updated = await patchRes.json()
      setProfile(updated)
      setSelectedFile(null)
      setMessage('Zapisano zmiany.')
    } catch {
      setError('Brak polaczenia z serwerem')
    } finally {
      setUploading(false)
      setSaving(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-2xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl sm:text-3xl font-bold text-white">Moje konto</h1>
          <Link to="/" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Ladowanie...</span>
          </div>
        )}
        {error && !loading && (
          <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>
        )}

        {!loading && !error && profile && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
            <div className="flex items-start gap-5 mb-6 pb-6 border-b border-slate-700/50">
              <div className="relative">
                {profile.profileImageUrl ? (
                  <img src={profile.profileImageUrl} alt="avatar" className="h-24 w-24 rounded-2xl object-cover ring-4 ring-slate-700/50" />
                ) : (
                  <div className="h-24 w-24 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
                    <span className="text-3xl font-bold text-white">{profile.name?.charAt(0)?.toUpperCase() || 'U'}</span>
                  </div>
                )}
                <div className={`absolute -bottom-1 -right-1 w-6 h-6 rounded-full flex items-center justify-center ${profile.isAccountVerified ? 'bg-emerald-500' : 'bg-slate-600'}`}>
                  {profile.isAccountVerified ? (
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M5 13l4 4L19 7" /></svg>
                  ) : (
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                  )}
                </div>
              </div>
              <div>
                <div className="text-xl font-semibold text-white">{profile.name || '-'}</div>
                <div className="text-slate-400 mt-1">{profile.email}</div>
                <div className={`inline-flex items-center gap-1 mt-2 text-sm ${profile.isAccountVerified ? 'text-emerald-400' : 'text-amber-400'}`}>
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
                  {profile.isAccountVerified ? 'Konto zweryfikowane' : 'Konto niezweryfikowane'}
                </div>
              </div>
            </div>

            <form onSubmit={handleSave} className="space-y-5">
              <div className="grid gap-5 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Imie / Nazwa</label>
                  <input className="h-11 w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20" value={name} onChange={(e) => setName(e.target.value)} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Zdjecie profilowe</label>
                  <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={(e) => setSelectedFile(e.target.files?.[0] || null)} />
                  <button type="button" onClick={() => fileInputRef.current?.click()} className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
                    <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                      <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    Wybierz zdjecie
                  </button>
                  {selectedFile && <div className="text-xs text-emerald-400 mt-2">Wybrany: {selectedFile.name}</div>}
                </div>
              </div>

              {message && <div className="rounded-xl bg-emerald-500/10 border border-emerald-500/20 px-4 py-3 text-sm text-emerald-400">{message}</div>}
              {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

              <div className="flex gap-3 pt-2">
                <button type="submit" className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" disabled={saving || uploading}>
                  {uploading ? 'Wysylanie...' : saving ? 'Zapisywanie...' : 'Zapisz zmiany'}
                </button>
                <button
                  type="button"
                  className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-6 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white"
                  onClick={() => { setName(profile.name || ''); setSelectedFile(null); setMessage(''); setError('') }}
                  disabled={saving || uploading}
                >
                  Anuluj
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
