import { createContext } from 'react';
import type { LoginRequest } from '../types/api';

export interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  apiCall: <T>(endpoint: string, method?: string, body?: unknown) => Promise<T | null>;
}

export const AuthContext = createContext<AuthContextType | null>(null);
