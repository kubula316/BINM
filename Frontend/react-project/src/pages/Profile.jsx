import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [name, setName] = useState('')
  const [selectedFile, setSelectedFile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

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
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Moje konto</h1>
        <div className="text-center">
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        {loading && <p className="ui-muted">Ladowanie...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && profile && (
          <div className="ui-section">
            <div className="flex items-start gap-4 mb-4">
              {profile.profileImageUrl && (
                <img src={profile.profileImageUrl} alt="avatar" className="h-24 w-24 rounded-xl object-cover" />
              )}
              <div>
                <div className="text-xl font-medium text-zinc-100">{profile.name || '-'}</div>
                <div className="text-sm text-zinc-400">{profile.email}</div>
                <div className="text-sm text-zinc-500">{profile.isAccountVerified ? 'Zweryfikowane' : 'Niezweryfikowane'}</div>
              </div>
            </div>

            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Imie</label>
                  <input className="ui-input w-full" value={name} onChange={(e) => setName(e.target.value)} />
                </div>
                <div>
                  <label className="block text-sm text-zinc-300 mb-1">Zdjecie profilowe</label>
                  <input type="file" accept="image/*" className="text-sm text-zinc-300" onChange={(e) => setSelectedFile(e.target.files?.[0] || null)} />
                  {selectedFile && <div className="text-xs text-zinc-500 mt-1">Wybrany: {selectedFile.name}</div>}
                </div>
              </div>

              {message && <p className="text-emerald-400">{message}</p>}

              <div className="flex gap-2">
                <button type="submit" className="ui-btn-primary" disabled={saving || uploading}>
                  {uploading ? 'Wysylanie...' : saving ? 'Zapisywanie...' : 'Zapisz'}
                </button>
                <button
                  type="button"
                  className="ui-btn"
                  onClick={() => { setName(profile.name || ''); setSelectedFile(null); setMessage(''); setError('') }}
                  disabled={saving || uploading}
                >
                  Cofnij
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
