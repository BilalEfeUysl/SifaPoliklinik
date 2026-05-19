import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { hastaAPI, randevuAPI, klinikAPI, doktorAPI } from '../api/axiosConfig';
import { Users, CalendarDays, Building2, Stethoscope, TrendingUp, Activity } from 'lucide-react';

export default function DashboardPage() {
  const { user, rolLabel } = useAuth();
  const [stats, setStats] = useState({
    hastaSayisi: 0,
    randevuSayisi: 0,
    klinikSayisi: 0,
    doktorSayisi: 0,
  });
  const [recentRandevular, setRecentRandevular] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [hastalar, randevular, klinikler, doktorlar] = await Promise.all([
        hastaAPI.getAll().catch(() => ({ data: [] })),
        randevuAPI.getAll().catch(() => ({ data: [] })),
        klinikAPI.getAll().catch(() => ({ data: [] })),
        doktorAPI.getAll().catch(() => ({ data: [] })),
      ]);

      setStats({
        hastaSayisi: hastalar.data.length,
        randevuSayisi: randevular.data.length,
        klinikSayisi: klinikler.data.length,
        doktorSayisi: doktorlar.data.length,
      });

      setRecentRandevular(randevular.data.slice(-5).reverse());
    } catch (err) {
      console.error('Dashboard verileri yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  const greetingTime = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Günaydın';
    if (hour < 18) return 'İyi günler';
    return 'İyi akşamlar';
  };

  if (loading) {
    return <div className="empty-state"><p>Veriler yükleniyor...</p></div>;
  }

  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h2 style={{ fontSize: 24, fontWeight: 800, color: 'var(--gray-900)', letterSpacing: '-0.5px' }}>
          {greetingTime()}, {user?.ad}! 👋
        </h2>
        <p style={{ fontSize: 14, color: 'var(--gray-500)', marginTop: 4 }}>
          {rolLabel} olarak giriş yaptınız. Bugünkü duruma genel bakış aşağıda.
        </p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon teal"><Users size={22} /></div>
          <div className="stat-content">
            <h4>Toplam Hasta</h4>
            <div className="value">{stats.hastaSayisi}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue"><CalendarDays size={22} /></div>
          <div className="stat-content">
            <h4>Toplam Randevu</h4>
            <div className="value">{stats.randevuSayisi}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><Building2 size={22} /></div>
          <div className="stat-content">
            <h4>Klinik Sayısı</h4>
            <div className="value">{stats.klinikSayisi}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple"><Stethoscope size={22} /></div>
          <div className="stat-content">
            <h4>Doktor Sayısı</h4>
            <div className="value">{stats.doktorSayisi}</div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Son Randevular</h3>
          <Activity size={18} style={{ color: 'var(--gray-400)' }} />
        </div>
        <div className="card-body" style={{ padding: 0 }}>
          {recentRandevular.length === 0 ? (
            <div className="empty-state">
              <CalendarDays size={48} />
              <h4>Henüz randevu yok</h4>
              <p>Sistem başladığında randevular burada görünecek.</p>
            </div>
          ) : (
            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Hasta</th>
                    <th>Doktor</th>
                    <th>Tarih/Saat</th>
                    <th>Durum</th>
                  </tr>
                </thead>
                <tbody>
                  {recentRandevular.map((r) => (
                    <tr key={r.id}>
                      <td style={{ fontWeight: 600 }}>{r.hasta?.ad} {r.hasta?.soyad}</td>
                      <td>{r.doktor?.unvan} {r.doktor?.kullanici?.ad} {r.doktor?.kullanici?.soyad}</td>
                      <td>{new Date(r.tarihSaat).toLocaleString('tr-TR')}</td>
                      <td>
                        <span className={`badge ${
                          r.durum === 'TAMAMLANDI' ? 'badge-success' :
                          r.durum === 'IPTAL' ? 'badge-danger' : 'badge-warning'
                        }`}>
                          <span className={`badge-dot ${
                            r.durum === 'TAMAMLANDI' ? 'green' :
                            r.durum === 'IPTAL' ? 'red' : 'yellow'
                          }`}></span>
                          {r.durum === 'TAMAMLANDI' ? 'Tamamlandı' :
                           r.durum === 'IPTAL' ? 'İptal' : 'Bekliyor'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
