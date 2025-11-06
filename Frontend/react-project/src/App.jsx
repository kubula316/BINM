import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import LoginForm from './components/LoginForm'
import Home from './pages/Home'
import Help from './pages/Help'
import Categories from './pages/Categories'
import './App.css'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [showLoginModal, setShowLoginModal] = useState(false)

  const handleLogin = (user) => {
    setUsername(user)
    setIsLoggedIn(true)
    setShowLoginModal(false)
  }

  const handleLogout = () => {
    setIsLoggedIn(false)
    setUsername('')
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
          <Route path="/" element={<Home />} />
          <Route path="/help" element={<Help />} />
          <Route path="/categories" element={<Categories />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default App
