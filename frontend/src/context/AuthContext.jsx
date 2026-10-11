import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('smartpark_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [token, setToken] = useState(() => localStorage.getItem('smartpark_token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const savedToken = localStorage.getItem('smartpark_token');
      if (savedToken) {
        try {
          const res = await authApi.getCurrentUser();
          if (res.success && res.data) {
            setUser(res.data);
            localStorage.setItem('smartpark_user', JSON.stringify(res.data));
          }
        } catch {
          // Token invalid or expired
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (credentials) => {
    const res = await authApi.login(credentials);
    if (res.success && res.data) {
      const { token: jwt, id, email, fullName, studentId, role } = res.data;
      const userData = { id, email, fullName, studentId, role };
      setToken(jwt);
      setUser(userData);
      localStorage.setItem('smartpark_token', jwt);
      localStorage.setItem('smartpark_user', JSON.stringify(userData));
      return userData;
    }
    throw new Error(res.message || 'Login failed');
  };

  const register = async (data) => {
    const res = await authApi.register(data);
    return res;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('smartpark_token');
    localStorage.removeItem('smartpark_user');
  };

  const isAdmin = user?.role === 'ADMIN';
  const isStudent = user?.role === 'STUDENT';
  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated,
        isAdmin,
        isStudent,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
