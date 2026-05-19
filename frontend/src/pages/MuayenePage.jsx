import { useState, useEffect } from 'react';
import { randevuAPI, muayeneAPI, doktorAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { Stethoscope, FileText, X, Eye } from 'lucide-react';

export default function MuayenePage() {
  const { hasRole, user } = useAuth();
  const isAdmin = hasRole('YONETICI');
  const isDoktor = hasRole('DOKTOR');

  const [randevular, setRandevular] = useState([]);
  const [muayeneler, setMuayeneler] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedRandevu, setSelectedRandevu] = useState(null);
  const [selectedMuayene, setSelectedMuayene] = useState(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ tani: '', recete: '', rapor: '', sevkKurumu: '' });
  const [doktorBilgi, setDoktorBilgi] = useState(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      if (isAdmin) {
        const res = await muayeneAPI.getAll();
        setMuayeneler(res.data);
      } else {
        // Doktor: önce kendi doktor kaydını bul, sonra bekleyen randevuları getir
        const doktorlarRes = await doktorAPI.getAll().catch(() => ({ data: [] }));
        const bulunan = doktorlarRes.data.find(
          d => d.kullanici?.id === user?.id || d.kullanici?.email === user?.email
        );
        setDoktorBilgi(bulunan || null);
        if (bulunan) {
          const res = await randevuAPI.getBekleyen(bulunan.id);
          const list = Array.isArray(res.data) ? res.data : [];
          setRandevular(list);
        } else {
          setRandevular([]);
        }
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openMuayene = (randevu) => {
    setSelectedRandevu(randevu);
    setForm({ tani: '', recete: '', rapor: '', sevkKurumu: '' });
    setError(''); setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      await muayeneAPI.create({
        randevuId: selectedRandevu.id,
        tani: form.tani, recete: form.recete, rapor: form.rapor, sevkKurumu: form.sevkKurumu
      });
      setShowModal(false); loadData();
    } catch (err) { setError(err.response?.data?.mesaj || 'Muayene kaydı oluşturulamadı.'); }
  };

  if (loading) return <div className="empty-state"><p>Yükleniyor...</p></div>;

  if (isAdmin) {
    return (
      <div>
        <div className="page-header"><h2>Muayene Kayıtları</h2></div>
        <div className="card">
          <div className="card-header">
            <h3>Tüm Muayeneler</h3>
            <span className="badge badge-success">{muayeneler.length} kayıt</span>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {muayeneler.length === 0 ? (
              <div className="empty-state"><Stethoscope size={48} /><h4>Henüz muayene kaydı yok</h4></div>
            ) : (
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead><tr>
                    <th>ID</th><th>Hasta</th><th>Doktor</th><th>Klinik</th><th>Muayene Tarihi</th><th>Tanı</th><th>İşlem</th>
                  </tr></thead>
                  <tbody>
                    {muayeneler.map(m => (
                      <tr key={m.id}>
                        <td>#{m.id}</td>
                        <td style={{ fontWeight: 600 }}>{m.randevu?.hasta?.ad} {m.randevu?.hasta?.soyad}</td>
                        <td>{m.randevu?.doktor?.unvan} {m.randevu?.doktor?.kullanici?.ad} {m.randevu?.doktor?.kullanici?.soyad}</td>
                        <td>{m.randevu?.doktor?.klinik?.ad || '-'}</td>
                        <td>{m.muayeneTarihi ? new Date(m.muayeneTarihi).toLocaleString('tr-TR') : '-'}</td>
                        <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{m.tani || '-'}</td>
                        <td>
                          <button className="btn btn-sm btn-secondary" style={{ fontSize: 11, padding: '4px 10px' }}
                            onClick={() => { setSelectedMuayene(m); setShowViewModal(true); }}>
                            <Eye size={12} /> Görüntüle
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {showViewModal && selectedMuayene && (
          <div className="modal-overlay" onClick={() => setShowViewModal(false)}>
            <div className="modal" style={{ maxWidth: 640 }} onClick={e => e.stopPropagation()}>
              <div className="modal-header">
                <h3>Muayene Detayı</h3>
                <button className="btn-icon" onClick={() => setShowViewModal(false)}><X size={18} /></button>
              </div>
              <div className="modal-body">
                <div style={{ background: 'var(--gray-50)', padding: 16, borderRadius: 8, marginBottom: 20 }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, fontSize: 13 }}>
                    <div><strong>Hasta:</strong> {selectedMuayene.randevu?.hasta?.ad} {selectedMuayene.randevu?.hasta?.soyad}</div>
                    <div><strong>TC:</strong> {selectedMuayene.randevu?.hasta?.tcKimlik}</div>
                    <div><strong>Doktor:</strong> {selectedMuayene.randevu?.doktor?.unvan} {selectedMuayene.randevu?.doktor?.kullanici?.ad}</div>
                    <div><strong>Klinik:</strong> {selectedMuayene.randevu?.doktor?.klinik?.ad}</div>
                  </div>
                </div>
                <div className="form-group"><label className="form-label">Tanı / Teşhis</label>
                  <div style={{ padding: '10px 12px', background: 'var(--gray-50)', borderRadius: 6, fontSize: 13, whiteSpace: 'pre-wrap' }}>{selectedMuayene.tani || '-'}</div></div>
                <div className="form-group"><label className="form-label">Reçete</label>
                  <div style={{ padding: '10px 12px', background: 'var(--gray-50)', borderRadius: 6, fontSize: 13, whiteSpace: 'pre-wrap' }}>{selectedMuayene.recete || '-'}</div></div>
                <div className="form-group"><label className="form-label">Rapor</label>
                  <div style={{ padding: '10px 12px', background: 'var(--gray-50)', borderRadius: 6, fontSize: 13, whiteSpace: 'pre-wrap' }}>{selectedMuayene.rapor || '-'}</div></div>
                {selectedMuayene.sevkKurumu && (
                  <div className="form-group"><label className="form-label">Sevk Kurumu</label>
                    <div style={{ padding: '10px 12px', background: 'var(--gray-50)', borderRadius: 6, fontSize: 13 }}>{selectedMuayene.sevkKurumu}</div></div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowViewModal(false)}>Kapat</button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div>
      <div className="page-header"><h2>Muayene Kayıtları</h2></div>

      {/* Doktor kaydı bulunamadıysa uyarı */}
      {isDoktor && !doktorBilgi && !loading && (
        <div style={{ background: 'var(--warning-bg, #fefce8)', color: '#854d0e', padding: '12px 16px', borderRadius: 8, fontSize: 13, marginBottom: 16, border: '1px solid #fde047' }}>
          ⚠️ Hesabınıza bağlı doktor kaydı bulunamadı. Randevularınız yüklenemedi. Yöneticiyle iletişime geçin.
        </div>
      )}

      <div className="card">
        <div className="card-header"><h3>Bekleyen Randevularım</h3>
          <span className="badge badge-warning">{randevular.length} bekleyen</span></div>
        <div className="card-body" style={{ padding: 0 }}>
          {randevular.length === 0 ? (
            <div className="empty-state"><Stethoscope size={48} /><h4>Bekleyen randevu yok</h4>
              <p>Tüm randevular tamamlandı veya henüz randevu oluşturulmadı.</p></div>
          ) : (
            <div className="data-table-wrapper"><table className="data-table"><thead><tr>
              <th>Hasta</th><th>TC Kimlik</th><th>Doktor</th><th>Klinik</th><th>Tarih</th><th>İşlem</th>
            </tr></thead><tbody>
              {randevular.map(r => (
                <tr key={r.id}>
                  <td style={{ fontWeight: 600 }}>{r.hasta?.ad} {r.hasta?.soyad}</td>
                  <td style={{ fontFamily: 'monospace' }}>{r.hasta?.tcKimlik}</td>
                  <td>{r.doktor?.unvan} {r.doktor?.kullanici?.ad}</td>
                  <td>{r.doktor?.klinik?.ad || '-'}</td>
                  <td>{new Date(r.tarihSaat).toLocaleString('tr-TR')}</td>
                  <td><button className="btn btn-sm btn-primary" onClick={() => openMuayene(r)}>
                    <Stethoscope size={12} /> Muayene Yap</button></td>
                </tr>
              ))}
            </tbody></table></div>
          )}
        </div>
      </div>

      {showModal && selectedRandevu && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" style={{ maxWidth: 640 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h3>Muayene Kaydı</h3>
              <button className="btn-icon" onClick={() => setShowModal(false)}><X size={18} /></button></div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && <div style={{ background: 'var(--danger-bg)', color: '#991b1b', padding: '10px', borderRadius: 6, fontSize: 13, marginBottom: 16 }}>{error}</div>}
                <div style={{ background: 'var(--gray-50)', padding: 16, borderRadius: 8, marginBottom: 20 }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, fontSize: 13 }}>
                    <div><strong>Hasta:</strong> {selectedRandevu.hasta?.ad} {selectedRandevu.hasta?.soyad}</div>
                    <div><strong>TC:</strong> {selectedRandevu.hasta?.tcKimlik}</div>
                    <div><strong>Doktor:</strong> {selectedRandevu.doktor?.unvan} {selectedRandevu.doktor?.kullanici?.ad}</div>
                    <div><strong>Klinik:</strong> {selectedRandevu.doktor?.klinik?.ad}</div>
                  </div>
                </div>
                <div className="form-group"><label className="form-label">Tanı / Teşhis *</label>
                  <textarea className="form-textarea" value={form.tani} rows={3}
                    onChange={e => setForm({ ...form, tani: e.target.value })} required
                    placeholder="Hastanın tanısını yazınız..." /></div>
                <div className="form-group"><label className="form-label">Reçete (İlaçlar)</label>
                  <textarea className="form-textarea" value={form.recete} rows={3}
                    onChange={e => setForm({ ...form, recete: e.target.value })}
                    placeholder="Yazılan ilaçları giriniz..." /></div>
                <div className="form-group"><label className="form-label">Rapor</label>
                  <textarea className="form-textarea" value={form.rapor} rows={3}
                    onChange={e => setForm({ ...form, rapor: e.target.value })}
                    placeholder="Muayene raporunu yazınız..." /></div>
                <div className="form-group"><label className="form-label">Sevk Edilecek Kurum (İsteğe Bağlı)</label>
                  <input className="form-input" value={form.sevkKurumu}
                    onChange={e => setForm({ ...form, sevkKurumu: e.target.value })}
                    placeholder="Örn: X Eğitim Araştırma Hastanesi - Kardiyoloji Polikliniği" /></div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>İptal</button>
                <button type="submit" className="btn btn-primary"><FileText size={16} /> Muayene Kaydet</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
