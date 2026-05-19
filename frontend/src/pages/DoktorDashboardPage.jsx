import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { doktorAPI, randevuAPI, muayeneAPI } from '../api/axiosConfig';
import { CalendarDays, Stethoscope, Clock, X } from 'lucide-react';

export default function DoktorDashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [doktorBilgi, setDoktorBilgi] = useState(null);
  const [randevular, setRandevular] = useState([]);
  const [muayeneler, setMuayeneler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [gecmisModal, setGecmisModal] = useState(null);
  const [gecmisYukleniyor, setGecmisYukleniyor] = useState(false);

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

  const showGecmisMuayeneler = async (hastaId, hastaAd) => {
    setGecmisYukleniyor(true);
    try {
      const res = await muayeneAPI.getByHasta(hastaId);
      const liste = Array.isArray(res.data) ? res.data : [];
      const sorted = [...liste].sort((a, b) => new Date(b.randevu?.tarihSaat) - new Date(a.randevu?.tarihSaat));
      setGecmisModal({ hastaAd, muayeneler: sorted });
    } catch { setGecmisModal({ hastaAd, muayeneler: [] }); }
    finally { setGecmisYukleniyor(false); }
  };

  const bugun = new Date().toDateString();
  const bugunRandevular = randevular
    .filter(r => new Date(r.tarihSaat).toDateString() === bugun)
    .sort((a, b) => new Date(a.tarihSaat) - new Date(b.tarihSaat));
  const bugunBekleyen = bugunRandevular.filter(r => r.durum === 'BEKLEYEN');
  const sonMuayeneler = [...muayeneler]
    .sort((a, b) => new Date(b.randevu?.tarihSaat) - new Date(a.randevu?.tarihSaat))
    .slice(0, 5);

  const greetingTime = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Günaydın';
    if (hour < 18) return 'İyi günler';
    return 'İyi akşamlar';
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  if (!doktorBilgi) return (
    <div className="empty-state">
      <Stethoscope size={48} />
      <h4>Doktor profili bulunamadı</h4>
      <p>Hesabınızla ilişkili bir doktor kaydı bulunamadı.</p>
    </div>
  );

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
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--gray-900)' }}>
                        {new Date(r.tarihSaat).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })} — {r.hasta?.ad} {r.hasta?.soyad}
                      </div>
                      <button
                        type="button"
                        onClick={() => showGecmisMuayeneler(r.hasta?.id, `${r.hasta?.ad} ${r.hasta?.soyad}`)}
                        style={{ fontSize: 11, color: 'var(--primary-600)', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left', padding: 0 }}
                      >
                        Geçmiş muayeneleri gör →
                      </button>
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
                    <button
                      type="button"
                      onClick={() => showGecmisMuayeneler(m.randevu?.hasta?.id, `${m.randevu?.hasta?.ad} ${m.randevu?.hasta?.soyad}`)}
                      style={{ fontSize: 11, color: 'var(--primary-600)', background: 'none', border: 'none', cursor: 'pointer', padding: 0, marginTop: 4 }}
                    >
                      Tüm geçmiş →
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
      {gecmisModal && (
        <div className="modal-overlay" onClick={() => setGecmisModal(null)}>
          <div className="modal" style={{ maxWidth: 600, maxHeight: '80vh', overflowY: 'auto' }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: 15 }}>Geçmiş Muayeneler — {gecmisModal.hastaAd}</h3>
              <button className="btn-icon" onClick={() => setGecmisModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body" style={{ padding: 0 }}>
              {gecmisModal.muayeneler.length === 0 ? (
                <div className="empty-state"><p>Muayene kaydı yok</p></div>
              ) : (
                gecmisModal.muayeneler.map(m => (
                  <div key={m.id} style={{ padding: '14px 18px', borderBottom: '1px solid var(--gray-100)' }}>
                    <div style={{ fontSize: 12, color: 'var(--gray-400)', marginBottom: 4 }}>
                      {new Date(m.randevu?.tarihSaat).toLocaleDateString('tr-TR')} — {m.randevu?.doktor?.kullanici?.ad}
                    </div>
                    <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>{m.tani || '—'}</div>
                    {m.recete && <div style={{ fontSize: 12, color: 'var(--gray-600)' }}>Reçete: {m.recete}</div>}
                    {m.rapor && <div style={{ fontSize: 12, color: 'var(--gray-600)' }}>Rapor: {m.rapor}</div>}
                    {m.sevkKurumu && <div style={{ fontSize: 12, color: '#7c3aed' }}>Sevk: {m.sevkKurumu}</div>}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
