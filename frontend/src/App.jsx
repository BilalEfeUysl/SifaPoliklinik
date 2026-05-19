import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';
import Layout from './components/layout/Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import DoktorDashboardPage from './pages/DoktorDashboardPage';
import HastaListPage from './pages/HastaListPage';
import RandevuListPage from './pages/RandevuListPage';
import MuayenePage from './pages/MuayenePage';
import OdemePage from './pages/OdemePage';
import YoneticiPage from './pages/YoneticiPage';

function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;
  return user ? children : <Navigate to="/login" />;
}

function HomeRedirect() {
  const { user } = useAuth();
  if (user?.rol === 'KAYIT_GOREVLISI') return <Navigate to="/hastalar" replace />;
  if (user?.rol === 'VEZNEDAR') return <Navigate to="/odemeler" replace />;
  if (user?.rol === 'DOKTOR') return <DoktorDashboardPage />;
  return <DashboardPage />;
}

function AppRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={
        user ? <Navigate to="/" /> : <LoginPage />
      } />
      <Route path="/" element={
        <PrivateRoute><Layout /></PrivateRoute>
      }>
        <Route index element={<HomeRedirect />} />
        <Route path="hastalar" element={<HastaListPage />} />
        <Route path="randevular" element={<RandevuListPage />} />
        <Route path="muayeneler" element={<MuayenePage />} />
        <Route path="odemeler" element={<OdemePage />} />
        <Route path="yonetici" element={<YoneticiPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
