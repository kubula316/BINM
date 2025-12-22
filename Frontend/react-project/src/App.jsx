import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import LoginForm from './components/LoginForm'
import Home from './pages/Home'
import Help from './pages/Help'
import Categories from './pages/Categories'
import CategoryDetails from './pages/CategoryDetails'
import AddListing from './pages/AddListing'
import ListingDetails from './pages/ListingDetails'
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
          <Route path="/" element={<Home isLoggedIn={isLoggedIn} />} />
          <Route path="/help" element={<Help />} />
          <Route path="/categories" element={<Categories />} />
          <Route path="/categories/:categoryId" element={<CategoryDetails />} />
          <Route path="/categories/:categoryId/:subCategoryId" element={<CategoryDetails />} />
          <Route path="/listing/:publicId" element={<ListingDetails />} />
          <Route path="/add-listing" element={<AddListing username={username} isLoggedIn={isLoggedIn} />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default App
