import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { API_BASE_URL } from '../config'

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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-4xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white">Wiadomosci</h1>
            <p className="text-slate-400 mt-1">Twoje konwersacje</p>
          </div>
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
        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        {!loading && !error && conversations.length === 0 && (
          <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-12 text-center">
            <svg className="w-16 h-16 text-slate-600 mx-auto mb-4" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
            <p className="text-slate-400">Brak konwersacji.</p>
          </div>
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
                  className="group flex items-start gap-4 rounded-xl border border-slate-700/50 bg-slate-800/50 p-4 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:shadow-lg"
                >
                  {c.listing?.coverImageUrl ? (
                    <img src={c.listing.coverImageUrl} alt="" className="h-16 w-16 flex-none rounded-lg object-cover" />
                  ) : (
                    <div className="h-16 w-16 flex-none rounded-lg bg-slate-700/50 flex items-center justify-center">
                      <svg className="w-6 h-6 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
                    </div>
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <div className="font-medium text-white truncate group-hover:text-emerald-400 transition-colors">{c.listing?.title || 'Ogloszenie'}</div>
                      {c.lastMessageTimestamp && (
                        <div className="text-xs text-slate-500 flex-none">
                          {new Date(c.lastMessageTimestamp).toLocaleDateString('pl-PL')}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center gap-2 mt-1">
                      <div className="w-6 h-6 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white text-xs font-medium flex-none">
                        {c.otherParticipantName?.charAt(0)?.toUpperCase() || '?'}
                      </div>
                      <div className="text-sm text-slate-400">{c.otherParticipantName}</div>
                    </div>
                    <div className="text-sm text-slate-500 truncate mt-2">{c.lastMessageContent}</div>
                  </div>
                  <svg className="w-5 h-5 text-slate-500 group-hover:text-emerald-400 transition-colors flex-none" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" /></svg>
                </Link>
              ))}
          </div>
        )}
      </div>
    </div>
  )
}
