import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/axiosConfig';

const AuthContext = createContext(null);

const ROL_LABELS = {
  YONETICI: 'Yönetici',
  KAYIT_GOREVLISI: 'Kayıt Görevlisi',
  RANDEVU_GOREVLISI: 'Randevu Görevlisi',
  DOKTOR: 'Doktor',
  VEZNEDAR: 'Veznedar',
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const savedToken = localStorage.getItem('token');
    if (savedUser && savedToken) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, sifre) => {
    const response = await authAPI.login({ email, sifre });
    const data = response.data;
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    setUser(data);
    return data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const hasRole = (...roles) => {
    return user && roles.includes(user.rol);
  };

  const rolLabel = user ? ROL_LABELS[user.rol] || user.rol : '';

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole, rolLabel }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}

export { ROL_LABELS };
