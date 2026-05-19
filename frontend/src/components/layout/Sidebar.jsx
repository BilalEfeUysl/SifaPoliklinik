import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard, Users, CalendarDays, Stethoscope,
  CreditCard, Settings, LogOut
} from 'lucide-react';

const NAV_ITEMS = [
  {
    section: 'Ana Menü',
    items: [
      { to: '/', icon: LayoutDashboard, label: 'Gösterge Paneli', roles: ['YONETICI', 'RANDEVU_GOREVLISI', 'DOKTOR'] },
    ]
  },
  {
    section: 'İşlemler',
    items: [
      { to: '/hastalar', icon: Users, label: 'Hasta Sorgulama ve Yönetimi', roles: ['KAYIT_GOREVLISI', 'YONETICI', 'DOKTOR', 'RANDEVU_GOREVLISI'] },
      { to: '/randevular', icon: CalendarDays, label: 'Randevu Yönetimi', roles: ['RANDEVU_GOREVLISI', 'YONETICI', 'DOKTOR'] },
      { to: '/muayeneler', icon: Stethoscope, label: 'Muayene', roles: ['DOKTOR', 'YONETICI'] },
      { to: '/odemeler', icon: CreditCard, label: 'Ödeme İşlemleri', roles: ['VEZNEDAR', 'YONETICI'] },
    ]
  },
  {
    section: 'Yönetim',
    items: [
      { to: '/yonetici', icon: Settings, label: 'Yönetici Paneli', roles: ['YONETICI'] },
    ]
  }
];

export default function Sidebar() {
  const { user, logout, rolLabel } = useAuth();
  const location = useLocation();

  const userInitials = user ? (user.ad[0] + user.soyad[0]).toUpperCase() : '';

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-brand-icon">Ş</div>
        <div className="sidebar-brand-text">
          <h2>Şifa Polikliniği</h2>
          <span>Bilgi Sistemi</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        {NAV_ITEMS.map((section) => {
          const visibleItems = section.items.filter(
            item => !item.roles || item.roles.includes(user?.rol)
          );

          if (visibleItems.length === 0) return null;

          return (
            <div key={section.section}>
              <div className="sidebar-section-title">{section.section}</div>
              {visibleItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  className={({ isActive }) =>
                    `sidebar-link ${isActive ? 'active' : ''}`
                  }
                >
                  <item.icon />
                  {item.label}
                </NavLink>
              ))}
            </div>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <div className="sidebar-avatar">{userInitials}</div>
          <div className="sidebar-user-info">
            <div className="name">{user?.ad} {user?.soyad}</div>
            <div className="role">{rolLabel}</div>
          </div>
          <button
            className="btn-icon"
            onClick={logout}
            title="Çıkış Yap"
            style={{ background: 'transparent', border: 'none', color: 'var(--gray-400)' }}
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  );
}
