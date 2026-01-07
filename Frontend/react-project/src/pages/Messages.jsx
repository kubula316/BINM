import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function Messages() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const listingIdFromQuery = searchParams.get('listingId')

  const [conversations, setConversations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const targetConversationId = useMemo(() => {
    if (!listingIdFromQuery) return null
    const found = conversations.find((c) => String(c?.listing?.publicId) === String(listingIdFromQuery))
    return found?.id || null
  }, [conversations, listingIdFromQuery])

  useEffect(() => {
    let cancelled = false
    const fetchConversations = async () => {
      try {
        setLoading(true)
        setError('')

        const res = await fetch(`${API_BASE_URL}/user/conversations`, { credentials: 'include' })
        if (!res.ok) {
          if (res.status === 401) setError('Musisz być zalogowany, aby zobaczyć wiadomości.')
          else setError('Nie udało się pobrać konwersacji.')
          return
        }

        const data = await res.json()
        if (!cancelled) setConversations(Array.isArray(data) ? data : [])
      } catch {
        if (!cancelled) setError('Brak połączenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetchConversations()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (targetConversationId) {
      navigate(`/messages/${targetConversationId}`)
    }
  }, [navigate, targetConversationId])

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Wiadomości</h1>

        <section className="electronics-section">
          <Link to="/" className="item-image-link">Wróć</Link>

          {loading && <p style={{ color: '#fff' }}>Ładowanie...</p>}
          {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

          {!loading && !error && conversations.length === 0 && (
            <p style={{ color: '#fff' }}>Brak konwersacji.</p>
          )}

          {!loading && !error && conversations.length > 0 && (
            <div className="items-grid" style={{ marginTop: 16 }}>
              {conversations
                .slice()
                .sort((a, b) => new Date(b.lastMessageTimestamp) - new Date(a.lastMessageTimestamp))
                .map((c) => (
                  <Link
                    key={c.id}
                    to={`/messages/${c.id}`}
                    className="item-card item-card-link"
                    style={{ textDecoration: 'none' }}
                  >
                    <div className="item-header">
                      <div>
                        <div className="item-name">{c.listing?.title || 'Ogłoszenie'}</div>
                        <div className="item-meta">{c.otherParticipantName}</div>
                        <div className="item-meta message-preview">{c.lastMessageContent}</div>
                      </div>
                      {c.listing?.coverImageUrl && (
                        <img src={c.listing.coverImageUrl} alt="miniatura" className="listing-thumb" />
                      )}
                    </div>
                  </Link>
                ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
