import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../hooks/useAuth';
import type { WaitingListing, PaginatedResponse } from '../types/api';

export function ListingsPanel() {
  const { apiCall } = useAuth();
  const [listings, setListings] = useState<WaitingListing[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedListing, setSelectedListing] = useState<WaitingListing | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadListings = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await apiCall<PaginatedResponse<WaitingListing>>(
        `/admin/listings/waiting?page=${page}&size=10`
      );
      if (data) {
        setListings(data.content);
        setTotalPages(data.totalPages);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd ładowania');
    } finally {
      setLoading(false);
    }
  }, [apiCall, page]);

  useEffect(() => {
    loadListings();
  }, [loadListings]);

  const loadListingDetails = async (publicId: string) => {
    try {
      const data = await apiCall<WaitingListing>(`/admin/listings/waiting/${publicId}`);
      if (data) {
        setSelectedListing(data);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd ładowania szczegółów');
    }
  };

  const approveListing = async (publicId: string) => {
    try {
      await apiCall(`/admin/listings/${publicId}/approve`, 'POST');
      setSelectedListing(null);
      loadListings();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd zatwierdzania');
    }
  };

  const rejectListing = async (publicId: string) => {
    const reason = prompt('Podaj powód odrzucenia:');
    if (!reason) return;

    try {
      await apiCall(`/admin/listings/${publicId}/reject`, 'POST', { reason });
      setSelectedListing(null);
      loadListings();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd odrzucania');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">Moderacja Ogłoszeń (WAITING)</h2>
        <button
          onClick={loadListings}
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

      <div className="bg-gray-800 rounded-lg overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">ID</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Tytuł</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Cena</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Sprzedawca</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Akcje</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-700">
            {listings.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                  Brak oczekujących ogłoszeń
                </td>
              </tr>
            ) : (
              listings.map((listing) => (
                <tr key={listing.publicId} className="hover:bg-gray-700/50">
                  <td className="px-4 py-3 text-sm text-gray-300">
                    <button
                      onClick={() => loadListingDetails(listing.publicId)}
                      className="text-blue-400 hover:underline"
                    >
                      {listing.publicId.substring(0, 8)}...
                    </button>
                  </td>
                  <td className="px-4 py-3 text-sm text-white">{listing.title}</td>
                  <td className="px-4 py-3 text-sm text-gray-300">
                    {listing.priceAmount} {listing.currency}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-300">{listing.seller.name}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => approveListing(listing.publicId)}
                        className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-sm rounded"
                      >
                        ✓
                      </button>
                      <button
                        onClick={() => rejectListing(listing.publicId)}
                        className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white text-sm rounded"
                      >
                        ✗
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded disabled:opacity-50"
          >
            Poprzednia
          </button>
          <span className="px-4 py-2 text-gray-300">
            Strona {page + 1} z {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded disabled:opacity-50"
          >
            Następna
          </button>
        </div>
      )}

      {selectedListing && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-xl font-bold text-white">{selectedListing.title}</h3>
              <button
                onClick={() => setSelectedListing(null)}
                className="text-gray-400 hover:text-white text-2xl"
              >
                ×
              </button>
            </div>

            <div className="space-y-4 text-gray-300">
              <p><strong>Opis:</strong> {selectedListing.description || 'Brak'}</p>
              <p><strong>Cena:</strong> {selectedListing.priceAmount} {selectedListing.currency}</p>
              <p><strong>Lokalizacja:</strong> {selectedListing.locationCity || 'Nie podano'}</p>

              {selectedListing.attributes && selectedListing.attributes.length > 0 && (
                <div>
                  <strong>Atrybuty:</strong>
                  <ul className="mt-2 space-y-1 ml-4">
                    {selectedListing.attributes.map((attr, i) => (
                      <li key={i}>
                        {attr.label}: {attr.stringValue || attr.numberValue || attr.enumLabel || String(attr.booleanValue)}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {selectedListing.media && selectedListing.media.length > 0 && (
                <div>
                  <strong>Zdjęcia:</strong>
                  <div className="flex gap-2 mt-2 overflow-x-auto">
                    {selectedListing.media.map((m, i) => (
                      <img key={i} src={m.url} alt="" className="h-24 rounded" />
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="flex gap-2 mt-6 justify-end">
              <button
                onClick={() => approveListing(selectedListing.publicId)}
                className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg"
              >
                Zatwierdź
              </button>
              <button
                onClick={() => rejectListing(selectedListing.publicId)}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg"
              >
                Odrzuć
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
