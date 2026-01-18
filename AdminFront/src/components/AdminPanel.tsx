import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { ListingsPanel } from './ListingsPanel';
import { CategoriesPanel } from './CategoriesPanel';
import { AttributesPanel } from './AttributesPanel';

type Tab = 'listings' | 'categories' | 'attributes';

export function AdminPanel() {
  const { logout } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('listings');
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);

  const tabs: { id: Tab; label: string }[] = [
    { id: 'listings', label: 'Moderacja Ogłoszeń' },
    { id: 'categories', label: 'Kategorie' },
    { id: 'attributes', label: 'Atrybuty' },
  ];

  return (
    <div className="min-h-screen bg-gray-900">
      <header className="bg-gray-800 border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-white">Panel Administratora</h1>
          <button
            onClick={logout}
            className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm"
          >
            Wyloguj
          </button>
        </div>
      </header>

      <nav className="bg-gray-800/50 border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex gap-1">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-3 text-sm font-medium transition-colors ${
                  activeTab === tab.id
                    ? 'text-blue-400 border-b-2 border-blue-400'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 py-6">
        {activeTab === 'listings' && <ListingsPanel />}
        
        {activeTab === 'categories' && (
          <CategoriesPanel
            onSelectCategory={setSelectedCategoryId}
            selectedCategoryId={selectedCategoryId}
          />
        )}
        
        {activeTab === 'attributes' && (
          <AttributesPanel categoryId={selectedCategoryId} />
        )}
      </main>
    </div>
  );
}
