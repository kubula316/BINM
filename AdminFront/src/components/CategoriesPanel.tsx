import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../hooks/useAuth';
import type { Category, CreateCategoryRequest } from '../types/api';

interface CategoryTreeProps {
  categories: Category[];
  onSelect: (id: number) => void;
  onDelete: (id: number) => void;
  selectedId: number | null;
  level?: number;
}

function CategoryTree({ categories, onSelect, onDelete, selectedId, level = 0 }: CategoryTreeProps) {
  return (
    <div className={level > 0 ? 'ml-4 border-l border-gray-600 pl-2' : ''}>
      {categories.map((cat) => (
        <div key={cat.id} className="py-1">
          <div className="flex items-center gap-2">
            <button
              onClick={() => onSelect(cat.id)}
              className={`text-sm hover:underline ${
                selectedId === cat.id ? 'text-blue-400 font-bold' : 'text-gray-300'
              }`}
            >
              [{cat.id}] {cat.name}
            </button>
            <button
              onClick={() => onDelete(cat.id)}
              className="text-red-400 hover:text-red-300 text-xs px-1"
            >
              ×
            </button>
          </div>
          {cat.children && cat.children.length > 0 && (
            <CategoryTree
              categories={cat.children}
              onSelect={onSelect}
              onDelete={onDelete}
              selectedId={selectedId}
              level={level + 1}
            />
          )}
        </div>
      ))}
    </div>
  );
}

interface CategoriesPanelProps {
  onSelectCategory: (id: number | null) => void;
  selectedCategoryId: number | null;
}

export function CategoriesPanel({ onSelectCategory, selectedCategoryId }: CategoriesPanelProps) {
  const { apiCall } = useAuth();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newCatName, setNewCatName] = useState('');
  const [newCatParentId, setNewCatParentId] = useState('');
  const [editingCat, setEditingCat] = useState<{ id: number; name: string } | null>(null);

  const loadCategories = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await apiCall<Category[]>('/public/category/all');
      if (data) {
        setCategories(data);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd ładowania');
    } finally {
      setLoading(false);
    }
  }, [apiCall]);

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  const addCategory = async () => {
    if (!newCatName.trim()) return;

    try {
      const body: CreateCategoryRequest = {
        name: newCatName,
        parentId: newCatParentId ? parseInt(newCatParentId) : null,
      };
      await apiCall('/admin/categories', 'POST', body);
      setNewCatName('');
      setNewCatParentId('');
      loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd dodawania');
    }
  };

  const deleteCategory = async (id: number) => {
    if (!confirm(`Usunąć kategorię ${id}?`)) return;

    try {
      await apiCall(`/admin/categories/${id}`, 'DELETE');
      if (selectedCategoryId === id) {
        onSelectCategory(null);
      }
      loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd usuwania');
    }
  };

  const updateCategory = async () => {
    if (!editingCat || !editingCat.name.trim()) return;

    try {
      await apiCall(`/admin/categories/${editingCat.id}`, 'PUT', { name: editingCat.name });
      setEditingCat(null);
      loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd aktualizacji');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">Zarządzanie Kategoriami</h2>
        <button
          onClick={loadCategories}
          disabled={loading}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-50"
        >
          {loading ? 'Ładowanie...' : 'Odśwież'}
        </button>
      </div>

      {error && (
        <div className="bg-red-500/20 border border-red-500 text-red-400 px-4 py-2 rounded-lg">
          {error}
        </div>
      )}

      <div className="bg-gray-800 rounded-lg p-4">
        <h3 className="text-lg font-medium text-white mb-3">Drzewo Kategorii</h3>
        <div className="max-h-64 overflow-y-auto">
          {categories.length === 0 ? (
            <p className="text-gray-400">Brak kategorii</p>
          ) : (
            <CategoryTree
              categories={categories}
              onSelect={onSelectCategory}
              onDelete={deleteCategory}
              selectedId={selectedCategoryId}
            />
          )}
        </div>
      </div>

      <div className="bg-gray-800 rounded-lg p-4">
        <h3 className="text-lg font-medium text-white mb-3">Dodaj Kategorię</h3>
        <div className="space-y-3">
          <div>
            <label className="block text-gray-300 text-sm mb-1">Nazwa</label>
            <input
              type="text"
              value={newCatName}
              onChange={(e) => setNewCatName(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
              placeholder="Nazwa kategorii"
            />
          </div>
          <div>
            <label className="block text-gray-300 text-sm mb-1">ID Rodzica (opcjonalne)</label>
            <input
              type="number"
              value={newCatParentId}
              onChange={(e) => setNewCatParentId(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
              placeholder="ID kategorii nadrzędnej"
            />
          </div>
          <button
            onClick={addCategory}
            className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg"
          >
            Dodaj
          </button>
        </div>
      </div>

      {selectedCategoryId && (
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-medium text-white mb-3">
            Edytuj Kategorię (ID: {selectedCategoryId})
          </h3>
          <div className="space-y-3">
            <div>
              <label className="block text-gray-300 text-sm mb-1">Nowa nazwa</label>
              <input
                type="text"
                value={editingCat?.name || ''}
                onChange={(e) => setEditingCat({ id: selectedCategoryId, name: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
                placeholder="Nowa nazwa"
              />
            </div>
            <button
              onClick={updateCategory}
              disabled={!editingCat?.name}
              className="px-4 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded-lg disabled:opacity-50"
            >
              Zapisz zmiany
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
