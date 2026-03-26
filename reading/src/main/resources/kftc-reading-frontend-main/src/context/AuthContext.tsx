import { createContext, useContext, useState } from 'react';
import axios from 'axios';

const TOKEN_KEY = 'kftc_admin_token';

interface AuthContextValue {
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    () => !!localStorage.getItem(TOKEN_KEY),
  );

  function login(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setIsAuthenticated(true);
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    delete axios.defaults.headers.common['Authorization'];
    setIsAuthenticated(false);
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

// Restore axios header on page reload if token exists in storage
const stored = localStorage.getItem(TOKEN_KEY);
if (stored) {
  axios.defaults.headers.common['Authorization'] = `Bearer ${stored}`;
}

// 401 응답 시 토큰 만료 처리 — 자동 로그아웃
axios.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      delete axios.defaults.headers.common['Authorization'];
      if (window.location.pathname.startsWith('/admin') && window.location.pathname !== '/admin/login') {
        window.location.href = '/admin/login';
      }
    }
    return Promise.reject(error);
  },
);
