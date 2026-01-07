import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import LoginForm from './components/LoginForm'
import Home from './pages/Home'
import Help from './pages/Help'
import Categories from './pages/Categories'
import CategoryDetails from './pages/CategoryDetails'
import AddListing from './pages/AddListing'
import ListingDetails from './pages/ListingDetails'
import VerifyOtp from './pages/VerifyOtp'
import MyListings from './pages/MyListings'
import Profile from './pages/Profile'
import ResetPassword from './pages/ResetPassword'
import Favorites from './pages/Favorites'
import SellerProfile from './pages/SellerProfile'
import Messages from './pages/Messages'
import Chat from './pages/Chat'
import NewMessage from './pages/NewMessage'
import './App.css'

const API_BASE_URL = 'http://localhost:8081'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [showLoginModal, setShowLoginModal] = useState(false)

  useEffect(() => {
    let cancelled = false

    if (localStorage.getItem('forceLoggedOut') === '1') {
      setIsLoggedIn(false)
      setUsername('')
      return () => {
        cancelled = true
      }
    }

    const checkSession = async () => {
      try {
        const authRes = await fetch(`${API_BASE_URL}/user/is-authenticated`, {
          credentials: 'include',
        })

        if (!authRes.ok) return

        const isAuth = await authRes.json().catch(() => false)
        if (!isAuth) return

        const profileRes = await fetch(`${API_BASE_URL}/user/profile`, {
          credentials: 'include',
        })

        if (!profileRes.ok) {
          if (!cancelled) setIsLoggedIn(true)
          return
        }

        const profile = await profileRes.json().catch(() => null)
        if (cancelled) return

        setIsLoggedIn(true)
        setUsername(profile?.email || profile?.name || '')
      } catch {
        // cicho
      }
    }

    checkSession()

    return () => {
      cancelled = true
    }
  }, [])

  const handleLogin = (user) => {
    localStorage.removeItem('forceLoggedOut')
    setUsername(user)
    setIsLoggedIn(true)
    setShowLoginModal(false)
  }

  const handleLogout = () => {
    // Nie mamy endpointu logout w backendzie (cookie jwt jest HttpOnly), więc czyścimy token do WebSocket
    // i przeładowujemy aplikację, aby odciąć istniejące połączenia czatu od poprzedniego usera.
    localStorage.setItem('forceLoggedOut', '1')
    setIsLoggedIn(false)
    setUsername('')
    localStorage.removeItem('jwtToken')
    window.location.reload()
  }

  return (
    <BrowserRouter>
      <div className="app">
        <Header 
          isLoggedIn={isLoggedIn}
          username={username}
          onLoginClick={() => setShowLoginModal(true)}
          onLogout={handleLogout}
        />

        {showLoginModal && (
          <LoginForm 
            onLogin={handleLogin}
            onClose={() => setShowLoginModal(false)}
          />
        )}

        <Routes>
          <Route path="/" element={<Home isLoggedIn={isLoggedIn} />} />
          <Route path="/help" element={<Help />} />
          <Route path="/categories" element={<Categories />} />
          <Route path="/categories/:categoryId" element={<CategoryDetails />} />
          <Route path="/categories/:categoryId/:subCategoryId" element={<CategoryDetails />} />
          <Route path="/listing/:publicId" element={<ListingDetails />} />
          <Route path="/add-listing" element={<AddListing username={username} isLoggedIn={isLoggedIn} />} />
          <Route path="/verify-otp" element={<VerifyOtp />} />
          <Route path="/my-listings" element={<MyListings />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/favorites" element={<Favorites />} />
          <Route path="/users/:userId" element={<SellerProfile />} />
          <Route path="/messages" element={<Messages />} />
          <Route path="/messages/:conversationId" element={<Chat />} />
          <Route path="/messages/new/:listingId/:recipientId" element={<NewMessage />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default App
