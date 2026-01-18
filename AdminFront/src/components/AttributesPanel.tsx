import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../hooks/useAuth';
import type { CategoryAttribute, AttributeOption } from '../types/api';

interface AttributesPanelProps {
  categoryId: number | null;
}

export function AttributesPanel({ categoryId }: AttributesPanelProps) {
  const { apiCall } = useAuth();
  const [attributes, setAttributes] = useState<CategoryAttribute[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [newAttr, setNewAttr] = useState({
    key: '',
    label: '',
    type: 'STRING' as CategoryAttribute['type'],
  });

  const [newOption, setNewOption] = useState({
    attributeId: null as number | null,
    value: '',
    label: '',
    sortOrder: 0,
  });

  const loadAttributes = useCallback(async () => {
    if (!categoryId) return;
    
    setLoading(true);
    setError('');
    try {
      const data = await apiCall<CategoryAttribute[]>(
        `/public/category/attributes?categoryId=${categoryId}`
      );
      if (data) {
        setAttributes(data);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd ładowania');
    } finally {
      setLoading(false);
    }
  }, [apiCall, categoryId]);

  useEffect(() => {
    if (categoryId) {
      loadAttributes();
    } else {
      setAttributes([]);
    }
  }, [categoryId, loadAttributes]);

  const addAttribute = async () => {
    if (!categoryId || !newAttr.key.trim() || !newAttr.label.trim()) return;

    try {
      const body = {
        key: newAttr.key,
        label: newAttr.label,
        type: newAttr.type,
        unit: null,
        sortOrder: 0,
        active: true,
        options: [],
      };
      await apiCall(`/admin/categories/${categoryId}/attributes`, 'POST', body);
      setNewAttr({ key: '', label: '', type: 'STRING' });
      loadAttributes();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd dodawania');
    }
  };

  const addOption = async () => {
    if (!newOption.attributeId || !newOption.value.trim() || !newOption.label.trim()) return;

    try {
      await apiCall(`/admin/attributes/${newOption.attributeId}/options`, 'POST', {
        value: newOption.value,
        label: newOption.label,
        sortOrder: newOption.sortOrder,
      });
      setNewOption({ attributeId: null, value: '', label: '', sortOrder: 0 });
      loadAttributes();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd dodawania opcji');
    }
  };

  const deleteOption = async (optionId: number) => {
    if (!confirm('Usunąć tę opcję?')) return;

    try {
      await apiCall(`/admin/attributes/options/${optionId}`, 'DELETE');
      loadAttributes();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd usuwania');
    }
  };

  if (!categoryId) {
    return (
      <div className="bg-gray-800 rounded-lg p-4">
        <h2 className="text-xl font-bold text-white mb-4">Atrybuty Kategorii</h2>
        <p className="text-gray-400">Wybierz kategorię z drzewa aby zobaczyć atrybuty</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">
          Atrybuty Kategorii (ID: {categoryId})
        </h2>
        <button
          onClick={loadAttributes}
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
        <h3 className="text-lg font-medium text-white mb-3">Lista Atrybutów</h3>
        {attributes.length === 0 ? (
          <p className="text-gray-400">Brak atrybutów dla tej kategorii</p>
        ) : (
          <div className="space-y-3">
            {attributes.map((attr) => (
              <div key={attr.id || attr.key} className="bg-gray-700 rounded p-3">
                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-white font-medium">{attr.label}</span>
                    <span className="text-gray-400 text-sm ml-2">
                      [{attr.key}] ({attr.type})
                    </span>
                  </div>
                  {attr.type === 'ENUM' && attr.id && (
                    <button
                      onClick={() => setNewOption({ ...newOption, attributeId: attr.id! })}
                      className="text-blue-400 hover:text-blue-300 text-sm"
                    >
                      + Opcja
                    </button>
                  )}
                </div>
                
                {attr.options && attr.options.length > 0 && (
                  <div className="mt-2 pl-4 border-l border-gray-600">
                    <span className="text-gray-400 text-sm">Opcje:</span>
                    <ul className="mt-1 space-y-1">
                      {attr.options.map((opt: AttributeOption) => (
                        <li key={opt.id || opt.value} className="flex items-center gap-2 text-sm">
                          <span className="text-gray-300">{opt.label} ({opt.value})</span>
                          {opt.id && (
                            <button
                              onClick={() => deleteOption(opt.id!)}
                              className="text-red-400 hover:text-red-300"
                            >
                              ×
                            </button>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="bg-gray-800 rounded-lg p-4">
        <h3 className="text-lg font-medium text-white mb-3">Dodaj Atrybut</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div>
            <label className="block text-gray-300 text-sm mb-1">Klucz</label>
            <input
              type="text"
              value={newAttr.key}
              onChange={(e) => setNewAttr({ ...newAttr, key: e.target.value })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
              placeholder="np. color"
            />
          </div>
          <div>
            <label className="block text-gray-300 text-sm mb-1">Etykieta</label>
            <input
              type="text"
              value={newAttr.label}
              onChange={(e) => setNewAttr({ ...newAttr, label: e.target.value })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
              placeholder="np. Kolor"
            />
          </div>
          <div>
            <label className="block text-gray-300 text-sm mb-1">Typ</label>
            <select
              value={newAttr.type}
              onChange={(e) => setNewAttr({ ...newAttr, type: e.target.value as CategoryAttribute['type'] })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
            >
              <option value="STRING">STRING</option>
              <option value="NUMBER">NUMBER</option>
              <option value="BOOLEAN">BOOLEAN</option>
              <option value="ENUM">ENUM</option>
            </select>
          </div>
        </div>
        <button
          onClick={addAttribute}
          className="mt-3 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg"
        >
          Dodaj Atrybut
        </button>
      </div>

      {newOption.attributeId && (
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-medium text-white mb-3">
            Dodaj Opcję do Atrybutu (ID: {newOption.attributeId})
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-gray-300 text-sm mb-1">Wartość</label>
              <input
                type="text"
                value={newOption.value}
                onChange={(e) => setNewOption({ ...newOption, value: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
                placeholder="np. red"
              />
            </div>
            <div>
              <label className="block text-gray-300 text-sm mb-1">Etykieta</label>
              <input
                type="text"
                value={newOption.label}
                onChange={(e) => setNewOption({ ...newOption, label: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
                placeholder="np. Czerwony"
              />
            </div>
            <div>
              <label className="block text-gray-300 text-sm mb-1">Kolejność</label>
              <input
                type="number"
                value={newOption.sortOrder}
                onChange={(e) => setNewOption({ ...newOption, sortOrder: parseInt(e.target.value) || 0 })}
                className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white"
              />
            </div>
          </div>
          <div className="flex gap-2 mt-3">
            <button
              onClick={addOption}
              className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg"
            >
              Dodaj Opcję
            </button>
            <button
              onClick={() => setNewOption({ attributeId: null, value: '', label: '', sortOrder: 0 })}
              className="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded-lg"
            >
              Anuluj
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
