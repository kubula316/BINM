import { useState, useCallback, type ReactNode } from 'react';
import type {LoginRequest, LoginResponse} from '../types/api';
import { AuthContext } from './authContextType';

const API_URL = 'http://localhost:8081';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('admin_token');
  });

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await fetch(`${API_URL}/public/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Błąd logowania' }));
      throw new Error(error.message || 'Błąd logowania');
    }

    const data: LoginResponse = await response.json();
    setToken(data.token);
    localStorage.setItem('admin_token', data.token);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    localStorage.removeItem('admin_token');
  }, []);

  const apiCall = useCallback(async <T,>(
    endpoint: string,
    method: string = 'GET',
    body?: unknown
  ): Promise<T | null> => {
    if (!token) {
      throw new Error('Brak tokenu autoryzacji');
    }

    const headers: HeadersInit = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    };

    const options: RequestInit = { method, headers };
    if (body) {
      options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_URL}${endpoint}`, options);

    if (response.status === 204) {
      return null;
    }

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Błąd API' }));
      throw new Error(error.message || `Błąd: ${response.status}`);
    }

    return response.json();
  }, [token]);

  return (
    <AuthContext.Provider value={{
      token,
      isAuthenticated: !!token,
      login,
      logout,
      apiCall,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
