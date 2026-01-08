import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'

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
      setError('Brak tokena. Zaloguj sie ponownie.')
      return
    }
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
    })
    client.onConnect = () => setConnecting(false)
    client.onStompError = () => { setConnecting(false); setError('Blad polaczenia.') }
    client.activate()
    clientRef.current = client
    return () => { client.deactivate(); clientRef.current = null }
  }, [token])

  const send = async () => {
    if (!text.trim()) return
    if (!clientRef.current?.connected) { setError('Brak polaczenia.'); return }
    setError('')
    clientRef.current.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({ listingId, recipientId, content: text.trim() }),
    })
    setTimeout(async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/user/conversations`, { credentials: 'include' })
        if (!res.ok) { navigate('/messages'); return }
        const data = await res.json().catch(() => [])
        const conv = Array.isArray(data) ? data.find((c) => String(c?.listing?.publicId) === String(listingId)) : null
        if (conv?.id) navigate(`/messages/${conv.id}`)
        else navigate('/messages')
      } catch { navigate('/messages') }
    }, 400)
  }

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <h1 className="ui-h1 text-center">Nowa wiadomosc</h1>
        <div className="text-center">
          <Link to="/messages" className="ui-btn">Wroc</Link>
        </div>

        {error && <p className="text-red-400">{error}</p>}

        <div className="ui-section">
          <p className="text-zinc-300 mb-3">Napisz do sprzedawcy</p>
          <div className="flex gap-2">
            <input
              className="ui-input flex-1"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Wiadomosc..."
              disabled={connecting}
            />
            <button type="button" className="ui-btn-primary" onClick={send} disabled={connecting}>
              {connecting ? 'Laczenie...' : 'Wyslij'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
