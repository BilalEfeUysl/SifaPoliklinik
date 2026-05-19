import { useState, useEffect } from 'react';
import { randevuAPI, muayeneAPI, doktorAPI } from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { Stethoscope, FileText, X, Eye, Printer } from 'lucide-react';

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
  const [showSevkModal, setShowSevkModal] = useState(false);
  const [sevkMuayene, setSevkMuayene] = useState(null);

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
      const res = await muayeneAPI.create({
        randevuId: selectedRandevu.id,
        tani: form.tani, recete: form.recete, rapor: form.rapor, sevkKurumu: form.sevkKurumu
      });
      setShowModal(false);
      if (form.sevkKurumu?.trim()) {
        const kaydedilen = res?.data ?? {
          sevkKurumu: form.sevkKurumu,
          tani: form.tani,
          muayeneTarihi: new Date().toISOString(),
          randevu: selectedRandevu,
        };
        setSevkMuayene(kaydedilen);
        setShowSevkModal(true);
      }
      loadData();
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

        {showSevkModal && sevkMuayene && (
          <SevkKagidiModal
            muayene={sevkMuayene}
            onClose={() => { setShowSevkModal(false); setSevkMuayene(null); }}
          />
        )}

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
              <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
                <div>
                  {selectedMuayene.sevkKurumu && (
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => {
                        setSevkMuayene(selectedMuayene);
                        setShowViewModal(false);
                        setShowSevkModal(true);
                      }}
                    >
                      <Printer size={14} /> Sevk Kâğıdı Yazdır
                    </button>
                  )}
                </div>
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
          <div className="modal" style={{ maxWidth: 500 }} onClick={e => e.stopPropagation()}>

            {/* Header */}
            <div className="modal-header" style={{ padding: '20px 24px 16px', borderBottom: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ width: 36, height: 36, background: '#f0fdfa', borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <Stethoscope size={18} color="#0d9488" />
                </div>
                <div>
                  <h3 style={{ fontSize: 15, fontWeight: 700 }}>Muayene Kaydı</h3>
                  <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 1 }}>Bulgular ve tedavi bilgilerini girin</div>
                </div>
              </div>
              <button className="btn-icon" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body" style={{ padding: '20px 24px 4px' }}>

                {error && (
                  <div style={{ background: '#fef2f2', color: '#991b1b', border: '1px solid #fecaca', padding: '10px 12px', borderRadius: 8, fontSize: 13, marginBottom: 16 }}>
                    {error}
                  </div>
                )}

                {/* Hasta şeridi */}
                <div style={{ background: '#f8fafc', borderRadius: 10, padding: '12px 16px', marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div style={{ width: 40, height: 40, background: 'linear-gradient(135deg, #0d9488, #06b6d4)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 13, fontWeight: 700, flexShrink: 0, letterSpacing: 0.5 }}>
                    {(selectedRandevu.hasta?.ad?.[0] || '') + (selectedRandevu.hasta?.soyad?.[0] || '')}
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>{selectedRandevu.hasta?.ad} {selectedRandevu.hasta?.soyad}</div>
                    <div style={{ fontSize: 11, color: '#64748b', marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      TC: {selectedRandevu.hasta?.tcKimlik} · {selectedRandevu.doktor?.unvan} {selectedRandevu.doktor?.kullanici?.ad}
                    </div>
                  </div>
                  <span style={{ display: 'inline-block', background: '#f0fdfa', border: '1px solid #99f6e4', color: '#0d9488', fontSize: 10, fontWeight: 600, borderRadius: 20, padding: '3px 10px', whiteSpace: 'nowrap', flexShrink: 0 }}>
                    {selectedRandevu.doktor?.klinik?.ad || 'Klinik'}
                  </span>
                </div>

                {/* Tanı */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#f59e0b', display: 'inline-block', flexShrink: 0 }} />
                    Tanı / Teşhis <span style={{ color: '#f43f5e', marginLeft: 2 }}>*</span>
                  </label>
                  <textarea className="form-textarea" value={form.tani} rows={3}
                    onChange={e => setForm({ ...form, tani: e.target.value })} required
                    placeholder="Hastanın tanısını ve teşhisini yazınız…" />
                </div>

                {/* Reçete */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#3b82f6', display: 'inline-block', flexShrink: 0 }} />
                    Reçete (İlaçlar)
                  </label>
                  <textarea className="form-textarea" value={form.recete} rows={3}
                    onChange={e => setForm({ ...form, recete: e.target.value })}
                    placeholder="Yazılan ilaçları ve dozajları giriniz…" />
                </div>

                {/* Rapor */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#10b981', display: 'inline-block', flexShrink: 0 }} />
                    Rapor
                  </label>
                  <textarea className="form-textarea" value={form.rapor} rows={3}
                    onChange={e => setForm({ ...form, rapor: e.target.value })}
                    placeholder="Muayene raporunu yazınız…" />
                </div>

                {/* Sevk */}
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#e2e8f0', display: 'inline-block', flexShrink: 0 }} />
                    Sevk Kurumu
                    <span style={{ fontSize: 10, fontWeight: 400, color: '#94a3b8', marginLeft: 2 }}>(isteğe bağlı)</span>
                  </label>
                  <input className="form-input" value={form.sevkKurumu}
                    onChange={e => setForm({ ...form, sevkKurumu: e.target.value })}
                    placeholder="Örn: X Eğitim Araştırma Hastanesi — Kardiyoloji Polikliniği"
                    style={{ border: '1.5px dashed var(--gray-200)', background: '#fafafa' }} />
                </div>
              </div>

              <div className="modal-footer" style={{ justifyContent: 'space-between', alignItems: 'center', padding: '14px 24px' }}>
                <span style={{ fontSize: 11, color: '#94a3b8' }}>* zorunlu alan</span>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>İptal</button>
                  <button type="submit" className="btn btn-primary"><FileText size={14} /> Muayene Kaydet</button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {showSevkModal && sevkMuayene && (
        <SevkKagidiModal
          muayene={sevkMuayene}
          onClose={() => { setShowSevkModal(false); setSevkMuayene(null); }}
        />
      )}
    </div>
  );
}

function SevkKagidiModal({ muayene, onClose }) {
  if (!muayene) return null;

  const hasta = muayene.randevu?.hasta;
  const doktor = muayene.randevu?.doktor;
  const tarih = muayene.muayeneTarihi
    ? new Date(muayene.muayeneTarihi).toLocaleDateString('tr-TR')
    : new Date().toLocaleDateString('tr-TR');
  const dogumTarihi = hasta?.dogumTarihi
    ? new Date(hasta.dogumTarihi).toLocaleDateString('tr-TR')
    : null;

  return (
    <div id="sevk-print-root">
      <div className="modal-overlay" onClick={onClose}>
        <div
          className="modal"
          style={{ maxWidth: 520, fontFamily: 'serif' }}
          onClick={e => e.stopPropagation()}
        >
          <div style={{ padding: '28px 32px' }}>
            <div style={{ textAlign: 'center', borderBottom: '2px solid #0d9488', paddingBottom: 12, marginBottom: 16 }}>
              <div style={{ fontSize: 18, fontWeight: 700, letterSpacing: 1, color: '#0d9488' }}>
                ŞIFA POLİKLİNİĞİ
              </div>
              <div style={{ fontSize: 11, color: '#64748b', letterSpacing: 2, textTransform: 'uppercase', marginTop: 2 }}>
                Sevk Kâğıdı
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 16px', fontSize: 13, marginBottom: 16 }}>
              <div><span style={{ color: '#64748b' }}>Hasta Adı Soyadı:</span><br /><strong>{hasta?.ad} {hasta?.soyad}</strong></div>
              <div><span style={{ color: '#64748b' }}>TC Kimlik No:</span><br /><strong>{hasta?.tcKimlik || '—'}</strong></div>
              {dogumTarihi && (
                <div><span style={{ color: '#64748b' }}>Doğum Tarihi:</span><br />{dogumTarihi}</div>
              )}
              <div><span style={{ color: '#64748b' }}>Sevk Tarihi:</span><br />{tarih}</div>
            </div>

            <div style={{ border: '1px solid #cbd5e1', borderRadius: 6, padding: '10px 14px', marginBottom: 12, fontSize: 13 }}>
              <div style={{ color: '#64748b', fontSize: 11, marginBottom: 4 }}>Sevk Edilen Kurum / Bölüm:</div>
              <strong>{muayene.sevkKurumu}</strong>
            </div>

            <div style={{ border: '1px solid #cbd5e1', borderRadius: 6, padding: '10px 14px', marginBottom: 20, fontSize: 13 }}>
              <div style={{ color: '#64748b', fontSize: 11, marginBottom: 4 }}>Tanı / Sevk Nedeni:</div>
              <span style={{ whiteSpace: 'pre-wrap' }}>{muayene.tani || '—'}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <div style={{ textAlign: 'center', borderTop: '1px solid #334155', paddingTop: 6, minWidth: 140 }}>
                <div style={{ fontSize: 13, fontWeight: 600 }}>
                  {doktor?.unvan} {doktor?.kullanici?.ad} {doktor?.kullanici?.soyad}
                </div>
                <div style={{ fontSize: 11, color: '#64748b' }}>
                  {doktor?.klinik?.ad || 'Klinik'}
                </div>
                <div style={{ fontSize: 10, color: '#94a3b8', marginTop: 2 }}>İmza / Kaşe</div>
              </div>
            </div>
          </div>

          <div className="modal-footer sevk-modal-actions" style={{ justifyContent: 'flex-end', gap: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Kapat</button>
            <button type="button" className="btn btn-primary" onClick={() => window.print()}>
              <Printer size={14} /> Yazdır / PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
