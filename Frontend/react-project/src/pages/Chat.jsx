import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

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
      <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
        <div className="ui-container"><p className="ui-muted">Ladowanie czatu...</p></div>
      </div>
    )
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <div className="flex items-center justify-between gap-3">
          <h1 className="ui-h1 truncate">Chat</h1>
          <Link to="/messages" className="ui-btn">Wroc</Link>
        </div>

        {conversation && (
          <div className="flex items-center gap-2 text-sm text-zinc-300">
            <span className="font-medium">{conversation.listing?.title}</span>
            <span className="text-zinc-500">-</span>
            <span>{conversation.otherParticipantName}</span>
            {listingId && <Link to={`/listing/${listingId}`} className="ui-btn ml-auto">Pokaż Ogloszenie</Link>}
          </div>
        )}

        {error && <p className="text-red-400">{error}</p>}

        <div className="ui-section">
          <div className="flex flex-col gap-2 max-h-96 overflow-y-auto">
            {messages.length === 0 ? (
              <div className="text-zinc-500">Brak wiadomosci.</div>
            ) : (
              messages.map((m) => {
                const mine = meId && m.senderId === meId
                return (
                  <div
                    key={m.id || `${m.senderId}-${m.createdAt}-${m.content}`}
                    className={`max-w-[80%] rounded-xl px-3 py-2 ${mine ? 'self-end bg-emerald-600/20 border border-emerald-500/30 text-right' : 'self-start bg-zinc-700/50 border border-zinc-600/50'}`}
                  >
                    <div className="text-xs text-zinc-400 mb-1">{mine ? 'Ja' : conversation?.otherParticipantName || 'Rozmowca'}</div>
                    <div className={`text-sm text-zinc-100 whitespace-pre-wrap break-words ${mine ? 'text-right' : ''}`}>{m.content}</div>
                    {m.createdAt && <div className="text-xs text-zinc-500 mt-1">{new Date(m.createdAt).toLocaleString('pl-PL')}</div>}
                  </div>
                )
              })
            )}
            <div ref={bottomRef} />
          </div>

          <div className="flex gap-2 mt-4">
            <input
              className="ui-input flex-1"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Napisz wiadomosc..."
              onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() } }}
            />
            <button type="button" className="ui-btn-primary" onClick={send} disabled={sending}>Wyslij</button>
          </div>
        </div>
      </div>
    </div>
  )
}
