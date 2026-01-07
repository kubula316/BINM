import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import './Categories.css'

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
          if (!cancelled) setError('Nie udało się pobrać danych czatu (zaloguj się).')
          return
        }

        const profile = await profileRes.json().catch(() => null)
        const conversations = await convRes.json().catch(() => [])
        const msgPage = await msgRes.json().catch(() => null)

        if (cancelled) return

        setMeId(profile?.userId || '')
        setConversation(Array.isArray(conversations) ? conversations.find((c) => c.id === convId) : null)
        const content = Array.isArray(msgPage?.content) ? msgPage.content : []
        // backend zwraca DESC, a my chcemy rosnąco
        setMessages(content.slice().reverse())

        fetch(`${API_BASE_URL}/user/conversations/${convId}/read`, {
          method: 'PATCH',
          credentials: 'include',
        }).catch(() => null)
      } catch {
        if (!cancelled) setError('Brak połączenia z serwerem')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    if (Number.isFinite(convId)) fetchAll()
    return () => {
      cancelled = true
    }
  }, [convId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages.length])

  useEffect(() => {
    const token = localStorage.getItem('jwtToken')
    if (!token) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 3000,
    })

    client.onConnect = () => {
      client.subscribe('/user/queue/messages', (frame) => {
        try {
          const payload = JSON.parse(frame.body)
          const incomingConvId = payload?.conversation?.id
          if (incomingConvId && incomingConvId !== convId) return
          setMessages((prev) => [...prev, payload])
        } catch {
          // ignore
        }
      })
    }

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [convId])

  const send = async () => {
    if (!text.trim()) return
    if (!listingId) {
      setError('Brak listingId dla konwersacji')
      return
    }
    if (!recipientId) {
      setError('Nie udało się ustalić odbiorcy wiadomości')
      return
    }
    if (!clientRef.current || !clientRef.current.connected) {
      setError('Brak połączenia z czatem (zaloguj się ponownie).')
      return
    }

    try {
      setSending(true)
      setError('')

      // optymistycznie dodaj wiadomość od razu (backend wysyła WS tylko do odbiorcy)
      const optimistic = {
        id: `local-${Date.now()}`,
        senderId: meId,
        recipientId,
        content: text.trim(),
        createdAt: new Date().toISOString(),
      }
      setMessages((prev) => [...prev, optimistic])

      clientRef.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({
          listingId,
          recipientId,
          content: text.trim(),
        }),
      })
      setText('')
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return (
      <div className="categories-page">
        <div className="categories-container">
          <p style={{ color: '#fff' }}>Ładowanie czatu...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Chat</h1>

        <section className="electronics-section">
          <Link to="/messages" className="item-image-link">Wróć</Link>

          {conversation && (
            <div style={{ marginTop: 12, color: '#fff', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
              <div>
                <strong>{conversation.listing?.title}</strong> — {conversation.otherParticipantName}
              </div>
              {listingId && (
                <Link
                  to={`/listing/${listingId}`}
                  className="item-image-link"
                  style={{ textDecoration: 'none' }}
                >
                  Przejdź do ogłoszenia
                </Link>
              )}
            </div>
          )}

          {error && <p style={{ color: '#ff6b6b', marginTop: 12 }}>{error}</p>}

          <div className="item-card" style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 420, overflow: 'auto' }}>
              {messages.length === 0 ? (
                <div style={{ color: '#fff' }}>Brak wiadomości.</div>
              ) : (
                messages.map((m) => {
                  const mine = meId && m.senderId === meId
                  const authorLabel = mine
                    ? 'Ty'
                    : (conversation?.otherParticipantName || 'Rozmówca')
                  return (
                    <div
                      key={m.id || `${m.senderId}-${m.createdAt}-${m.content}`}
                      style={{
                        alignSelf: mine ? 'flex-end' : 'flex-start',
                        maxWidth: '80%',
                        background: mine ? 'rgba(59,130,246,0.25)' : 'rgba(255,255,255,0.08)',
                        border: '1px solid rgba(148, 163, 184, 0.25)',
                        borderRadius: 12,
                        padding: '8px 10px',
                        color: '#fff',
                      }}
                    >
                      <div style={{ fontSize: 11, opacity: 0.8, marginBottom: 4 }}>
                        {authorLabel}
                      </div>
                      <div
                        style={{
                          fontSize: 14,
                          whiteSpace: 'pre-wrap',
                          overflowWrap: 'anywhere',
                          wordBreak: 'break-word',
                        }}
                      >
                        {m.content}
                      </div>
                      {m.createdAt && (
                        <div style={{ fontSize: 11, opacity: 0.75, marginTop: 4 }}>
                          {new Date(m.createdAt).toLocaleString('pl-PL')}
                        </div>
                      )}
                    </div>
                  )
                })
              )}
              <div ref={bottomRef} />
            </div>

            <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
              <input
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Napisz wiadomość..."
                style={{ flex: 1 }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault()
                    send()
                  }
                }}
              />
              <button
                type="button"
                className="filters-button apply"
                onClick={send}
                disabled={sending}
              >
                Wyślij
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
