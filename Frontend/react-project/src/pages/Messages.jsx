import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'

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
          setError(res.status === 401 ? 'Musisz byc zalogowany.' : 'Blad pobierania.')
          return
        }
        const data = await res.json()
        if (!cancelled) setConversations(Array.isArray(data) ? data : [])
      } catch {
        if (!cancelled) setError('Brak polaczenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    fetchConversations()
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    if (targetConversationId) navigate(`/messages/${targetConversationId}`)
  }, [navigate, targetConversationId])

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Wiadomosci</h1>
        <div className="text-center">
          <Link to="/" className="ui-btn">Wroc</Link>
        </div>

        {loading && <p className="ui-muted">Ladowanie...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && !error && conversations.length === 0 && (
          <p className="ui-muted">Brak konwersacji.</p>
        )}

        {!loading && !error && conversations.length > 0 && (
          <div className="space-y-3">
            {conversations
              .slice()
              .sort((a, b) => new Date(b.lastMessageTimestamp) - new Date(a.lastMessageTimestamp))
              .map((c) => (
                <Link
                  key={c.id}
                  to={`/messages/${c.id}`}
                  className="flex items-start gap-3 rounded-xl border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-750 hover:border-zinc-600"
                >
                  {c.listing?.coverImageUrl && (
                    <img src={c.listing.coverImageUrl} alt="" className="h-14 w-14 flex-none rounded-lg object-cover" />
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="font-medium text-zinc-100 truncate">{c.listing?.title || 'Ogloszenie'}</div>
                    <div className="text-sm text-zinc-400">{c.otherParticipantName}</div>
                    <div className="text-sm text-zinc-500 truncate line-clamp-1">{c.lastMessageContent}</div>
                  </div>
                </Link>
              ))}
          </div>
        )}
      </div>
    </div>
  )
}
