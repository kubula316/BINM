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

  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-4">
        <div className="text-center">
          <h1 className="ui-h1">Kategorie</h1>
          <p className="ui-muted mt-1">Wybierz kategorie, aby przegladac ogloszenia.</p>
        </div>

        <section className="ui-section">
          {loading && <p className="ui-muted">Ladowanie kategorii...</p>}
          {error && <p className="text-sm text-red-400">{error}</p>}

          {!loading && !error && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {Array.isArray(categories) &&
                categories
                  .filter((c) => c && c.parentId === null)
                  .map((node) => (
                    <Link
                      key={node.id}
                      to={`/categories/${node.id}`}
                      className="flex items-center gap-3 rounded-lg border border-zinc-700 bg-zinc-800 p-3 transition hover:bg-zinc-700"
                    >
                      <div className="flex h-10 w-10 flex-none items-center justify-center rounded-lg bg-zinc-700">
                        {node.imageUrl ? (
                          <img src={node.imageUrl} alt={node.name} className="h-6 w-6 object-contain" />
                        ) : (
                          <span className="text-sm font-semibold text-zinc-300">{String(node.name || '?').slice(0, 1)}</span>
                        )}
                      </div>
                      <div className="min-w-0">
                        <div className="truncate font-medium text-zinc-100">{node.name}</div>
                        {node.description && <div className="truncate text-sm text-zinc-400">{node.description}</div>}
                      </div>
                    </Link>
                  ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

export default Categories
