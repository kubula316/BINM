import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { API_BASE_URL } from '../config'


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
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-2xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl sm:text-3xl font-bold text-white">Nowa wiadomosc</h1>
          <Link to="/messages" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        {error && <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>}

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
            </div>
            <div>
              <p className="font-medium text-white">Napisz do sprzedawcy</p>
              <p className="text-sm text-slate-400">Rozpocznij nowa konwersacje</p>
            </div>
          </div>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Twoja wiadomosc</label>
              <textarea
                className="w-full rounded-xl border border-slate-700/50 bg-slate-900/50 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 outline-none transition-all focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20 resize-none"
                rows={4}
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Czesc! Interesuje mnie to ogloszenie..."
                disabled={connecting}
              />
            </div>
            <button 
              type="button" 
              className="w-full inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50" 
              onClick={send} 
              disabled={connecting}
            >
              {connecting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                  Laczenie...
                </>
              ) : (
                <>
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
                  Wyslij wiadomosc
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
