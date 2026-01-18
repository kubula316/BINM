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
          setError('Nie udalo sie pobrac kategorii')
          return
        }

        const data = await response.json()
        setCategories(data || [])
      } catch {
        setError('Brak polaczenia z serwerem')
      } finally {
        setLoading(false)
      }
    }

    fetchCategories()
  }, [])

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white">Kategorie</h1>
            <p className="text-slate-400 mt-1">Przegladaj ogloszenia wedlug kategorii</p>
          </div>
          <Link to="/" className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-800/50 px-4 py-2.5 text-sm font-medium text-slate-300 transition-all hover:bg-slate-700 hover:text-white">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Wroc
          </Link>
        </div>

        <div className="rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 sm:p-8">
          {loading && (
            <div className="flex items-center justify-center py-12">
              <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
              <span className="ml-3 text-slate-400">Ladowanie kategorii...</span>
            </div>
          )}
          {error && (
            <div className="rounded-xl bg-red-500/10 border border-red-500/20 px-4 py-3 text-sm text-red-400">{error}</div>
          )}

          {!loading && !error && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {Array.isArray(categories) &&
                categories
                  .filter((c) => c && c.parentId === null)
                  .map((node) => (
                    <Link
                      key={node.id}
                      to={`/categories/${node.id}`}
                      className="group flex items-center gap-4 rounded-xl border border-slate-700/50 bg-slate-800/50 p-4 transition-all hover:bg-slate-700/50 hover:border-slate-600 hover:shadow-lg hover:shadow-slate-950/50"
                    >
                      <div className="flex h-14 w-14 flex-none items-center justify-center rounded-xl bg-slate-700/50 group-hover:bg-emerald-500/20 transition-colors">
                        {node.imageUrl ? (
                          <img src={node.imageUrl} alt={node.name} className="h-8 w-8 object-contain" />
                        ) : (
                          <span className="text-lg font-bold text-slate-400 group-hover:text-emerald-400 transition-colors">{String(node.name || '?').slice(0, 1)}</span>
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="truncate font-semibold text-white group-hover:text-emerald-400 transition-colors">{node.name}</div>
                        {node.description && <div className="truncate text-sm text-slate-400">{node.description}</div>}
                      </div>
                      <svg className="w-5 h-5 text-slate-500 group-hover:text-emerald-400 transition-colors" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7" /></svg>
                    </Link>
                  ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Categories
