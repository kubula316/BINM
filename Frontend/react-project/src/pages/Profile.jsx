import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './Categories.css'

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
        setMessage('')

        const res = await fetch(`${API_BASE_URL}/user/profile`, {
          credentials: 'include',
        })

        if (!res.ok) {
          if (res.status === 401) {
            setError('Musisz być zalogowany, aby zobaczyć profil.')
          } else {
            setError('Nie udało się pobrać profilu.')
          }
          return
        }

        const data = await res.json()
        if (!cancelled) {
          setProfile(data)
          setName(data?.name || '')
        }
      } catch {
        if (!cancelled) setError('Brak połączenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetchProfile()

    return () => {
      cancelled = true
    }
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
        const uploadRes = await fetch(`${API_BASE_URL}/user/upload/profile-image`, {
          method: 'POST',
          body: formData,
          credentials: 'include',
        })

        if (!uploadRes.ok) {
          setError('Nie udało się wysłać zdjęcia profilowego.')
          return
        }

        uploadedUrl = await uploadRes.text()
        setUploading(false)
      }

      const trimmedName = name.trim()
      const updateBody = {}
      if (trimmedName && trimmedName !== (profile.name || '')) {
        updateBody.name = trimmedName
      }
      if (uploadedUrl) {
        updateBody.profileImageUrl = uploadedUrl
      }

      if (Object.keys(updateBody).length === 0) {
        setMessage('Brak zmian do zapisania.')
        return
      }

      const patchRes = await fetch(`${API_BASE_URL}/user/profile`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(updateBody),
      })

      if (!patchRes.ok) {
        if (patchRes.status === 401) {
          setError('Musisz być zalogowany, aby edytować profil.')
        } else {
          setError('Nie udało się zapisać zmian profilu.')
        }
        return
      }

      const updated = await patchRes.json()
      setProfile(updated)
      setSelectedFile(null)
      setMessage('Zapisano zmiany profilu.')
    } catch {
      setError('Brak połączenia z serwerem')
    } finally {
      setUploading(false)
      setSaving(false)
    }
  }

  return (
    <div className="categories-page profile-page">
      <div className="categories-container">
        <h1>Mój profil</h1>

        <section className="electronics-section">
          <Link to="/" className="item-image-link">Wróć</Link>

          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && profile && (
            <div className="item-card" style={{ marginTop: 16 }}>
              <div className="item-header">
                <div>
                  <div className="item-name">{profile.name || '—'}</div>
                  <div className="item-meta" style={{ color: '#ddd' }}>{profile.email}</div>
                  <div className="item-meta" style={{ color: '#ddd' }}>
                    Status: {profile.isAccountVerified ? 'Zweryfikowane' : 'Niezweryfikowane'}
                  </div>
                </div>
                {profile.profileImageUrl && (
                  <img
                    src={profile.profileImageUrl}
                    alt="avatar"
                    style={{ width: 96, height: 96, borderRadius: 12, objectFit: 'cover' }}
                  />
                )}
              </div>

              <div className="item-body">
                <form onSubmit={handleSave} className="add-listing-form">
                  <div className="items-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                    <div className="form-group">
                      <label>Imię</label>
                      <input value={name} onChange={(e) => setName(e.target.value)} />
                    </div>

                    <div className="form-group">
                      <label>Zdjęcie profilowe</label>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                          const file = e.target.files && e.target.files[0]
                          setSelectedFile(file || null)
                        }}
                      />
                      {selectedFile && (
                        <div style={{ marginTop: 6, color: '#ddd', fontSize: 12 }}>
                          Wybrany plik: {selectedFile.name}
                        </div>
                      )}
                    </div>
                  </div>

                  {message && <p style={{ color: '#9ae6b4', marginTop: 12 }}>{message}</p>}
                  {error && <p style={{ color: '#ff6b6b', marginTop: 12 }}>{error}</p>}

                  <div style={{ marginTop: 12, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    <button
                      type="submit"
                      className="filters-button apply"
                      disabled={saving || uploading}
                    >
                      {uploading ? 'Wysyłanie zdjęcia...' : saving ? 'Zapisywanie...' : 'Zapisz'}
                    </button>
                    <button
                      type="button"
                      className="filters-button clear"
                      onClick={() => {
                        setName(profile.name || '')
                        setSelectedFile(null)
                        setMessage('')
                        setError('')
                      }}
                      disabled={saving || uploading}
                    >
                      Cofnij
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
