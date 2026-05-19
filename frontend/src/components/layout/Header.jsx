import { useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const PAGE_TITLES = {
  '/': 'Gösterge Paneli',
  '/hastalar': 'Hasta Yönetimi',
  '/randevular': 'Randevu Yönetimi',
  '/muayeneler': 'Muayene Kayıtları',
  '/odemeler': 'Ödeme İşlemleri',
  '/yonetici': 'Yönetici Paneli',
};

export default function Header() {
  const location = useLocation();
  const { rolLabel } = useAuth();
  const title = PAGE_TITLES[location.pathname] || 'Şifa Polikliniği';

  return (
    <header className="header">
      <h1 className="header-title">{title}</h1>
      <div className="header-actions">
        <span style={{ fontSize: '12px', color: 'var(--gray-500)', marginRight: '8px' }}>
          {rolLabel}
        </span>
      </div>
    </header>
  );
}
