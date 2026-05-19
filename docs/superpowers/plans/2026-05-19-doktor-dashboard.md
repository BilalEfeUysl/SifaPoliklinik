# Doktor Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** DOKTOR rolündeki kullanıcılara giriş sonrası kendi randevularını ve muayene geçmişlerini gösteren özel bir dashboard ekle.

**Architecture:** Yeni `DoktorDashboardPage.jsx` sayfası oluşturulur. `App.jsx`'teki `HomeRedirect` bileşeninde DOKTOR rolü için `/muayeneler` yönlendirmesi kaldırılır; doktorlar ana sayfada `DoktorDashboardPage` görür. Doktor kimliği tüm doktorlar listesinden `kullanici.id` eşleşmesiyle bulunur, ardından `randevuAPI.getByDoktor` ve `muayeneAPI.getByDoktor` paralel olarak çağrılır.

**Tech Stack:** React, react-router-dom (useNavigate), lucide-react, mevcut CSS sınıfları (stat-card, card, badge, btn btn-primary)

---

### Task 1: DoktorDashboardPage.jsx oluştur

**Files:**
- Create: `frontend/src/pages/DoktorDashboardPage.jsx`

- [ ] **Step 1: Dosyayı oluştur**

```jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { doktorAPI, randevuAPI, muayeneAPI } from '../api/axiosConfig';
import { CalendarDays, Stethoscope, Clock } from 'lucide-react';

export default function DoktorDashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [doktorBilgi, setDoktorBilgi] = useState(null);
  const [randevular, setRandevular] = useState([]);
  const [muayeneler, setMuayeneler] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const doktorlarRes = await doktorAPI.getAll();
      const bulunan = doktorlarRes.data.find(
        d => d.kullanici?.id === user?.id || d.kullanici?.email === user?.email
      );
      setDoktorBilgi(bulunan || null);
      if (bulunan) {
        const [randevuRes, muayeneRes] = await Promise.all([
          randevuAPI.getByDoktor(bulunan.id),
          muayeneAPI.getByDoktor(bulunan.id),
        ]);
        setRandevular(Array.isArray(randevuRes.data) ? randevuRes.data : []);
        setMuayeneler(Array.isArray(muayeneRes.data) ? muayeneRes.data : []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const bugun = new Date().toDateString();
  const bugunRandevular = randevular
    .filter(r => new Date(r.tarihSaat).toDateString() === bugun)
    .sort((a, b) => new Date(a.tarihSaat) - new Date(b.tarihSaat));
  const bugunBekleyen = bugunRandevular.filter(r => r.durum === 'BEKLEYEN');
  const sonMuayeneler = [...muayeneler].reverse().slice(0, 5);

  const greetingTime = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Günaydın';
    if (hour < 18) return 'İyi günler';
    return 'İyi akşamlar';
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 800, color: 'var(--gray-900)', letterSpacing: '-0.5px' }}>
          {greetingTime()}, {doktorBilgi?.unvan} {user?.ad}! 👋
        </h2>
        <p style={{ fontSize: 14, color: 'var(--gray-500)', marginTop: 4 }}>
          {doktorBilgi?.klinik?.ad} — Bugünkü programınıza genel bakış.
        </p>
      </div>

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        <div className="stat-card">
          <div className="stat-icon blue"><Clock size={22} /></div>
          <div className="stat-content">
            <h4>Bugün Bekleyen</h4>
            <div className="value">{bugunBekleyen.length}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teal"><CalendarDays size={22} /></div>
          <div className="stat-content">
            <h4>Toplam Randevu</h4>
            <div className="value">{randevular.length}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple"><Stethoscope size={22} /></div>
          <div className="stat-content">
            <h4>Toplam Muayene</h4>
            <div className="value">{muayeneler.length}</div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginTop: 20 }}>
        <div className="card">
          <div className="card-header">
            <h3>Bugünkü Randevular</h3>
            <CalendarDays size={18} style={{ color: 'var(--gray-400)' }} />
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {bugunRandevular.length === 0 ? (
              <div className="empty-state">
                <CalendarDays size={40} />
                <h4>Bugün randevu yok</h4>
              </div>
            ) : (
              <div>
                {bugunRandevular.map(r => (
                  <div key={r.id} style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    padding: '12px 16px', borderBottom: '1px solid var(--gray-100)'
                  }}>
                    <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--gray-900)' }}>
                      {new Date(r.tarihSaat).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })} — {r.hasta?.ad} {r.hasta?.soyad}
                    </div>
                    {r.durum === 'BEKLEYEN' ? (
                      <button
                        className="btn btn-primary"
                        style={{ fontSize: 12, padding: '4px 12px' }}
                        onClick={() => navigate('/muayeneler')}
                      >
                        Muayeneye Başla
                      </button>
                    ) : (
                      <span className={`badge ${r.durum === 'TAMAMLANDI' ? 'badge-success' : 'badge-danger'}`}>
                        <span className={`badge-dot ${r.durum === 'TAMAMLANDI' ? 'green' : 'red'}`}></span>
                        {r.durum === 'TAMAMLANDI' ? 'Tamamlandı' : 'İptal'}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3>Son Muayeneler</h3>
            <Stethoscope size={18} style={{ color: 'var(--gray-400)' }} />
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {sonMuayeneler.length === 0 ? (
              <div className="empty-state">
                <Stethoscope size={40} />
                <h4>Henüz muayene yok</h4>
              </div>
            ) : (
              <div>
                {sonMuayeneler.map(m => (
                  <div key={m.id} style={{ padding: '12px 16px', borderBottom: '1px solid var(--gray-100)' }}>
                    <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--gray-900)' }}>
                      {m.randevu?.hasta?.ad} {m.randevu?.hasta?.soyad}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--gray-500)', marginTop: 2 }}>
                      {new Date(m.randevu?.tarihSaat).toLocaleDateString('tr-TR')} · {m.tani}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/pages/DoktorDashboardPage.jsx
git commit -m "feat: add DoktorDashboardPage with stats, today's appointments, and recent examinations"
```

---

### Task 2: App.jsx routing güncelle

**Files:**
- Modify: `frontend/src/App.jsx:1-11` (import ekle)
- Modify: `frontend/src/App.jsx:19-25` (HomeRedirect güncelle)

- [ ] **Step 1: Import ekle**

`App.jsx` dosyasının import bloğuna şunu ekle (diğer page importlarının yanına):

```js
import DoktorDashboardPage from './pages/DoktorDashboardPage';
```

- [ ] **Step 2: HomeRedirect güncelle**

Mevcut `HomeRedirect`:

```jsx
function HomeRedirect() {
  const { user } = useAuth();
  if (user?.rol === 'KAYIT_GOREVLISI') return <Navigate to="/hastalar" replace />;
  if (user?.rol === 'VEZNEDAR') return <Navigate to="/odemeler" replace />;
  if (user?.rol === 'DOKTOR') return <Navigate to="/muayeneler" replace />;
  return <DashboardPage />;
}
```

Şununla değiştir:

```jsx
function HomeRedirect() {
  const { user } = useAuth();
  if (user?.rol === 'KAYIT_GOREVLISI') return <Navigate to="/hastalar" replace />;
  if (user?.rol === 'VEZNEDAR') return <Navigate to="/odemeler" replace />;
  if (user?.rol === 'DOKTOR') return <DoktorDashboardPage />;
  return <DashboardPage />;
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/App.jsx
git commit -m "feat: route DOKTOR role to DoktorDashboardPage on login"
```
