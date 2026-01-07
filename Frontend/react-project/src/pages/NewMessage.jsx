import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import './Categories.css'

const API_BASE_URL = 'http://localhost:8081'

export default function NewMessage() {
  const navigate = useNavigate()
  const { listingId, recipientId } = useParams()
  const [text, setText] = useState('')
  const [error, setError] = useState('')
  const [connecting, setConnecting] = useState(true)
  const clientRef = useRef(null)

  const token = useMemo(() => localStorage.getItem('jwtToken') || '', [])

  useEffect(() => {
    if (!token) {
      setConnecting(false)
      setError('Brak tokena do czatu. Zaloguj się ponownie.')
      return
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 3000,
    })

    client.onConnect = () => {
      setConnecting(false)
    }

    client.onStompError = () => {
      setConnecting(false)
      setError('Nie udało się połączyć z czatem (sprawdź logowanie).')
    }

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [token])

  const send = async () => {
    if (!text.trim()) return
    if (!clientRef.current || !clientRef.current.connected) {
      setError('Brak połączenia z czatem.')
      return
    }

    setError('')
    clientRef.current.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        listingId,
        recipientId,
        content: text.trim(),
      }),
    })

    // Po wysłaniu: odśwież listę konwersacji i znajdź tę od tego listingId
    setTimeout(async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/user/conversations`, { credentials: 'include' })
        if (!res.ok) {
          navigate('/messages')
          return
        }
        const data = await res.json().catch(() => [])
        const conv = Array.isArray(data)
          ? data.find((c) => String(c?.listing?.publicId) === String(listingId))
          : null
        if (conv?.id) navigate(`/messages/${conv.id}`)
        else navigate('/messages')
      } catch {
        navigate('/messages')
      }
    }, 400)
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Nowa wiadomość</h1>
        <section className="electronics-section">
          <Link to="/messages" className="item-image-link">Wróć</Link>

          {error && <p style={{ color: '#ff6b6b', marginTop: 12 }}>{error}</p>}

          <div className="item-card" style={{ marginTop: 16 }}>
            <div style={{ color: '#fff', marginBottom: 10 }}>
              Napisz do sprzedawcy
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Wiadomość..."
                style={{ flex: 1 }}
                disabled={connecting}
              />
              <button
                type="button"
                className="filters-button apply"
                onClick={send}
                disabled={connecting}
              >
                {connecting ? 'Łączenie...' : 'Wyślij'}
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
