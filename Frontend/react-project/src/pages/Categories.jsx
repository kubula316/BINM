import './Categories.css'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const API_BASE_URL = 'http://localhost:8081'

function Categories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true)
        setError('')
        const response = await fetch(`${API_BASE_URL}/public/category/all`)

        if (!response.ok) {
          setError('Nie udało się pobrać kategorii')
          return
        }

        const data = await response.json()
        setCategories(data || [])
      } catch {
        setError('Brak połączenia z serwerem')
      } finally {
        setLoading(false)
      }
    }

    fetchCategories()
  }, [])

  const renderTopLevelCategories = (nodes) => {
    if (!Array.isArray(nodes)) return null

    return nodes
      .filter((node) => node.parentId === null)
      .map((node) => (
        <Link key={node.id} className="category-card" to={`/categories/${node.id}`}>
          {node.imageUrl && (
            <div className="category-icon-wrapper">
              <img src={node.imageUrl} alt={node.name} className="category-icon" />
            </div>
          )}
          <h3>{node.name}</h3>
          {node.description && <p>{node.description}</p>}
        </Link>
      ))
  }

  return (
    <div className="categories-page">
      <div className="categories-container">
        <h1>Kategorie produktów</h1>

        {loading && <p className="subtitle">Ładowanie kategorii...</p>}
        {error && <p className="subtitle" style={{ color: '#ff6b6b' }}>{error}</p>}

        <section className="electronics-section">
          <div className="categories-grid">
            {!loading && !error && renderTopLevelCategories(categories)}
          </div>
        </section>
      </div>
    </div>
  )
}

export default Categories
