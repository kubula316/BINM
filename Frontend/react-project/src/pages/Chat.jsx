import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { API_BASE_URL } from '../config'


export default function Chat() {
  const { conversationId } = useParams()
  const convId = Number(conversationId)

  const [meId, setMeId] = useState('')
  const [conversation, setConversation] = useState(null)
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)

  const clientRef = useRef(null)
  const bottomRef = useRef(null)

  const listingId = conversation?.listing?.publicId
  const sellerId = conversation?.listing?.seller?.id

  const recipientId = useMemo(() => {
    if (!meId || !sellerId) return ''
    if (meId !== sellerId) return sellerId
    const other = messages.find((m) => m.senderId && m.senderId !== meId)
    return other?.senderId || ''
  }, [meId, messages, sellerId])

  useEffect(() => {
    let cancelled = false
    const fetchAll = async () => {
      try {
        setLoading(true)
        setError('')
        const [profileRes, convRes, msgRes] = await Promise.all([
          fetch(`${API_BASE_URL}/user/profile`, { credentials: 'include' }),
          fetch(`${API_BASE_URL}/user/conversations`, { credentials: 'include' }),
          fetch(`${API_BASE_URL}/user/conversations/${convId}/messages?page=0&size=50`, { credentials: 'include' }),
        ])
        if (!profileRes.ok || !convRes.ok || !msgRes.ok) {
          if (!cancelled) setError('Blad pobierania (zaloguj sie).')
          return
        }
        const profile = await profileRes.json().catch(() => null)
        const conversations = await convRes.json().catch(() => [])
        const msgPage = await msgRes.json().catch(() => null)
        if (cancelled) return
        setMeId(profile?.userId || '')
        setConversation(Array.isArray(conversations) ? conversations.find((c) => c.id === convId) : null)
        const content = Array.isArray(msgPage?.content) ? msgPage.content : []
        setMessages(content.slice().reverse())
        fetch(`${API_BASE_URL}/user/conversations/${convId}/read`, { method: 'PATCH', credentials: 'include' }).catch(() => null)
      } catch {
        if (!cancelled) setError('Brak polaczenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    if (Number.isFinite(convId)) fetchAll()
    return () => { cancelled = true }
  }, [convId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages.length])

  useEffect(() => {
    const token = localStorage.getItem('jwtToken')
    if (!token) return
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
    })
    client.onConnect = () => {
      client.subscribe('/user/queue/messages', (frame) => {
        try {
          const payload = JSON.parse(frame.body)
          if (payload?.conversation?.id && payload.conversation.id !== convId) return
          setMessages((prev) => [...prev, payload])
        } catch { /* ignore */ }
      })
    }
    client.activate()
    clientRef.current = client
    return () => { client.deactivate(); clientRef.current = null }
  }, [convId])

  const send = async () => {
    if (!text.trim()) return
    if (!listingId || !recipientId) { setError('Brak danych do wyslania'); return }
    if (!clientRef.current?.connected) { setError('Brak polaczenia z czatem.'); return }
    try {
      setSending(true)
      setError('')
      const optimistic = { id: `local-${Date.now()}`, senderId: meId, recipientId, content: text.trim(), createdAt: new Date().toISOString() }
      setMessages((prev) => [...prev, optimistic])
      clientRef.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({ listingId, recipientId, content: text.trim() }),
      })
      setText('')
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
        <div className="mx-auto w-full max-w-4xl px-4 sm:px-6">
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span className="ml-3 text-slate-400">Ladowanie czatu...</span>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-4xl px-4 sm:px-6 space-y-4">
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-2xl sm:text-3xl font-bold text-white truncate">Czat</h1>
          <Link to="/messages" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white flex-none">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        {conversation && (
          <div className="flex items-center gap-4 p-4 rounded-xl border border-slate-700/50 bg-slate-800/30">
            {conversation.listing?.coverImageUrl ? (
              <img src={conversation.listing.coverImageUrl} alt="" className="h-12 w-12 rounded-lg object-cover" />
            ) : (
              <div className="h-12 w-12 rounded-lg bg-slate-700/50 flex items-center justify-center">
                <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" /></svg>
              </div>
            )}
            <div className="min-w-0 flex-1">
              <div className="font-medium text-white truncate">{conversation.listing?.title}</div>
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <div className="w-5 h-5 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white text-xs font-medium">
                  {conversation.otherParticipantName?.charAt(0)?.toUpperCase() || '?'}
                </div>
                {conversation.otherParticipantName}
              </div>
            </div>
            {listingId && (
              <Link to={`/listing/${listingId}`} className="inline-flex items-center justify-center rounded-lg border border-slate-700/50 bg-slate-800/50 px-3 py-1.5 text-xs font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
                Zobacz ogloszenie
              </Link>
            )}
          </div>
        )}

        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-4 sm:p-6">
          <div className="flex flex-col gap-3 max-h-[400px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent">
            {messages.length === 0 ? (
              <div className="text-center py-8">
                <svg className="w-12 h-12 text-slate-600 mx-auto mb-3" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
                <p className="text-slate-500">Brak wiadomosci. Rozpocznij konwersacje!</p>
              </div>
            ) : (
              messages.map((m) => {
                const mine = meId && m.senderId === meId
                return (
                  <div
                    key={m.id || `${m.senderId}-${m.createdAt}-${m.content}`}
                    className={`max-w-[80%] rounded-2xl px-4 py-3 ${mine ? 'self-end bg-gradient-to-r from-emerald-600 to-teal-600 text-white' : 'self-start bg-slate-700/50 border border-slate-600/50 text-slate-100'}`}
                  >
                    <div className={`text-xs mb-1 ${mine ? 'text-emerald-200' : 'text-slate-400'}`}>{mine ? 'Ja' : conversation?.otherParticipantName || 'Rozmowca'}</div>
                    <div className="text-sm whitespace-pre-wrap break-words">{m.content}</div>
                    {m.createdAt && <div className={`text-xs mt-1.5 ${mine ? 'text-emerald-200' : 'text-slate-500'}`}>{new Date(m.createdAt).toLocaleString('pl-PL')}</div>}
                  </div>
                )
              })
            )}
            <div ref={bottomRef} />
          </div>

          <div className="flex gap-3 mt-4 pt-4 border-t border-slate-700/50">
            <input
              className="h-11 flex-1 rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Napisz wiadomosc..."
              onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() } }}
            />
            <button type="button" className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" onClick={send} disabled={sending}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
